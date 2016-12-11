package com.example;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by Admin on 11.12.2016.
 */

class Utils {

    static String getFieldType(VariableElement variableElement, ProcessingEnvironment pe) {
        TypeMirror typeMirror = variableElement.asType();
        if (typeMirror.getKind().isPrimitive()) {
            switch (typeMirror.getKind()) {
                case BOOLEAN:
                    return Const.TEXT;
                case BYTE:
                    return Const.INTEGER;
                case SHORT:
                    return Const.INTEGER;
                case INT:
                    return Const.INTEGER;
                case CHAR:
                    return Const.INTEGER;
                case LONG: // TODO: 11.12.2016 check the long in sqlite
                    return Const.INTEGER;
                case FLOAT:
                    return Const.REAL;
                case DOUBLE:
                    return Const.REAL;
                default:
                    pe.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Field type " + typeMirror.getKind() + " doesn't support ",
                            variableElement);
            }
        } else {
            Element element = pe.getTypeUtils().asElement(typeMirror);
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                //pe.getMessager().printMessage(Diagnostic.Kind.NOTE, typeElement.getQualifiedName());
                if (typeElement.getQualifiedName().toString().equals(String.class.getCanonicalName())) {
                    return Const.TEXT;
                }else{
                    pe.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Field type " + typeElement.getQualifiedName().toString() + " doesn't support ",
                            variableElement);
                }
            } else {
                pe.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field type doesn't support ", variableElement);
            }

        }
        return Const.TEXT;
    }
}
