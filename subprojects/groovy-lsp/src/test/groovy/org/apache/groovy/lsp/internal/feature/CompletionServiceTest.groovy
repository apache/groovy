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

import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.util.Uris
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.m12n.ExtensionModule
import org.codehaus.groovy.runtime.metaclass.ReflectionMetaMethod
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.Position
import org.junit.jupiter.api.Test

import groovy.lang.MetaMethod

import java.lang.reflect.Method
import java.lang.reflect.Modifier

final class CompletionServiceTest {

    private static final PositionEncoding UTF16 = PositionEncoding.UTF16

    @Test
    void descriptorTypeNamesCoverJvmForms() {
        assert CompletionService.typeName(named('[B')) == 'byte[]'
        assert CompletionService.typeName(named('[C')) == 'char[]'
        assert CompletionService.typeName(named('[D')) == 'double[]'
        assert CompletionService.typeName(named('[F')) == 'float[]'
        assert CompletionService.typeName(named('[I')) == 'int[]'
        assert CompletionService.typeName(named('[J')) == 'long[]'
        assert CompletionService.typeName(named('[S')) == 'short[]'
        assert CompletionService.typeName(named('[Z')) == 'boolean[]'
        assert CompletionService.typeName(named('[[I')) == 'int[][]'
        assert CompletionService.typeName(named('[Ljava.lang.String;')) == 'String[]'
        assert CompletionService.typeName(named('[Ljava.util.Map$Entry;')) == 'Map.Entry[]'
        assert CompletionService.typeName(named('[Ljava/util/List;')) == 'List[]'
        assert CompletionService.typeName(named('[Q')).contains('[]')
        assert CompletionService.typeName(named('[')) == '['
        assert CompletionService.typeName(named('')) == 'Object'
        assert CompletionService.typeName(named('java.lang.String')) == 'String'
    }

    @Test
    void memberCompletionReachesReceiversArraysAndRepairs() {
        def service = new CompletionService()
        labels(service, '''\
            int[] items = new int[1]
            items.
            '''.stripIndent(), 'items.')
        labels(service, '''\
            int n = 1
            n.
            '''.stripIndent(), 'n.')
        labels(service, '''\
            def n = 1.0
            n.
            '''.stripIndent(), 'n.')
        labels(service, 'BigDecimal.\n', 'BigDecimal.')
        labels(service, 'BigInteger.\n', 'BigInteger.')
        labels(service, 'String.\n', 'String.')
        labels(service, '''\
            def s = new String('hi')
            s.
            '''.stripIndent(), 's.')
        labels(service, '''\
            def s = 'hi'.toUpperCase()
            s.
            '''.stripIndent(), 's.')
        def chained = labels(service, '''\
            class Box { String name }
            def b = new Box()
            b.name.
            '''.stripIndent(), 'b.name.')
        assert 'substring' in chained || 'length' in chained
        labels(service, 'null.\n', 'null.')
        labels(service, '.\n', '.')
        labels(service, '''\
            import java.util.*
            Collections.
            '''.stripIndent(), 'Collections.')
        labels(service, 'NoSuchType.\n', 'NoSuchType.')
        labels(service, "def a = ''\r\na.\n", 'a.')
        def wide = new TextDocument(fileUri('file:///tmp/cov-wide.groovy'), 'groovy', 1, 'a.\n')
        service.complete(wide, compileOf(wide), new Position(0, 80), UTF16)

        def config = new CompilerConfiguration()
        config.scriptBaseClass = 'groovy.lang.Script'
        config.addCompilationCustomizers(new ImportCustomizer())
        def broken = new TextDocument(fileUri('file:///tmp/cov-repair.groovy'), 'groovy', 1, 'def a.\n')
        service.complete(broken, null, new Position(0, 6), UTF16, CompletionServiceTest.class.classLoader, config)

        labels(service, "def a = ''\na?.\n", 'a?.')
        labels(service, "def a = []\na*.\n", 'a*.')
        labels(service, '''\
            def a = ''
            a = a
            a.
            '''.stripIndent(), 'a.')
        labels(service, '''\
            class Box {
                public String name
                String title
                String getCity() { title }
            }
            def b = new Box()
            b.name.
            '''.stripIndent(), 'b.name.')

        def syntax = SyntaxView.tokens('def x = 1 /*c*/')
        assert syntax.any { it.name }
        def operators = SyntaxView.tokens('a + (b)')
        assert operators.any { it.text == '+' || it.text == '(' }
        assert SyntaxView.tokens('').empty
        assert SyntaxView.tokens(null).empty
        def tree = SyntaxView.cst('class C { }')
        assert tree.name
        def operatorTree = SyntaxView.cst('a + (b)')
        assert operatorTree.name
        assert SyntaxView.cst(null).name
        assert SyntaxView.cst('\u0000').name
    }

