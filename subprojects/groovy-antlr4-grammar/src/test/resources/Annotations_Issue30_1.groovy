@Annotated package com.xseagullx.groovy.gsoc;

import groovy.transform.Canonical
import groovy.util.logging.Log

@Annotated import java.beans.Transient

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Canonical(
    includes = ['a', 'b'], excludes = ['c']
) class AnnotatedClass {}

@Log class ClassMarkerAnnotation {}

@      Log class ClassMarkerAnnotationWithSpaces {}


@groovy.transform.ToString class ClassMarkerAnnotationFull {}

@Category(a) class ClassSingleValueAnnotation { }
@Category(a.b.c.d) class PathValueAnnotation { }

@Ann(
    @Retention(value = RetentionPolicy.CLASS)
) class ClassNestedAnnotation {}

class A {
    @Deprecated def markerField

    @Transient(1) @Log private <T> T singleValueMethod(@Deprecated int a) {}
}
