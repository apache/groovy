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

import org.apache.groovy.lsp.internal.LspFixture
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.eclipse.lsp4j.Position
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

import javax.tools.ToolProvider

final class JointCompilationLspTest {

    @TempDir
    Path folder

    @Test
    void groovyResolvesJavaTypeAndJumpsToJavaSource() {
        assumeJavac()
        def fixture = new LspFixture()
        try {
            def javaUri = fixture.open(folder.resolve('Widget.java').toString(), '''\
                public class Widget {
                    public String id() { return "w"; }
                }
                '''.stripIndent(), 'java')
            def groovyUri = fixture.open(folder.resolve('Use.groovy').toString(), '''\
                class Use {
                    def run() {
                        new Widget().id()
                    }
                }
                '''.stripIndent())
            def compiled = fixture.server.context.snapshot.get(groovyUri)
            assert compiled != null
            assert compiled.errors.every { !it.toString().contains('unable to resolve class Widget') }
            def document = fixture.server.context.documentFor(groovyUri)
            def line = document.text.readLines().findIndexOf { it.contains('Widget') }
            def column = document.text.readLines()[line].indexOf('Widget')
            def links = fixture.server.features().navigation().definition(
                    document, fixture.server.context.snapshot,
                    new Position(line, column), PositionEncoding.UTF16)
            assert links
            assert links[0].targetUri == javaUri || links[0].targetUri.endsWith('Widget.java')
            def idLine = document.text.readLines().findIndexOf { it.contains('.id()') }
            def idColumn = document.text.readLines()[idLine].indexOf('id')
            def methodLinks = fixture.server.features().navigation().definition(
                    document, fixture.server.context.snapshot,
                    new Position(idLine, idColumn), PositionEncoding.UTF16)
            assert methodLinks
            assert methodLinks[0].targetUri.endsWith('Widget.java')
        } finally {
            fixture.close()
        }
    }

    @Test
    void javaResolvesGroovyType() {
        assumeJavac()
        def fixture = new LspFixture()
        try {
            fixture.open(folder.resolve('Gadget.groovy').toString(), '''\
                class Gadget {
                    String name
                }
                '''.stripIndent())
            def javaUri = fixture.open(folder.resolve('Holder.java').toString(), '''\
                public class Holder {
                    Gadget gadget;
                }
                '''.stripIndent(), 'java')
            def document = fixture.server.context.documentFor(javaUri)
            def line = document.text.readLines().findIndexOf { it.contains('Gadget') }
            def column = document.text.readLines()[line].indexOf('Gadget')
            def links = fixture.server.features().navigation().definition(
                    document, fixture.server.context.snapshot,
                    new Position(line, column), PositionEncoding.UTF16)
            assert links
            assert links[0].targetUri.endsWith('Gadget.groovy')
            def javaDoc = fixture.server.context.snapshot.get(javaUri)
            assert javaDoc.javaMessages.every { !it.text.contains('cannot find symbol') }
        } finally {
            fixture.close()
        }
    }

    @Test
    void wordAroundIncludesCaretAtStart() {
        assert Identifiers.wordAround('    Gadget gadget;', 4) == 'Gadget'
        assert Identifiers.wordAround('    Gadget gadget;', 8) == 'Gadget'
        assert Identifiers.wordAround('    Gadget gadget;', 11) == 'gadget'
    }

    private static void assumeJavac() {
        Assumptions.assumeTrue(ToolProvider.systemJavaCompiler != null, 'jdk.compiler is required')
    }
}
