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

import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.classgen.GeneratorContext;

/**
 * This is the delegate class for {@link GroovyASTTransformation}s.
 *
 * @author Danno Ferrin (shemnon)
 */
public interface ASTAnnotationTransformation {

    /**
     * The call made when the compiler encounters an AST Transformation Annotation
     *
     * @param node The actual annotation node that triggered the call
     * @param parent The node that was being annotated by the AST Transformation Annotation
     * @param source The source unit being compiled
     * @param generatorContext the generation context of the source unit
     */
    public void visit(AnnotationNode node, AnnotatedNode parent, SourceUnit source, GeneratorContext generatorContext);
}
