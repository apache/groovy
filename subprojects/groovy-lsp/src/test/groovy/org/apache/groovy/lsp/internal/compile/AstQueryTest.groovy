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
package org.apache.groovy.lsp.internal.compile

import org.codehaus.groovy.ast.ModuleNode
import java.nio.file.Path
import org.eclipse.lsp4j.Position
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.control.SourceUnit
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.junit.jupiter.api.Test
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.junit.jupiter.api.io.TempDir

final class AstQueryTest {

    @TempDir
    Path folder


    @Test
    void astQueryCoversImportsParametersFieldsAndAttributeNames() {
        def source = '''\
            package demo.cov

            import java.util.ArrayList
            import java.io.*
            import static java.lang.Math.PI
            import static java.lang.System.*

            class Outer {
                private int secret
                int visible
                class Inner {
                    def nested() { 1 }
                }
                def use(ArrayList items) {
                    def closure = { int x -> x }
                    for (int i = 0; i < items.size(); i++) {
                        i
                    }
                    for (int idx, val in items) {
                        idx
                    }
                    try {
                        this.@secret
                    } catch (Exception caught) {
                        caught
                    }
                    PI
                }
            }
            '''.stripIndent()
        def compiled = compile(source)
        def module = compiled.module
        assert module.package != null
        assert module.classes.any { it instanceof InnerClassNode }

        def script = new ClassNode('ExtraScript', 0, ClassHelper.OBJECT_TYPE)
        script.script = true
        script.lineNumber = 0
        module.addClass(script)

        assert AstQuery.nodeAt(module, 1, 1) != null
        def declarations = AstQuery.declarations(module)
        assert declarations.any { it.is(module.package) }
        assert declarations.any { it instanceof FieldNode && it.name == 'secret' }

        def pkg = module.package
        assert AstQuery.containing(module, pkg.lineNumber, pkg.columnNumber).any { it.is(pkg) }
        def imp = module.imports[0]
        assert AstQuery.containing(module, imp.lineNumber, imp.columnNumber).any { it.is(imp) }

        def attributes = []
        AstQuery.walk(module, { node, ctx ->
            if (node instanceof AttributeExpression) {
                attributes << node
            }
        })
        assert !attributes.isEmpty()
        def property = attributes[0].property
        assert property.lineNumber > 0
        assert AstQuery.nodeAt(module, property.lineNumber, property.columnNumber) instanceof AttributeExpression
        def lift = AstQuery.getDeclaredMethod('liftNameLeaf', ASTNode, ASTNode)
        lift.accessible = true
        def liftedName = new ConstantExpression('secret')
        def lifted = new AttributeExpression(new VariableExpression('this'), liftedName)
        assert lift.invoke(null, liftedName, lifted).is(lifted)

        def document = compiled.toTextDocument()
        assert AstQuery.enclosingCall(module, document, new Position(0, 0), PositionEncoding.UTF16) != null ||
                document.text.contains('use')
        assert AstQuery.nodeAt(module, (TextDocument) null, new Position(0, 0), PositionEncoding.UTF16) == null
        assert AstQuery.enclosingCall(module, document, null, PositionEncoding.UTF16) == null
    }

    @Test
    void astQueryNullGuards() {
        assert AstQuery.containing(null, 1, 1).isEmpty()
        assert AstQuery.enclosingMethod(null, 1, 1) == null
        AstQuery.walk((ModuleNode) null, { node, ctx -> })
        AstQuery.walk(new ModuleNode((SourceUnit) null), null)
        AstQuery.walk((ASTNode) null, { node, ctx -> })
        AstQuery.walk(new ASTNode(), null)

        def script = compile('println 1\n')
        assert AstQuery.declarations(script.module) != null
    }

    private CompiledDocument compile(String text) {
        def compiler = new GroovyCompiler()
        try {
            def uri = folder.resolve("Cov${System.nanoTime()}.groovy").toUri()
            return compiler.compile([new TextDocument(uri, 'groovy', 1, text)], [],
                    CompilerSettings.defaults(), AstQueryTest.classLoader).get(uri)
        } finally {
            compiler.close()
        }
    }
}
