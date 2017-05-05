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

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.syntax.Token

/**
 * Some basic test cases for the new parser
 *
 * @author  <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on    2016/08/14
 */
class GroovyParserTest extends GroovyTestCase {

    void setUp() {}

    void tearDown() {}

    void "test groovy core - Comments"() {
        TestUtils.doTest('core/Comments_01.groovy', [ExpressionStatement]);
        doTestAttachedComments();
    }

    private static doTestAttachedComments() {
        def (newAST, oldAST) = TestUtils.doTest('core/Comments_02.groovy');
        List<ClassNode> classes = new ArrayList<>(newAST.classes).sort { c1, c2 -> c1.name <=> c2.name };
        List<MethodNode> methods = new ArrayList<>(newAST.methods).sort { m1, m2 -> m1.name <=> m2.name };

        assert classes[0].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '')            == '/** * test class Comments */'
        assert classes[0].fields[0].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**     * test Comments.SOME_VAR     */'
        assert classes[0].fields[1].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**     * test Comments.SOME_VAR2     */'
        assert classes[0].fields[2].nodeMetaData[GroovydocManager.DOC_COMMENT] == null
        assert classes[0].fields[3].nodeMetaData[GroovydocManager.DOC_COMMENT] == null
        assert classes[0].declaredConstructors[0].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/**     * test Comments.constructor1     */'
        assert classes[0].methods[0].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/**     * test Comments.m1     */'
        assert classes[0].methods[1].nodeMetaData[GroovydocManager.DOC_COMMENT] == null
        assert classes[0].methods[2].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/**     * test Comments.m3     */'

