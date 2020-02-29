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
import org.apache.groovy.parser.antlr4.util.ASTComparatorCategory
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.syntax.Token

import static org.apache.groovy.parser.antlr4.TestUtils.doRunAndTest
import static org.apache.groovy.parser.antlr4.TestUtils.doRunAndTestAntlr4
import static org.apache.groovy.parser.antlr4.TestUtils.doTest

/**
 * Some basic test cases for the new parser
 */
final class GroovyParserTest extends GroovyTestCase {

    void "test groovy core - Comments"() {
        doTest('core/Comments_01.groovy', [ExpressionStatement])
        doTestAttachedComments()
    }

    // java.lang.ClassFormatError: Illegal method name "test IO stream/reader closed by the parser properly" when using Java9
    void "test IO reader closed by the parser properly"() {
        def f = File.createTempFile("Script${System.nanoTime()}", ".groovy")
        f.text = '''
            def a = 123
        '''

        TestUtils.createAntlr4Shell().evaluate(f)

        boolean deleted = f.delete()
        assert deleted: "Failed to delete file: ${f.getAbsolutePath()}"
    }

    // java.lang.ClassFormatError: Illegal method name "test IO stream/reader closed by the parser properly" when using Java9
    void "test reader closed by the compiler properly"() {
        def f = File.createTempFile("Script${System.nanoTime()}", ".groovy")
        f.text = '''
            def a = 123
        '''

        TestUtils.createAntlr4Shell().evaluate(f)

        boolean deleted = f.delete()
        assert deleted: "Failed to delete file: ${f.getAbsolutePath()}"
    }

    private static doTestAttachedComments() {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
        compilerConfiguration.setOptimizationOptions([(CompilerConfiguration.GROOVYDOC): true])
        def (newAST, oldAST) = doTest('core/Comments_02.groovy', ASTComparatorCategory.DEFAULT_CONFIGURATION, compilerConfiguration)
        List<ClassNode> classes = new ArrayList<>(newAST.classes).sort { c1, c2 -> c1.name <=> c2.name }
        List<MethodNode> methods = new ArrayList<>(newAST.methods).sort { m1, m2 -> m1.name <=> m2.name }

        assert classes[0].groovydoc.content.replaceAll(/\r?\n/, '')            == '/** * test class Comments */'
        assert classes[0].fields[0].groovydoc.content.replaceAll(/\r?\n/, '')  == '/**     * test Comments.SOME_VAR     */'
        assert classes[0].fields[1].groovydoc.content.replaceAll(/\r?\n/, '')  == '/**     * test Comments.SOME_VAR2     */'
        assert !classes[0].fields[2].groovydoc.isPresent()
        assert !classes[0].fields[3].groovydoc.isPresent()
        assert classes[0].declaredConstructors[0].groovydoc.content.replaceAll(/\r?\n/, '') == '/**     * test Comments.constructor1     */'
        assert classes[0].methods[0].groovydoc.content.replaceAll(/\r?\n/, '') == '/**     * test Comments.m1     */'
        assert !classes[0].methods[1].groovydoc.isPresent()
        assert classes[0].methods[2].groovydoc.content.replaceAll(/\r?\n/, '') == '/**     * test Comments.m3     */'

        assert classes[1].groovydoc.content.replaceAll(/\r?\n/, '')            == '/**     * test class InnerClazz     */'
        assert classes[1].fields[0].groovydoc.content.replaceAll(/\r?\n/, '')  == '/**         * test InnerClazz.SOME_VAR3         */'
        assert classes[1].fields[1].groovydoc.content.replaceAll(/\r?\n/, '')  == '/**         * test InnerClazz.SOME_VAR4         */'
        assert classes[1].methods[0].groovydoc.content.replaceAll(/\r?\n/, '') == '/**         * test Comments.m4         */'
        assert classes[1].methods[1].groovydoc.content.replaceAll(/\r?\n/, '') == '/**         * test Comments.m5         */'

        assert classes[2].groovydoc.content.replaceAll(/\r?\n/, '')            == '/**     * test class InnerEnum     */'
        assert classes[2].fields[0].groovydoc.content.replaceAll(/\r?\n/, '')  == '/**         * InnerEnum.NEW         */'
        assert classes[2].fields[1].groovydoc.content.replaceAll(/\r?\n/, '')  == '/**         * InnerEnum.OLD         */'

        assert !classes[3].groovydoc.isPresent()

        assert !classes[4].fields[0].groovydoc.isPresent()

        assert !classes[5].groovydoc.isPresent()

        assert methods[0].groovydoc.content.replaceAll(/\r?\n/, '') == '/** * test someScriptMethod1 */'
        assert !methods[1].groovydoc.isPresent()
    }

