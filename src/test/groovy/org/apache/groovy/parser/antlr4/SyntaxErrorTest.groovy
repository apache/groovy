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

import groovy.test.NotYetImplemented
import groovy.transform.AutoFinal
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.Assert
import org.junit.Test

import static org.apache.groovy.parser.antlr4.util.ASTComparatorCategory.LOCATION_IGNORE_LIST

/**
 * Some syntax error test cases for the new parser.
 */
@AutoFinal
final class SyntaxErrorTest {

    @Test
    void 'groovy core - List'() {
        TestUtils.shouldFail('fail/List_01.groovy')
    }

    @Test
    void 'groovy core - Expression'() {
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

    @Test
    void 'groovy core - CommandExpression'() {
        TestUtils.doRunAndShouldFail('fail/CommandExpression_01x.groovy')
    }

    @Test
    void 'groovy core - Switch'() {
        TestUtils.shouldFail('fail/Switch_01.groovy')
    }

    @NotYetImplemented @Test
    void 'groovy core - LocalVariableDeclaration'() {
        TestUtils.shouldFail('fail/LocalVariableDeclaration_01.groovy')
    }

    @Test
    void 'groovy core - Continue'() {
        TestUtils.doRunAndShouldFail('fail/Continue_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Continue_02x.groovy')
    }

    @Test
    void 'groovy core - Break'() {
        TestUtils.doRunAndShouldFail('fail/Break_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Break_02x.groovy')
    }

    @Test
    void 'groovy core - UnexpectedCharacter 1'() {
        TestUtils.doRunAndShouldFail('fail/UnexpectedCharacter_01x.groovy')
    }

    @Test // TODO: Could the character be escaped in the error message?
    void 'groovy core - UnexpectedCharacter 2'() {
        expectParseError '''\
            |def \u200Bname = null
            |'''.stripMargin(), '''\
            |Unexpected character: '\u200B' @ line 1, column 5.
            |   def \u200Bname = null
            |       ^
            |
            |1 error
            |'''.stripMargin()

        expectParseError '''\
            |def na\u200Bme = null
            |'''.stripMargin(), '''\
            |Unexpected character: '\u200B' @ line 1, column 7.
            |   def na\u200Bme = null
            |         ^
            |
            |1 error
            |'''.stripMargin()

        expectParseError '''\
            |def na\u000Cme = null
            |'''.stripMargin(), '''\
            |Unexpected character: '\u000C' @ line 1, column 7.
            |   def na\u000Cme = null
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - UnexpectedCharacter 3'() {
        expectParseError '''\
            |foo.bar {
            |  println 'Hello
            |}
            |'''.stripMargin(), '''\
            |Unexpected character: '\\'' @ line 2, column 11.
            |     println 'Hello
            |             ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - ParExpression'() {
        TestUtils.doRunAndShouldFail('fail/ParExpression_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/ParExpression_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/ParExpression_03x.groovy')
    }

    @Test
    void 'groovy core - Parentheses'() {
        TestUtils.shouldFail('fail/Parentheses_01.groovy')
    }

    @Test
    void 'groovy core - This'() {
        TestUtils.doRunAndShouldFail('fail/This_01x.groovy')
    }

    @Test
    void 'groovy core - Super'() {
        TestUtils.doRunAndShouldFail('fail/Super_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Super_02x.groovy')
    }

    // GROOVY-9391
    @Test
    void 'groovy core - Typecast super'() {
        expectParseError '''\
            |class A { def m() {} }
            |class B extends A {  }
            |class C extends B {
            |    def m() {
            |        ((A) super).m()
            |    }
            |}
            |'''.stripMargin(), '''\
            |Cannot cast or coerce `super` @ line 5, column 10.
            |           ((A) super).m()
            |            ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AbstractMethod'() {
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_05x.groovy')
        TestUtils.doRunAndShouldFail('fail/AbstractMethod_06x.groovy')
    }

    @Test
    void 'groovy core - BUGs'() {
        TestUtils.doRunAndShouldFail('bugs/BUG-GROOVY-5318.groovy')
        TestUtils.doRunAndShouldFail('bugs/BUG-GROOVY-8150.groovy')
        TestUtils.doRunAndShouldFail('bugs/BUG-GROOVY-8216.groovy')
    }

    @Test
    void 'groovy core - DoWhile'() {
        TestUtils.doRunAndShouldFail('fail/DoWhile_01x.groovy')
    }

    @Test
    void 'groovy core - For'() {
        TestUtils.shouldFail('fail/For_01.groovy')
        TestUtils.shouldFail('fail/For_02.groovy')
    }

    @Test
    void 'groovy core - Modifier'() {
        TestUtils.doRunAndShouldFail('fail/Modifier_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Modifier_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/Modifier_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/Modifier_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/Modifier_05x.groovy')
        TestUtils.shouldFail('fail/Modifier_07.groovy')
    }

    @Test
    void 'groovy core - ClassDeclaration 1'() {
        TestUtils.doRunAndShouldFail('fail/ClassDeclaration_01x.groovy')
    }

    @Test
    void 'groovy core - ClassDeclaration 2'() {
        TestUtils.doRunAndShouldFail('fail/ClassDeclaration_02x.groovy')
    }

    @Test
    void 'groovy core - ClassDeclaration 3'() {
        expectParseError '''\
            |class C extends Object, Number {}
            |'''.stripMargin(), '''\
            |Cannot extend multiple classes @ line 1, column 9.
            |   class C extends Object, Number {}
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - ClassDeclaration 4'() {
        TestUtils.doRunAndShouldFail('fail/ClassDeclaration_04x.groovy')
    }

    @Test
    void 'groovy core - EnumDeclaration 1'() {
        expectParseError '''\
            |enum E<T> {}
            |'''.stripMargin(), '''\
            |enum declaration cannot have type parameters @ line 1, column 7.
            |   enum E<T> {}
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - EnumDeclaration 2'() {
        expectParseError '''\
            |enum E extends Object {}
            |'''.stripMargin(), '''\
            |No extends clause allowed for enum declaration @ line 1, column 8.
            |   enum E extends Object {}
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    // GROOVY-4438, GROOVY-7773, GROOVY-8507, GROOVY-9301, GROOVY-9306
    @Test
    void 'groovy core - EnumDeclaration 3'() {
        expectParseError '''\
            |enum E {
            |  X, Y,
            |  def z() { }
            |}
            |'''.stripMargin(), '''\
            |Unexpected input: ',\\n  def' @ line 3, column 3.
            |     def z() { }
            |     ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AnnotationDeclaration 1'() {
        TestUtils.doRunAndShouldFail('fail/AnnotationDeclaration_01x.groovy')
    }

    @Test
    void 'groovy core - AnnotationDeclaration 2'() {
        expectParseError '''\
            |@interface A<T> {}
            |'''.stripMargin(), '''\
            |annotation declaration cannot have type parameters @ line 1, column 13.
            |   @interface A<T> {}
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AnnotationDeclaration 3'() {
        expectParseError '''\
            |@interface A extends Object {}
            |'''.stripMargin(), '''\
            |No extends clause allowed for annotation declaration @ line 1, column 14.
            |   @interface A extends Object {}
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AnnotationDeclaration 4'() {
        expectParseError '''\
            |@interface A implements Serializable {}
            |'''.stripMargin(), '''\
            |No implements clause allowed for annotation declaration @ line 1, column 14.
            |   @interface A implements Serializable {}
            |                ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - AnnotationDeclaration 5'() {
        expectParseError '''\
            |@interface A {
            |    String a() {
            |    }
            |}'''.stripMargin(), '''\
            |Annotation type element should not have body @ line 2, column 5.
            |       String a() {
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - SealedTypeDeclaration'() {
        TestUtils.doRunAndShouldFail('fail/SealedTypeDeclaration_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/SealedTypeDeclaration_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/SealedTypeDeclaration_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/SealedTypeDeclaration_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/SealedTypeDeclaration_05x.groovy')
        TestUtils.doRunAndShouldFail('fail/SealedTypeDeclaration_06x.groovy')
        TestUtils.doRunAndShouldFail('fail/SealedTypeDeclaration_07x.groovy')
        TestUtils.doRunAndShouldFail('fail/SealedTypeDeclaration_08x.groovy')
        TestUtils.doRunAndShouldFail('fail/SealedTypeDeclaration_09x.groovy')
    }

    @Test
    void 'groovy core - MethodDeclaration'() {
        TestUtils.shouldFail('fail/MethodDeclaration_01.groovy')
        TestUtils.doRunAndShouldFail('fail/MethodDeclaration_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/MethodDeclaration_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/MethodDeclaration_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/MethodDeclaration_05x.groovy')
    }

    @Test
    void 'groovy core - ConstructorDeclaration'() {
        TestUtils.shouldFail('fail/ConstructorDeclaration_01.groovy')
    }

    @Test
    void 'groovy core - ClosureListExpression'() {
        TestUtils.shouldFail('fail/ClosureListExpression_01.groovy')
        TestUtils.shouldFail('fail/ClosureListExpression_02.groovy')
        TestUtils.shouldFail('fail/ClosureListExpression_03.groovy')
        TestUtils.shouldFail('fail/ClosureListExpression_04.groovy')
    }

    @Test
    void 'groovy core - InterfaceDeclaration 1'() {
        TestUtils.shouldFail('fail/InterfaceDeclaration_01.groovy')
    }

    @Test
    void 'groovy core - InterfaceDeclaration 2'() {
        expectParseError '''\
            |interface I implements Serializable {}
            |'''.stripMargin(), '''\
            |No implements clause allowed for interface declaration @ line 1, column 13.
            |   interface I implements Serializable {}
            |               ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test // GROOVY-3908: groovyc should enforce correct usage of "continue"
    void 'groovy core - Continue 1'() {
        expectParseError '''\
            |class UseContinueAsGoto {
            |   static main(args) {
            |     continue label1
            |     return
            |
            |     label1:
            |     println "Groovy supports goto!"
            |   }
            |}
            |'''.stripMargin(), '''\
            |continue statement is only allowed inside loops @ line 3, column 6.
            |        continue label1
            |        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'groovy core - void'() {
        TestUtils.doRunAndShouldFail('fail/Void_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Void_02x.groovy')
    }

    @Test
    void 'groovy core - FieldDeclaration'() {
        TestUtils.doRunAndShouldFail('fail/FieldDeclaration_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/FieldDeclaration_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/FieldDeclaration_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/FieldDeclaration_04x.groovy')
    }

    @Test
    void 'groovy core - Assert'() {
        TestUtils.doRunAndShouldFail('fail/Assert_01x.groovy')
    }

    @Test
    void 'groovy core - DuplicatedNamedParameter'() {
        TestUtils.doRunAndShouldFail('fail/DuplicatedNamedParameter_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/DuplicatedNamedParameter_02x.groovy')
    }

    @Test
    void 'groovy core - threadsafe'() {
        TestUtils.doRunAndShouldFail('fail/ThreadSafe_01x.groovy')
    }

    @Test
    void 'groovy core - VarArgParameter'() {
        TestUtils.doRunAndShouldFail('fail/VarArgParameter_01x.groovy')
    }

    @Test
    void 'groovy core - Number'() {
        TestUtils.doRunAndShouldFail('fail/Number_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Number_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/Number_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/Number_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/Number_05x.groovy')
    }

    @Test
    void 'groovy core - MethodCall'() {
        TestUtils.doRunAndShouldFail('fail/MethodCall_01x.groovy')
    }

    @Test
    void 'groovy core - var'() {
        TestUtils.doRunAndShouldFail('fail/Var_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Var_02x.groovy')
    }

    @Test
    void 'groovy core - String'() {
        TestUtils.doRunAndShouldFail('fail/String_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/String_02x.groovy')
        TestUtils.shouldFail('fail/String_03.groovy')
        TestUtils.shouldFail('fail/String_04.groovy')
    }

    @Test
    void 'groovy core - NonStaticClass'() {
        TestUtils.doRunAndShouldFail('fail/NonStaticClass_01x.groovy')
    }

    @Test
    void 'groovy core - Import'() {
        TestUtils.doRunAndShouldFail('fail/Import_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Import_02x.groovy')
    }

    @Test
    void 'groovy core - UnaryOperator'() {
        TestUtils.doRunAndShouldFail('fail/UnaryOperator_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/UnaryOperator_02x.groovy')
    }

    @Test
    void 'groovy core - Trait'() {
        TestUtils.shouldFail('fail/Trait_01.groovy')
    }

    @Test
    void 'groovy core - Record'() {
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_05x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_06x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_07x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_08x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_09x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_10x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_11x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_12x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_13x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_14x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_15x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_16x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_17x.groovy')
        TestUtils.doRunAndShouldFail('fail/RecordDeclaration_18x.groovy')
    }

    @Test
    void 'groovy core - Array'() {
        TestUtils.doRunAndShouldFail('fail/Array_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/Array_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/Array_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/Array_04x.groovy')
    }

    @Test
    void 'groovy core - SwitchExpression'() {
        TestUtils.doRunAndShouldFail('fail/SwitchExpression_01x.groovy')
        TestUtils.doRunAndShouldFail('fail/SwitchExpression_02x.groovy')
        TestUtils.doRunAndShouldFail('fail/SwitchExpression_03x.groovy')
        TestUtils.doRunAndShouldFail('fail/SwitchExpression_04x.groovy')
        TestUtils.doRunAndShouldFail('fail/SwitchExpression_05x.groovy')
        TestUtils.doRunAndShouldFail('fail/SwitchExpression_06x.groovy')
        TestUtils.doRunAndShouldFail('fail/SwitchExpression_07x.groovy')
        TestUtils.doRunAndShouldFail('fail/SwitchExpression_08x.groovy')
        TestUtils.doRunAndShouldFail('fail/SwitchExpression_09x.groovy')
        TestUtils.doRunAndShouldFail('fail/SwitchExpression_10x.groovy')
    }

    @NotYetImplemented @Test
    void 'error alternative - Missing ")" 1'() {
        expectParseError '''\
            |println ((int 123)
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 15.
            |   println ((int 123)
            |                 ^
            |
            |1 error
            |'''.stripMargin()
    }

    @NotYetImplemented @Test
    void 'error alternative - Missing ")" 2'() {
        expectParseError '''\
            |def x() {
            |    println((int) 123
            |}
            |'''.stripMargin(), '''\
            |Missing ')' @ line 2, column 22.
            |       println((int) 123
            |                        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @NotYetImplemented @Test
    void 'error alternative - Missing ")" 3'() {
        expectParseError '''\
            |def m( {
            |}
            |'''.stripMargin(), '''\
            |Missing ')' @ line 1, column 8.
            |   def m( {
            |          ^
            |
            |1 error
            |'''.stripMargin()
    }

    @NotYetImplemented @Test
    void 'CompilerErrorTest_001'() {
        unzipScriptAndShouldFail('scripts/CompilerErrorTest_001.groovy', [])
    }

    @NotYetImplemented @Test
    void 'CompilerErrorTest_002'() {
        unzipScriptAndShouldFail('scripts/CompilerErrorTest_002.groovy', [])
    }

    @NotYetImplemented @Test
    void 'DifferencesFromJavaTest_002'() {
        unzipScriptAndShouldFail('scripts/DifferencesFromJavaTest_002.groovy', [])
    }

    @NotYetImplemented @Test
    void 'Groovy5212Bug_001'() {
        unzipScriptAndShouldFail('scripts/Groovy5212Bug_001.groovy', [])
    }

    @NotYetImplemented @Test
    void 'GStringEndTest_001'() {
        unzipScriptAndShouldFail('scripts/GStringEndTest_001.groovy', [])
    }

    //--------------------------------------------------------------------------

    private static void expectParseError(String source, String expect) {
        try {
            new CompilationUnit().with {
                addSource('test.groovy', source)
                compile(Phases.CONVERSION)
                getAST()
            }
            Assert.fail('expected parse to fail')
        } catch (e) {
            def line = (expect =~ /@ line (\d+),/)[0][1]
            Assert.assertEquals("startup failed:\ntest.groovy: $line: $expect".toString(), e.message.replace('\r\n', '\n'))
        }
    }

    private static unzipScriptAndShouldFail(String entryName, List ignoreClazzList, Map<String, String> replacementsMap = [:], boolean toCheckNewParserOnly = false) {
        ignoreClazzList.addAll(TestUtils.COMMON_IGNORE_CLASS_LIST)

        TestUtils.unzipAndFail(SCRIPT_ZIP_PATH, entryName, TestUtils.addIgnore(ignoreClazzList, LOCATION_IGNORE_LIST), replacementsMap, toCheckNewParserOnly)
    }

    private static final String SCRIPT_ZIP_PATH = "${TestUtils.RESOURCES_PATH}/groovy-2.5.0/groovy-2.5.0-SNAPSHOT-20160921-allscripts.zip"
}
