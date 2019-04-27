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
package org.codehaus.groovy.transform

import groovy.transform.CompilationUnitAware
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit

class TestTransform implements ASTTransformation, CompilationUnitAware {
    static List<ASTNode[]> visitedNodes = []
    static Map<String, List<CompilePhase>> phases = [:].withDefault{ [] }
    CompilationUnit unit = null

    void visit(ASTNode[] nodes, SourceUnit source) {
        visitedNodes += nodes
        // TODO work out why source.phase is not equal to unit.phase in all cases
        phases[getClass().simpleName] += CompilePhase.phases[unit.phase]
    }

    @Override
    void setCompilationUnit(CompilationUnit unit) {
        this.unit = unit
    }
}

@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
class TestTransformConversion extends TestTransform { }

@GroovyASTTransformation(phase=CompilePhase.CLASS_GENERATION)
class TestTransformClassGeneration extends TestTransform { }
