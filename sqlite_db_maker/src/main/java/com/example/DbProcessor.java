package com.example;


import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class DbProcessor extends AbstractProcessor {

    private static final String CLASS_NAME = "DB";
    private Map<String, MakeDbAnnotatedClass> classList = new LinkedHashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if (roundEnvironment.getElementsAnnotatedWith(MakeDbFromClass.class).isEmpty()) {
            log("Don't have any @MakeDbFromClass annotation");
            return false;
        }

        log("Has annotated classes " + String.valueOf(roundEnvironment.getElementsAnnotatedWith(MakeDbFromClass.class).size()));
        // Iterate over all @MakeDbFromClass annotated elements
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(MakeDbFromClass.class)) {
            // Check if a class valid
            if (isValidClass(annotatedElement)) {
                if (classList.get(annotatedElement.getSimpleName().toString()) == null) {
                    classList.put(annotatedElement.getSimpleName().toString()
                            , new MakeDbAnnotatedClass((TypeElement) annotatedElement));
                    log("Add new class " + annotatedElement.getSimpleName());
                } else {
                    log("Class was not added " + annotatedElement.getSimpleName());
                }

            } else {
                return false;
            }
        }

        try {
            generateCode();
            log("Generated a new class file");
        } catch (IOException e) {
            e.printStackTrace();
            fatalError("Can't write a class file");
            return false;
        }
        classList.clear();
        return true;
    }

    private boolean isValidClass(Element item) {

        if (item.getKind() != ElementKind.CLASS) {
            fatalError("Only classes can be annotated with @MakeDbFromClass");
            return false; // Exit processing
        }

        // Cast to TypeElement, has more type specific methods
        TypeElement classElement = (TypeElement) item;

        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            error("The class is not public", classElement);
            return false;
        }

        // Check if it's an abstract class
        if (classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            error("The class is abstract. You can't annotate abstract classes @MakeDbFromClass", classElement);
            return false;
        }

        // Check fields: all fields must be public and not final;
        boolean classNotHasFields = true;
        for (VariableElement variableElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            if (skipField(variableElement)) {
                continue;
            }
            if (variableElement.getModifiers().contains(Modifier.FINAL)) {
                error("Field "+variableElement.getSimpleName()+" can't be final", variableElement);
                return false;
            }
            if (!variableElement.getModifiers().contains(Modifier.PUBLIC)) {
                error("Field "+variableElement.getSimpleName()+" should be public", variableElement);
                return false;
            }
            classNotHasFields = false;

        }

        if (classNotHasFields) {
            error("Class must has minimum one valid field", classElement);
            return false;
        }

        // Check if an empty public constructor is given
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 & constructorElement.getModifiers()
                        .contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    return true;
                }
            }
        }

        // No empty constructor found
        error("The class must provide an public empty default constructor", classElement);
        return false;
    }

    private void generateCode() throws IOException {
        if (classList.isEmpty()) {
            log("Annotated classes list is empty");
        }

        TypeSpec.Builder mainClassBuilder = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC);

        String classPackage = "";
        for (Map.Entry<String, MakeDbAnnotatedClass> classEntry : classList.entrySet()) {
            TypeElement annotatedClass = classEntry.getValue().getTypeElement();
            mainClassBuilder.addType(buildInnerClass(annotatedClass));
            classPackage = processingEnv.getElementUtils().getPackageOf(annotatedClass)
                    .getQualifiedName().toString();
        }


        final JavaFile javaFile = JavaFile.builder(classPackage, mainClassBuilder.build())
                .addFileComment("Generated by annotation processor, do not modify")
                .build();

        final JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(
                classPackage + "." + CLASS_NAME);

        final Writer writer = new BufferedWriter(sourceFile.openWriter());
        javaFile.writeTo(writer);
        writer.close();

    }

    private TypeSpec buildInnerClass(TypeElement annotatedClass) {
        FieldSpec tableNameField = FieldSpec.builder(String.class, Const.TABLE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", annotatedClass.getSimpleName().toString().toLowerCase())
                .build();
        TypeSpec.Builder innerClassBuilder = TypeSpec.classBuilder(annotatedClass.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addField(tableNameField);

        StringBuilder tableCreateString = new StringBuilder()
                .append("\"")
                .append(Const.CREATE_TABLE)
                .append(annotatedClass.getSimpleName().toString().toLowerCase())
                .append(" (" + "\"+\n");

        List<VariableElement> fieldList = ElementFilter.fieldsIn(annotatedClass.getEnclosedElements());
        int skipedCount = 0;
        for (int i = 0; i < fieldList.size(); i++) {
            VariableElement variableElement = fieldList.get(i);
            if (skipField(variableElement)){
                skipedCount += 1;
                continue;
            }
            tableCreateString.append("\"")
                    .append(variableElement.getSimpleName())
                    .append(Utils.getSQLiteFieldType(variableElement, processingEnv));
            if (i + 1 < fieldList.size() - skipedCount) {
                tableCreateString.append(Const.COMMA);
            }
            tableCreateString.append("\"+\n");
        }
        tableCreateString.append("\" );\"");
        FieldSpec tableCreateField = FieldSpec.builder(String.class, Const.CREATE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(tableCreateString.toString())
                .build();

        innerClassBuilder.addField(tableCreateField);

        return innerClassBuilder.build();
    }

    private boolean skipField(VariableElement variableElement) {
        List<? extends AnnotationMirror> anList = variableElement.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : anList) {
            if (annotationMirror.getAnnotationType().toString().equals(ExcludeField.class.getCanonicalName())) {
                log("Skip field " + variableElement.getSimpleName());
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> annotationSet = new LinkedHashSet<>();
        annotationSet.add(MakeDbFromClass.class.getCanonicalName());
        annotationSet.add(ExcludeField.class.getCanonicalName());
        return annotationSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void log(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    private void error(String msg, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element);
    }

    private void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }
}
