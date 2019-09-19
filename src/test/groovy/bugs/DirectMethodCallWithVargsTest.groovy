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
package groovy.bugs

import groovy.test.GroovyTestCase
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.ast.ClassNode

class DirectMethodCallWithVargsTest extends GroovyTestCase {
    
    void testDirectMethodCallWithVargs() {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(
                new MyCustomizer()
        )
        GroovyShell shell = new GroovyShell(config)
        shell.evaluate '''
            def foo(String... args) {
                (args as List).join(',')
            }
            assert foo() == ''
            assert foo('1') == '1'
            assert foo('1','2','3') == '1,2,3'
            assert foo('1','2','3','4') == '1,2,3,4'
            
            def a = '1'
            def b = '2'
            def c = '3'
            assert foo(a,b,c) == '1,2,3'
            
        '''
    }

    void testDirectMethodCallWithPrimitiveVargs() {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(
                new MyCustomizer()
        )
        GroovyShell shell = new GroovyShell(config)
        shell.evaluate '''
            def foo(int... args) {
                (args as List).join(',')
            }
            assert foo() == ''
            assert foo(1) == '1'
            assert foo(1,2,3) == '1,2,3'
            assert foo(1,2,3,4) == '1,2,3,4'
        '''
    }
    
    void testDirectMethodCallWithArgPlusVargs() {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(
                new MyCustomizer()
        )
        GroovyShell shell = new GroovyShell(config)
        shell.evaluate '''
            def foo(String prefix, String... args) {
                prefix+(args as List).join(',')
            }
            assert foo('A') == 'A'
            assert foo('A','1') == 'A1'
            assert foo('A','1','2','3') == 'A1,2,3'
            assert foo('A','1','2','3','4') == 'A1,2,3,4'
            
            def a = '1'
            def b = '2'
            def c = '3'
            assert foo('A',a,b,c) == 'A1,2,3'
            
        '''
    }

    void testDirectMethodCallWithPrefixAndPrimitiveVargs() {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(
                new MyCustomizer()
        )
        GroovyShell shell = new GroovyShell(config)
        shell.evaluate '''
            def foo(int prefix, int... args) {
                "$prefix"+(args as List).join(',')
            }
            assert foo(1) == '1'
            assert foo(1,1) == '11'
            assert foo(1,1,2,3) == '11,2,3'
            assert foo(1,1,2,3,4) == '11,2,3,4'
        '''
    }

    private static class MyCustomizer extends CompilationCustomizer {

        MyCustomizer() {
            super(CompilePhase.CANONICALIZATION)
        }

        @Override
        void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
            def visitor = new MethodCallVisitor(source)
            classNode.methods.each { visitor.visitMethod(it) }
            visitor.visitClass(classNode)
        }
    }
    
    private static class MethodCallVisitor extends ClassCodeVisitorSupport {
        private final SourceUnit unit
        private MethodNode fooMethod
        
        MethodCallVisitor(SourceUnit source) {
            unit = source
        }
        
        @Override
        protected SourceUnit getSourceUnit() {
            return unit
        }

        @Override
        void visitMethod(final MethodNode node) {
            super.visitMethod(node)
            if (node.name=='foo') {
                fooMethod = node
            }
        }


        @Override
        void visitMethodCallExpression(final MethodCallExpression call) {
            super.visitMethodCallExpression(call)
            if (call.methodAsString=='foo') {
                call.methodTarget = fooMethod
            }
        }


    }
}
