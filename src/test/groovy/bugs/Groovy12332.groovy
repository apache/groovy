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
package bugs

import org.antlr.v4.runtime.ParserRuleContext
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.control.CompilationUnit
import org.junit.jupiter.api.Test

import java.lang.ref.SoftReference
import java.lang.ref.WeakReference

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * A finished class must not keep its parse tree or the parser's prediction
 * cache alive. Both are large for one big class and used to stay reachable
 * through class generation.
 */
final class Groovy12332 {

    private ModuleNode heldModule

    // GROOVY-12332
    @Test
    void testFinishedClassDoesNotPinParseGraph() {
        def unit = new CompilationUnit()
        unit.addSource('C.groovy', '''
            @groovy.transform.CompileStatic
            class C {
                int m(int a, String b) {
                    def c = { int x -> x + a }
                    c(1) + (b == null ? 0 : b.length())
                }
            }
        ''')
        unit.compile()
        heldModule = unit.AST.modules[0]

        assertTrue(parserContexts(heldModule).isEmpty(),
                'parameters must not retain their parse context after the class is built')

        def atn = currentParserAtn()
        assertNotNull(atn, 'precondition: the parser cache exists after a compile')
        def atnRef = new WeakReference(atn)
        clearParserWrapperSoftReference()
        atn = null

        boolean collected = false
        for (int i = 0; i < 50 && !collected; i++) {
            System.gc()
            collected = atnRef.get() == null
            if (!collected) Thread.sleep(10)
        }
        assertTrue(collected, 'the prediction cache must not stay strongly reachable from the finished class')
        assertNotNull(heldModule.classes[0].name)
    }

    private static List<Object> parserContexts(ModuleNode module) {
        List<Object> found = []
        module.classes.each { ClassNode classNode ->
            classNode.declaredConstructors.each { ctor ->
                collect(ctor.parameters, found)
                ctor.code?.visit(visitor(found))
            }
            classNode.methods.each { MethodNode method ->
                collect(method.parameters, found)
                method.code?.visit(visitor(found))
            }
        }
        found
    }

    private static CodeVisitorSupport visitor(List<Object> found) {
        new CodeVisitorSupport() {
            @Override
            void visitClosureExpression(ClosureExpression expression) {
                collect(expression.parameters, found)
                super.visitClosureExpression(expression)
            }
        }
    }

    private static void collect(Parameter[] parameters, List<Object> found) {
        if (parameters == null) return
        for (Parameter parameter : parameters) {
            def map = parameter?.metaDataMap
            if (map == null) continue
            map.values().each { value ->
                if (value instanceof ParserRuleContext) found.add(value)
            }
        }
    }

    private static Object currentParserAtn() {
        def wrapper = parserWrapperSoftReference()?.get()
        if (wrapper == null) return null
        def atnField = wrapper.getClass().getDeclaredField('atn')
        atnField.accessible = true
        atnField.get(wrapper)
    }

    private static void clearParserWrapperSoftReference() {
        parserWrapperSoftReference()?.clear()
    }

    private static SoftReference parserWrapperSoftReference() {
        def manager = Class.forName('org.apache.groovy.parser.antlr4.internal.atnmanager.ParserAtnManager').INSTANCE
        def refField = Class.forName('org.apache.groovy.parser.antlr4.internal.atnmanager.AtnManager')
                .getDeclaredField('atnWrapperSoftReference')
        refField.accessible = true
        (SoftReference) refField.get(manager)
    }
}
