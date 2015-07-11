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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.sc.StaticCompilationVisitor
import org.codehaus.groovy.transform.sc.transformers.StaticCompilationTransformer
import org.objectweb.asm.Opcodes

class Groovy7222OptimizationsTest extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport  {
    void testShouldOptimizeConstantInitialization() {
        def values = [
                (short) 2,
                (byte) 2,
                2i,
                2.0G,
                2.5G,
                2.0f,
                2.5f,
                2.5d,
                2L
        ]
        def types = [
                ClassHelper.byte_TYPE,
                ClassHelper.short_TYPE,
                ClassHelper.int_TYPE,
                ClassHelper.long_TYPE,
                ClassHelper.float_TYPE,
                ClassHelper.double_TYPE,

                ClassHelper.Byte_TYPE,
                ClassHelper.Short_TYPE,
                ClassHelper.Integer_TYPE,
                ClassHelper.Long_TYPE,
                ClassHelper.Float_TYPE,
                ClassHelper.Double_TYPE,

                ClassHelper.BigInteger_TYPE,
                ClassHelper.BigDecimal_TYPE
        ]
        // dummy source setup
        ClassNode cn = new ClassNode('Foo', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        CompilerConfiguration conf = new CompilerConfiguration()
        SourceUnit su = new SourceUnit('Foo.groovy', '', conf, new GroovyClassLoader(), new ErrorCollector(conf))
        StaticCompilationTransformer transformer = new StaticCompilationTransformer(su, new StaticCompilationVisitor(su, cn))
        [types,values].combinations { declarationType, value ->
            DeclarationExpression dex = new DeclarationExpression(
                    new VariableExpression("x", declarationType),
                    Token.newSymbol(Types.EQUAL, -1, -1),
                    new ConstantExpression(value,true)
            )
            def optimized = transformer.transform(dex)
            assert optimized instanceof DeclarationExpression
            def varType = optimized.leftExpression.originType
            def constantType = optimized.rightExpression.type
            assert varType == constantType
        }

    }

    void testShouldNotContainBigDecimalInBytecode() {
        try {
            assertScript '''
                double d = 2.5 // forgot to add the 'd' so normally implies new BigDecimal
            '''
        } finally {
            def bytecode = astTrees.entrySet().find { it.key =~ /BigDecimal/ }.value[1]
            assert bytecode.contains('LDC 2.5')
            assert bytecode.contains('DSTORE')
            assert !bytecode.contains('java/math/BigDecimal')
        }
    }

    void testShouldNotThrowNPE() {
        assertScript '''
            @groovy.transform.CompileStatic
            void foo() {
              Double d = null
            }

            foo()
        '''
    }
}
