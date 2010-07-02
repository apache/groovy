package gls.annotations.closures;

import groovy.lang.Closure;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JavaAnnotationWithClassElementParameterized {
    Class<? extends Closure> elem();
}