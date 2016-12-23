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

    static String getSQLiteFieldType(VariableElement variableElement, ProcessingEnvironment pe) {
        TypeMirror typeMirror = variableElement.asType();
        if (typeMirror.getKind().isPrimitive()) {
            switch (typeMirror.getKind()) {
                case BOOLEAN:
                    return Const.INTEGER;
                case BYTE:
                    return Const.INTEGER;
                case SHORT:
                    return Const.INTEGER;
                case INT:
                    return Const.INTEGER;
                case CHAR:
                    return Const.INTEGER;
                case LONG:
                    return Const.INTEGER;
                case FLOAT:
                    return Const.REAL;
                case DOUBLE:
                    return Const.REAL;
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
                    return Const.TEXT;
                }else if(typeElement.getQualifiedName().toString().equals(Integer.class.getCanonicalName())){
                    return Const.INTEGER;
                }else if(typeElement.getQualifiedName().toString().equals(Byte.class.getCanonicalName())){
                    return Const.INTEGER;
                }else if(typeElement.getQualifiedName().toString().equals(Short.class.getCanonicalName())){
                    return Const.INTEGER;
                }else if(typeElement.getQualifiedName().toString().equals(Long.class.getCanonicalName())){
                    return Const.INTEGER;
                }else if(typeElement.getQualifiedName().toString().equals(Double.class.getCanonicalName())){
                    return Const.REAL;
                }else if(typeElement.getQualifiedName().toString().equals(Float.class.getCanonicalName())){
                    return Const.REAL;
                }else if(typeElement.getQualifiedName().toString().equals(Boolean.class.getCanonicalName())) {
                    return Const.INTEGER;
                }else if(hasSerializableInterface(typeElement, pe)){
                    return Const.TEXT;
                }else{
                    pe.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Field type " + typeElement.getQualifiedName().toString() + " doesn't supported ",
                            variableElement);
                }
            } else {
                pe.getMessager().printMessage(Diagnostic.Kind.ERROR, "Field "+ variableElement.getSimpleName()
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

}
