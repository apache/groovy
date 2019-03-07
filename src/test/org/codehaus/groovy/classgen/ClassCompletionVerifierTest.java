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

import java.io.PrintWriter;
import java.io.StringWriter;

public class ClassCompletionVerifierTest extends TestSupport {
    private SourceUnit source;
    private ClassCompletionVerifier verifier;
    private static final String ABSTRACT_FINAL_CLASS = "AbstractFinalClass";
    private static final String FINAL_INTERFACE = "FinalInterface";
    private static final String EXPECTED_CLASS_MODIFIER_ERROR_MESSAGE =
            "The class '" + ABSTRACT_FINAL_CLASS + "' must not be both final and abstract.";
    private static final String EXPECTED_INTERFACE_MODIFIER_ERROR_MESSAGE =
            "The interface '" + FINAL_INTERFACE + "' must not be final. It is by definition abstract.";
    private static final String EXPECTED_INTERFACE_FINAL_METHOD_ERROR_MESSAGE =
            "The method 'java.lang.Object xxx()' from interface 'zzz' must not be final. It is by definition abstract.";
    private static final String EXPECTED_INTERFACE_STATIC_METHOD_ERROR_MESSAGE =
            "The method 'java.lang.Object yyy()' from interface 'zzz' must not be static. Only fields may be static in an interface.";
    private static final String EXPECTED_TRANSIENT_CLASS_ERROR_MESSAGE =
            "The class 'DodgyClass' has an incorrect modifier transient.";
    // can't check synchronized here as it doubles up with ACC_SUPER
    //private static final String EXPECTED_SYNCHRONIZED_CLASS_ERROR_MESSAGE =
    //        "The class 'DodgyClass' has an incorrect modifier synchronized.";
    private static final String EXPECTED_NATIVE_CLASS_ERROR_MESSAGE =
            "The class 'DodgyClass' has an incorrect modifier native.";
    private static final String EXPECTED_VOLATILE_CLASS_ERROR_MESSAGE =
            "The class 'DodgyClass' has an incorrect modifier volatile.";
    private static final String EXPECTED_DUPLICATE_METHOD_ERROR_CLASS_MESSAGE =
            "Repetitive method name/signature for method 'java.lang.Object xxx()' in class 'zzz'.";
    private static final String EXPECTED_DUPLICATE_METHOD_ERROR_INTERFACE_MESSAGE =
            "Repetitive method name/signature for method 'java.lang.Object xxx(java.lang.String)' in interface 'zzz'.";
    // can't check volatile here as it doubles up with bridge
    //private static final String EXPECTED_VOLATILE_METHOD_ERROR_MESSAGE =
    //        "The method 'java.lang.Object vo()' has an incorrect modifier volatile.";
    private static final String EXPECTED_STRICT_METHOD_ERROR_MESSAGE =
            "The method 'java.lang.Object st()' has an incorrect modifier strictfp.";
    private static final String EXPECTED_NATIVE_METHOD_ERROR_MESSAGE =
            "The method 'java.lang.Object na()' has an incorrect modifier native.";
    private static final String EXPECTED_SYNCHRONIZED_METHOD_ERROR_MESSAGE =
            "The method 'java.lang.Object sy()' has an incorrect modifier synchronized.";
    private static final String EXPECTED_PROTECTED_FIELD_ERROR_MESSAGE =
            "The field 'prof' is not 'public static final' but is defined in interface 'zzz'.";
    private static final String EXPECTED_PRIVATE_FIELD_ERROR_MESSAGE =
            "The field 'prif' is not 'public static final' but is defined in interface 'zzz'.";
    private static final String EXPECTED_PROTECTED_METHOD_ERROR_MESSAGE =
            "Method 'prom' is protected but should be public in interface 'zzz'.";
    private static final String EXPECTED_PRIVATE_METHOD_ERROR_MESSAGE =
            "Method 'prim' is private but should be public in interface 'zzz'.";
    private static final String EXPECTED_ABSTRACT_PRIVATE_METHOD_ERROR_MESSAGE =
            "Method 'y' from class 'X' must not be private as it is declared as an abstract method.";

    protected void setUp() throws Exception {
        super.setUp();
        source = SourceUnit.create("dummy.groovy", "");
        verifier = new ClassCompletionVerifier(source);
    }

    public void testDetectsAbstractPrivateMethod() throws Exception {
        ClassNode node = new ClassNode("X", ACC_ABSTRACT, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("y", ACC_PRIVATE | ACC_ABSTRACT, ClassHelper.VOID_TYPE, new Parameter[0], ClassNode.EMPTY_ARRAY, null));
        verifier.visitClass(node);
        checkErrorMessage(EXPECTED_ABSTRACT_PRIVATE_METHOD_ERROR_MESSAGE);
    }

    public void testDetectsFinalAbstractClass() throws Exception {
        checkVisitErrors("FinalClass", ACC_FINAL, false);
        checkVisitErrors("AbstractClass", ACC_ABSTRACT, false);
        checkVisitErrors(ABSTRACT_FINAL_CLASS, ACC_ABSTRACT | ACC_FINAL, true);
        checkErrorMessage(EXPECTED_CLASS_MODIFIER_ERROR_MESSAGE);
    }