    void "test groovy core - PackageDeclaration"() {
        doTest('core/PackageDeclaration_01.groovy')
        doTest('core/PackageDeclaration_02.groovy')
        doTest('core/PackageDeclaration_03.groovy')
        doTest('core/PackageDeclaration_04.groovy')
        doTest('core/PackageDeclaration_05.groovy')
        doTest('core/PackageDeclaration_06.groovy')
    }

    void "test groovy core - ImportDeclaration"() {
        doTest('core/ImportDeclaration_01.groovy')
        doTest('core/ImportDeclaration_02.groovy')
        doTest('core/ImportDeclaration_03.groovy')
        doTest('core/ImportDeclaration_04.groovy')
        doTest('core/ImportDeclaration_05.groovy')
        doTest('core/ImportDeclaration_06.groovy')
        doTest('core/ImportDeclaration_07.groovy')
        doTest('core/ImportDeclaration_08.groovy')
    }

    void "test groovy core - Annotation"() {
        doTest('core/Annotation_01.groovy')
        doTest('core/Annotation_02.groovy')
        doTest('core/Annotation_03.groovy')
        doTest('core/Annotation_04.groovy')
        doTest('core/Annotation_05.groovy')
        doTest('core/Annotation_06.groovy')
        doTest('core/Annotation_07.groovy')
        doTest('core/Annotation_08.groovy')
        doTest('core/Annotation_09.groovy')
        doRunAndTestAntlr4('core/Annotation_10x.groovy')
    }

    void "test groovy core - Literal"() {
        doTest('core/Literal_01.groovy')
        doTest('core/Literal_02.groovy', [ExpressionStatement])
        doTest('core/Literal_03.groovy')
    }

    void "test groovy core - GString"() {
        doTest('core/GString_01.groovy')
        doTest('core/GString_02.groovy')
        doTest('core/GString_03.groovy')
        doTest('core/GString_04.groovy')
        doTest('core/GString_05.groovy')
        doTest('core/GString_06.groovy')
    }

    void "test groovy core - Closure"() {
        doTest('core/Closure_01.groovy')
        doTest('core/Closure_02.groovy')
        doTest('core/Closure_03.groovy')
        doTest('core/Closure_04.groovy')
        doTest('core/Closure_05.groovy', [Parameter])
        doTest('core/Closure_06.groovy', [Parameter])
        doTest('core/Closure_07.groovy', [Parameter])
        doTest('core/Closure_08.groovy', [Parameter])
        doTest('core/Closure_09.groovy', [Parameter])
        doTest('core/Closure_10.groovy', [Parameter])
    }

    void "test groovy core - Lambda"() {
        doRunAndTestAntlr4('core/Lambda_01x.groovy')
    }

    void "test groovy core - MethodReference"() {
        doRunAndTestAntlr4('core/MethodReference_01x.groovy')
    }

    void "test groovy core - MethodPointer"() {
        doRunAndTestAntlr4('core/MethodPointer_01x.groovy')
    }

    void "test groovy core - ElvisAssignment"() {
        doRunAndTestAntlr4('core/ElvisAssignment_01x.groovy')
    }

    void "test groovy core - List"() {
        doTest('core/List_01.groovy')
    }

    void "test groovy core - Map"() {
        doTest('core/Map_01.groovy')
        doRunAndTestAntlr4('core/Map_02x.groovy')
    }

    void "test groovy core - Expression"() {
        doTest('core/Expression_01.groovy')
        doTest('core/Expression_02.groovy')
        doTest('core/Expression_03.groovy')
        doTest('core/Expression_04.groovy')
        doTest('core/Expression_05.groovy')
        doTest('core/Expression_06.groovy')
        doTest('core/Expression_07.groovy')
        doTest('core/Expression_08.groovy')
        doTest('core/Expression_09.groovy')
        doTest('core/Expression_10.groovy')
        doTest('core/Expression_11.groovy')
        doTest('core/Expression_12.groovy')
        doTest('core/Expression_13.groovy')
        doTest('core/Expression_14.groovy')
        doTest('core/Expression_15.groovy')
        doTest('core/Expression_16.groovy', [Parameter, ExpressionStatement])
        doTest('core/Expression_17.groovy')
        doTest('core/Expression_18.groovy')
        doTest('core/Expression_19.groovy')
        doTest('core/Expression_20.groovy')
        doRunAndTestAntlr4('core/Expression_21x.groovy')
        doTest('core/Expression_22x.groovy')
        doRunAndTestAntlr4('core/Expression_22x.groovy')
        doRunAndTestAntlr4('core/Expression_23x.groovy')
    }

