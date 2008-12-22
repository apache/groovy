package org.codehaus.groovy.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an annotation on some item that indicates that
 * an associated transform classes should be executed.  As of
 * Groovy 1.6 the only valid target is the annotation type.
 *
 * Each of the class names in the value must be annotated with
 * {@link GroovyASTTransformation}.
 *
 * It is a compile time error to specify a {@link GroovyASTTransformationClass}
 * that is not accessible at compile time.  It need not be available at runtime.
 *
 * @author Danno Ferrin (shemnon)
 */

@Retention(RetentionPolicy.RUNTIME)
// in the future the target will be wider than annotations, but for now it is just on annotations
@Target(ElementType.ANNOTATION_TYPE)
public @interface GroovyASTTransformationClass {
    String[] value();
}
