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

import org.codehaus.groovy.control.CompilePhase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an annotation on a class, currently just {@link ASTTransformation}.
 * This provides information about how and when to apply the transformation,
 * such as what phase it should be applied in.
 *
 * The allowed phase is a function of how the transformation is introduced
 * into the compile process.  If the transform is automatically added via a
 * marker annotation only the SEMANTIC_ANALYSIS and latter phases are legal
 * for the phase().  This is because the annotations are not inspected until
 * after the classes are all resolved.
 *
 * Also, only annotation types present during the SEMANTIC_ANALYSIS phase
 * will be handled.  Transformations adding other annotations that are
 * transformable will have those new annotations only considered in
 * latter phases, and only if the type was present in the source unit
 * during SEMANTIC_ANALYSIS.
 *
 * @see ASTTransformation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GroovyASTTransformation {
    CompilePhase phase() default CompilePhase.CANONICALIZATION;
}