    public void testDetectsDuplicateMethodsForClassNoParams() throws Exception {
        checkDetectsDuplicateMethods(0, EXPECTED_DUPLICATE_METHOD_ERROR_CLASS_MESSAGE, Parameter.EMPTY_ARRAY);
    }

    public void testDetectsDuplicateMethodsForInterfaceOneParam() throws Exception {
        Parameter[] stringParam = {new Parameter(ClassHelper.STRING_TYPE, "x")};
        checkDetectsDuplicateMethods(ACC_INTERFACE, EXPECTED_DUPLICATE_METHOD_ERROR_INTERFACE_MESSAGE, stringParam);
    }

    private void checkDetectsDuplicateMethods(int modifiers, String expectedErrorMessage, Parameter[] params) {
        ClassNode node = new ClassNode("zzz", modifiers, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("xxx", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, params, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("xxx", ACC_PUBLIC, ClassHelper.OBJECT_TYPE, params, ClassNode.EMPTY_ARRAY, null));
        verifier.visitClass(node);
        checkErrorCount(2);
        checkErrorMessage(expectedErrorMessage);
    }

    public void testDetectsIncorrectOtherModifier() throws Exception {
        // can't check synchronized here as it doubles up with ACC_SUPER
        checkVisitErrors("DodgyClass", ACC_TRANSIENT | ACC_VOLATILE | ACC_NATIVE, true);
        checkErrorMessage(EXPECTED_TRANSIENT_CLASS_ERROR_MESSAGE);
        checkErrorMessage(EXPECTED_VOLATILE_CLASS_ERROR_MESSAGE);
        checkErrorMessage(EXPECTED_NATIVE_CLASS_ERROR_MESSAGE);
    }

    public void testDetectsFinalAbstractInterface() throws Exception {
        checkVisitErrors(FINAL_INTERFACE, ACC_ABSTRACT | ACC_FINAL | ACC_INTERFACE, true);
        checkErrorMessage(EXPECTED_INTERFACE_MODIFIER_ERROR_MESSAGE);
    }

    public void testDetectsFinalAndStaticMethodsInInterface() throws Exception {
        ClassNode node = new ClassNode("zzz", ACC_ABSTRACT | ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("xxx", ACC_PUBLIC | ACC_FINAL, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("yyy", ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        addDummyConstructor(node);
        verifier.visitClass(node);
        checkErrorCount(2);
        checkErrorMessage(EXPECTED_INTERFACE_FINAL_METHOD_ERROR_MESSAGE);
        checkErrorMessage(EXPECTED_INTERFACE_STATIC_METHOD_ERROR_MESSAGE);
    }

    public void testDetectsIncorrectMethodModifiersInInterface() throws Exception {
        // can't check volatile here as it doubles up with bridge
        ClassNode node = new ClassNode("zzz", ACC_ABSTRACT | ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("st", ACC_STRICT, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("na", ACC_NATIVE, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("sy", ACC_SYNCHRONIZED, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        addDummyConstructor(node);
        verifier.visitClass(node);
        checkErrorCount(3);
        checkErrorMessage(EXPECTED_STRICT_METHOD_ERROR_MESSAGE);
        checkErrorMessage(EXPECTED_NATIVE_METHOD_ERROR_MESSAGE);
        checkErrorMessage(EXPECTED_SYNCHRONIZED_METHOD_ERROR_MESSAGE);
    }

    public void testDetectsIncorrectMemberVisibilityInInterface() throws Exception {
        ClassNode node = new ClassNode("zzz", ACC_ABSTRACT | ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("prim", ACC_PRIVATE, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("prom", ACC_PROTECTED, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addField("prif", ACC_PRIVATE, ClassHelper.OBJECT_TYPE, null);
        node.addField("prof", ACC_PROTECTED, ClassHelper.OBJECT_TYPE, null);
        addDummyConstructor(node);
        verifier.visitClass(node);
        checkErrorCount(4);
        checkErrorMessage(EXPECTED_PROTECTED_FIELD_ERROR_MESSAGE);
        checkErrorMessage(EXPECTED_PRIVATE_FIELD_ERROR_MESSAGE);
        checkErrorMessage(EXPECTED_PROTECTED_METHOD_ERROR_MESSAGE);
        checkErrorMessage(EXPECTED_PRIVATE_METHOD_ERROR_MESSAGE);
    }

    public void testDetectsCorrectMethodModifiersInClass() throws Exception {
        // can't check volatile here as it doubles up with bridge
        ClassNode node = new ClassNode("zzz", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("st", ACC_STRICT, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("na", ACC_NATIVE, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("sy", ACC_SYNCHRONIZED, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        addDummyConstructor(node);
        verifier.visitClass(node);
        checkErrorCount(0);
    }

    private void addDummyConstructor(ClassNode node) {
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
        assertTrue("Expected message to contain <" + expectedErrorMessage +
                "> but was <" + flattenErrorMessage() + ">.",
                flattenErrorMessage().contains(expectedErrorMessage));
    }

    private String flattenErrorMessage() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter, true);
        for (int i = source.getErrorCollector().getErrorCount() - 1; i >= 0; i--) {
            source.getErrorCollector().getError(i).write(writer);
        }
        writer.close();
        return stringWriter.toString();
    }
}