        assert classes[1].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '')            == '/**     * test class InnerClazz     */'
        assert classes[1].fields[0].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**         * test InnerClazz.SOME_VAR3         */'
        assert classes[1].fields[1].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**         * test InnerClazz.SOME_VAR4         */'
        assert classes[1].methods[0].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/**         * test Comments.m4         */'
        assert classes[1].methods[1].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/**         * test Comments.m5         */'

        assert classes[2].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '')            == '/**     * test class InnerEnum     */'
        assert classes[2].fields[0].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**         * InnerEnum.NEW         */'
        assert classes[2].fields[1].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '')  == '/**         * InnerEnum.OLD         */'

        assert classes[3].nodeMetaData[GroovydocManager.DOC_COMMENT] == null

        assert classes[4].fields[0].nodeMetaData[GroovydocManager.DOC_COMMENT] == null

        assert classes[5].nodeMetaData[GroovydocManager.DOC_COMMENT] == null

        assert methods[0].nodeMetaData[GroovydocManager.DOC_COMMENT].replaceAll(/\r?\n/, '') == '/** * test someScriptMethod1 */'
        assert methods[1].nodeMetaData[GroovydocManager.DOC_COMMENT] == null
    }

    void "test groovy core - PackageDeclaration"() {
        TestUtils.doTest('core/PackageDeclaration_01.groovy');
        TestUtils.doTest('core/PackageDeclaration_02.groovy');
        TestUtils.doTest('core/PackageDeclaration_03.groovy');
        TestUtils.doTest('core/PackageDeclaration_04.groovy');
        TestUtils.doTest('core/PackageDeclaration_05.groovy');
        TestUtils.doTest('core/PackageDeclaration_06.groovy');
    }

    void "test groovy core - ImportDeclaration"() {
        TestUtils.doTest('core/ImportDeclaration_01.groovy');
        TestUtils.doTest('core/ImportDeclaration_02.groovy');
        TestUtils.doTest('core/ImportDeclaration_03.groovy');
        TestUtils.doTest('core/ImportDeclaration_04.groovy');
        TestUtils.doTest('core/ImportDeclaration_05.groovy');
        TestUtils.doTest('core/ImportDeclaration_06.groovy');
        TestUtils.doTest('core/ImportDeclaration_07.groovy');
        TestUtils.doTest('core/ImportDeclaration_08.groovy');
    }

    void "test groovy core - Annotation"() {
        TestUtils.doTest('core/Annotation_01.groovy');
        TestUtils.doTest('core/Annotation_02.groovy');
        TestUtils.doTest('core/Annotation_03.groovy');
        TestUtils.doTest('core/Annotation_04.groovy');
        TestUtils.doTest('core/Annotation_05.groovy');
        TestUtils.doTest('core/Annotation_06.groovy');
        TestUtils.doTest('core/Annotation_07.groovy');
        TestUtils.doTest('core/Annotation_08.groovy');
        TestUtils.doTest('core/Annotation_09.groovy');
        TestUtils.doRunAndTest('core/Annotation_10x.groovy');
    }

    void "test groovy core - Literal"() {
        TestUtils.doTest('core/Literal_01.groovy');
        TestUtils.doTest('core/Literal_02.groovy', [ExpressionStatement]);
        TestUtils.doTest('core/Literal_03.groovy');
    }

    void "test groovy core - GString"() {
        TestUtils.doTest('core/GString_01.groovy');
        TestUtils.doTest('core/GString_02.groovy');
        TestUtils.doTest('core/GString_03.groovy');
        TestUtils.doTest('core/GString_04.groovy');
        TestUtils.doTest('core/GString_05.groovy');
        TestUtils.doTest('core/GString_06.groovy');
    }

    void "test groovy core - Closure"() {
        TestUtils.doTest('core/Closure_01.groovy');
        TestUtils.doTest('core/Closure_02.groovy');
        TestUtils.doTest('core/Closure_03.groovy');
        TestUtils.doTest('core/Closure_04.groovy');
        TestUtils.doTest('core/Closure_05.groovy', [Parameter]);
        TestUtils.doTest('core/Closure_06.groovy', [Parameter]);
        TestUtils.doTest('core/Closure_07.groovy', [Parameter]);
        TestUtils.doTest('core/Closure_08.groovy', [Parameter]);
        TestUtils.doTest('core/Closure_09.groovy', [Parameter]);
        TestUtils.doTest('core/Closure_10.groovy', [Parameter]);
    }

    void "test groovy core - Lambda"() {
        TestUtils.doRunAndTest('core/Lambda_01x.groovy');
    }

    void "test groovy core - MethodReference"() {
        TestUtils.doRunAndTest('core/MethodReference_01x.groovy');
    }

    void "test groovy core - MethodPointer"() {
        TestUtils.doRunAndTest('core/MethodPointer_01x.groovy');
    }

    void "test groovy core - ElvisAssignment"() {
        TestUtils.doRunAndTest('core/ElvisAssignment_01x.groovy');
    }

    void "test groovy core - List"() {
        TestUtils.doTest('core/List_01.groovy');
    }

    void "test groovy core - Map"() {
        TestUtils.doTest('core/Map_01.groovy');
    }

    void "test groovy core - Expression"() {
        TestUtils.doTest('core/Expression_01.groovy');
        TestUtils.doTest('core/Expression_02.groovy');
        TestUtils.doTest('core/Expression_03.groovy');
        TestUtils.doTest('core/Expression_04.groovy');
        TestUtils.doTest('core/Expression_05.groovy');
        TestUtils.doTest('core/Expression_06.groovy');
        TestUtils.doTest('core/Expression_07.groovy');
        TestUtils.doTest('core/Expression_08.groovy');
        TestUtils.doTest('core/Expression_09.groovy');
        TestUtils.doTest('core/Expression_10.groovy');
        TestUtils.doTest('core/Expression_11.groovy');
        TestUtils.doTest('core/Expression_12.groovy');
        TestUtils.doTest('core/Expression_13.groovy');
        TestUtils.doTest('core/Expression_14.groovy');
        TestUtils.doTest('core/Expression_15.groovy');
        TestUtils.doTest('core/Expression_16.groovy', [Parameter, ExpressionStatement]);
        TestUtils.doTest('core/Expression_17.groovy');
        TestUtils.doTest('core/Expression_18.groovy');
        TestUtils.doTest('core/Expression_19.groovy');
        TestUtils.doTest('core/Expression_20.groovy');
        TestUtils.doRunAndTest('core/Expression_21x.groovy');
        TestUtils.doTest('core/Expression_22x.groovy');
        TestUtils.doRunAndTest('core/Expression_22x.groovy');
        TestUtils.doRunAndTest('core/Expression_23x.groovy');
    }

    void "test groovy core - IdenticalOp"() {
        TestUtils.doRunAndTest('core/IdenticalOp_01x.groovy');
    }

    void "test groovy core - Assert"() {
        TestUtils.doTest('core/Assert_01.groovy');
        TestUtils.doRunAndTest('core/Assert_02x.groovy');
        TestUtils.doRunAndTest('core/Assert_03x.groovy');
    }

    void "test groovy core - IfElse"() {
        TestUtils.doTest('core/IfElse_01.groovy', [AssertStatement]);
    }

    void "test groovy core - For"() {
        TestUtils.doTest('core/For_01.groovy', [AssertStatement]);
        TestUtils.doTest('core/For_02.groovy');
        TestUtils.doTest('core/For_03.groovy');
        TestUtils.doRunAndTest('core/For_04x.groovy');
        TestUtils.doRunAndTest('core/For_05x.groovy');
    }

    void "test groovy core - While"() {
        TestUtils.doTest('core/While_01.groovy');
        TestUtils.doRunAndTest('core/While_02x.groovy');
    }

    void "test groovy core - CodeBlock"() {
        TestUtils.doRunAndTest('core/CodeBlock_01x.groovy');
    }

    void "test groovy core - DoWhile"() {
        TestUtils.doRunAndTest('core/DoWhile_01x.groovy');
        TestUtils.doRunAndTest('core/DoWhile_02x.groovy');
        TestUtils.doRunAndTest('core/DoWhile_03x.groovy');
        TestUtils.doRunAndTest('core/DoWhile_04x.groovy');
    }


    void "test groovy core - TryCatch"() {
        TestUtils.doTest('core/TryCatch_01.groovy');
    }

    void "test groovy core - TryWithResources"() {
        TestUtils.doRunAndTest('core/TryWithResources_01x.groovy');
    }

    void "test groovy core - SafeIndex"() {
        TestUtils.doRunAndTest('core/SafeIndex_01x.groovy');
        TestUtils.doRunAndTest('core/SafeIndex_02x.groovy');
        TestUtils.doRunAndTest('core/SafeIndex_03x.groovy');
    }

    void "test groovy core - NegativeRelationalOperators"() {
        TestUtils.doRunAndTest('core/NegativeRelationalOperators_01x.groovy');
        TestUtils.doRunAndTest('core/NegativeRelationalOperators_02x.groovy');
    }

    void "test groovy core - DefaultMethod"() {
        TestUtils.doRunAndTest('core/DefaultMethod_01x.groovy');
        TestUtils.doRunAndTest('core/DefaultMethod_02x.groovy');
    }


    void "test groovy core - Switch"() {
        TestUtils.doTest('core/Switch_01.groovy');
    }

    void "test groovy core - Synchronized"() {
        TestUtils.doTest('core/Synchronized_01.groovy');
    }

    void "test groovy core - Return"() {
        TestUtils.doTest('core/Return_01.groovy');
    }

    void "test groovy core - Throw"() {
        TestUtils.doTest('core/Throw_01.groovy');
    }

    void "test groovy core - Label"() {
        TestUtils.doTest('core/Label_01.groovy');
    }

    void "test groovy core - LocalVariableDeclaration"() {
        TestUtils.doTest('core/LocalVariableDeclaration_01.groovy', [Token]); // [class org.codehaus.groovy.syntax.Token][startLine]:: 9 != 8
    }

    void "test groovy core - MethodDeclaration"() {
        TestUtils.doTest('core/MethodDeclaration_01.groovy');
        TestUtils.doTest('core/MethodDeclaration_02.groovy');
    }

    void "test groovy core - ClassDeclaration"() {
        TestUtils.doTest('core/ClassDeclaration_01.groovy');
        TestUtils.doTest('core/ClassDeclaration_02.groovy');
        TestUtils.doTest('core/ClassDeclaration_03.groovy');
        TestUtils.doTest('core/ClassDeclaration_04.groovy', [PropertyNode, FieldNode]);
        TestUtils.doTest('core/ClassDeclaration_05.groovy', [ExpressionStatement]);
        TestUtils.doTest('core/ClassDeclaration_06.groovy');
        TestUtils.doTest('core/ClassDeclaration_07.groovy');
    }

    void "test groovy core - InterfaceDeclaration"() {
        TestUtils.doTest('core/InterfaceDeclaration_01.groovy');
        TestUtils.doTest('core/InterfaceDeclaration_02.groovy');
        TestUtils.doTest('core/InterfaceDeclaration_03.groovy');
    }

    void "test groovy core - EnumDeclaration"() {
        TestUtils.doTest('core/EnumDeclaration_01.groovy');
        TestUtils.doTest('core/EnumDeclaration_02.groovy', [ExpressionStatement]);
        TestUtils.doTest('core/EnumDeclaration_03.groovy');
        TestUtils.doTest('core/EnumDeclaration_04.groovy');
        TestUtils.doTest('core/EnumDeclaration_05.groovy');
    }

    void "test groovy core - TraitDeclaration"() {
        TestUtils.doTest('core/TraitDeclaration_01.groovy');
        TestUtils.doTest('core/TraitDeclaration_02.groovy');
        TestUtils.doTest('core/TraitDeclaration_03.groovy');
        TestUtils.doTest('core/TraitDeclaration_04.groovy', [PropertyNode, FieldNode]);
        TestUtils.doTest('core/TraitDeclaration_05.groovy');
    }

    void "test groovy core - AnnotationDeclaration"() {
        TestUtils.doTest('core/AnnotationDeclaration_01.groovy');
    }

    void "test groovy core - Command"() {
        TestUtils.doTest('core/Command_01.groovy');
        TestUtils.doTest('core/Command_02.groovy');
        TestUtils.doTest('core/Command_03.groovy', [ExpressionStatement, Parameter]);
        TestUtils.doTest('core/Command_04.groovy', [ExpressionStatement]);
        TestUtils.doTest('core/Command_05.groovy');
        TestUtils.doRunAndTest('core/Command_06x.groovy')
    }

    void "test groovy core - Unicode"() {
        TestUtils.doTest('core/Unicode_01.groovy');
    }

    void "test groovy core - BreakingChanges"() {
        TestUtils.doRunAndTest('core/BreakingChange_01x.groovy');
        TestUtils.doRunAndTest('core/BreakingChange_02x.groovy');
        TestUtils.doRunAndTest('core/BreakingChange_03x.groovy');
        TestUtils.doRunAndTest('core/BreakingChange_04x.groovy');
    }

    void "test groovy core - Array"() {
        TestUtils.doRunAndTest('core/Array_01x.groovy');
    }

    void "test groovy core - Groovydoc"() {
        TestUtils.doRunAndTest('core/Groovydoc_01x.groovy');
    }

    void "test groovy core - Script"() {
        TestUtils.doRunAndTest('core/Script_01x.groovy');
    }

    void "test groovy core - BUG"() {
        TestUtils.doRunAndTest('bugs/BUG-GROOVY-4757.groovy');
        TestUtils.doRunAndTest('bugs/GROOVY-3898.groovy');
        TestUtils.doRunAndTest('bugs/BUG-GROOVY-5652.groovy');
        TestUtils.doRunAndTest('bugs/BUG-GROOVY-4762.groovy');
        TestUtils.doRunAndTest('bugs/BUG-GROOVY-4438.groovy');
        TestUtils.doRunAndTest('bugs/BUG-GROOVY-6038.groovy');
        TestUtils.doRunAndTest('bugs/BUG-GROOVY-2324.groovy');
    }
}
