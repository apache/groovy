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

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This will let the Groovy compiler use compile time checks in the style of Java
 * then perform static compilation, thus bypassing the Groovy meta object protocol.
 * <p>
 * When a class is annotated, all methods, properties, files, inner classes, etc.
 * of the annotated class will be type checked. When a method is annotated, static
 * compilation applies only to items (closures and anonymous inner classes) within
 * the method.
 * <p>
 * By using {@link TypeCheckingMode#SKIP}, static compilation can be skipped on an
 * element within a class or method otherwise marked with CompileStatic. For example
 * a class can be annotated with CompileStatic, and a method within can be marked
 * to skip static checking to use dynamic language features.
 *
 * @see CompileDynamic
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({   ElementType.METHOD,         ElementType.TYPE,
            ElementType.CONSTRUCTOR
})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.sc.StaticCompileTransformation")
public @interface CompileStatic {
    TypeCheckingMode value() default TypeCheckingMode.PASS;

    /**
     * The list of (classpath resources) paths to type checking DSL scripts, also known
     * as type checking extensions.
     * @return an array of paths to groovy scripts that must be on compile classpath
     */
    String[] extensions() default {};
}
