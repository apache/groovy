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
package groovy.transform.stc

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

/**
 * Units tests for type checking extensions.
 */
class TypeCheckingExtensionsTest extends StaticTypeCheckingTestCase {

    private void setExtension(String name) {
        def cz = config.compilationCustomizers.find {
            it instanceof ASTTransformationCustomizer
        }
        if (name) {
            cz.annotationParameters = [extensions: name]
        } else {
            cz.annotationParameters = [:]
        }
    }

    void testSetupExtension() {
        extension = 'groovy/transform/stc/SetupTestExtension.groovy'
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData('setup')
            })
            class A {}
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData('setup') == null
            })
            class B {}
            new A()
        '''
    }

    void testNonExistentExtension() {
        def extensionPath = 'groovy/transform/stc/NonExistentTestExtension.groovy'
        extension = extensionPath

        String errorMessage = "Static type checking extension '${extensionPath}' was not found on the classpath."

        String message = shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                assert true
            '''
        }

        assert message.contains(errorMessage)
    }

    void testFinishExtension() {
        extension = 'groovy/transform/stc/FinishTestExtension.groovy'
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData('finish')
            })
            class A {}
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData('finish') == null
            })
            class B {}
            new A()
        '''
    }

    void testNewMethodAndIsGenerated() {
        extension = 'groovy/transform/stc/NewMethodAndIsGeneratedTestExtension.groovy'
        shouldFailWithMessages '''
            'foo'
        ''', 'Extension was executed properly'
    }

    void testUndefinedVariable() {
        extension = 'groovy/transform/stc/UndefinedVariableTestExtension.groovy'
        try {
            assertScript '''
                foo.toUpperCase() // normal type checker would fail here
            '''
        } catch (MissingPropertyException e) {
            // normal
        }
    }

    void testUndefinedVariableNoHandle() {
        extension = 'groovy/transform/stc/UndefinedVariableNoHandleTestExtension.groovy'
        shouldFailWithMessages '''
                foo.toUpperCase() // normal type checker would fail here
            ''', 'The variable [foo] is undeclared'
    }

    void testMissingMethod() {
        extension = null
        shouldFailWithMessages '''
            String msg = 'foo'
            msg.TOUPPERCASE()
        ''', 'Cannot find matching method'
        extension = 'groovy/transform/stc/MissingMethod1TestExtension.groovy'
        try {
            assertScript '''
                String msg = 'foo'
                msg.TOUPPERCASE()
            '''
        } catch (MissingMethodException e) {
            // normal
        }
    }

    void testMissingMethodWithLogic() {
        extension = null
        shouldFailWithMessages '''
            String msg = 'foo'
            msg.SIZE()
            msg.CONCAT('bar')
        ''', 'Cannot find matching method java.lang.String#SIZE()', 'Cannot find matching method java.lang.String#CONCAT(java.lang.String)'
        extension = 'groovy/transform/stc/MissingMethod2TestExtension.groovy'
        try {
            assertScript '''
                String msg = 'foo'
                msg.SIZE()
                msg.CONCAT('bar')
            '''
        } catch (MissingMethodException e) {
            // normal
        }
    }

    void testShouldSilenceTypeChecker() {
        extension = 'groovy/transform/stc/SilentTestExtension.groovy'
        assertScript '''import org.codehaus.groovy.runtime.typehandling.GroovyCastException
            try {
                int x = 'foo'
            } catch (GroovyCastException e) {
            }
        '''
    }

    void testShouldChangeErrorPrefix() {
        extension = 'groovy/transform/stc/PrefixChangerTestExtension.groovy'
        shouldFailWithMessages '''
           int x = 'foo'
        ''', '[Custom] - Cannot assign value of type java.lang.String to variable of type int'
    }

    void testAfterMethodCallHook() {
        extension = 'groovy/transform/stc/SprintfExtension.groovy'
        shouldFailWithMessages '''
            String count = 'foo'
            sprintf("Count = %d", count)
        ''', 'Parameter types didn\'t match types expected from the format String',
                'For placeholder 1 [%d] expected \'int\' but was \'java.lang.String\''
    }

    void testBeforeMethodCallHook() {
        extension = 'groovy/transform/stc/UpperCaseMethodTest1Extension.groovy'
        shouldFailWithMessages '''
            String method() { 'foo' }
            String BOO() { 'bar' }
            method() // ok
            BOO() // error
        ''', 'Calling a method which is all uppercase is not allowed'
    }

    void testBeforeMethodHook() {
        extension = 'groovy/transform/stc/UpperCaseMethodTest2Extension.groovy'
        shouldFailWithMessages '''
            String method() { 'foo' } // ok
            String BOO() { 'bar' } // error
        ''', 'Defining method which is all uppercase is not allowed'
    }

    void testAfterMethodHook() {
        extension = 'groovy/transform/stc/UpperCaseMethodTest3Extension.groovy'
        shouldFailWithMessages '''
            String method() { 'foo' } // ok
            String BOO() { 'bar' } // error
        ''', 'Defining method which is all uppercase is not allowed'
    }

    void testMethodSelection() {
        // first step checks that without extension, type checking works properly
        extension = null
        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData('selected') == null
        })
        def str = 'foo'.toUpperCase()
        '''

        // then we use a type checking extension, we add node metadata
        extension = 'groovy/transform/stc/OnMethodSelectionTestExtension.groovy'
        assertScript '''
        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData('selected') == true
        })
        def str = 'foo'.toUpperCase()
        '''
    }

    void testUnresolvedProperty() {
        extension = null
        shouldFailWithMessages '''
            'str'.FOO
        ''', 'No such property: FOO for class: java.lang.String'

        extension = 'groovy/transform/stc/UnresolvedPropertyTestExtension.groovy'
        assertScript '''
            try {
                'str'.FOO
            } catch (MissingPropertyException ex) {
            }
        '''
    }

    void testUnresolvedAttribute() {
        extension = null
        shouldFailWithMessages '''
            'str'.@FOO
        ''', 'No such attribute: FOO for class: java.lang.String'

        extension = 'groovy/transform/stc/UnresolvedAttributeTestExtension.groovy'
        assertScript '''
            try {
                'str'.@FOO
            } catch (MissingFieldException ex) {
            }
        '''
    }

    void testScopeEnterScopeExit() {
        extension = 'groovy/transform/stc/ScopeEnterExitTestExtension.groovy'
        shouldFailWithMessages '''
            class Support {
                void foo(Closure c) { c() }
            }
            new Support().foo {
                'a'.toUpperCase()
            }
        ''', 'Scope enter and exit behave correctly' // we're using shouldFail just to verify that the extension is ran
    }

    void testMatchingArguments() {
        extension = 'groovy/transform/stc/ArgumentsTestingTestExtension.groovy'
        shouldFailWithMessages '''
            def zero() {}
            def two(String a, Integer b) {}
            def three(String a, int b, Date c) {}
            'foo'.concat('bar')
            zero()
            two('foo', 1)
            three('foo', 2, new Date())
            three('foo', (Integer)2, new Date())
        ''', 'Method [zero] with matching arguments found: 0',
             'Method [concat] with matching arguments found: 1',
                'Method [two] with matching arguments found: 2',
                'Method [three] with matching arguments found: 3',
                'Method [three] with matching arguments found: 3'
    }

    void testFirstArgsMatches() {
        extension = 'groovy/transform/stc/FirstArgumentsTestingTestExtension.groovy'
        shouldFailWithMessages '''
            def two(String a, Integer b) {}
            def three(String a, int b, Date c) {}
            two('foo', 1)
            three('foo', 2, new Date())
            three('foo', (Integer)2, new Date())
        ''', 'Method [two] with matching arguments found: 2',
                'Method [three] with matching arguments found: 3',
                'Method [three] with matching arguments found: 3'
    }

    void testNthArgMatches() {
        extension = 'groovy/transform/stc/NthArgumentTestingTestExtension.groovy'
        shouldFailWithMessages '''
            def two(String a, Integer b) {}
            def three(String a, int b, Date c) {}
            two('foo', 1)
            three('foo', 2, new Date())
            three('foo', (Integer)2, new Date())
        ''', 'Method [two] with matching argument found: [0, class java.lang.String]',
             'Method [two] with matching argument found: [1, class java.lang.Integer]',
             'Method [three] with matching argument found: [0, class java.lang.String]',
             'Method [three] with matching argument found: [1, class java.lang.Integer]',
             'Method [three] with matching argument found: [2, class java.util.Date]',
             'Method [three] with matching argument found: [0, class java.lang.String]',
             'Method [three] with matching argument found: [1, class java.lang.Integer]',
             'Method [three] with matching argument found: [2, class java.util.Date]'
    }

    void testIncompatibleAssignment() {
        extension = null
        shouldFailWithMessages '''
            int x = 'foo'
        ''', 'Cannot assign value of type java.lang.String to variable of type int'

        extension = 'groovy/transform/stc/IncompatibleAssignmentTestExtension.groovy'
        assertScript '''
            try {
                int x = 'foo'
            } catch (e) {}
        '''
    }

    void testBinaryOperatorNotFound() {
        extension = null
        shouldFailWithMessages '''
            int x = 1
            Date y = new Date()
            x+y
        ''', 'Cannot find matching method int#plus(java.util.Date)'

        extension = 'groovy/transform/stc/BinaryOperatorTestExtension.groovy'
        assertScript '''
            try {
                int x = 1
                Date y = new Date()
                x+y
            } catch (e) {}
        '''
    }

    void testBinaryOperatorNotFound2() {
        extension = null
        shouldFailWithMessages '''
            int x = 1
            Date y = new Date()
            x << y
        ''', 'Cannot find matching method int#leftShift(java.util.Date)'

        extension = 'groovy/transform/stc/BinaryOperatorTestExtension.groovy'
        assertScript '''
            try {
                int x = 1
                Date y = new Date()
                x+y
            } catch (e) {}
        '''
    }

    void testDelegatesTo() {
        extension = null
        shouldFailWithMessages '''
        class Item { void pick(){} }
        void build(Closure arg) {
            arg.delegate = new Item()
            arg()
        }
        build {
            pick()
        }
        ''', 'Cannot find matching method'

        extension = 'groovy/transform/stc/DelegatesToTestExtension.groovy'
        assertScript '''
        class Item { void pick(){} }
        void build(Closure arg) {
            arg.delegate = new Item()
            arg()
        }
        build {
            pick()
        }
        '''
    }

    void testIsAnnotatedBy() {
        extension = null
        assertScript '''
        @groovy.transform.stc.MyType(String)
        int foo() { 1 }
        '''

        extension = 'groovy/transform/stc/AnnotatedByTestExtension.groovy'
        assertScript '''
        @groovy.transform.stc.MyType(String)
        @ASTTest(phase=INSTRUCTION_SELECTION,value={
            assert node.getNodeMetaData(INFERRED_RETURN_TYPE) == STRING_TYPE
        })
        int foo() { 1 }
        '''
    }

    void testBeforeAfterClass() {
        extension = 'groovy/transform/stc/BeforeAfterClassTestExtension.groovy'
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE)?.name == 'A'
                assert node.getNodeMetaData('after') == true
            })
            class A {}

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == null // disabled through beforeVisitClass
                assert node.getNodeMetaData('after') == true
            })
            class B {
                void hasTypeCheckingError() { int x = 'foo' }
            }
            new A()
        '''
    }

    void testLookupClassNodeNotAvailableOnExtensionsClasspath() {
        extension = 'groovy/transform/stc/RobotMove.groovy'
        assertScript '''
            class Robot {
                void move(String dist) { println "Moved $dist" }
            }

            this.binding.setVariable('robot', new Robot())

            void operate() {
                robot.move "left"
            }

            operate()
        '''
    }

    void testShouldNotThrowNPE_Groovy6047() {
        extension = 'groovy/transform/stc/Groovy6047Extension.groovy'
        try {
            assertScript '''
                def b = new Vector()
                b.elems()
            '''
        } catch (MissingMethodException e) {
            // it's ok
        }
    }

    void testAmbiguousMethodCall() {
        // fail with error from type checker
        extension = null
        shouldFailWithMessages '''
            int foo(Integer x) { 1 }
            int foo(String s) { 2 }
            int foo(Date d) { 3 }
            assert foo(null) == 2
        ''', 'Reference to method is ambiguous'
        // fail with error from runtime
        extension = 'groovy/transform/stc/AmbiguousMethods.groovy'
        shouldFail { assertScript '''
            int foo(Integer x) { 1 }
            int foo(String s) { 2 }
            int foo(Date d) { 3 }
            assert foo(null) == 2
        '''}
    }

    void testIncompatibleReturnType() {
        extension = null
        shouldFailWithMessages '''
            Date foo() { '1' }
            true
        ''', 'Cannot return value of type'
        extension = 'groovy/transform/stc/IncompatibleReturnTypeTestExtension.groovy'
        assertScript '''
            Date foo() { '1' }
            true
        '''
    }

    void testPrecompiledExtension() {
        extension = null
        assertScript '''
            println 'Everything is ok'
        '''
        extension = 'groovy.transform.stc.PrecompiledExtension'
        shouldFailWithMessages '''
            println 'Everything is ok'
        ''', 'Error thrown from extension'

    }

    void testPrecompiledExtensionNotExtendingTypeCheckingDSL() {
        extension = null
        assertScript '''
            println 'Everything is ok'
        '''
        extension = 'groovy.transform.stc.PrecompiledExtensionNotExtendingDSL'
        shouldFailWithMessages '''
            println 'Everything is ok'
        ''', 'Error thrown from extension in setup', 'Error thrown from extension in onMethodSelection'

    }
}
