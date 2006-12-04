package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.SourceUnit;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author Paul King
 */
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
            "Method 'xxx' from Interface 'zzz' must not be final. It is by definition abstract.";
    private static final String EXPECTED_INTERFACE_STATIC_METHOD_ERROR_MESSAGE =
            "Method 'yyy' from Interface 'zzz' must not be static. Only fields may be static in an interface.";
    private static final String EXPECTED_TRANSIENT_CLASS_ERROR_MESSAGE =
            "The class 'DodgyClass' has an incorrect modifier transient.";
    private static final String EXPECTED_VOLATILE_CLASS_ERROR_MESSAGE =
            "The class 'DodgyClass' has an incorrect modifier volatile.";

    protected void setUp() throws Exception {
        super.setUp();
        source = SourceUnit.create("dummy.groovy", "");
        verifier = new ClassCompletionVerifier(source);
    }

    public void testDetectsFinalAbstractClass() throws Exception {
        checkVisitErrors("FinalClass", ACC_FINAL, false);
        checkVisitErrors("AbstractClass", ACC_ABSTRACT, false);
        checkVisitErrors(ABSTRACT_FINAL_CLASS, ACC_ABSTRACT | ACC_FINAL, true);
        checkErrorMessage(EXPECTED_CLASS_MODIFIER_ERROR_MESSAGE);
    }

    public void testDetectsIncorrectOtherModifier() throws Exception {
        checkVisitErrors("DodgyClass", ACC_TRANSIENT | ACC_VOLATILE, true);
        checkErrorMessage(EXPECTED_TRANSIENT_CLASS_ERROR_MESSAGE);
        checkErrorMessage(EXPECTED_VOLATILE_CLASS_ERROR_MESSAGE);
    }

    public void testDetectsFinalAbstractInterface() throws Exception {
        checkVisitErrors(FINAL_INTERFACE, ACC_ABSTRACT | ACC_FINAL | ACC_INTERFACE, true);
        checkErrorMessage(EXPECTED_INTERFACE_MODIFIER_ERROR_MESSAGE);
    }

    public void testDetectsFinalAndStaticMethodsInInterface() throws Exception {
        ClassNode node = new ClassNode("zzz", ACC_ABSTRACT | ACC_INTERFACE, ClassHelper.OBJECT_TYPE);
        node.addMethod(new MethodNode("xxx", ACC_PUBLIC | ACC_FINAL, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        node.addMethod(new MethodNode("yyy", ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        // constructors should not be treated as errors (they have no real meaning for interfaces anyway)
        node.addMethod(new MethodNode("<clinit>", ACC_PUBLIC | ACC_STATIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null));
        verifier.visitClass(node);
        assertEquals(2, source.getErrorCollector().getErrorCount());
        checkErrorMessage(EXPECTED_INTERFACE_FINAL_METHOD_ERROR_MESSAGE);
        checkErrorMessage(EXPECTED_INTERFACE_STATIC_METHOD_ERROR_MESSAGE);
    }

    private void checkVisitErrors(String name, int modifiers, boolean expectedToFail) {
        ClassNode node = new ClassNode(name, modifiers, ClassHelper.OBJECT_TYPE);
        verifier.visitClass(node);
        assertTrue(source.getErrorCollector().hasErrors() == expectedToFail);
    }

    private void checkErrorMessage(String expectedErrorMessage) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter, true);
        for (int i = source.getErrorCollector().getErrorCount() - 1; i >= 0; i--) {
            source.getErrorCollector().getError(i).write(writer);
        }
        writer.close();
        assertTrue("Expected an error message but none found.", source.getErrorCollector().hasErrors());
        assertTrue("Expected message to contain <" + expectedErrorMessage +
                "> but was <" + stringWriter.toString() + ">.",
                stringWriter.toString().indexOf(expectedErrorMessage) != -1);
    }
}