    @Test
    void privateCompletionHelpersCoverTheRemainingBranches() {
        def boom = new ClassNode(String) {
            @Override
            boolean isResolved() { true }

            @Override
            Class getTypeClass() { throw new GroovyBugError('unloaded') }
        }
        assert call('typeClass', [ClassNode] as Class[], [boom] as Object[]) == null
        def parts = new CompletionItem('m')
        call('attachParts', [CompletionItem, String, String] as Class[], [parts, null, null] as Object[])
        assert parts.data['r'] == ''
        assert parts.data['p'] == ''
        call('attachParts', [CompletionItem, String, String] as Class[], [parts, 'int', 'String s'] as Object[])
        assert parts.data['r'] == 'int'
        assert parts.data['p'] == 'String s'

        def into = new LinkedHashSet()
        call('addExtensions', [Set, Class, boolean] as Class[], [into, null, true] as Object[])
        call('addExtensions', [Set, Class, boolean] as Class[], [into, String, false] as Object[])
        call('addExtensions', [Set, Class, boolean] as Class[], [into, Object, true] as Object[])
        assert !into.empty

        assert call('arityOf', [String] as Class[], [null] as Object[]) == -1
        assert call('arityOf', [String] as Class[], ['no-parens'] as Object[]) == -1
        assert call('arityOf', [String] as Class[], ['m(Map<String, List<Object>> a, int b)'] as Object[]) == 2

        def proxy = InvokerHelper.getMetaClass(String).metaMethods.find { !(it instanceof ReflectionMetaMethod) }
        if (proxy != null) {
            def names = call('parameterNames', [MetaMethod, int] as Class[], [proxy, 1] as Object[])
            assert names != null
        }
        def reflected = InvokerHelper.getMetaClass(String).metaMethods.find {
            it instanceof ReflectionMetaMethod && it.parameterTypes
        }
        if (reflected != null) {
            def wide = call('reflectionNames', [ReflectionMetaMethod, int] as Class[], [reflected, 40] as Object[])
            assert wide != null
        }

        assert call('simpleName', [Class] as Class[], [null] as Object[]) == 'Object'
        assert call('receiverParts', [String, int] as Class[], ['a?.b.', 5] as Object[]) == ['a', 'b']
        assert call('receiverEndOffset', [String, int] as Class[], ['name', 4] as Object[]) == 0
        assert call('expressionType', [ASTNode, ModuleNode] as Class[],
                [new ConstructorCallExpression(ClassHelper.STRING_TYPE, ArgumentListExpression.EMPTY_ARGUMENTS), null] as Object[]) != null
        assert call('expressionType', [ASTNode, ModuleNode] as Class[],
                [new MethodCallExpression(new VariableExpression('a'), 'size', ArgumentListExpression.EMPTY_ARGUMENTS), null] as Object[]) != null
        assert call('expressionType', [ASTNode, ModuleNode] as Class[],
                [new ClassExpression(ClassHelper.STRING_TYPE), null] as Object[]) == ClassHelper.STRING_TYPE
        assert call('expressionType', [ASTNode, ModuleNode] as Class[],
                [new EmptyExpression(), null] as Object[]) == null
        assert call('expressionType', [ASTNode, ModuleNode] as Class[],
                [new ConstantExpression('hi'), null] as Object[]) != null

        def owner = new ClassNode('Box', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        owner.addProperty('title', Modifier.PUBLIC, ClassHelper.STRING_TYPE, null, null, null)
        owner.addField('hidden', Modifier.PRIVATE, ClassHelper.STRING_TYPE, null)
        assert call('fieldOrPropertyType', [ClassNode, String] as Class[], [owner, 'missing'] as Object[]) == null
        assert call('zeroArgReturn', [ClassNode, String] as Class[], [ClassHelper.STRING_TYPE, 'charAt'] as Object[]) == null
        def items = new ArrayList<CompletionItem>()
        call('addVisibleMembers', [List, ClassNode, String, boolean] as Class[],
                [items, ClassHelper.int_TYPE, '', false] as Object[])
        call('addFieldsAndProperties', [List, Set, ClassNode, String] as Class[],
                [items, new LinkedHashSet(), owner, 'title'] as Object[])

        assert call('repairMemberAccess', [String, int, int] as Class[], [null, 0, 0] as Object[]) == null
        assert call('isIdentifier', [String] as Class[], ['1bad'] as Object[]) == false
        assert call('isIdentifier', [String] as Class[], [null] as Object[]) == false
        def range = call('lineRange', [String, int] as Class[], ['a\r\nb', 1] as Object[])
        assert range[0] == 3
        assert call('lineRange', [String, int] as Class[], ['a', 4] as Object[]) == [1, 1]
        assert call('qualify', [String, String] as Class[], [null, 'List'] as Object[]) == null
        assert call('qualify', [String, String] as Class[], ['java.util', 'List'] as Object[]) == 'java.util.List'
        assert call('loadClass', [String, SourceUnit] as Class[], [null, null] as Object[]) == null
        assert call('loadClass', [String, SourceUnit] as Class[], ['no.such.Type', null] as Object[]) == null
        assert call('sourceClass', [ModuleNode, String] as Class[], [null, 'Box'] as Object[]) == null
        assert call('resolveSimpleName', [String, ModuleNode, SourceUnit] as Class[],
                [null, null, null] as Object[]) == null
        assert call('concretize', [ClassNode, ModuleNode, SourceUnit] as Class[],
                [null, null, null] as Object[]) == null
        assert call('repairedModule', [TextDocument, Position, ClassLoader, CompilerConfiguration, CompilerSettings] as Class[],
                [null, null, null, null, null] as Object[]) == null

        System.clearProperty('probe.ran')
        def astText = '''\
import groovy.transform.ASTTest
@ASTTest(value={ System.setProperty('probe.ran','yes') })
def x = 'abc'
x.
'''
        def astDoc = new TextDocument(URI.create('groovy-buffer:repair-ast'), 'groovy', 1, astText)
        call('repairedModule', [TextDocument, Position, ClassLoader, CompilerConfiguration, CompilerSettings] as Class[],
                [astDoc, new Position(3, 2), CompletionServiceTest.classLoader, null, CompilerSettings.defaults()] as Object[])
        assert System.getProperty('probe.ran') == null
        def astOn = new CompilerSettings([], [], CompilerSettings.defaults().throughPhase, false, true)
        call('repairedModule', [TextDocument, Position, ClassLoader, CompilerConfiguration, CompilerSettings] as Class[],
                [astDoc, new Position(3, 2), CompletionServiceTest.classLoader, null, astOn] as Object[])
        assert System.getProperty('probe.ran') == 'yes'
        System.clearProperty('probe.ran')

        def nodes = [new ClassNode[1], new int[1]]
        call('offer', [int, int, ClassNode, int, ClassNode[], int[]] as Class[],
                [0, 0, ClassHelper.STRING_TYPE, 10, nodes[0], nodes[1]] as Object[])

        assert SyntaxView.tokens('a + (b)').any { it.text == '+' || it.text == '(' }
        assert SyntaxView.cst('a + (b)').name
        assert SyntaxView.cst('\uFFFF').name

        def text = '''\
            class Box {
                public String name
                String ping(String name) { return name }
            }
            '''.stripIndent()
        def document = new TextDocument(fileUri('file:///tmp/cov-decl.groovy'), 'groovy', 1, text)
        def module = compileOf(document).get(document).module
        assert call('declaredType', [ModuleNode, String, int, int] as Class[], [null, null, 0, 0] as Object[]) == null
        assert call('declaredType', [ModuleNode, String, int, int] as Class[], [module, 'name', 80, 0] as Object[]) != null
        assert call('memberType', [ClassNode, String] as Class[], [null, 'name'] as Object[]) == null
        assert call('memberType', [ClassNode, String] as Class[], [owner, 'missing'] as Object[]) == null
        assert call('zeroArgReturn', [ClassNode, String] as Class[], [ClassHelper.make(String), 'missing'] as Object[]) == null
        owner.addMethod('init', Modifier.PUBLIC, ClassHelper.VOID_TYPE, [] as Parameter[], [] as ClassNode[], null)
        def weird = new MethodNode('<init>', Modifier.PUBLIC, ClassHelper.VOID_TYPE,
                [] as Parameter[], [] as ClassNode[], null)
        owner.addMethod(weird)
        owner.addField('secret', Modifier.PRIVATE, ClassHelper.STRING_TYPE, null)
        owner.addProperty('hidden', Modifier.PRIVATE, ClassHelper.int_TYPE, null, null, null)
        call('addVisibleMembers', [List, ClassNode, String, boolean] as Class[],
                [new ArrayList<CompletionItem>(), owner, '', false] as Object[])
        def unresolved = new ClassNode('java.util.ArrayList', 0, ClassHelper.OBJECT_TYPE)
        assert call('concretize', [ClassNode, ModuleNode, SourceUnit] as Class[], [unresolved, module, null] as Object[]) != null
        def missing = new ClassNode('com.example.Missing', 0, ClassHelper.OBJECT_TYPE)
        assert call('concretize', [ClassNode, ModuleNode, SourceUnit] as Class[], [missing, module, null] as Object[]) != null
        def blank = new ClassNode('', 0, ClassHelper.OBJECT_TYPE)
        assert call('concretize', [ClassNode, ModuleNode, SourceUnit] as Class[], [blank, null, null] as Object[]) != null
        module.addImport('Ghost', ClassHelper.make('com.example.Ghost'))
        module.addStarImport('com.example.missing.')
        assert call('resolveSimpleName', [String, ModuleNode, SourceUnit] as Class[], ['Ghost', module, null] as Object[]) != null
        assert call('resolveSimpleName', [String, ModuleNode, SourceUnit] as Class[], ['Thing', module, null] as Object[]) == null
    }

    @Test
    void memberCompletionWalksParametersFieldsAndRepairs() {
        def service = new CompletionService()
        labels(service, '''\
            class Box {
                public String name
                String ping(String name) { name. }
            }
            '''.stripIndent(), 'name.')
        labels(service, '''\
            def (a, b) = [1, 2]
            a.
            '''.stripIndent(), 'a.')
        labels(service, '''\
            import java.util.List
            List.
            '''.stripIndent(), 'List.')
        def broken = new TextDocument(fileUri('file:///tmp/cov-repair.groovy'), 'groovy', 1, 'def a.\n')
        def host = new CompilerConfiguration()
        host.scriptBaseClass = 'groovy.lang.Script'
        host.addCompilationCustomizers(new ImportCustomizer())
        service.complete(broken, null, new Position(0, 6), UTF16, CompletionServiceTest.class.classLoader, host)
    }

    @Test
    void extractedCatalogAndReceiverLinesRun() {
        assert new ExtensionCatalog() != null
        assert new ReceiverInference() != null
        assert new MemberRepair() != null
        assert new ExtensionCatalog.ExtensionParameterNameCache() != null
        def receiver = new ReceiverInference.Receiver(null, true)
        assert receiver.type.name == 'java.lang.Object'
        assert receiver.staticAccess

        def plugins = new LinkedHashSet()
        call('addStaticPlugins', [Set, Class[]] as Class[], [plugins, null] as Object[])
        call('addStaticPlugins', [Set, Class[]] as Class[], [plugins, [String] as Class[]] as Object[])
        def modules = [
                null,
                new ExtensionModule('empty', '1') {
                    @Override
                    List<MetaMethod> getMetaMethods() { [] }
                },
                new ExtensionModule('boom', '1') {
                    @Override
                    List<MetaMethod> getMetaMethods() { throw new RuntimeException('boom') }
                }
        ]
        call('addModuleMethods', [Set, Iterable] as Class[], [plugins, null] as Object[])
        call('addModuleMethods', [Set, Iterable] as Class[], [plugins, modules] as Object[])

        def fresh = new LinkedHashSet()
        call('addExtensions', [Set, Class, boolean] as Class[], [fresh, FreshMembers, true] as Object[])
        def visible = String.getMethod('valueOf', Object)
        assert call('cachedOrNew', [Method, CachedMethod] as Class[], [visible, null] as Object[]) != null
        assert call('cachedOrNew', [Method, CachedMethod] as Class[], [visible, CachedMethod.find(visible)] as Object[]) != null
        def cached = CachedMethod.find(String.getMethod('valueOf', Object))
        def odd = new ReflectionMetaMethod(cached) {
            @Override
            MetaMethod getCachedMethod() { null }
        }
        assert call('reflectionNames', [ReflectionMetaMethod, int] as Class[], [odd, 1] as Object[]) != null

        assert call('receiverParts', [String, int] as Class[], ['name', 4] as Object[]) == []
        def box = new ClassNode('Box', 0, ClassHelper.OBJECT_TYPE)
        box.addMethod('go', Modifier.PUBLIC, ClassHelper.VOID_TYPE,
                [new Parameter(ClassHelper.OBJECT_TYPE, 'a')] as Parameter[], ClassNode.EMPTY_ARRAY, null)
        box.addMethod('hiddenGo', Modifier.PRIVATE, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        box.addMethod('a$b', Modifier.PUBLIC, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        box.addField('hidden', Modifier.PRIVATE, ClassHelper.OBJECT_TYPE, null)
        box.addField('shown', Modifier.PUBLIC, ClassHelper.int_TYPE, null)
        box.addProperty('secret', Modifier.PRIVATE, ClassHelper.OBJECT_TYPE, null, null, null)
        box.addProperty('shownProp', Modifier.PUBLIC, ClassHelper.int_TYPE, null, null, null)
        def nameless = new MethodNode('temp', Modifier.PUBLIC, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null) {
            @Override
            String getName() { null }
        }
        box.addMethod(nameless)
        assert call('zeroArgReturn', [ClassNode, String] as Class[], [box, 'go'] as Object[]) == null
        def items = []
        def seen = new LinkedHashSet(['shown', 'shownProp'])
        call('addFieldsAndProperties', [List, Set, ClassNode, String] as Class[],
                [items, seen, box, ''] as Object[])
        call('addVisibleMembers', [List, ClassNode, String, boolean] as Class[],
                [items, box, '', false] as Object[])
        assert call('importedSimpleName', [String, ModuleNode, SourceUnit] as Class[],
                ['List', null, null] as Object[]) == null
    }

    /** A class the extension cache has not seen, so both skips and a missing cached method occur. */
    static class FreshMembers {
        static int zoom(String value) { value.length() }

        void instanceOnly(String value) { }

        static void noArgs() { }
    }

    private static Object call(String name, Class[] types, Object[] args) {
        for (Class<?> type : [CompletionService, ExtensionCatalog, ReceiverInference, MemberRepair]) {
            try {
                def method = type.getDeclaredMethod(name, types)
                method.accessible = true
                return method.invoke(null, args)
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException(name)
    }

    private static ClassNode named(String name) {
        new ClassNode(name, 0, ClassHelper.OBJECT_TYPE)
    }

    private static List<String> labels(CompletionService service, String text, String token) {
        def document = new TextDocument(fileUri('file:///tmp/cov-' + token.hashCode() + '.groovy'), 'groovy', 1, text)
        def items = service.complete(document, compileOf(document), positionOf(text, token), UTF16).items
        items*.label
    }

    private static Position positionOf(String text, String token) {
        def lines = text.readLines()
        int line = 0
        int column = token.length()
        for (int i = 0; i < lines.size(); i++) {
            int at = lines[i].indexOf(token)
            if (at >= 0) {
                line = i
                column = at + token.length()
            }
        }
        new Position(line, column)
    }

    private static Object compileOf(TextDocument document) {
        def compiler = new GroovyCompiler()
        try {
            return compiler.compile([document], [], CompilerSettings.defaults(), CompletionServiceTest.class.classLoader)
        } finally {
            compiler.close()
        }
    }

    private static URI fileUri(String path) {
        Uris.normalize(URI.create(path))
    }
}

