package com.abelichenko;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
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

    private Map<String, MakeDbAnnotatedClass> classList = new LinkedHashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if (roundEnvironment.getElementsAnnotatedWith(TableFromClass.class).isEmpty()) {
            log("Don't have any @TableFromClass annotation");
            return false;
        }

        log("Has annotated classes " + String.valueOf(roundEnvironment.getElementsAnnotatedWith(TableFromClass.class).size()));
        // Iterate over all @TableFromClass annotated elements
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(TableFromClass.class)) {
            // Check if a class valid
            if (isValidClass(annotatedElement)) {
                if (classList.get(annotatedElement.getSimpleName().toString()) == null) {
                    classList.put(annotatedElement.getSimpleName().toString()
                            , new MakeDbAnnotatedClass((TypeElement) annotatedElement));
                } else {
                    log("Class already in the list " + annotatedElement.getSimpleName());
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
            fatalError("Only classes can be annotated with @TableFromClass");
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
            error("The class is abstract. You can't annotate abstract classes @TableFromClass", classElement);
            return false;
        }

        // Check fields: all fields must be public and not final;
        boolean classNotHasFields = true;
        for (VariableElement variableElement : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
            if (skipField(variableElement)) {
                continue;
            }
            if (variableElement.getModifiers().contains(Modifier.FINAL)) {
                error("Field " + variableElement.getSimpleName() + " can't be final", variableElement);
                return false;
            }
            if (!variableElement.getModifiers().contains(Modifier.PUBLIC)) {
                error("Field " + variableElement.getSimpleName() + " should be public", variableElement);
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

        TypeSpec.Builder mainClassBuilder = TypeSpec.classBuilder(Const.CLASS_NAME)
                .addModifiers(Modifier.PUBLIC);

        String classPackage = "";
        for (Map.Entry<String, MakeDbAnnotatedClass> classEntry : classList.entrySet()) {
            TypeElement annotatedClass = classEntry.getValue().getTypeElement();
            mainClassBuilder.addType(buildInnerClass(annotatedClass));
            classPackage = processingEnv.getElementUtils().getPackageOf(annotatedClass)
                    .getQualifiedName().toString();
            log("Add new class " + annotatedClass.getSimpleName());
        }

        final JavaFile javaFile = JavaFile.builder(classPackage, mainClassBuilder.build())
                .addFileComment("Generated by annotation processor, do not modify")
                .build();

        final JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(
                classPackage + "." + Const.CLASS_NAME);

        final Writer writer = new BufferedWriter(sourceFile.openWriter());
        javaFile.writeTo(writer);
        writer.close();

    }

    private TypeSpec buildInnerClass(final TypeElement annotatedClass) {
        FieldSpec tableNameField = FieldSpec.builder(String.class, Const.TABLE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", annotatedClass.getSimpleName().toString().toLowerCase())
                .build();
        TypeSpec.Builder innerClassBuilder = TypeSpec.classBuilder(annotatedClass.getSimpleName().toString() + Const.SUFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addField(tableNameField);

        TypeSpec columnClass = getColumnClass(annotatedClass);
        innerClassBuilder.addType(columnClass);

        FieldSpec tableCreateField = getTableCreateField(annotatedClass);
        innerClassBuilder.addField(tableCreateField);

        MethodSpec toContentValueMethod = getContentValueMethod(annotatedClass);
        innerClassBuilder.addMethod(toContentValueMethod);

        MethodSpec parseCursorMethod = getParseCursorMethod(annotatedClass);
        innerClassBuilder.addMethod(parseCursorMethod);

        return innerClassBuilder.build();
    }


    private TypeSpec getColumnClass(TypeElement annotatedClass) {
        TypeSpec.Builder columnClass = TypeSpec.classBuilder(Const.COLUMN_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.STATIC);

        final List<VariableElement> fieldList = ElementFilter.fieldsIn(annotatedClass.getEnclosedElements());
        for (int i = 0; i < fieldList.size(); i++) {
            VariableElement variableElement = fieldList.get(i);
            if (skipField(variableElement)) {
                continue;
            }
            FieldSpec columnField = FieldSpec.builder(String.class, variableElement.getSimpleName().toString().toUpperCase())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", variableElement.getSimpleName().toString())
                    .build();
            columnClass.addField(columnField);
        }

        return columnClass.build();
    }

    private MethodSpec getParseCursorMethod(TypeElement annotatedClass) {
        ClassName cursorClassName = ClassName.get("android.database", "Cursor");
        String returnVariable = annotatedClass.getSimpleName().toString().toLowerCase();
        MethodSpec.Builder toContentValue = MethodSpec.methodBuilder("parseCursor")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.get(annotatedClass.asType()))
                .addParameter(cursorClassName, "cursor")
                .addStatement("$1T $2L = new $1T()"
                        , TypeName.get(annotatedClass.asType())
                        , returnVariable);

        final List<VariableElement> fieldList = ElementFilter.fieldsIn(annotatedClass.getEnclosedElements());
        for (int i = 0; i < fieldList.size(); i++) {
            VariableElement variableElement = fieldList.get(i);
            if (skipField(variableElement)) {
                continue;
            }
            toContentValue.addStatement("$L.$L = cursor.$L(cursor.getColumnIndexOrThrow(\"$L\"))$L"
                    , annotatedClass.getSimpleName().toString().toLowerCase()
                    , variableElement.getSimpleName().toString()
                    , Utils.getFieldType(variableElement, processingEnv, false)
                    , variableElement.getSimpleName().toString()
                    , Utils.addBooleanPostfix(variableElement, processingEnv));
        }
        toContentValue.addStatement("return $L", returnVariable);
        return toContentValue.build();
    }

    private MethodSpec getContentValueMethod(final TypeElement annotatedClass) {
        ClassName contentValues = ClassName.get("android.content", "ContentValues");
        MethodSpec.Builder toContentValue = MethodSpec.methodBuilder("getContentValue")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(contentValues)
                .addParameter(TypeName.get(annotatedClass.asType())
                        , annotatedClass.getSimpleName().toString().toLowerCase())
                .addStatement("$T result = new $T()", contentValues, contentValues);

        final List<VariableElement> fieldList = ElementFilter.fieldsIn(annotatedClass.getEnclosedElements());
        for (int i = 0; i < fieldList.size(); i++) {
            VariableElement variableElement = fieldList.get(i);
            if (skipField(variableElement)) {
                continue;
            }
            toContentValue.addStatement("result.put($S, $L.$L)"
                    , variableElement.getSimpleName().toString()
                    , annotatedClass.getSimpleName().toString().toLowerCase()
                    , variableElement.getSimpleName().toString());

        }
        toContentValue.addStatement("return result");
        return toContentValue.build();
    }

    private FieldSpec getTableCreateField(TypeElement annotatedClass) {
        StringBuilder tableCreateString = new StringBuilder()
                .append(Const.QUOTE)
                .append(Const.CREATE_TABLE)
                .append(annotatedClass.getSimpleName().toString().toLowerCase())
                .append(" (" + Const.LINE_END)
                .append(Const.QUOTE)
                .append(Const._ID)
                .append(Const.INTEGER)
                .append(Const.PRIMARY_KEY)
                .append(Const.AUTOINCREMENT)
                .append(Const.COMMA)
                .append(Const.LINE_END);

        final List<VariableElement> fieldList = ElementFilter.fieldsIn(annotatedClass.getEnclosedElements());
        for (int i = 0; i < fieldList.size(); i++) {
            VariableElement variableElement = fieldList.get(i);
            if (skipField(variableElement)) {
                continue;
            }
            tableCreateString.append(Const.QUOTE)
                    .append(variableElement.getSimpleName())
                    .append(Utils.getFieldType(variableElement, processingEnv, true));
            tableCreateString.append(Const.COMMA);
            tableCreateString.append(Const.LINE_END);
        }
        tableCreateString.deleteCharAt(tableCreateString.length()-5);
        tableCreateString.append(Const.STRING_END);
        return FieldSpec.builder(String.class, Const.CREATE)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(tableCreateString.toString())
                .build();
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
        annotationSet.add(TableFromClass.class.getCanonicalName());
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
