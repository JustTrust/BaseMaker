package com.example;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

/**
 * Created by Belichenko Anton on 08.12.16.
 * mailto: a.belichenko@gmail.com
 */

class MakeDbAnnotatedClass {

    private TypeElement annotatedClassElement;
    private String qualifiedSuperClassName;
    private String simpleTypeName;

    public MakeDbAnnotatedClass(TypeElement classElement) throws IllegalArgumentException  {
        this.annotatedClassElement = classElement;
        MakeDbFromClass annotation = classElement.getAnnotation(MakeDbFromClass.class);

        // Get the full QualifiedTypeName
        try {
            Class<?> clazz = annotation.annotationType();
            qualifiedSuperClassName = clazz.getCanonicalName();
            simpleTypeName = clazz.getSimpleName();
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
            simpleTypeName = classTypeElement.getSimpleName().toString();
        }
    }

    public String getQualifiedFactoryGroupName() {
        return qualifiedSuperClassName;
    }


    public String getSimpleFactoryGroupName() {
        return simpleTypeName;
    }


    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }
}
