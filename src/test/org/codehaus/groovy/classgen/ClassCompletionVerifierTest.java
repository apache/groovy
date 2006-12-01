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
    private static final String EXPECTED_ERROR_MESSAGE_FOR_CLASS =
            "The class '" + ABSTRACT_FINAL_CLASS + "' must not be both final and abstract.";
    private static final String EXPECTED_ERROR_MESSAGE_FOR_INTERFACE =
            "The interface '" + FINAL_INTERFACE + "' must not be final. It is by definition abstract.";

    protected void setUp() throws Exception {
        super.setUp();
        source = SourceUnit.create("dummy.groovy", "");
        verifier = new ClassCompletionVerifier(source);
    }

    public void testDetectsFinalAbstractClass() throws Exception {
        checkVisitErrors("FinalClass", ACC_FINAL, false);
        checkVisitErrors("AbstractClass", ACC_ABSTRACT, false);
        checkVisitErrors(ABSTRACT_FINAL_CLASS, ACC_ABSTRACT | ACC_FINAL, true);
        checkErrorMessage(EXPECTED_ERROR_MESSAGE_FOR_CLASS);
    }

    public void testDetectsFinalAbstractInterface() throws Exception {
        checkVisitErrors(FINAL_INTERFACE, ACC_ABSTRACT | ACC_FINAL | ACC_INTERFACE, true);
        checkErrorMessage(EXPECTED_ERROR_MESSAGE_FOR_INTERFACE);
    }

    private void checkVisitErrors(String name, int modifiers, boolean expectedToFail) {
        ClassNode node = new ClassNode(name, modifiers, ClassHelper.OBJECT_TYPE);
        verifier.visitClass(node);
        assertTrue(source.getErrorCollector().hasErrors() == expectedToFail);
    }

    private void checkErrorMessage(String expectedErrorMessage) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter, true);
        source.getErrorCollector().getError(0).write(writer);
        writer.close();
        assertTrue(stringWriter.toString().indexOf(expectedErrorMessage) != -1);
    }
}
