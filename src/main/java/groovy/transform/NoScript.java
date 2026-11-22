/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.transform;

import groovy.lang.Script;
import groovy.lang.annotation.ExtendedElementType;
import groovy.lang.annotation.ExtendedTarget;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Compiles a script file that contains only method definitions and declarations
 * into a plain public class, rather than a {@link Script} subclass.
 * <p>
 * The generated class extends {@code Object}. It has no {@code run()} method,
 * no generated {@code main} method and no {@code Binding} constructor; the file's
 * declarations become fields and its methods become methods of the class.
 * If every method is static and there are no declarations, the class gets a
 * private no-arg constructor instead of the default public one.
 * <p>
 * The annotation may be placed on the package declaration, on an import, or on a
 * top-level variable declaration. Other annotations placed alongside it on the package
 * declaration or an import are moved to the generated class:
 * <pre class="groovyTestCase">
 * def src = '''
 *     import groovy.transform.CompileStatic
 *     import groovy.transform.stc.POJO
 *
 *     {@code @NoScript} {@code @CompileStatic} {@code @POJO}
 *     import groovy.transform.NoScript
 *
 *     def greet(String name) { "Hello, $name" }
 * '''
 * def type = new GroovyClassLoader().parseClass(src, 'Greeter.groovy')
 * assert type.superclass == Object
 * assert !GroovyObject.isAssignableFrom(type)
 * assert type.getDeclaredConstructor().newInstance().greet('World') == 'Hello, World'
 * </pre>
 * A file with {@code @NoScript} must not contain statements other than declarations,
 * and the binding is not available.
 *
 * @see BaseScript
 * @since 7.0.0
 */
@Documented
@Incubating
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.LOCAL_VARIABLE, ElementType.PACKAGE})
@ExtendedTarget(ExtendedElementType.IMPORT)
@GroovyASTTransformationClass("org.codehaus.groovy.transform.NoScriptASTTransformation")
public @interface NoScript {
}
