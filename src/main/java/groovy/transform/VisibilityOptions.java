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

import groovy.transform.options.Visibility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation used in the context of AST transformations to provide a custom visibility.
 *
 * @since 2.5.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface VisibilityOptions {
    Visibility value() default Visibility.UNDEFINED;
    String id() default Undefined.STRING;
    Visibility type() default Visibility.UNDEFINED;
    Visibility method() default Visibility.UNDEFINED;
    Visibility constructor() default Visibility.UNDEFINED;
//    Visibility field() default Visibility.UNDEFINED;
}
