/*
 * Copyright 2008 the original author or authors.
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

package org.codehaus.groovy.ast;

import org.codehaus.groovy.control.Phases;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * This is an annotaton on an annotaiton.  This indicates that the annotated
 * annotation be handeled specially by the groovy compiler.
 *
 * Specifically, when the annotated annotation is encountered in the
 * appropriate compilation phase ( see {@link Phases}) then an instance of
 * the {@link #transformationClassName} has its
 * {@link ASTSingleNodeTransformation#visit(ASTNode[],org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.classgen.GeneratorContext)}
 * method called at that annotation.
 *
 * Note that only the SEMANTIC_ANALYSIS and latter phases are legal for
 * the phase().  This is because the annotations are not inspected until
 * after the classes are all resolved.
 *
 * Also, only annotation types present durring the SEMANTIC_ANALYSIS phase
 * will be handleded.  Transformations adding other annotations that are
 * transformable will have those new annotations only considered in
 * latter phases, and only if the type was present in the source unit
 * durring SEMANTIC_ANALYSIS.
 *
 * It is a compile time error to specify {@link Phases#INITIALIZATION},
 * {@link Phases#PARSING}, or {@link Phases#CONVERSION} as a {@link #phase}.
 *
 * It is a compile time error to specify a {@link #phase} that does not exit
 *
 * It is a compile time error to specify a {@link #transformationClassName} that
 * is not accessable at compileTime. 
 *
 * @author Danno Ferrin (shemnon)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface GroovyASTTransformation {
    String transformationClassName();
    int phase() default Phases.CANONICALIZATION;
}
