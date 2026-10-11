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
package org.apache.groovy.lsp.internal.compile

import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.LambdaExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.junit.jupiter.api.Test

import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_STATIC

final class ScriptNamesTest {

    @Test
    void ofNullModuleIsEmpty() {
        assert ScriptNames.of(null).isEmpty()
    }

    @Test
    void scriptLevelMethodRejectsSyntheticAndGeneratedNames() {
        assert !ScriptNames.scriptLevelMethod(null)
        def synthetic = new MethodNode('ping', ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        synthetic.synthetic = true
        assert !ScriptNames.scriptLevelMethod(synthetic)
        assert !ScriptNames.scriptLevelMethod(new MethodNode(null, ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null))
        assert !ScriptNames.scriptLevelMethod(new MethodNode('', ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null))
        assert !ScriptNames.scriptLevelMethod(new MethodNode('<init>', ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null))
        assert !ScriptNames.scriptLevelMethod(new MethodNode('<clinit>', ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null))
        assert !ScriptNames.scriptLevelMethod(new MethodNode('run', ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null))
        assert ScriptNames.scriptLevelMethod(new MethodNode('run', ACC_PUBLIC, ClassHelper.VOID_TYPE,
                [new Parameter(ClassHelper.OBJECT_TYPE, 'args')] as Parameter[], ClassNode.EMPTY_ARRAY, null))
        assert ScriptNames.scriptLevelMethod(new MethodNode('main', ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null))
        def generatedMain = new MethodNode('main', ACC_PUBLIC | ACC_STATIC, ClassHelper.VOID_TYPE,
                [new Parameter(ClassHelper.STRING_TYPE.makeArray(), 'args')] as Parameter[], ClassNode.EMPTY_ARRAY, null)
        assert !ScriptNames.scriptLevelMethod(generatedMain)
        assert ScriptNames.scriptLevelMethod(new MethodNode('main', ACC_PUBLIC | ACC_STATIC, ClassHelper.VOID_TYPE,
                [new Parameter(null, 'args')] as Parameter[], ClassNode.EMPTY_ARRAY, null))
        assert ScriptNames.scriptLevelMethod(new MethodNode('main', ACC_PUBLIC | ACC_STATIC, ClassHelper.VOID_TYPE,
                [new Parameter(ClassHelper.STRING_TYPE, 'args')] as Parameter[], ClassNode.EMPTY_ARRAY, null))
    }

    @Test
    void addScriptAssignmentsSkipsMissingRunAndNullCode() {
        def collected = new LinkedHashSet<String>()
        ScriptNames.addScriptAssignments(null, collected)
        ScriptNames.addScriptAssignments(new ClassNode('NoRun', ACC_PUBLIC, ClassHelper.OBJECT_TYPE), collected)
        assert collected.isEmpty()
        def script = new ClassNode('EmptyRun', ACC_PUBLIC, ClassHelper.SCRIPT_TYPE)
        script.addMethod(new MethodNode('run', ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null))
        ScriptNames.addScriptAssignments(script, collected)
        assert collected.isEmpty()
        ScriptNames.addScriptAssignments(script, null)
    }

    @Test
    void assignmentsVisitClosuresLambdasAndBlankNames() {
        def names = new LinkedHashSet<String>()
        def module = new ModuleNode((CompileUnit) null)
        module.addClass(new ClassNode('', ACC_PUBLIC, ClassHelper.OBJECT_TYPE))
        assert ScriptNames.of(module).isEmpty()

        def script = new ClassNode('Walk', ACC_PUBLIC, ClassHelper.SCRIPT_TYPE)
        def block = new BlockStatement()
        block.addStatement(expression(assign('c', new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE))))
        block.addStatement(expression(assign('l', new LambdaExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE))))
        block.addStatement(expression(new BinaryExpression(new ConstantExpression(1), Token.newSymbol(Types.PLUS, -1, -1), new ConstantExpression(2))))
        def noOperation = new BinaryExpression(new VariableExpression('q'), Token.newSymbol(Types.ASSIGN, -1, -1), ConstantExpression.NULL)
        BinaryExpression.getDeclaredField('operation').with {
            accessible = true
            set(noOperation, null)
        }
        block.addStatement(expression(noOperation))
        block.addStatement(expression(new DeclarationExpression(new VariableExpression(''), Token.newSymbol(Types.ASSIGN, -1, -1), ConstantExpression.NULL)))
        block.addStatement(expression(new DeclarationExpression(new VariableExpression((String) null), Token.newSymbol(Types.ASSIGN, -1, -1), ConstantExpression.NULL)))
        block.addStatement(expression(new DeclarationExpression(new TupleExpression([new VariableExpression('t')]), Token.newSymbol(Types.ASSIGN, -1, -1), new TupleExpression())))
        script.addMethod(new MethodNode('run', ACC_PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block))
        ScriptNames.addScriptAssignments(script, names)
        assert names.contains('c')
        assert names.contains('l')
        assert names.contains('t')
        assert !names.contains('')
    }

    private static ExpressionStatement expression(Expression expression) {
        new ExpressionStatement(expression)
    }

    private static BinaryExpression assign(String name, Expression right) {
        new BinaryExpression(new VariableExpression(name), Token.newSymbol(Types.ASSIGN, -1, -1), right)
    }
}
