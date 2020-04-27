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
package org.apache.groovy.parser.antlr4

import groovy.test.GroovyTestCase
import groovy.test.NotYetImplemented
import groovy.transform.AutoFinal
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases

import static org.apache.groovy.parser.antlr4.util.ASTComparatorCategory.LOCATION_IGNORE_LIST

/**
 * Some syntax error test cases for the new parser.
 */
@AutoFinal
final class SyntaxErrorTest extends GroovyTestCase {

    void 'test groovy core - List'() {
        TestUtils.shouldFail('fail/List_01.groovy')
    }

    void 'test groovy core - Expression'() {
        TestUtils.shouldFail('fail/Expression_01.groovy')
        TestUtils.shouldFail('fail/Expression_02.groovy')
        TestUtils.shouldFail('fail/Expression_03.groovy')
      //TestUtils.shouldFail('fail/Expression_04.groovy')
      //TestUtils.shouldFail('fail/Expression_05.groovy')
        TestUtils.shouldFail('fail/Expression_06.groovy')
        TestUtils.shouldFail('fail/Expression_07.groovy')
        TestUtils.shouldFail('fail/Expression_08.groovy')
        TestUtils.shouldFail('fail/Expression_09.groovy')
    }

    void 'test groovy core - CommandExpression'() {
        TestUtils.doRunAndShouldFail('fail/CommandExpression_01x.groovy')
    }

    void 'test groovy core - Switch'() {
        TestUtils.shouldFail('fail/Switch_01.groovy')
    }

    @NotYetImplemented
    void 'test groovy core - LocalVariableDeclaration'() {
        TestUtils.shouldFail('fail/LocalVariableDeclaration_01.groovy')
    }

    void 'test groovy core - Continue'() {
        TestUtils.doRunAndShouldFail('fail/Continue_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Continue_02x.groovy')
    }

    void 'test groovy core - Break'() {
        TestUtils.doRunAndShouldFail('fail/Break_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Break_02x.groovy')
    }

    void 'test groovy core - UnexpectedCharacter 1'() {
        TestUtils.doRunAndShouldFail('fail/UnexpectedCharacter_01x.groovy')
    }

