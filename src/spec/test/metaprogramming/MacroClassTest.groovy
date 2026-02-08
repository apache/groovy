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

package metaprogramming

import groovy.test.GroovyTestCase
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.macro.transform.MacroClass
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import static org.codehaus.groovy.ast.tools.GeneralUtils.constX

class MacroClassTest extends GroovyTestCase {

    void testMacroClass() {
        assertScript '''
        package metaprogramming

        @Statistics
        class Person {
            Integer age
            String name
        }

        def person = new Person(age: 12, name: 'john')

        assert person.methodCount == 0
        assert person.fieldCount  == 2
        '''
    }
}

// tag::statisticsannotation[]
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["metaprogramming.StatisticsASTTransformation"])
@interface Statistics {}
// end::statisticsannotation[]

// tag::statisticstransformation[]
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class StatisticsASTTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        ClassNode classNode = (ClassNode) nodes[1]
        ClassNode templateClass = buildTemplateClass(classNode)  // <1>

        templateClass.methods.each { MethodNode node ->          // <2>
            classNode.addMethod(node)
        }
    }

    @CompileDynamic
    ClassNode buildTemplateClass(ClassNode reference) {          // <3>
        def methodCount = constX(reference.methods.size())       // <4>
        def fieldCount = constX(reference.fields.size())         // <5>

        return new MacroClass() {
            class Statistics {
                java.lang.Integer getMethodCount() {             // <6>
                    return $v { methodCount }
                }

                java.lang.Integer getFieldCount() {              // <7>
                    return $v { fieldCount }
                }
            }
        }
    }
}
// end::statisticstransformation[]
