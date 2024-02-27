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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.SourceUnit;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_STRICT;
import static org.objectweb.asm.Opcodes.ACC_SYNCHRONIZED;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;
import static org.objectweb.asm.Opcodes.ACC_VOLATILE;

public final class ClassCompletionVerifierTest {

    private SourceUnit source = SourceUnit.create("dummy.groovy", "");
    private ClassCompletionVerifier verifier = new ClassCompletionVerifier(source);

    @Test
    public void shouldDetectAbstractPrivateMethod() {
        ClassNode node = new ClassNode("X", ACC_ABSTRACT, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("y", ACC_PRIVATE | ACC_ABSTRACT, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        verifier.visitClass(node);
        checkErrorMessage("The method 'y' must not be private as it is declared abstract in class 'X'.");
    }

    @Test
    public void shouldDetectFinalAbstractClass() {
        checkVisitErrors("FinalClass", ACC_FINAL, false);
        checkVisitErrors("AbstractClass", ACC_ABSTRACT, false);
        checkVisitErrors("AbstractFinalClass", ACC_ABSTRACT | ACC_FINAL, true);
        checkErrorMessage("The class 'AbstractFinalClass' must not be both final and abstract.");
    }

    @Test
    public void shouldDetectDuplicateMethodsForClassNoParams() {
        tryDetectDuplicateMethods(ACC_ABSTRACT, "Repetitive method name/signature for method 'java.lang.Object xxx()' in class 'zzz'.");
    }

    @Test
    public void shouldDetectDuplicateMethodsForInterfaceOneParam() {
        tryDetectDuplicateMethods(ACC_INTERFACE, "Repetitive method name/signature for method 'java.lang.Object xxx(java.lang.String)' in interface 'zzz'.", new Parameter(ClassHelper.STRING_TYPE, "x"));
    }

    private void tryDetectDuplicateMethods(int modifiers, String expectedErrorMessage, Parameter... params) {
        ClassNode node = new ClassNode("zzz", modifiers, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("xxx", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, params, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("xxx", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, params, ClassNode.EMPTY_ARRAY, null));
        verifier.visitClass(node);
        checkErrorCount(2);
        checkErrorMessage(expectedErrorMessage);
    }

    @Test
    public void shouldDetectIncorrectOtherModifier() {
        // can't check synchronized here as it doubles up with ACC_SUPER
        checkVisitErrors("DodgyClass", ACC_TRANSIENT | ACC_VOLATILE | ACC_NATIVE, true);
        checkErrorMessage("The class 'DodgyClass' has an incorrect modifier transient.");
        checkErrorMessage("The class 'DodgyClass' has an incorrect modifier volatile.");
        checkErrorMessage("The class 'DodgyClass' has an incorrect modifier native.");
    }

    @Test
    public void shouldDetectFinalAbstractInterface() {
        checkVisitErrors("FinalInterface", ACC_ABSTRACT | ACC_FINAL | ACC_INTERFACE, true);
        checkErrorMessage("The interface 'FinalInterface' must not be final. It is by definition abstract.");
    }

    @Test
    public void shouldDetectFinalMethodsInInterface() {
        ClassNode node = new ClassNode("zzz", ACC_ABSTRACT | ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("xxx", ACC_PUBLIC | ACC_FINAL, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        addStaticConstructor(node);
        verifier.visitClass(node);
        checkErrorCount(1);
        checkErrorMessage("The method 'java.lang.Object xxx()' from interface 'zzz' must not be final. It is by definition abstract.");
    }

    @Test
    public void shouldDetectIncorrectMethodModifiersInInterface() {
        ClassNode node = new ClassNode("zzz", ACC_ABSTRACT | ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("st", ACC_PUBLIC | ACC_STRICT       , ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("na", ACC_PUBLIC | ACC_NATIVE       , ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("sy", ACC_PUBLIC | ACC_SYNCHRONIZED , ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("tr", ACC_PUBLIC | ACC_TRANSIENT    , ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        // can't check volatile here as it doubles up with bridge
        addStaticConstructor(node);
        verifier.visitClass(node);
        checkErrorCount(4);
        checkErrorMessage("The method 'java.lang.Object st()' has an incorrect modifier strictfp.");
        checkErrorMessage("The method 'java.lang.Object na()' has an incorrect modifier native.");
        checkErrorMessage("The method 'java.lang.Object sy()' has an incorrect modifier synchronized.");
        checkErrorMessage("The method 'java.lang.Object tr()' has an incorrect modifier transient.");
    }

    @Test
    public void shouldDetectIncorrectMemberVisibilityInInterface() {
        ClassNode node = new ClassNode("zzz", ACC_ABSTRACT | ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("pro1", ACC_ABSTRACT | ACC_PROTECTED, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("pro2", ACC_STATIC   | ACC_PROTECTED, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("pro3",                ACC_PROTECTED, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block()));
        node.addMethod(new MethodNode("pp_1", ACC_ABSTRACT                , ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("pp_2", ACC_STATIC                  , ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addField("prof", ACC_PROTECTED, ClassHelper.OBJECT_TYPE, null);
        node.addField("prif", ACC_PRIVATE  , ClassHelper.OBJECT_TYPE, null);
        node.addField("pp_f", 0            , ClassHelper.OBJECT_TYPE, null);
        addStaticConstructor(node);
        verifier.visitClass(node);
        checkErrorCount(8);
        checkErrorMessage("The field 'prof' is not 'public static final' but is defined in interface 'zzz'");
        checkErrorMessage("The field 'prif' is not 'public static final' but is defined in interface 'zzz'");
        checkErrorMessage("The field 'pp_f' is not 'public static final' but is defined in interface 'zzz'");
        checkErrorMessage("The method 'pro1' must be public as it is declared abstract in interface 'zzz'");
        checkErrorMessage("The method 'pro2' is protected but must be public or private in interface 'zzz'");
        checkErrorMessage("The method 'pro3' is protected but must be default or private in interface 'zzz'");
        checkErrorMessage("The method 'pp_1' must be public as it is declared abstract in interface 'zzz'");
        checkErrorMessage("The method 'pp_2' is package-private but must be public or private in interface 'zzz'");
    }

    @Test
    public void shouldDetectCorrectMethodModifiersInClass() {
        // can't check volatile here as it doubles up with bridge
        ClassNode node = new ClassNode("zzz", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("st", ACC_STRICT, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("na", ACC_NATIVE, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("sy", ACC_SYNCHRONIZED, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        addStaticConstructor(node);
        verifier.visitClass(node);
        checkErrorCount(0);
    }

    @Test
    public void shouldDetectIncorrectMethodModifiersInClass() {
        // can't check volatile here as it doubles up with bridge
        ClassNode node = new ClassNode("zzz", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("tr", ACC_TRANSIENT, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        addStaticConstructor(node);
        verifier.visitClass(node);
        checkErrorCount(1);
        checkErrorMessage("The method 'java.lang.Object tr()' has an incorrect modifier transient.");
    }

    @Test
    public void shouldDetectInvalidFieldModifiers() {
        ClassNode node = new ClassNode("foo", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        node.addField("bar", ACC_FINAL | ACC_VOLATILE, ClassHelper.STRING_TYPE, null);
        verifier.visitClass(node);
        checkErrorCount(1);
        checkErrorMessage("Illegal combination of modifiers, final and volatile, for field 'bar'");
    }

    @Test
    public void shouldDetectClassExtendsInterface() {
        ClassNode node = new ClassNode("C", ACC_PUBLIC, ClassHelper.SERIALIZABLE_TYPE);
        verifier.visitClass(node);
        checkErrorCount(1);
        checkErrorMessage("You are not allowed to extend the interface 'java.io.Serializable', use implements instead.");
    }

    @Test
    public void shouldDetectClassImplementsNonInterface() {
        ClassNode[] impl = {ClassHelper.OBJECT_TYPE, ClassHelper.SERIALIZABLE_TYPE, ClassHelper.ELEMENT_TYPE_TYPE};
        ClassNode node = new ClassNode("C", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, impl, null);
        verifier.visitClass(node);
        checkErrorCount(2);
        checkErrorMessage("You are not allowed to implement the class 'java.lang.Object', use extends instead.");
        checkErrorMessage("You are not allowed to implement the enum 'java.lang.annotation.ElementType', use extends instead.");
    }

    @Test
    public void shouldAcceptInterfaceExtendsInterface() {
        ClassNode node = new ClassNode("I", ACC_PUBLIC | ACC_INTERFACE, ClassHelper.LIST_TYPE.getPlainNodeReference());
        verifier.visitClass(node);
        checkErrorCount(0);
    }

    //--------------------------------------------------------------------------

    private void addStaticConstructor(ClassNode node) {
        // constructors should not be treated as errors (they have no real meaning for interfaces anyway)
        node.addMethod(new MethodNode("<clinit>", ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
    }

    private void checkErrorCount(int count) {
        assertEquals(buildErrorMessage(count), count, source.getErrorCollector().getErrorCount());
    }

    private String buildErrorMessage(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("Expected ").append(count);
        sb.append(" error messages but found ");
        sb.append(source.getErrorCollector().getErrorCount()).append(":\n");
        sb.append(flattenErrorMessage());
        return sb.toString();
    }

    private void checkVisitErrors(String name, int modifiers, boolean expectedToFail) {
        ClassNode node = new ClassNode(name, modifiers, ClassHelper.OBJECT_TYPE);
        verifier.visitClass(node);
        assertTrue(source.getErrorCollector().hasErrors() == expectedToFail);
    }

    private void checkErrorMessage(String expectedErrorMessage) {
        assertTrue("Expected an error message but none found.", source.getErrorCollector().hasErrors());
        assertTrue("Expected message to contain <" + expectedErrorMessage + "> but was <" + flattenErrorMessage() + ">.", flattenErrorMessage().contains(expectedErrorMessage));
    }

    private String flattenErrorMessage() {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter, true)) {
            for (int i = source.getErrorCollector().getErrorCount() - 1; i >= 0; i -= 1) {
                source.getErrorCollector().getError(i).write(printWriter);
            }
        }
        return stringWriter.toString();
    }
}
