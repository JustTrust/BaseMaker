package com.abelichenko;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Belichenko Anton on 08.12.16.
 * mailto: a.belichenko@gmail.com
 */

@Target(ElementType.FIELD) @Retention(RetentionPolicy.SOURCE)
public @interface ExcludeField {
}
