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

import groovy.transform.Trait
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.control.ResolveVisitor
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
import org.junit.jupiter.api.Test

import java.lang.reflect.Method

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT
import static org.objectweb.asm.Opcodes.ACC_INTERFACE
import static org.objectweb.asm.Opcodes.ACC_PUBLIC

/**
 * Branches in the qualified-{@code super} check that source programs do not reach:
 * null receivers, a null interface list, and an anonymous class with no superclass.
 */
final class Groovy12526pt2 {

    private static final Method DIRECT = ResolveVisitor.getDeclaredMethod(
            'directlyDeclaresInterface', ClassNode, ClassNode)

    static {
        DIRECT.accessible = true
    }

    @Test
    void testNullTypeAndCandidateAreNotDirectSuperinterfaces() {
        def face = interfaceNode('I')
        assert !declares(null, face)
        assert !declares(face, null)
        assert !declares(null, null)
    }

    @Test
    void testNullInterfaceListIsNotADirectSuperinterface() {
        def host = new ClassNode('Host', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        host.interfaces = null
        assert !declares(host, interfaceNode('I'))
    }

    @Test
    void testAnonymousClassWithNoSuperclassDoesNotMatch() {
        def outer = new ClassNode('Outer', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def anon = new InnerClassNode(outer, 'Outer$1', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        anon.anonymous = true
        anon.unresolvedSuperClass = null
        assert !declares(anon, interfaceNode('I'))
    }

    @Test
    void testStaticTypeCheckerTraitSuperShortCircuit() {
        def traitType = interfaceNode('T')
        traitType.addAnnotation(new AnnotationNode(ClassHelper.make(Trait)))
        def plain = interfaceNode('I')
        def objectType = ClassHelper.OBJECT_TYPE

        def implementor = new ClassNode('Impl', ACC_PUBLIC, objectType)
        implementor.addInterface(traitType)
        def stranger = new ClassNode('Stranger', ACC_PUBLIC, objectType)

        assert probe(implementor, traitType)
        assert !probe(stranger, traitType)
        assert probe(stranger, plain)
        assert !probe(stranger, objectType)
    }

    private static boolean declares(ClassNode type, ClassNode candidate) {
        DIRECT.invoke(null, [type, candidate] as Object[])
    }

    private static boolean probe(ClassNode enclosing, ClassNode qualifier) {
        def source = SourceUnit.create('Groovy12526pt2.groovy', '')
        def expression = new PropertyExpression(new ClassExpression(qualifier), 'super')
        new Probe(source, enclosing).check(expression)
    }

    private static class Probe extends StaticTypeCheckingVisitor {
        Probe(SourceUnit source, ClassNode type) {
            super(source, type)
        }

        boolean check(PropertyExpression expression) {
            existsProperty(expression, true)
        }
    }

    private static ClassNode interfaceNode(String name) {
        new ClassNode(name, ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT, ClassHelper.OBJECT_TYPE)
    }
}
