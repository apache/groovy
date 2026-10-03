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

import org.codehaus.groovy.ast.ModuleNode
import java.nio.file.Path
import org.codehaus.groovy.ast.expr.ClassExpression
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.codehaus.groovy.ast.expr.MapExpression
import org.apache.groovy.lsp.internal.LspFixture
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.codehaus.groovy.ast.Parameter
import org.objectweb.asm.Opcodes
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.junit.jupiter.api.AfterEach
import org.codehaus.groovy.ast.expr.TupleExpression
import static org.objectweb.asm.Opcodes.ACC_STATIC
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.ast.expr.PropertyExpression
import static org.objectweb.asm.Opcodes.ACC_INTERFACE
import org.junit.jupiter.api.Test
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.apache.groovy.lsp.internal.util.Uris

final class MethodBindingTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }


    @Test
    void methodBindingResolvesTargetsPropertiesAndArity() {
        def target = new MethodNode('foo', ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        def call = new MethodCallExpression(new VariableExpression('this'), 'foo', new ArgumentListExpression())
        call.methodTarget = target
        assert MethodBinding.resolveMethodCandidates(call, null) == [target]
        assert MethodBinding.methodAt(call, null).is(target)

        def owner = new ClassNode('Hello', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def go = owner.addMethod('go', ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        def stat = new StaticMethodCallExpression(owner, 'go', new ArgumentListExpression())
        assert MethodBinding.methodAt(stat, null).is(go)
        assert MethodBinding.methodAt(new VariableExpression('x'), null) == null

        def field = owner.addField('name', ACC_PUBLIC, ClassHelper.STRING_TYPE, null)
        def property = new PropertyExpression(new ClassExpression(owner), 'name')
        assert MethodBinding.resolveProperty(property).is(field)
        assert MethodBinding.resolveProperty(new PropertyExpression(new ClassExpression(owner), 'absent')) == null

        assert MethodBinding.resolveConstructorCandidates(null).isEmpty()
        def anon = new ConstructorCallExpression(owner, ArgumentListExpression.EMPTY_ARGUMENTS)
        anon.usingAnonymousInnerClass = true
        assert MethodBinding.resolveConstructorCandidates(anon).isEmpty()
        assert MethodBinding.resolveConstructorCandidates(
                new ConstructorCallExpression(ClassHelper.OBJECT_TYPE, ArgumentListExpression.EMPTY_ARGUMENTS)).isEmpty()

        def box = new ClassNode('Box', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def synthetic = box.addConstructor(ACC_PUBLIC | ACC_SYNTHETIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        synthetic.synthetic = true
        def real = box.addConstructor(ACC_PUBLIC, [new Parameter(ClassHelper.STRING_TYPE, 's')] as Parameter[],
                ClassNode.EMPTY_ARRAY, null)
        def ctor = new ConstructorCallExpression(box, new ArgumentListExpression(new ConstantExpression('a')))
        assert MethodBinding.resolveConstructor(ctor).is(real)
    }

    @Test
    void methodBindingWalksHierarchyLookupAndEnclosingClass() {
        assert MethodBinding.superOf(null) == null
        def orphan = new MethodNode('x', ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        assert orphan.declaringClass == null
        assert MethodBinding.superOf(orphan) == null

        def base = new ClassNode('Base', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def child = new ClassNode('Child', ACC_PUBLIC, base)
        def only = child.addMethod('only', ACC_PUBLIC, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new EmptyStatement())
        assert MethodBinding.superOf(only) == null

        def face = new ClassNode('Face', ACC_INTERFACE | ACC_ABSTRACT, ClassHelper.OBJECT_TYPE)
        def ping = face.addMethod('ping', ACC_PUBLIC | ACC_ABSTRACT, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        def direct = new ClassNode('Direct', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        direct.addInterface(face)
        def directPing = direct.addMethod('ping', ACC_PUBLIC, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new EmptyStatement())
        assert MethodBinding.superOf(directPing).is(ping)

        def top = new ClassNode('Top', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        top.addInterface(face)
        def mid = new ClassNode('Mid', ACC_PUBLIC, top)
        def grand = new ClassNode('Grand', ACC_PUBLIC, mid)
        def grandPing = grand.addMethod('ping', ACC_PUBLIC, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new EmptyStatement())
        assert MethodBinding.superOf(grandPing).is(ping)

        def parent = new ClassNode('Parent', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def derived = new ClassNode('Derived', ACC_PUBLIC, parent)
        def further = new ClassNode('Further', ACC_PUBLIC, derived)
        assert MethodBinding.sameOrDerived(derived, parent)
        assert MethodBinding.sameOrDerived(further, ClassHelper.OBJECT_TYPE)
        assert !MethodBinding.sameOrDerived(null, parent)
        assert MethodBinding.overloads((ClassNode) null, 'foo').isEmpty()
        assert MethodBinding.overloads(ClassHelper.STRING_TYPE, (String) null).isEmpty()

        face.lineNumber = 1
        def impl = new ClassNode('Impl', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        impl.addInterface(face)
        impl.lineNumber = 2
        def ghost = new ClassNode('Ghost', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def module = new ModuleNode((SourceUnit) null)
        module.addClass(face)
        module.addClass(impl)
        module.addClass(ghost)
        def implUri = fileUri('file:///tmp/cov-impl.groovy')
        def emptyUri = fileUri('file:///tmp/cov-emptymod.groovy')
        def snap = snapshot(
                new CompiledDocument(emptyUri, 1, '', null, null, null),
                new CompiledDocument(implUri, 1, 'class Impl implements Face {}', module, null, null))
        assert MethodBinding.implementations(face, snap).contains(impl)
        assert MethodBinding.methodImplementations(ping, snap).isEmpty()

        def holder = new ClassNode('demo.Holder', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def foo = holder.addMethod('foo', ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        foo.lineNumber = 2
        def holderModule = new ModuleNode((SourceUnit) null)
        holderModule.addClass(holder)
        def holderUri = fileUri('file:///tmp/cov-holder.groovy')
        def lookup = snapshot(
                new CompiledDocument(emptyUri, 1, '', null, null, null),
                new CompiledDocument(holderUri, 1, 'class Holder { def foo() {} }', holderModule, null, null))
        assert MethodBinding.findMethod(null, 'demo.Holder', 'foo', null) == null
        assert MethodBinding.findMethod(lookup, 'demo.Holder', null, null) == null
        assert MethodBinding.findMethod(lookup, 'demo.Missing', 'foo', null) == null
        assert MethodBinding.findMethod(lookup, 'demo.Holder', 'foo', '').is(foo)

        def loose = new MethodCallExpression(new VariableExpression('missing'), 'nope', new ArgumentListExpression())
        assert MethodBinding.resolveMethodCandidates(loose, null).isEmpty()
        assert MethodBinding.resolveMethodCandidates(loose, lookup).isEmpty()
    }

    private static CompilationSnapshot snapshot(CompiledDocument... documents) {
        def map = [:]
        documents.each { map[it.uri] = it }
        new CompilationSnapshot(map)
    }

    private static URI fileUri(String path) {
        Uris.normalize(URI.create(path))
    }

    private static URI fileUri(Path path) {
        Uris.normalize(path.toUri())
    }

    @Test
    void callSitesNamedNullAndBareArgument() {
        def named = new MapExpression()
        assert CallSites.hasNamedArgs(named)
        assert CallSites.argumentExpressions(named) == [named]

        assert CallSites.argumentExpressions(null).isEmpty()
        assert !CallSites.hasNamedArgs(null)

        def literal = new ConstantExpression(1)
        assert !CallSites.hasNamedArgs(literal)
        assert CallSites.argumentExpressions(literal) == [literal]

        def tuple = new TupleExpression(new ConstantExpression(2))
        assert !CallSites.hasNamedArgs(tuple)
        assert CallSites.argumentExpressions(tuple)*.value == [2]
    }

    @Test
    void methodBindingCoversTargetsOverridesAndCalls() {
        fixture = new LspFixture()
        fixture.open('Types.groovy', '''\
            interface I { void ping(String s) }
            class Base implements I { void ping(String s) {} }
            class Child extends Base {
                void ping(String s) { ping('x') }
                static void util(int n) { util(n) }
            }
            '''.stripIndent())
        def snapshot = fixture.server.context.snapshot
        def child = snapshot.types().byName('Child')
        assert child != null
        def module = snapshot.documents().find { it.module?.classes?.any { it.name == 'Child' } }.module
        def childNode = module.classes.find { it.name == 'Child' }
        def baseNode = module.classes.find { it.name == 'Base' }
        def ping = childNode.methods.find { it.name == 'ping' && it.lineNumber > 0 }
        assert ping != null
        def iface = baseNode.interfaces ? baseNode.interfaces[0] : baseNode
        def impls = MethodBinding.implementations(iface, snapshot)
        assert impls.any { it.name == 'Base' || it.name == 'Child' }
        assert MethodBinding.methodImplementations(ping, snapshot) != null
        assert MethodBinding.superOf(ping) != null
        assert MethodBinding.superOf(null) == null
        assert MethodBinding.methodAt(ping, snapshot).is(ping)
        assert MethodBinding.methodAt(new VariableExpression('x'), snapshot) == null
        assert MethodBinding.findMethod(null, 'Child', 'ping', null) == null
        assert MethodBinding.findMethod(snapshot, 'Child', 'ping', ping.typeDescriptor)?.name == 'ping'
        assert MethodBinding.findMethod(snapshot, null, 'util', '')?.name == 'util'
        assert MethodBinding.overloads((ClassNode) null, 'ping').isEmpty()
        assert !MethodBinding.overloads(childNode, 'util').isEmpty()
        def call = new MethodCallExpression(new VariableExpression('this'), 'ping', new TupleExpression())
        call.methodTarget = ping
        assert MethodBinding.resolveMethodCandidates(call, snapshot) == [ping]
        assert MethodBinding.resolveMethodCandidates(new VariableExpression('x'), snapshot).isEmpty()
        assert MethodBinding.overloads((MethodCallExpression) null, snapshot).isEmpty()
        assert MethodBinding.argumentCount(null) == 0
        assert MethodBinding.argumentCount(new VariableExpression('x')) == 1
        assert MethodBinding.argumentCount(new TupleExpression([new VariableExpression('a')])) == 1
        assert !MethodBinding.sameType(null, ClassHelper.OBJECT_TYPE)
        assert MethodBinding.sameOrDerived(ClassHelper.STRING_TYPE, ClassHelper.OBJECT_TYPE)
        def ctor = new ConstructorCallExpression(ClassHelper.OBJECT_TYPE, new TupleExpression())
        ctor.usingAnonymousInnerClass = true
        assert MethodBinding.resolveConstructorCandidates(ctor).isEmpty()
        assert MethodBinding.resolveConstructor(null) == null
        def field = childNode.addField('label', 0, ClassHelper.STRING_TYPE, null)
        field.synthetic = false
        def property = new PropertyExpression(new VariableExpression('this', childNode), 'label')
        assert MethodBinding.resolveProperty(property)?.name == 'label'
        assert MethodBinding.resolveProperty(null) == null
        def stat = new StaticMethodCallExpression(childNode, 'util', new TupleExpression())
        def util = childNode.methods.find { it.name == 'util' && it.lineNumber > 0 }
        assert MethodBinding.staticCallMatches(stat, util)
        def param = new Parameter(ClassHelper.STRING_TYPE, 'n')
        param.lineNumber = 1
        def local = new VariableExpression('n')
        local.accessedVariable = param
        assert MethodBinding.isRenameSafe(local, snapshot)
    }

    @Test
    void methodImplementationsFindsDefRunOnAnImplementingClass() {
        fixture = new LspFixture()
        fixture.open('Face.groovy', '''\
            interface Face {
                def run()
            }
            class Impl implements Face {
                def run() {}
            }
            '''.stripIndent())
        def snapshot = fixture.server.context.snapshot
        def module = snapshot.documents().find { it.module?.classes?.any { it.name == 'Face' } }.module
        def face = module.classes.find { it.name == 'Face' }
        def run = face.methods.find { it.name == 'run' && it.lineNumber > 0 }
        assert run != null
        assert MethodBinding.methodImplementations(run, snapshot).any { it.declaringClass.name == 'Impl' }
    }

    @Test
    void callSitesCoverNamedNullAndSingleArguments() {
        def named = new TupleExpression([new MapExpression()])
        assert CallSites.hasNamedArgs(named)
        assert CallSites.argumentExpressions(null).isEmpty()
        def single = new VariableExpression('x')
        assert CallSites.argumentExpressions(single) == [single]
        assert !CallSites.hasNamedArgs(single)
    }

    @Test
    void methodBindingEdges() {
        fixture = new LspFixture()
        def text = '''\
            interface Top { void ping() }
            interface Mid extends Top {}
            class Impl implements Mid {
                void ping() { ping() }
                static void util(int n) { util(n) }
            }
            class Child extends Impl {}
            class Solo { void solo() {} }
            class A {}
            class B extends A {}
            class C extends B {}
            '''.stripIndent()
        fixture.open('Types.groovy', text)
        def snapshot = fixture.server.context.snapshot
        def module = snapshot.documents().find { it.module?.classes?.any { it.name == 'Impl' } }.module
        def impl = module.classes.find { it.name == 'Impl' }
        def child = module.classes.find { it.name == 'Child' }
        def solo = module.classes.find { it.name == 'Solo' }
        def top = module.classes.find { it.name == 'Top' }
        def typeC = module.classes.find { it.name == 'C' }
        def typeA = module.classes.find { it.name == 'A' }
        def ping = impl.methods.find { it.name == 'ping' && it.lineNumber > 0 }
        def soloMethod = solo.methods.find { it.name == 'solo' && it.lineNumber > 0 }
        def topPing = top.methods.find { it.name == 'ping' }

        assert MethodBinding.superOf(ping)?.declaringClass?.name == 'Top'
        assert MethodBinding.superOf(soloMethod) == null
        assert MethodBinding.superOf(null) == null
        def orphan = new MethodNode('x', 0, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        assert MethodBinding.methodImplementations(orphan, snapshot).isEmpty()
        assert MethodBinding.methodImplementations(topPing, snapshot).any { it.declaringClass.name == 'Impl' }
        assert MethodBinding.implementations(impl, snapshot).any { it.name == 'Child' }

        def blank = new CompiledDocument(URI.create('file:///blank.groovy'), 1, '', null, null, null)
        def mixed = new CompilationSnapshot(snapshot.documents().collectEntries { [(it.uri): it] } + [(blank.uri): blank])
        assert MethodBinding.implementations(impl, mixed).any { it.name == 'Child' }
        assert MethodBinding.findMethod(mixed, 'Nope', 'ping', null) == null

        def unresolved = new ClassNode('Missing', 0, ClassHelper.OBJECT_TYPE)
        def ctor = new ConstructorCallExpression(unresolved, new TupleExpression())
        assert MethodBinding.resolveConstructorCandidates(ctor).isEmpty()
        assert MethodBinding.resolveConstructor(ctor) == null
        assert MethodBinding.resolveProperty(null) == null
        def dynamicProp = new PropertyExpression(new VariableExpression('obj'), new VariableExpression('dyn'))
        assert MethodBinding.resolveProperty(dynamicProp) == null

        def staticCall = new StaticMethodCallExpression(impl, 'util', new ArgumentListExpression(new ConstantExpression(1)))
        assert MethodBinding.methodAt(staticCall, snapshot)?.name == 'util'
        def instanceCall = new MethodCallExpression(new VariableExpression('this'), 'ping', new TupleExpression())
        instanceCall.implicitThis = true
        instanceCall.methodTarget = ping
        assert MethodBinding.methodAt(instanceCall, snapshot).is(ping)
        assert MethodBinding.resolveMethodCandidates(instanceCall, snapshot) == [ping]
        assert MethodBinding.methodAt(new ConstantExpression(1), snapshot) == null
        assert MethodBinding.callMatches(instanceCall, ping, null)
        assert !MethodBinding.callMatches(null, ping, null)
        assert !MethodBinding.staticCallMatches(null, ping)
        assert !MethodBinding.staticCallMatches(staticCall, ping)
        assert MethodBinding.staticCallMatches(staticCall, impl.methods.find { it.name == 'util' && it.lineNumber > 0 })
        assert !MethodBinding.receiverMatchesOwner(new VariableExpression('x'), '')
        assert !MethodBinding.receiverMatchesOwner(null, 'Impl')

        def implicit = new MethodCallExpression(new VariableExpression('this'), 'absent', new TupleExpression())
        implicit.implicitThis = true
        assert MethodBinding.overloads(implicit, null).isEmpty()
        assert MethodBinding.overloads(implicit, mixed).isEmpty()
        assert MethodBinding.overloads(impl, null).isEmpty()
        assert MethodBinding.overloads((MethodCallExpression) null, snapshot).isEmpty()
        assert !MethodBinding.sameType(null, impl)
        assert MethodBinding.sameOrDerived(null, ClassHelper.OBJECT_TYPE) == false
        assert MethodBinding.sameOrDerived(child, impl)
        assert MethodBinding.sameOrDerived(typeC, typeA)
        assert MethodBinding.argumentCount(null) == 0
        assert MethodBinding.argumentCount(new ConstantExpression(1)) == 1

        def grand = new ClassNode('pkg.Grand', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def parent = new ClassNode('pkg.Parent', Opcodes.ACC_PUBLIC, grand)
        def kid = new ClassNode('pkg.Kid', Opcodes.ACC_PUBLIC, parent)
        grand.addMethod(new MethodNode('ping', Opcodes.ACC_PUBLIC, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE))
        def same = new MethodNode('ping', Opcodes.ACC_PUBLIC, ClassHelper.VOID_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        parent.addMethod(same)
        same.declaringClass = kid
        assert MethodBinding.superOf(same)?.declaringClass?.name == 'pkg.Grand'

        def synthetic = new ConstructorNode(Opcodes.ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        synthetic.synthetic = true
        def host = new ClassNode('pkg.Host', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        host.addConstructor(synthetic)
        assert MethodBinding.resolveConstructorCandidates(new ConstructorCallExpression(host, new TupleExpression())).isEmpty()
        def real = new ConstructorNode(Opcodes.ACC_PUBLIC,
                [new Parameter(ClassHelper.STRING_TYPE, 'name')] as Parameter[], ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE)
        host.addConstructor(real)
        def args = new ArgumentListExpression(new ConstantExpression('a'))
        assert MethodBinding.resolveConstructor(new ConstructorCallExpression(host, args))?.parameters?.length == 1

        assert !MethodBinding.isRenameSafe(new ConstantExpression(1), snapshot)
        def unsafe = new MethodCallExpression(new VariableExpression('this'), 'ping', new TupleExpression())
        assert !MethodBinding.isRenameSafe(unsafe, snapshot)
        assert MethodBinding.isRenameSafe(staticCall, snapshot)
    }
}
