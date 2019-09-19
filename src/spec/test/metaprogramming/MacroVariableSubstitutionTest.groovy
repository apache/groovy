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
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

class MacroVariableSubstitutionTest extends GroovyTestCase {

    void testVariableSubstitution() {
        assertScript '''
        package metaprogramming

        class A {
            @MD5
            String word
        }

        def instance = new A(word: "Groovy")

        assert instance.getWordMD5() == '2d19c57fdd4fdc270c971f69ee8d5169'
        '''
    }
}
// tag::md5annotation[]
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.FIELD])
@GroovyASTTransformationClass(["metaprogramming.MD5ASTTransformation"])
@interface MD5 { }
// end::md5annotation[]

// tag::md5transformation[]
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class MD5ASTTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        FieldNode fieldNode = nodes[1]
        ClassNode classNode = fieldNode.declaringClass
        String capitalizedName = fieldNode.name.capitalize()
        MethodNode methodNode = new MethodNode(
                "get${capitalizedName}MD5",
                ACC_PUBLIC,
                ClassHelper.STRING_TYPE,
                [] as Parameter[],
                [] as ClassNode[],
                buildMD5MethodCode(fieldNode))

        classNode.addMethod(methodNode)
    }

    BlockStatement buildMD5MethodCode(FieldNode fieldNode) {
        VariableExpression fieldVar = GeneralUtils.varX(fieldNode.name) // <1>

        return macro(CompilePhase.SEMANTIC_ANALYSIS, true) {            // <2>
            return java.security.MessageDigest
                    .getInstance('MD5')
                    .digest($v { fieldVar }.getBytes())                 // <3>
                    .encodeHex()
                    .toString()
        }
    }
}
// end::md5transformation[]
