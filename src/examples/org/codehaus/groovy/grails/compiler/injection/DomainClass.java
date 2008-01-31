package org.codehaus.groovy.grails.compiler.injection;

import org.codehaus.groovy.ast.GroovyASTTransformation;
import org.codehaus.groovy.control.Phases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Jan 30, 2008
 * Time: 8:11:08 PM
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformation(transformationClass = DefaultGrailsDomainClassInjector.class, phase = Phases.CANONICALIZATION)
public @interface DomainClass {
}
