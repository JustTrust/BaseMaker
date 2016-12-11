package com.example;


import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
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
        boolean classHasFields = false;
        for (VariableElement variableElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            if (variableElement.getModifiers().contains(Modifier.FINAL)) {
                error("Fields in class can't be final", classElement);
                return false;
            }
            if (!variableElement.getModifiers().contains(Modifier.PUBLIC)) {
                error("Fields in class should be public", classElement);
                return false;
            }
//            List<? extends AnnotationMirror> fieldAnnotations = variableElement.getAnnotationMirrors();
//            for (AnnotationMirror fieldAn : fieldAnnotations) {
//                Map map = fieldAn.getElementValues();
//                for (  : map.entrySet()) {
//
//                }
//            }
            classHasFields = true;
        }

        if (!classHasFields) {
            error("Class must has minimum one field", classElement);
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

        TypeSpec.Builder mainClassBuilder = TypeSpec.classBuilder("DBfile")
                .addModifiers(Modifier.PUBLIC);

        for (Map.Entry<String, MakeDbAnnotatedClass> classEntry : classList.entrySet()) {
            TypeElement annotatedClass = classEntry.getValue().getTypeElement();
            mainClassBuilder.addType(buildInnerClass(annotatedClass));
        }


        final JavaFile javaFile = JavaFile.builder("com.base_maker.java.basemaker", mainClassBuilder.build())
                .addFileComment("Generated by annotation processor, do not modify")
                .build();

        final JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(
                "com.base_maker.java.basemaker.DBfile");

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
        for (int i = 0; i < fieldList.size(); i++) {
            VariableElement variableElement = fieldList.get(i);
            tableCreateString.append("\"")
                    .append(variableElement.getSimpleName())
                    .append(Utils.getFieldType(variableElement, processingEnv));
            if (i+1 < fieldList.size()) {
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
