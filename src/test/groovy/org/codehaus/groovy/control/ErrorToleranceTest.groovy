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
package org.codehaus.groovy.control

import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Tests {@link CompilerConfiguration#setTolerance(int)}, the number of non-fatal
 * errors collected before compilation is abandoned.
 */
final class ErrorToleranceTest {

    private static final int MANY = 14

    /**
     * Static type-checking errors, one per method. These are raised via
     * {@code StaticTypeCheckingVisitor#addError}, which overrides the base
     * {@code ClassCodeVisitorSupport} reporting used below.
     */
    private static String typeCheckingErrors(int count) {
        def methods = (0..<count).collect { "    def m$it() { new Object().nope$it() }" }
        "@groovy.transform.CompileStatic\nclass Subject {\n${methods.join('\n')}\n}\n"
    }

    /**
     * Class resolution errors, one per field. These are raised by {@link ResolveVisitor}
     * via {@code ClassCodeVisitorSupport#addError}, a different reporting path and a
     * different compile phase to the type-checking errors above.
     */
    private static String resolutionErrors(int count) {
        def fields = (0..<count).collect { "    NoSuchType$it field$it" }
        "class Subject {\n${fields.join('\n')}\n}\n"
    }

    /**
     * Errors reported by a precompiled type-checking extension, one per method. These are
     * raised from the extension's {@code afterVisitMethod} handler, so the exception thrown
     * when the tolerance is reached passes back through the extension support.
     */
    private static String extensionErrors(int count) {
        def methods = (0..<count).collect { "    void m$it() {}" }
        "@groovy.transform.TypeChecked(extensions='${ErrorPerMethod.name}')\nclass Subject {\n${methods.join('\n')}\n}\n"
    }

    static class ErrorPerMethod extends GroovyTypeCheckingExtensionSupport.TypeCheckingDSL {
        @Override
        Object run() {
            afterVisitMethod { MethodNode node ->
                addStaticTypeError("extension error in $node.name", node)
            }
        }
    }

    private static MultipleCompilationErrorsException compilationFailure(String source, Integer tolerance = null) {
        def config = new CompilerConfiguration()
        if (tolerance != null) config.tolerance = tolerance
        def unit = new CompilationUnit(config, null, new GroovyClassLoader(ErrorToleranceTest.class.classLoader))
        unit.addSource('Subject.groovy', source)
        try {
            unit.compile()
            null
        } catch (MultipleCompilationErrorsException e) {
            e
        }
    }

    private static int errorCount(String source, Integer tolerance = null) {
        compilationFailure(source, tolerance)?.errorCollector?.errorCount ?: 0
    }

    // GROOVY-12306: tolerance was ignored for type-checking errors, which reported in full
    // however low it was set, so there was no way to ask the compiler to stop after the first
    @Test
    void testTypeCheckingErrorsHonourTolerance() {
        assertEquals(1, errorCount(typeCheckingErrors(MANY), 1))
        assertEquals(3, errorCount(typeCheckingErrors(MANY), 3))
    }

    // the same must hold for errors reported through the base ClassCodeVisitorSupport path
    @Test
    void testResolutionErrorsHonourTolerance() {
        assertEquals(1, errorCount(resolutionErrors(MANY), 1))
        assertEquals(3, errorCount(resolutionErrors(MANY), 3))
    }

    // GROOVY-12687: when an extension reported the error that reached the tolerance, the exception
    // thrown for it was recorded as one more error in the collector it carries, so rendering the
    // errors rendered that exception, which rendered the errors again until the stack overflowed
    @Test
    void testExtensionErrorsHonourTolerance() {
        def failure = compilationFailure(extensionErrors(MANY))
        String report = failure.message
        assertEquals(CompilerConfiguration.DEFAULT_TOLERANCE, failure.errorCollector.errorCount)
        assertTrue(failure.errorCollector.errors.every { it instanceof SyntaxErrorMessage })
        assertTrue(report.contains('extension error in m0'))
        assertEquals(MANY, errorCount(extensionErrors(MANY), 0))
    }

    // GROOVY-12306: zero means unlimited, the setting to reach for when every error is wanted
    @Test
    void testZeroToleranceMeansUnlimited() {
        assertEquals(MANY, errorCount(typeCheckingErrors(MANY), 0))
        assertEquals(MANY, errorCount(resolutionErrors(MANY), 0))
    }

    // a negative tolerance is meaningless as a count, so it is treated as unlimited too
    @Test
    void testNegativeToleranceMeansUnlimited() {
        assertEquals(MANY, errorCount(typeCheckingErrors(MANY), -1))
    }

    // the default is unchanged by GROOVY-12306, and now applies to both kinds of error
    @Test
    void testDefaultToleranceAppliesToBothErrorKinds() {
        assertEquals(CompilerConfiguration.DEFAULT_TOLERANCE, new CompilerConfiguration().tolerance)
        assertEquals(CompilerConfiguration.DEFAULT_TOLERANCE, errorCount(typeCheckingErrors(MANY)))
        assertEquals(CompilerConfiguration.DEFAULT_TOLERANCE, errorCount(resolutionErrors(MANY)))
    }

    // fewer errors than the tolerance are all reported, whichever path raised them
    @Test
    void testAllErrorsReportedBelowTolerance() {
        assertEquals(3, errorCount(typeCheckingErrors(3), 10))
        assertEquals(3, errorCount(resolutionErrors(3), 10))
    }
}