    void "test groovy core - IdenticalOp"() {
        doRunAndTestAntlr4('core/IdenticalOp_01x.groovy')
    }

    void "test groovy core - Assert"() {
        doTest('core/Assert_01.groovy')
        doRunAndTestAntlr4('core/Assert_02x.groovy')
        doRunAndTestAntlr4('core/Assert_03x.groovy')
        doRunAndTestAntlr4('core/Assert_04x.groovy')
    }

    void "test groovy core - IfElse"() {
        doTest('core/IfElse_01.groovy', [AssertStatement])
    }

    void "test groovy core - For"() {
        doTest('core/For_01.groovy', [AssertStatement])
        doTest('core/For_02.groovy')
        doTest('core/For_03.groovy')
        doRunAndTestAntlr4('core/For_04x.groovy')
        doRunAndTestAntlr4('core/For_05x.groovy')
    }

    void "test groovy core - While"() {
        doTest('core/While_01.groovy')
        doRunAndTestAntlr4('core/While_02x.groovy')
    }

    void "test groovy core - CodeBlock"() {
        doRunAndTestAntlr4('core/CodeBlock_01x.groovy')
    }

    void "test groovy core - DoWhile"() {
        doRunAndTestAntlr4('core/DoWhile_01x.groovy')
        doRunAndTestAntlr4('core/DoWhile_02x.groovy')
        doRunAndTestAntlr4('core/DoWhile_03x.groovy')
        doRunAndTestAntlr4('core/DoWhile_04x.groovy')
    }


    void "test groovy core - TryCatch"() {
        doTest('core/TryCatch_01.groovy')
    }

    void "test groovy core - TryWithResources"() {
        doRunAndTestAntlr4('core/TryWithResources_01x.groovy')
        doRunAndTestAntlr4('core/TryWithResources_02x.groovy')
    }

    void "test groovy core - SafeIndex"() {
        doRunAndTestAntlr4('core/SafeIndex_01x.groovy')
        doRunAndTestAntlr4('core/SafeIndex_02x.groovy')
        doRunAndTestAntlr4('core/SafeIndex_03x.groovy')
        doRunAndTestAntlr4('core/SafeIndex_04x.groovy')
    }

    void "test groovy core - NegativeRelationalOperators"() {
        doRunAndTestAntlr4('core/NegativeRelationalOperators_01x.groovy')
        doRunAndTestAntlr4('core/NegativeRelationalOperators_02x.groovy')
    }

    void "test groovy core - DefaultMethod"() {
        doRunAndTestAntlr4('core/DefaultMethod_01x.groovy')
        doRunAndTestAntlr4('core/DefaultMethod_02x.groovy')
    }


    void "test groovy core - Switch"() {
        doTest('core/Switch_01.groovy')
    }

    void "test groovy core - Synchronized"() {
        doTest('core/Synchronized_01.groovy')
    }

    void "test groovy core - Break"() {
        doTest('core/Break_01.groovy')
    }

    void "test groovy core - Return"() {
        doTest('core/Return_01.groovy')
    }

    void "test groovy core - Throw"() {
        doTest('core/Throw_01.groovy')
    }

    void "test groovy core - Label"() {
        doTest('core/Label_01.groovy')
    }

    void "test groovy core - LocalVariableDeclaration"() {
        doTest('core/LocalVariableDeclaration_01.groovy', [Token]) // [class org.codehaus.groovy.syntax.Token][startLine]:: 9 != 8
        doRunAndTestAntlr4('core/LocalVariableDeclaration_02x.groovy')

    }

    void "test groovy core - MethodDeclaration"() {
        doTest('core/MethodDeclaration_01.groovy')
        doTest('core/MethodDeclaration_02.groovy')
    }

    void "test groovy core - ClassDeclaration"() {
        doTest('core/ClassDeclaration_01.groovy')
        doTest('core/ClassDeclaration_02.groovy')
        doTest('core/ClassDeclaration_03.groovy')
        doTest('core/ClassDeclaration_04.groovy', [PropertyNode, FieldNode])
        doTest('core/ClassDeclaration_05.groovy', [ExpressionStatement])
        doTest('core/ClassDeclaration_06.groovy')
        doTest('core/ClassDeclaration_07.groovy')
    }

    void "test groovy core - InterfaceDeclaration"() {
        doTest('core/InterfaceDeclaration_01.groovy')
        doTest('core/InterfaceDeclaration_02.groovy', [PropertyNode, FieldNode])
        doTest('core/InterfaceDeclaration_03.groovy')
    }

