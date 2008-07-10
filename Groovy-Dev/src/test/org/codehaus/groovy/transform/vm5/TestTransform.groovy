/*
 * Copyright 2003-2008 the original author or authors.
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
package org.codehaus.groovy.transform.vm5

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * @author Danno.Ferrin
 */
class TestTransform implements ASTTransformation {

    static List<ASTNode[]> visitedNodes = []
    static List<CompilePhase> phases = []

    public void visit(ASTNode[] nodes, SourceUnit source) {
        visitedNodes += nodes
        phases += CompilePhase.phases[source.getPhase()]
    }

}

@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
class TestTransformConversion extends TestTransform {

}

@GroovyASTTransformation(phase=CompilePhase.CLASS_GENERATION)
class TestTransformClassGeneration extends TestTransform {

}