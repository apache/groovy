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
package org.apache.groovy.lsp.internal.feature

import java.lang.reflect.Modifier
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.position.Positions
import org.apache.groovy.lsp.internal.util.GroovydocMarkdown
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.transform.stc.StaticTypesMarker
import org.objectweb.asm.Opcodes

import org.apache.groovy.lsp.internal.LspFixture
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CodeLensParams
import org.eclipse.lsp4j.FileRename
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.RenameFilesParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class JdtInspiredTest {
    private static final PositionEncoding UTF16 = PositionEncoding.UTF16

    private static final PositionEncoding ENC = PositionEncoding.UTF16


    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void generateAccessorsForPrivateField() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                private String name
            }
            '''.stripIndent())
        def actions = titles(uri, 1, 20)
        assert 'Generate getters and setters' in actions
        def edit = editFor(uri, 1, 20, 'Generate getters and setters')
        assert edit.contains('getName')
        assert edit.contains('setName')
    }

    @Test
    void generateToStringAndConstructor() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                String name
            }
            '''.stripIndent())
        def actions = titles(uri, 0, 6)
        assert 'Generate toString()' in actions
        assert 'Generate constructor' in actions
        assert 'Generate equals() and hashCode()' in actions
        def toString = editFor(uri, 0, 6, 'Generate toString()')
        assert toString.contains('toString')
        assert toString.contains('name:')
        def ctor = editFor(uri, 0, 6, 'Generate constructor')
        assert ctor.contains('Hello(String name)')
    }

    @Test
    void skipToStringWhenCanonicalPresent() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            @groovy.transform.Canonical
            class Hello {
                String name
            }
            '''.stripIndent())
        def actions = titles(uri, 1, 6)
        assert !('Generate toString()' in actions)
        assert !('Generate constructor' in actions)
    }

    @Test
    void addOverrideAnnotation() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Base { String name() { 'x' } }
            class Hello extends Base {
                String name() { 'y' }
            }
            '''.stripIndent())
        def edit = editFor(uri, 2, 11, 'Add @Override annotations')
        assert edit.contains('@Override')
    }

    @Test
    void codeLensShowsReferenceCount() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo() { foo() }
            }
            '''.stripIndent())
        def lenses = fixture.server.textDocumentService.codeLens(
                new CodeLensParams(new TextDocumentIdentifier(uri))).get()
        assert lenses.any { it.command?.title ==~ /\d+ references?/ }
    }

    @Test
    void renameClassRenamesFileWhenNamesMatch() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {}\n')
        def edit = fixture.server.textDocumentService.rename(
                new RenameParams(new TextDocumentIdentifier(uri), new Position(0, 6), 'World')).get()
        assert edit.documentChanges
        assert edit.documentChanges.any { it.isRight() && it.getRight().newUri.contains('World.groovy') }
    }

    @Test
    void willRenameFilesRewritesMatchingClassIdentity() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {}\n')
        def newUri = uri.replace('Hello.groovy', 'World.groovy')
        def edit = fixture.server.workspaceService.willRenameFiles(
                new RenameFilesParams([new FileRename(uri, newUri)])).get()
        assert edit.changes
        assert edit.changes.values().flatten().any { it.newText == 'World' }
        def script = fixture.open('script.groovy', 'println 1\n')
        def refused = fixture.server.workspaceService.willRenameFiles(
                new RenameFilesParams(
                        [new FileRename(script, script.replace('script.groovy', 'other.groovy'))])).get()
        assert !refused.changes || refused.changes.values().every { it.isEmpty() }
    }

    private List<String> titles(String uri, int line, int character) {
        fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(line, character), new Position(line, character + 1)),
                        new CodeActionContext([]))).get()
                .findAll { it.isRight() }
                .collect { it.getRight().title }
    }

    private String editFor(String uri, int line, int character, String title) {
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(line, character), new Position(line, character + 1)),
                        new CodeActionContext([]))).get()
        def action = actions.find { it.isRight() && it.getRight().title == title }
        assert action != null
        action.getRight().edit.changes.values().flatten()[0].newText
    }
    @Test
    void sourceGenerationFiltersCapitalizeAndUnpositionedInsert() {
        assert SourceGeneration.instanceFields(null).isEmpty()
        assert SourceGeneration.capitalize(null) == ''
        assert SourceGeneration.capitalize('') == ''
        assert SourceGeneration.capitalize('widget') == 'Widget'

        def owner = new ClassNode('demo.Filtered', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        def kept = field(owner, 'kept', Modifier.PRIVATE, ClassHelper.STRING_TYPE, 2)
        owner.addField(kept)
        def stat = field(owner, 'STAT', Modifier.STATIC, ClassHelper.int_TYPE, 3)
        owner.addField(stat)
        def hidden = field(owner, 'hidden', Modifier.PRIVATE, ClassHelper.OBJECT_TYPE, 4)
        hidden.synthetic = true
        owner.addField(hidden)
        def meta = field(owner, 'metaClass', Modifier.PRIVATE, ClassHelper.OBJECT_TYPE, 5)
        owner.addField(meta)
        owner.addField(field(owner, 'nolines', Modifier.PRIVATE, ClassHelper.STRING_TYPE, -1))

        def ghost = new PropertyNode('ghost', Modifier.PUBLIC, ClassHelper.STRING_TYPE, owner, null, null, null)
        ghost.field = null
        owner.getProperties().add(ghost)
        def staticProp = new PropertyNode('COUNT', Modifier.STATIC | Modifier.PUBLIC, ClassHelper.int_TYPE, owner, null, null, null)
        owner.getProperties().add(staticProp)
        def syntheticProp = new PropertyNode('syn', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, owner, null, null, null)
        syntheticProp.synthetic = true
        owner.getProperties().add(syntheticProp)
        def metaProp = new PropertyNode('metaClass', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, owner, null, null, null)
        owner.getProperties().add(metaProp)
        assert SourceGeneration.instanceFields(owner)*.name == ['kept']

        def bare = new ClassNode('Bare', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        bare.addField(field(bare, 'name', Modifier.PRIVATE, ClassHelper.STRING_TYPE, 1))
        def bareDoc = new CompiledDocument(URI.create('file:///Bare.groovy'), 1, 'class Bare {\n}\n', null, null, null)
        assert SourceGeneration.beforeClose(bare, bareDoc, ENC) == null
        assert SourceGeneration.toStringMethod(bare, bareDoc, ENC).isEmpty()
        assert SourceGeneration.overrideAnnotations(null, bareDoc, ENC).isEmpty()

        def text = 'class Bare {\n    int a\n\n'
        def open = new ClassNode('Bare', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        open.lineNumber = 1
        open.columnNumber = 1
        open.lastLineNumber = 3
        open.lastColumnNumber = 1
        def openDoc = new CompiledDocument(URI.create('file:///Bare.groovy'), 1, text, null, null, null)
        def end = Positions.toRange(open, text, ENC).end
        def insert = SourceGeneration.beforeClose(open, openDoc, ENC)
        assert insert.line == end.line
        assert insert.character == end.character

        def noInterfaces = new ClassNode('Lonely', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        noInterfaces.setInterfaces(null)
        def run = new MethodNode('run', Modifier.PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, new EmptyStatement())
        run.lineNumber = 1
        run.columnNumber = 1
        run.lastLineNumber = 1
        run.lastColumnNumber = 4
        noInterfaces.addMethod(run)
        assert SourceGeneration.overrideAnnotations(noInterfaces, bareDoc, ENC).isEmpty()
    }

    @Test
    void sourceGenerationEditsAndOverrideAnnotations() {
        def src = '''\
            class Pair {
                String left
                String right
            }
            class HasString {
                String name
                String toString() { name }
            }
            class HasEquals {
                String name
                boolean equals(Object o) { false }
            }
            class HasCtor {
                String name
                HasCtor(String name) { this.name = name }
            }
            class Parent {
                void run() {}
                void ping() {}
            }
            class Child extends Parent {
                @Deprecated
                void run() {}
                @Override
                void ping() {}
            }
            interface Face {
                void run()
            }
            class Impl implements Face {
                void run() {}
            }
            '''.stripIndent()
        def uri = open('Gen.groovy', src)
        def doc = compiled(uri)
        def pair = typeNamed(doc, 'Pair')
        def toString = SourceGeneration.toStringMethod(pair, doc, ENC)
        assert toString.size() == 1
        assert toString[0].newText.contains('left: ${left}, right: ${right}')
        def equals = SourceGeneration.equalsAndHashCode(pair, doc, ENC)
        assert equals.size() == 1
        assert equals[0].newText.contains('left == other.left && right == other.right')
        assert equals[0].newText.contains('Objects.hash(left, right)')
        assert SourceGeneration.toStringMethod(typeNamed(doc, 'HasString'), doc, ENC).isEmpty()
        assert SourceGeneration.equalsAndHashCode(typeNamed(doc, 'HasEquals'), doc, ENC).isEmpty()
        assert SourceGeneration.constructor(typeNamed(doc, 'HasCtor'), doc, ENC).isEmpty()

        def generatedCtor = SourceGeneration.constructor(pair, doc, ENC)
        if (generatedCtor.isEmpty()) {
            pair.declaredConstructors.clear()
            def synthetic = new ConstructorNode(Modifier.PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new EmptyStatement())
            synthetic.synthetic = true
            pair.addConstructor(synthetic)
            generatedCtor = SourceGeneration.constructor(pair, doc, ENC)
        }
        assert generatedCtor.size() == 1
        assert generatedCtor[0].newText.contains('String left, String right')
        assert generatedCtor[0].newText.contains('this.left = left')

        def lines = doc.text.readLines()
        def childEdits = SourceGeneration.overrideAnnotations(typeNamed(doc, 'Child'), doc, ENC)
        assert childEdits
        assert childEdits.every { it.newText.contains('@Override') }
        assert childEdits.any { lines[it.range.start.line].contains('run') }
        assert childEdits.every { !lines[it.range.start.line].contains('ping') }

        def impl = typeNamed(doc, 'Impl')
        def implEdits = SourceGeneration.overrideAnnotations(impl, doc, ENC)
        assert implEdits.any { it.newText.contains('@Override') && lines[it.range.start.line].contains('run') }
        def run = impl.methods.find { it.name == 'run' && !it.synthetic }
        run.columnNumber = 1
        run.lastColumnNumber = 2
        run.lastLineNumber = run.lineNumber
        assert SourceGeneration.overrideAnnotations(impl, doc, ENC).isEmpty()
    }

    @Test
    void codeLensSkipsFieldsAndUnlocatedNames() {
        def src = '''\
            class Hello {
                String name
                def foo() { foo() }
            }
            '''.stripIndent()
        def uri = open('Hello.groovy', src)
        def doc = compiled(uri)
        ensureField(doc, typeNamed(doc, 'Hello'), 'name')
        def service = new CodeLensService()
        def decls = AstQuery.declarations(doc.module)
        assert decls.any { it instanceof PropertyNode }
        def lenses = service.codeLenses(doc, snapshot(), ENC)
        assert lenses.size() == decls.count { it instanceof ClassNode || it instanceof MethodNode }
        assert lenses.every { it.command.title.contains('reference') }

        def hello = typeNamed(doc, 'Hello')
        hello.columnNumber = 1
        hello.lastColumnNumber = 2
        hello.lastLineNumber = hello.lineNumber
        def after = service.codeLenses(doc, snapshot(), ENC)
        assert lenses.any { it.range.start.line == 0 }
        assert after.size() == lenses.size() - 1
        assert after.every { it.range.start.line != 0 }
    }

    private String open(String path, String text) {
        if (fixture == null) {
            fixture = new LspFixture()
        }
        fixture.open(path, text)
    }

    private CompilationSnapshot snapshot() {
        fixture.server.context.snapshot
    }

    private CompiledDocument compiled(String uri) {
        def doc = snapshot().get(uri)
        assert doc?.module != null
        doc
    }

    private static ClassNode typeNamed(CompiledDocument doc, String simple) {
        def found = doc.module.classes.find { it.nameWithoutPackage == simple && !it.name.contains('$') }
        assert found != null : doc.module.classes*.name
        found
    }

    private static FieldNode field(ClassNode owner, String name, int modifiers, ClassNode type, int line) {
        def node = new FieldNode(name, modifiers, type, owner, null)
        node.lineNumber = line
        node.columnNumber = 1
        node.lastLineNumber = line
        node.lastColumnNumber = 2
        node
    }

    private static FieldNode ensureField(CompiledDocument doc, ClassNode type, String name) {
        def existing = type.fields.find { it.name == name && !it.synthetic }
        if (existing != null) {
            return existing
        }
        def lines = doc.text.readLines()
        int line = lines.findIndexOf { it.contains(name) }
        assert line >= 0 : name
        int column = lines[line].indexOf(name)
        def field = new FieldNode(name, Modifier.PRIVATE, ClassHelper.STRING_TYPE, type, null)
        place(field, line + 1, column + 1, column + 1 + name.length())
        type.addField(field)
        field
    }

    private static void place(ASTNode node, int line, int column, int lastColumn) {
        node.lineNumber = line
        node.columnNumber = column
        node.lastLineNumber = line
        node.lastColumnNumber = lastColumn
    }

    @Test
    void sourceGenerationHoverAndInference() {
        fixture = new LspFixture()
        def text = '''\
            /**
             * Documented API.
             * @param name who
             * @return a label
             */
            interface Face {
                void ping()
                void run(String name, int count)
                Object value()
            }
            class Impl implements Face {
                String left
                String right
                private String hidden
                void ping() { ping() }
                void run(String name, int count) { left }
                Object value() { 'v' }
                def local(def label) {
                    def item = 'z'
                    /*xdef*/ def other = 1
                    this.@left
                    String
                }
            }
            enum Hue { RED }
            record Rec(String name) {}
            class Plain {
                private String secret
                String a
                String b
            }
            class HasCtor {
                String a
                HasCtor(String a) { this.a = a }
                String toString() { a }
                boolean equals(Object o) { true }
            }
            '''.stripIndent()
        def uri = fixture.open('Sample.groovy', text)
        def snapshot = fixture.server.context.snapshot
        def compiled = snapshot.get(uri)
        def module = compiled.module
        def impl = module.classes.find { it.name == 'Impl' }
        def face = module.classes.find { it.name == 'Face' }
        def hue = module.classes.find { it.name == 'Hue' }
        def rec = module.classes.find { it.name == 'Rec' }
        def plain = module.classes.find { it.name == 'Plain' }
        def hasCtor = module.classes.find { it.name == 'HasCtor' }
        def run = impl.methods.find { it.name == 'run' && it.lineNumber > 0 }

        assert SourceGeneration.instanceFields(null).isEmpty()
        assert SourceGeneration.capitalize(null) == ''
        assert SourceGeneration.capitalize('') == ''
        assert SourceGeneration.capitalize('über').startsWith('Ü')
        assert SourceGeneration.toStringMethod(hasCtor, compiled, UTF16).isEmpty()
        assert SourceGeneration.equalsAndHashCode(hasCtor, compiled, UTF16).isEmpty()
        assert SourceGeneration.constructor(hasCtor, compiled, UTF16).isEmpty()
        assert SourceGeneration.overrideAnnotations(null, compiled, UTF16).isEmpty()
        def unpositioned = new ClassNode('X', 0, ClassHelper.OBJECT_TYPE)
        assert SourceGeneration.beforeClose(unpositioned, compiled, UTF16) == null

        def accessors = SourceGeneration.accessors(plain, compiled, UTF16)
        assert accessors[0].newText.contains('getSecret')
        assert accessors[0].newText.contains('setSecret')
        def generated = SourceGeneration.toStringMethod(plain, compiled, UTF16)[0].newText
        assert generated.contains('a: ${a}, b: ${b}')
        def equals = SourceGeneration.equalsAndHashCode(plain, compiled, UTF16)[0].newText
        assert equals.contains('a == other.a && b == other.b')
        assert equals.contains('Objects.hash(secret, a, b)')
        def ctor = SourceGeneration.constructor(impl, compiled, UTF16)[0].newText
        assert ctor.contains('String left, String right')
        def overrides = SourceGeneration.overrideAnnotations(impl, compiled, UTF16)
        assert overrides.any { it.newText.contains('@Override') }

        def empty = module.classes.find { it.name == 'Hue' }
        assert SourceGeneration.equalsAndHashCode(new ClassNode('pkg.Empty', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE), compiled, UTF16).isEmpty() || empty != null

        def hover = new HoverService()
        assert hover.markdown(null, snapshot, new Position(0, 0), UTF16) == ''
        assert hover.hover(null, snapshot, new Position(0, 0), UTF16) == null
        assert HoverService.render(face).contains('interface ')
        assert HoverService.render(hue).contains('enum ')
        assert HoverService.render(rec).contains('record ')
        assert HoverService.render(impl).contains('class ')
        assert HoverService.render(run).contains(', ')
        def noOwner = new MethodNode('ping', 0, ClassHelper.VOID_TYPE,
                [new Parameter(ClassHelper.STRING_TYPE, 'a'), new Parameter(ClassHelper.int_TYPE, 'b')] as Parameter[],
                ClassNode.EMPTY_ARRAY, null)
        assert HoverService.render(noOwner).contains(', ')
        assert !HoverService.render(noOwner).contains('.')
        def field = impl.fields.find { it.name == 'left' }
        assert HoverService.render(field).contains('left')
        def property = impl.properties.find { it.name == 'left' }
        assert HoverService.render(property).contains('left')
        assert HoverService.render(run.parameters[0]).contains('name')
        def dynamicProperty = new PropertyExpression(new VariableExpression('obj'), new VariableExpression('dyn'))
        assert HoverService.render(dynamicProperty).contains('?')
        def targeted = new MethodCallExpression(new VariableExpression('this'), 'run', new TupleExpression())
        targeted.methodTarget = run
        assert HoverService.render(targeted).contains('run')
        def loose = new MethodCallExpression(new VariableExpression('this'), 'run', new TupleExpression())
        assert HoverService.render(loose).contains('run(...)')
        def imp = module.imports.find { it.className?.contains('Object') } ?: module.imports[0]
        if (imp != null) {
            assert HoverService.render(imp).contains('import')
        }
        assert HoverService.render(new ConstantExpression('z')).contains('z')
        assert HoverService.render(new ASTNode()) == null

        def document = compiled.toTextDocument()
        def faceHover = hover.hover(document, snapshot, caret(text, 'Face'), UTF16)
        assert faceHover.contents.getRight().value.contains('Documented API')
        def itemHover = hover.hover(document, snapshot, caret(text, 'item'), UTF16)
        assert itemHover.contents.getRight().value.contains('_inferred_')

        def looseDoc = new TextDocument(URI.create('file:///loose.groovy'), 'groovy', 1, 'Hello')
        def looseHover = hover.hover(looseDoc, CompilationSnapshot.EMPTY, new Position(0, 'Hello'.length()), UTF16)
        assert looseHover.contents.getRight().value.contains('Hello')
        assert looseHover.range == null

        assert TypeInference.of((FieldNode) null) == null
        assert TypeInference.of((Parameter) null) == null
        assert TypeInference.of((VariableExpression) null, null) == null
        assert !TypeInference.usedInitializer((FieldNode) null)
        assert !TypeInference.usedInitializer((VariableExpression) null, module)
        def marked = new FieldNode('name', 0, ClassHelper.DYNAMIC_TYPE, ClassHelper.OBJECT_TYPE, new ConstantExpression('hi'))
        marked.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, 'nope')
        marked.putNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE, ClassHelper.STRING_TYPE)
        assert TypeInference.of(marked).name == 'java.lang.String'
        def fromInit = new FieldNode('n', 0, ClassHelper.DYNAMIC_TYPE, ClassHelper.OBJECT_TYPE, new ConstantExpression('hi'))
        assert TypeInference.of(fromInit) != null
        def typed = new FieldNode('s', 0, ClassHelper.STRING_TYPE, ClassHelper.OBJECT_TYPE, null)
        assert !TypeInference.usedInitializer(typed)
        def parameter = new Parameter(ClassHelper.OBJECT_TYPE, 'p')
        parameter.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.Integer_TYPE)
        assert TypeInference.of(parameter).name == 'java.lang.Integer'
        def viaField = new VariableExpression('v', ClassHelper.DYNAMIC_TYPE)
        viaField.accessedVariable = fromInit
        assert TypeInference.of(viaField, null) != null

        def markdown = GroovydocMarkdown.format('''\
            /**
             * See {@link java.lang.String#length() size} and {@link #member(int a)}.
             * {@link pkg.Type} {@link Type}
             * {@link java.lang.String#length()
             * <b>bold</b><br><dangling
             * @param name the name
             * @return the value
             * @throws IllegalStateException when broken
             * @exception IOException also
             * @see java.lang.String#length()
             * @see java.util.List
             */
            '''.stripIndent())
        assert markdown.contains('**@param** `name`')
        assert markdown.contains('**@throws** `IllegalStateException`')
        assert markdown.contains('**@exception** `IOException`')
        assert markdown.contains('**@see**')
        assert markdown.contains('String.length')
        assert markdown.contains('**bold**')
        assert GroovydocMarkdown.format('@param only') == '**@param** `only`'
    }

    private static Position caret(String text, String token) {
        int idx = text.indexOf(token)
        assert idx >= 0 : token
        int line = 0
        int character = 0
        for (int i = 0; i < idx; i++) {
            if (text.charAt(i) == '\n') {
                line++
                character = 0
            } else {
                character++
            }
        }
        new Position(line, character)
    }

}
