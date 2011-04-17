/*
 * Copyright 2008-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform;

import groovy.transform.EqualsAndHashCode;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.util.HashCodeHelper;

import java.util.List;

import static org.codehaus.groovy.transform.AbstractASTTransformUtil.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class EqualsAndHashCodeASTTransformation extends AbstractASTTransformation {
    static final Class MY_CLASS = EqualsAndHashCode.class;
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode HASHUTIL_TYPE = ClassHelper.make(HashCodeHelper.class);
    private static final Token ASSIGN = Token.newSymbol(Types.ASSIGN, -1, -1);
    private static final ClassNode OBJECT_TYPE = new ClassNode(Object.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            checkNotInterface(cNode, MY_TYPE_NAME);
            boolean callSuper = memberHasValue(anno, "callSuper", true);
            if (callSuper && cNode.getSuperClass().getName().equals("java.lang.Object")) {
                addError("Error during " + MY_TYPE_NAME + " processing: callSuper=true but '" + cNode.getName() + "' has no super class.", anno);
            }
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            List<String> excludes = tokenize((String) getMemberValue(anno, "excludes"));
            createHashCode(cNode, false, includeFields, callSuper, excludes);
            createEquals(cNode, includeFields, callSuper, excludes);
        }
    }

    public static void createHashCode(ClassNode cNode, boolean cacheResult, boolean includeFields, boolean callSuper, List<String> excludes) {
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingHashCode = hasDeclaredMethod(cNode, "hashCode", 0);
        if (hasExistingHashCode && hasDeclaredMethod(cNode, "_hashCode", 0)) return;

        final BlockStatement body = new BlockStatement();
        List<FieldNode> list = getInstancePropertyFields(cNode);
        if (includeFields) {
            list.addAll(getInstanceNonPropertyFields(cNode));
        }
        if (cacheResult) {
            final FieldNode hashField = cNode.addField("$hash$code", ACC_PRIVATE | ACC_SYNTHETIC, ClassHelper.int_TYPE, null);
            final Expression hash = new VariableExpression(hashField);
            body.addStatement(new IfStatement(
                    isZeroExpr(hash),
                    calculateHashStatements(hash, list, callSuper, excludes),
                    new EmptyStatement()
            ));
            body.addStatement(new ReturnStatement(hash));
        } else {
            body.addStatement(calculateHashStatements(null, list, callSuper, excludes));
        }

        cNode.addMethod(new MethodNode(hasExistingHashCode ? "_hashCode" : "hashCode", hasExistingHashCode ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body));
    }

    private static Statement calculateHashStatements(Expression hash, List<FieldNode> list, boolean callSuper, List<String> excludes) {
        final BlockStatement body = new BlockStatement();
        // def _result = HashCodeHelper.initHash()
        final Expression result = new VariableExpression("_result");
        final Expression init = new StaticMethodCallExpression(HASHUTIL_TYPE, "initHash", MethodCallExpression.NO_ARGUMENTS);
        body.addStatement(new ExpressionStatement(new DeclarationExpression(result, ASSIGN, init)));

        for (FieldNode fNode : list) {
            if (excludes.contains(fNode.getName()) || fNode.getName().contains("$")) continue;
            // _result = HashCodeHelper.updateHash(_result, field)
            final Expression fieldExpr = new VariableExpression(fNode);
            final Expression args = new TupleExpression(result, fieldExpr);
            final Expression current = new StaticMethodCallExpression(HASHUTIL_TYPE, "updateHash", args);
            body.addStatement(assignStatement(result, current));
        }
        if (callSuper) {
            // _result = HashCodeHelper.updateHash(_result, super.hashCode())
            final Expression args = new TupleExpression(result, new MethodCallExpression(VariableExpression.SUPER_EXPRESSION, "hashCode", MethodCallExpression.NO_ARGUMENTS));
            final Expression current = new StaticMethodCallExpression(HASHUTIL_TYPE, "updateHash", args);
            body.addStatement(assignStatement(result, current));
        }
        // $hash$code = _result
        if (hash != null) {
            body.addStatement(assignStatement(hash, result));
        } else {
            body.addStatement(new ReturnStatement(result));
        }
        return body;
    }

    public static void createEquals(ClassNode cNode, boolean includeFields, boolean callSuper, List<String> excludes) {
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingEquals = hasDeclaredMethod(cNode, "equals", 1);
        if (hasExistingEquals && hasDeclaredMethod(cNode, "_equals", 1)) return;

        final BlockStatement body = new BlockStatement();
        Expression other = new VariableExpression("other");

        // some short circuit cases for efficiency
        body.addStatement(returnFalseIfNull(other));
        body.addStatement(returnFalseIfWrongType(cNode, other));
        body.addStatement(returnTrueIfIdentical(VariableExpression.THIS_EXPRESSION, other));

        body.addStatement(new ExpressionStatement(new BinaryExpression(other, ASSIGN, new CastExpression(cNode, other))));

        List<FieldNode> list = getInstancePropertyFields(cNode);
        if (includeFields) {
            list.addAll(getInstanceNonPropertyFields(cNode));
        }
        for (FieldNode fNode : list) {
            if (excludes.contains(fNode.getName()) || fNode.getName().contains("$")) continue;
            body.addStatement(returnFalseIfPropertyNotEqual(fNode, other));
        }
        if (callSuper) {
            Statement result = new IfStatement(
                    isTrueExpr(new MethodCallExpression(VariableExpression.SUPER_EXPRESSION, "equals", other)),
                    new EmptyStatement(),
                    new ReturnStatement(ConstantExpression.FALSE)
            );
            body.addStatement(result);
        }

        // default
        body.addStatement(new ReturnStatement(ConstantExpression.TRUE));

        Parameter[] params = {new Parameter(OBJECT_TYPE, "other")};
        cNode.addMethod(new MethodNode(hasExistingEquals ? "_equals" : "equals", hasExistingEquals ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.boolean_TYPE, params, ClassNode.EMPTY_ARRAY, body));
    }
}
