package org.codehaus.groovy.grails.compiler.injection;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Jan 30, 2008
 * Time: 8:11:08 PM
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass("org.codehaus.groovy.grails.compiler.injection.DefaultGrailsDomainClassInjector")
public @interface DomainClass {
}
