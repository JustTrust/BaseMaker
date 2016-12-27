package com.abelichenko;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by Admin on 11.12.2016.
 */

class Utils {

    static String getFieldType(VariableElement variableElement, ProcessingEnvironment pe, boolean getType) {
        TypeMirror typeMirror = variableElement.asType();
        if (typeMirror.getKind().isPrimitive()) {
            switch (typeMirror.getKind()) {
                case BOOLEAN:
                    return getType ? Const.INTEGER : Const.GET_INT;
                case BYTE:
                    return getType ? Const.INTEGER : Const.GET_INT;
                case SHORT:
                    return getType ? Const.INTEGER : Const.GET_SHORT;
                case INT:
                    return getType ? Const.INTEGER : Const.GET_INT;
                case CHAR:
                    return getType ? Const.INTEGER : Const.GET_INT;
                case LONG:
                    return getType ? Const.INTEGER : Const.GET_LONG;
                case FLOAT:
                    return getType ? Const.REAL : Const.GET_FLOAT;
                case DOUBLE:
                    return getType ? Const.REAL : Const.GET_DOUBLE;
                default:
                    pe.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Field type " + typeMirror.getKind() + " doesn't supported ",
                            variableElement);
            }
        } else {
            Element element = pe.getTypeUtils().asElement(typeMirror);
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;

                if (typeElement.getQualifiedName().toString().equals(String.class.getCanonicalName())) {
                    return getType ? Const.TEXT : Const.GET_TEXT;
                } else if (typeElement.getQualifiedName().toString().equals(Integer.class.getCanonicalName())) {
                    return getType ? Const.INTEGER : Const.GET_INT;
                } else if (typeElement.getQualifiedName().toString().equals(Byte.class.getCanonicalName())) {
                    return getType ? Const.INTEGER : Const.GET_INT;
                } else if (typeElement.getQualifiedName().toString().equals(Short.class.getCanonicalName())) {
                    return getType ? Const.INTEGER : Const.GET_SHORT;
                } else if (typeElement.getQualifiedName().toString().equals(Long.class.getCanonicalName())) {
                    return getType ? Const.INTEGER : Const.GET_LONG;
                } else if (typeElement.getQualifiedName().toString().equals(Double.class.getCanonicalName())) {
                    return getType ? Const.REAL : Const.GET_DOUBLE;
                } else if (typeElement.getQualifiedName().toString().equals(Float.class.getCanonicalName())) {
                    return getType ? Const.REAL : Const.GET_FLOAT;
                } else if (typeElement.getQualifiedName().toString().equals(Boolean.class.getCanonicalName())) {
                    return getType ? Const.INTEGER : Const.GET_INT;
                } else if (hasSerializableInterface(typeElement, pe)) {
                    return getType ? Const.TEXT : Const.GET_TEXT;
                } else {
                    pe.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Field type " + typeElement.getQualifiedName().toString() + " doesn't supported ",
                            variableElement);
                }
            } else {
                pe.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field " + variableElement.getSimpleName()
                        + " has not supported type ", variableElement);
            }
        }
        return Const.TEXT;
    }

    private static boolean hasSerializableInterface(TypeElement typeElement, ProcessingEnvironment pe) {
        for (TypeMirror typeMirror : typeElement.getInterfaces()) {
            if (typeMirror.toString().equals("java.io.Serializable")) return true;
        }

        return false;
    }

    public static String addBooleanPostfix(VariableElement variableElement, ProcessingEnvironment pe) {
        String result = "";
        TypeMirror typeMirror = variableElement.asType();
        if (typeMirror.getKind().isPrimitive()) {
            if (typeMirror.getKind() == TypeKind.BOOLEAN) {
                result = " > 0";
            }
        } else {
            Element element = pe.getTypeUtils().asElement(typeMirror);
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                if (typeElement.getQualifiedName().toString().equals(Boolean.class.getCanonicalName())) {
                    result = " > 0";
                }
            }
        }
        return result;
    }
}
