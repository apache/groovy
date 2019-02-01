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
 */

@Retention(RetentionPolicy.RUNTIME)
// in the future the target will be wider than annotations, but for now it is just on annotations
@Target(ElementType.ANNOTATION_TYPE)
public @interface GroovyASTTransformationClass {
    String[] value() default {};
    Class[] classes() default {};
}