    void "test groovy core - EnumDeclaration"() {
        doTest('core/EnumDeclaration_01.groovy', [PropertyNode, FieldNode])
        doTest('core/EnumDeclaration_02.groovy', [ExpressionStatement])
        doTest('core/EnumDeclaration_03.groovy')
        doTest('core/EnumDeclaration_04.groovy')
        doTest('core/EnumDeclaration_05.groovy')
    }

    void "test groovy core - TraitDeclaration"() {
        doTest('core/TraitDeclaration_01.groovy')
        doTest('core/TraitDeclaration_02.groovy')
        doTest('core/TraitDeclaration_03.groovy')
        doTest('core/TraitDeclaration_04.groovy', [PropertyNode, FieldNode])
        doTest('core/TraitDeclaration_05.groovy')
    }

    void "test groovy core - AnnotationDeclaration"() {
        doTest('core/AnnotationDeclaration_01.groovy')
    }

    void "test groovy core - Command"() {
        doTest('core/Command_01.groovy')
        doTest('core/Command_02.groovy')
        doTest('core/Command_03.groovy', [ExpressionStatement, Parameter])
        doTest('core/Command_04.groovy', [ExpressionStatement])
        doTest('core/Command_05.groovy')
        doRunAndTestAntlr4('core/Command_06x.groovy')
    }

    /*
    void "test groovy core - Unicode"() {
        doTest('core/Unicode_01.groovy')
    }
    */

    void "test groovy core - BreakingChanges"() {
        doRunAndTestAntlr4('core/BreakingChange_01x.groovy')
        doRunAndTestAntlr4('core/BreakingChange_02x.groovy')
        doRunAndTestAntlr4('core/BreakingChange_03x.groovy')
        doRunAndTestAntlr4('core/BreakingChange_04x.groovy')
    }

    void "test groovy core - Array"() {
        doRunAndTestAntlr4('core/Array_01x.groovy')
    }

    void "test groovy core - Groovydoc"() {
        CompilerConfiguration compilerConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
        compilerConfiguration.setOptimizationOptions([(CompilerConfiguration.RUNTIME_GROOVYDOC): true])

        doRunAndTestAntlr4('core/Groovydoc_01x.groovy', compilerConfiguration)
    }

    void "test groovy core - Script"() {
        doRunAndTestAntlr4('core/Script_01x.groovy')
    }

    void "test groovy core - FieldDeclaration"() {
        doRunAndTestAntlr4('core/FieldDeclaration_01x.groovy')
    }

    void "test groovy core - Number"() {
        doRunAndTestAntlr4('core/Number_01x.groovy')
    }

    void "test groovy core - SafeChainOperator"() {
        doRunAndTestAntlr4('core/SafeChainOperator.groovy')
    }

    void "test groovy core - var"() {
        doRunAndTestAntlr4('core/Var_01x.groovy');
    }

    void "test groovy core - String"() {
        doRunAndTestAntlr4('core/String_01x.groovy');
    }

    void "test groovy core - NonStaticClass"() {
        doRunAndTestAntlr4('core/NonStaticClass_01x.groovy');
    }

    void "test groovy core - BUG"() {
        doRunAndTestAntlr4('bugs/BUG-GROOVY-4757.groovy')
        doRunAndTestAntlr4('bugs/BUG-GROOVY-5652.groovy')
        doRunAndTestAntlr4('bugs/BUG-GROOVY-4762.groovy')
        doRunAndTestAntlr4('bugs/BUG-GROOVY-4438.groovy')
        doRunAndTestAntlr4('bugs/BUG-GROOVY-6038.groovy')
        doRunAndTestAntlr4('bugs/BUG-GROOVY-2324.groovy')
        doTest('bugs/BUG-GROOVY-8161.groovy')
        doRunAndTestAntlr4('bugs/GROOVY-3898.groovy')
        doRunAndTestAntlr4('bugs/GROOVY-8228.groovy')
        doRunAndTestAntlr4('bugs/BUG-GROOVY-8311.groovy')
        doRunAndTest('bugs/BUG-GROOVY-8426.groovy')
        doTest('bugs/BUG-GROOVY-8511.groovy')
        doRunAndTestAntlr4('bugs/BUG-GROOVY-8613.groovy')
        doTest('bugs/BUG-GROOVY-8641.groovy')
        doTest('bugs/BUG-GROOVY-8913.groovy')
        doRunAndTestAntlr4('bugs/BUG-GROOVY-8991.groovy')
        doTest('bugs/BUG-GROOVY-9399.groovy')
    }

    void "test groovy core - GROOVY-9427"() {
        doTest('bugs/BUG-GROOVY-9427.groovy');
    }
}