    void 'test groovy core - UnexpectedCharacter 2'() {
        def err = expectParseError '''\
            |def \u200Bname = null
            |'''.stripMargin()

        // TODO: Could the character be escaped in the error message?
        assert err == '''\
            |startup failed:
            |test.groovy: 1: Unexpected input: 'def \u200B' @ line 1, column 5.
            |   def \u200Bname = null
            |       ^
            |
            |1 error
            |'''.stripMargin()

        //

        err = expectParseError '''\
            |def na\u200Bme = null
            |'''.stripMargin()

        // TODO: Could the character be escaped in the error message?
        assert err == '''\
            |startup failed:
            |test.groovy: 1: Unexpected input: '\u200B'; Expecting <EOF> @ line 1, column 7.
            |   def na\u200Bme = null
            |         ^
            |
            |1 error
            |'''.stripMargin()

        //

        err = expectParseError '''\
            |def na\u000Cme = null
            |'''.stripMargin()

        // TODO: Could the character be escaped in the error message?
        assert err == '''\
            |startup failed:
            |test.groovy: 1: Unexpected input: '\u000C'; Expecting <EOF> @ line 1, column 7.
            |   def na\u000Cme = null
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    void 'test groovy core - ParExpression'() {
        TestUtils.doRunAndShouldFail('fail/ParExpression_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/ParExpression_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/ParExpression_03x.groovy')
    }

    void 'test groovy core - Parentheses'() {
        TestUtils.shouldFail('fail/Parentheses_01.groovy')
    }

    void 'test groovy core - This'() {
        TestUtils.doRunAndShouldFail('fail/This_01x.groovy')
    }

    void 'test groovy core - Super'() {
        TestUtils.doRunAndShouldFail('fail/Super_01x.groovy')
    }

    void 'test groovy core - AbstractMethod'() {
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_05x.groovy')
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_06x.groovy')
    }

    void 'test groovy core - BUGs'() {
        TestUtils.doRunAndShouldFail('bugs/BUG-GROOVY-5318.groovy')
        TestUtils.doRunAndShouldFail('bugs/BUG-GROOVY-8150.groovy')
        TestUtils.doRunAndShouldFail('bugs/BUG-GROOVY-8216.groovy')
    }

    void 'test groovy core - DoWhile'() {
        TestUtils.doRunAndShouldFail('fail/DoWhile_01x.groovy')
    }

    void 'test groovy core - For'() {
        TestUtils.shouldFail('fail/For_01.groovy')
        TestUtils.shouldFail('fail/For_02.groovy')
    }

    void 'test groovy core - Modifier'() {
        TestUtils.doRunAndShouldFail('fail/Modifier_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Modifier_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/Modifier_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/Modifier_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/Modifier_05x.groovy')
        TestUtils.shouldFail('fail/Modifier_07.groovy')
    }

    void 'test groovy core - ClassDeclaration 1'() {
        TestUtils.doRunAndShouldFail('fail/ClassDeclaration_02x.groovy')
    }

    void 'test groovy core - ClassDeclaration 2'() {
        def err = expectParseError '''\
            |class C extends Object, Number {}
            |'''.stripMargin()

        assert err == '''\
            |startup failed:
            |test.groovy: 1: Cannot extend multiple classes @ line 1, column 9.
            |   class C extends Object, Number {}
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    void 'test groovy core - EnumDeclaration 1'() {
        def err = expectParseError '''\
            |enum E<T> {}
            |'''.stripMargin()

        assert err == '''\
            |startup failed:
            |test.groovy: 1: enum declaration cannot have type parameters @ line 1, column 7.
            |   enum E<T> {}
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    void 'test groovy core - EnumDeclaration 2'() {
        def err = expectParseError '''\
            |enum E extends Object {}
            |'''.stripMargin()

        assert err == '''\
            |startup failed:
            |test.groovy: 1: No extends clause allowed for enum declaration @ line 1, column 8.
            |   enum E extends Object {}
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    void 'test groovy core - AnnotationDeclaration 1'() {
        TestUtils.doRunAndShouldFail('fail/AnnotationDeclaration_01x.groovy');
    }

    void 'test groovy core - AnnotationDeclaration 2'() {
        def err = expectParseError '''\
            |@interface A<T> {}
            |'''.stripMargin()

        assert err == '''\
            |startup failed:
            |test.groovy: 1: annotation declaration cannot have type parameters @ line 1, column 13.
            |   @interface A<T> {}
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    void 'test groovy core - AnnotationDeclaration 3'() {
        def err = expectParseError '''\
            |@interface A extends Object {}
            |'''.stripMargin()

        assert err == '''\
            |startup failed:
            |test.groovy: 1: No extends clause allowed for annotation declaration @ line 1, column 14.
            |   @interface A extends Object {}
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    void 'test groovy core - AnnotationDeclaration 4'() {
        def err = expectParseError '''\
            |@interface A implements Serializable {}
            |'''.stripMargin()

        assert err == '''\
            |startup failed:
            |test.groovy: 1: No implements clause allowed for annotation declaration @ line 1, column 14.
            |   @interface A implements Serializable {}
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    void 'test groovy core - MethodDeclaration'() {
        TestUtils.shouldFail('fail/MethodDeclaration_01.groovy')
        TestUtils.doRunAndShouldFail('fail/MethodDeclaration_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/MethodDeclaration_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/MethodDeclaration_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/MethodDeclaration_05x.groovy')
    }

    void 'test groovy core - ConstructorDeclaration'() {
        TestUtils.shouldFail('fail/ConstructorDeclaration_01.groovy')
    }

    void 'test groovy core - ClosureListExpression'() {
        TestUtils.shouldFail('fail/ClosureListExpression_01.groovy')
        TestUtils.shouldFail('fail/ClosureListExpression_02.groovy')
        TestUtils.shouldFail('fail/ClosureListExpression_03.groovy')
        TestUtils.shouldFail('fail/ClosureListExpression_04.groovy')
    }

    void 'test groovy core - InterfaceDeclaration 1'() {
        TestUtils.shouldFail('fail/InterfaceDeclaration_01.groovy')
    }

    void 'test groovy core - InterfaceDeclaration 2'() {
        def err = expectParseError '''\
            |interface I implements Serializable {}
            |'''.stripMargin()

        assert err == '''\
            |startup failed:
            |test.groovy: 1: No implements clause allowed for interface declaration @ line 1, column 13.
            |   interface I implements Serializable {}
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    void 'test groovy core - void'() {
        TestUtils.doRunAndShouldFail('fail/Void_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Void_02x.groovy')
    }

    void 'test groovy core - FieldDeclaration'() {
        TestUtils.doRunAndShouldFail('fail/FieldDeclaration_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/FieldDeclaration_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/FieldDeclaration_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/FieldDeclaration_04x.groovy')
    }

    void 'test groovy core - Assert'() {
        TestUtils.doRunAndShouldFail('fail/Assert_01x.groovy')
    }

    void 'test groovy core - DuplicatedNamedParameter'() {
        TestUtils.doRunAndShouldFail('fail/DuplicatedNamedParameter_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/DuplicatedNamedParameter_02x.groovy')
    }

    void 'test groovy core - threadsafe'() {
        TestUtils.doRunAndShouldFail('fail/ThreadSafe_01x.groovy')
    }

    void 'test groovy core - VarArgParameter'() {
        TestUtils.doRunAndShouldFail('fail/VarArgParameter_01x.groovy')
    }

    void 'test groovy core - Number'() {
        TestUtils.doRunAndShouldFail('fail/Number_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Number_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/Number_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/Number_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/Number_05x.groovy')
    }

    void 'test groovy core - MethodCall'() {
        TestUtils.doRunAndShouldFail('fail/MethodCall_01x.groovy')
    }

    void 'test groovy core - var'() {
        TestUtils.doRunAndShouldFail('fail/Var_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Var_02x.groovy')
    }
	
	void 'test groovy core - val'() {
		TestUtils.doRunAndShouldFail('fail/Val_01x.groovy')
		TestUtils.doRunAndShouldFail('fail/Val_02x.groovy')
		TestUtils.doRunAndShouldFail('fail/Val_03x.groovy')
	}
	
	void 'test groovy core - let'() {
		TestUtils.doRunAndShouldFail('fail/Let_01x.groovy')
		TestUtils.doRunAndShouldFail('fail/Let_02x.groovy')
		TestUtils.doRunAndShouldFail('fail/Let_03x.groovy')
	}

    void 'test groovy core - String'() {
        TestUtils.doRunAndShouldFail('fail/String_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/String_02x.groovy')
        TestUtils.shouldFail('fail/String_03.groovy')
        TestUtils.shouldFail('fail/String_04.groovy')
    }

    void 'test groovy core - NonStaticClass'() {
        TestUtils.doRunAndShouldFail('fail/NonStaticClass_01x.groovy')
    }

    void 'test groovy core - Import'() {
        TestUtils.doRunAndShouldFail('fail/Import_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Import_02x.groovy')
    }

    void 'test groovy core - UnaryOperator'() {
        TestUtils.doRunAndShouldFail('fail/UnaryOperator_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/UnaryOperator_02x.groovy')
    }

    void 'test groovy core - Trait'() {
        TestUtils.shouldFail('fail/Trait_01.groovy')
    }

    void 'test groovy core - Array'() {
        TestUtils.doRunAndShouldFail('fail/Array_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Array_02x.groovy')
    }

    void 'test error alternative - Missing ")" 1'() {
        def err = expectParseError '''\
            |println ((int 123)
            |'''.stripMargin()

        assert err == '''\
            |startup failed:
            |test.groovy: 1: Missing ')' @ line 1, column 14.
            |   println ((int 123)
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    void 'test error alternative - Missing ")" 2'() {
        def err = expectParseError '''\
            |def x() {
            |    println((int) 123
            |}
            |'''.stripMargin()

        assert err == '''\
            |startup failed:
            |test.groovy: 2: Missing ')' @ line 2, column 22.
            |       println((int) 123
            |                        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @NotYetImplemented
    void 'test CompilerErrorTest_001'() {
        unzipScriptAndShouldFail('scripts/CompilerErrorTest_001.groovy', [])
    }

    @NotYetImplemented
    void 'test CompilerErrorTest_002'() {
        unzipScriptAndShouldFail('scripts/CompilerErrorTest_002.groovy', [])
    }

    @NotYetImplemented
    void 'test DifferencesFromJavaTest_002'() {
        unzipScriptAndShouldFail('scripts/DifferencesFromJavaTest_002.groovy', [])
    }

    @NotYetImplemented
    void 'test Groovy5212Bug_001'() {
        unzipScriptAndShouldFail('scripts/Groovy5212Bug_001.groovy', [])
    }

    @NotYetImplemented
    void 'test GStringEndTest_001'() {
        unzipScriptAndShouldFail('scripts/GStringEndTest_001.groovy', [])
    }

    //--------------------------------------------------------------------------

    private static String expectParseError(String source) {
        try {
            new CompilationUnit().with {
                addSource('test.groovy', source)
                compile(Phases.CONVERSION)
                getAST()
            }

            fail('expected parse to fail')
        } catch (e) {
            return e.message.replace('\r\n', '\n')
        }
    }

    private static unzipScriptAndShouldFail(String entryName, List ignoreClazzList, Map<String, String> replacementsMap = [:], boolean toCheckNewParserOnly = false) {
        ignoreClazzList.addAll(TestUtils.COMMON_IGNORE_CLASS_LIST)

        TestUtils.unzipAndFail(SCRIPT_ZIP_PATH, entryName, TestUtils.addIgnore(ignoreClazzList, LOCATION_IGNORE_LIST), replacementsMap, toCheckNewParserOnly)
    }

    private static final String SCRIPT_ZIP_PATH = "${TestUtils.RESOURCES_PATH}/groovy-2.5.0/groovy-2.5.0-SNAPSHOT-20160921-allscripts.zip"
}
