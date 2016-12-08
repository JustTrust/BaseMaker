package com.example;


import com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import javax.tools.Diagnostic;

public class DbProcessor extends AbstractProcessor {

    private Map<String, MakeDbAnnotatedClass> classList = new LinkedHashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if(roundEnvironment.getElementsAnnotatedWith(MakeDbFromClass.class).isEmpty()){
            return true;
        }

        log("Has annotated classes " + String.valueOf(roundEnvironment.getElementsAnnotatedWith(MakeDbFromClass.class).size()));
        // Iterate over all @MakeDbFromClass annotated elements
        for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(MakeDbFromClass.class)) {
            // Check if a class valid
            if (isValidClass(annotatedElement)){
                if (classList.get(annotatedElement.getClass().getSimpleName()) != null) {
                    classList.put(annotatedElement.getClass().getSimpleName(), new MakeDbAnnotatedClass((TypeElement) annotatedElement));
                    log("Add new class "+annotatedElement.getSimpleName());
                }
            } else {
                return true;
            }
        }

        generateCode();
        return false;
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
            error("The class %s is abstract. You can't annotate abstract classes @MakeDbFromClass", classElement);
            return false;
        }

        // Check fields: all fields must be public and not final;
        boolean classHasFields = false;
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                classHasFields = true;
                VariableElement variableElement = (VariableElement) enclosed;
                if (variableElement.getModifiers().contains(Modifier.FINAL)){
                    error("Fields in class can't be final", classElement);
                    return false;
                }
                if (! variableElement.getModifiers().contains(Modifier.PUBLIC)){
                    error("Fields in class should be public", classElement);
                    return false;
                }
            }
        }

        if (!classHasFields){
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

    private void generateCode(){
        if (classList.isEmpty()){
            log("List annotated classes is empty");
        }

        final TypeSpec buildMainClass = TypeSpec.classBuilder("DB")
                .addModifiers(Modifier.PUBLIC)
                .build();


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
