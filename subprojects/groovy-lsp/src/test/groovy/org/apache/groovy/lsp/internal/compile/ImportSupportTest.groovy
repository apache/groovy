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
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.control.SourceUnit
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.codehaus.groovy.ast.PackageNode
import org.junit.jupiter.api.Test
import org.codehaus.groovy.ast.ClassHelper
import org.junit.jupiter.api.io.TempDir

final class ImportSupportTest {

    @TempDir
    Path folder


    @Test
    void importSupportCoversAliasesStarsAndUnpositionedRanges() {
        def plain = new ModuleNode((SourceUnit) null)
        plain.addImport('List', ClassHelper.make('java.util.List'))
        assert ImportSupport.needsImport(plain, 'com.example.Other')
        assert ImportSupport.importedName(new ImportNode(ClassHelper.make('java.lang.Math'), 'PI', 'Pie')) == 'Pie'
        assert ImportSupport.importedName(new ImportNode('java.util.')) == null

        def compiled = compile('class Holder {}\n')
        assert ImportSupport.addImport(compiled, 'java.lang.String', PositionEncoding.UTF16).isEmpty()

        def module = new ModuleNode((SourceUnit) null)
        module.setPackage(new PackageNode('demo'))
        def text = '\nimport com.example.Widget as Alias\nclass C {}\n'
        module.addImport('Alias', ClassHelper.make('com.example.Widget'))
        module.addImport('Missing', ClassHelper.make('org.missing.Thing'))
        def blank = new ImportNode(ClassHelper.make('org.blank.Type'), 'Blank') {
            @Override
            String getText() {
                return ''
            }
        }
        assert blank.getText().isEmpty()
        def uri = URI.create('file:///tmp/coverage-import.groovy')
        def document = new CompiledDocument(uri, 1, text, module, null, null)
        def rangeOf = ImportSupport.getDeclaredMethod('rangeOf', ImportNode, CompiledDocument, PositionEncoding)
        rangeOf.accessible = true
        assert rangeOf.invoke(null, blank, document, PositionEncoding.UTF16) == null
        assert ImportSupport.unusedDiagnostics(document, PositionEncoding.UTF16).size() == 1
        assert ImportSupport.removeUnused(document, PositionEncoding.UTF16).size() == 1

        def positioned = new ModuleNode((SourceUnit) null)
        positioned.setPackage(new PackageNode('demo'))
        positioned.addImport('List', ClassHelper.make('java.util.List'))
        def added = positioned.imports[0]
        added.lineNumber = 2
        added.columnNumber = 1
        added.lastLineNumber = 2
        added.lastColumnNumber = 24
        def withPackage = new CompiledDocument(uri, 1, '\nimport java.util.List\nclass C {}\n', positioned, null, null)
        def insert = ImportSupport.insertPosition(withPackage, PositionEncoding.UTF16)
        assert insert.line == 1
        assert insert.character == 0

        def star = new ImportNode('java.util.')
        def field = ImportNode.getDeclaredField('packageName')
        field.accessible = true
        field.set(star, null)
        assert star.packageName == null
        def starred = new ModuleNode((SourceUnit) null)
        starred.starImports.add(star)
        assert ImportSupport.needsImport(starred, 'com.example.Foo')
    }

    private CompiledDocument compile(String text) {
        def compiler = new GroovyCompiler()
        try {
            def uri = folder.resolve("Cov${System.nanoTime()}.groovy").toUri()
            return compiler.compile([new TextDocument(uri, 'groovy', 1, text)], [],
                    CompilerSettings.defaults(), ImportSupportTest.classLoader).get(uri)
        } finally {
            compiler.close()
        }
    }
}
