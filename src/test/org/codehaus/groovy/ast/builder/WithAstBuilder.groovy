package org.codehaus.groovy.ast.builder;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention
import java.lang.annotation.ElementType;

/**
 * This transformation annotation allows you to run AstBuilder.fromCode transformations
 * as local transformations in situations where the Global Transformation cannot be used.
 * There is no reason to ever use this outside the Groovy test sources tree.
 *
 * @author Hamlet D'Arcy
 */

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["org.codehaus.groovy.ast.builder.AstBuilderTransformation"])
public @interface WithAstBuilder {
}
