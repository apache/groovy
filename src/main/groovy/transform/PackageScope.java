/*
 * Copyright 2008-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.transform;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class, method or field annotation used for turning off Groovy's auto
 * visibility conventions. By default, Groovy automatically turns package
 * protected fields into properties and makes package protected methods
 * and classes public. This annotation allows this feature to be turned
 * off and revert back to Java behavior if needed.
 *
 * Place it on classes, fields or methods of interest as follows:
 * <pre>
 * {@code @}PackageScope class Bar {      // package protected
 *     {@code @}PackageScope int field    // package protected; not a property
 *     {@code @}PackageScope method(){}   // package protected
 * }
 * </pre>
 * or for greater control, at the class level with one or more
 * <code>PackageScopeTarget</code> values:
 * <pre>
 * import static groovy.transform.PackageScopeTarget.*
 * {@code @}PackageScope([CLASS, FIELDS])
 * class Foo {              // class will have package protected scope
 *     int field1, field2   // both package protected
 *     def method(){}       // public
 * }
 * {@code @}PackageScope(METHODS)
 * class Bar {         // public
 *     int field       // treated as a property
 *     def method1(){} // package protected
 *     def method2(){} // package protected
 * }
 * </pre>
 *
 * This transformation is typically only used in conjunction with a third-party
 * library or framework which relies upon package scoping.
 *
 * @author Paul King
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.PackageScopeASTTransformation")
public @interface PackageScope {
    groovy.transform.PackageScopeTarget[] value() default {PackageScopeTarget.CLASS};
}
