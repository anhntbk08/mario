package com.nhb.common.cache;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ FIELD, METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyField {
	Class<?> translator() default BaseKeyTranslator.class;
}
