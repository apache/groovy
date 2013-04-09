/*
 * Copyright 2008-2013 the original author or authors.
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
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.util.HashCodeHelper;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.transform.AbstractASTTransformUtil.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class EqualsAndHashCodeASTTransformation extends AbstractASTTransformation {
    static final Class MY_CLASS = EqualsAndHashCode.class;
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode HASHUTIL_TYPE = ClassHelper.make(HashCodeHelper.class);
    private static final Token ASSIGN = Token.newSymbol(Types.ASSIGN, -1, -1);
    private static final Token LOGICAL_OR = Token.newSymbol(Types.LOGICAL_OR, -1, -1);
    private static final Token LOGICAL_AND = Token.newSymbol(Types.LOGICAL_AND, -1, -1);
    private static final ClassNode OBJECT_TYPE = ClassHelper.make(Object.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            checkNotInterface(cNode, MY_TYPE_NAME);
            boolean callSuper = memberHasValue(anno, "callSuper", true);
            boolean cacheHashCode = memberHasValue(anno, "cache", true);
            boolean useCanEqual = !memberHasValue(anno, "useCanEqual", false);
            if (callSuper && cNode.getSuperClass().getName().equals("java.lang.Object")) {
                addError("Error during " + MY_TYPE_NAME + " processing: callSuper=true but '" + cNode.getName() + "' has no super class.", anno);
            }
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            List<String> excludes = getMemberList(anno, "excludes");
            List<String> includes = getMemberList(anno, "includes");
            if (hasAnnotation(cNode, CanonicalASTTransformation.MY_TYPE)) {
                AnnotationNode canonical = cNode.getAnnotations(CanonicalASTTransformation.MY_TYPE).get(0);
                if (excludes == null || excludes.isEmpty()) excludes = getMemberList(canonical, "excludes");
                if (includes == null || includes.isEmpty()) includes = getMemberList(canonical, "includes");
            }
            if (includes != null && !includes.isEmpty() && excludes != null && !excludes.isEmpty()) {
                addError("Error during " + MY_TYPE_NAME + " processing: Only one of 'includes' and 'excludes' should be supplied not both.", anno);
            }
            createHashCode(cNode, cacheHashCode, includeFields, callSuper, excludes, includes);
            createEquals(cNode, includeFields, callSuper, useCanEqual, excludes, includes);
        }
    }

    public static void createHashCode(ClassNode cNode, boolean cacheResult, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes) {
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingHashCode = hasDeclaredMethod(cNode, "hashCode", 0);
        if (hasExistingHashCode && hasDeclaredMethod(cNode, "_hashCode", 0)) return;

        final BlockStatement body = new BlockStatement();
        // TODO use pList and fList
        if (cacheResult) {
            final FieldNode hashField = cNode.addField("$hash$code", ACC_PRIVATE | ACC_SYNTHETIC, ClassHelper.int_TYPE, null);
            final Expression hash = new VariableExpression(hashField);
            body.addStatement(new IfStatement(
                    isZeroExpr(hash),
                    calculateHashStatements(cNode, hash, includeFields, callSuper, excludes, includes),
                    new EmptyStatement()
            ));
            body.addStatement(new ReturnStatement(hash));
        } else {
            body.addStatement(calculateHashStatements(cNode, null, includeFields, callSuper, excludes, includes));
        }

        cNode.addMethod(new MethodNode(hasExistingHashCode ? "_hashCode" : "hashCode", hasExistingHashCode ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body));
    }

    private static Statement calculateHashStatements(ClassNode cNode, Expression hash, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes) {
        final List<PropertyNode> pList = getInstanceProperties(cNode);
        final List<FieldNode> fList = new ArrayList<FieldNode>();
        if (includeFields) {
            fList.addAll(getInstanceNonPropertyFields(cNode));
        }
        final BlockStatement body = new BlockStatement();
        // def _result = HashCodeHelper.initHash()
        final Expression result = new VariableExpression("_result");
        final Expression init = new StaticMethodCallExpression(HASHUTIL_TYPE, "initHash", MethodCallExpression.NO_ARGUMENTS);
        body.addStatement(new ExpressionStatement(new DeclarationExpression(result, ASSIGN, init)));

        for (PropertyNode pNode : pList) {
            if (shouldSkip(pNode.getName(), excludes, includes)) continue;
            // _result = HashCodeHelper.updateHash(_result, getProperty()) // plus self-reference checking
            String getterName = "get" + Verifier.capitalize(pNode.getName());
            Expression getter = new MethodCallExpression(VariableExpression.THIS_EXPRESSION, getterName, MethodCallExpression.NO_ARGUMENTS);
            final Expression args = new TupleExpression(result, getter);
            final Expression current = new StaticMethodCallExpression(HASHUTIL_TYPE, "updateHash", args);
            body.addStatement(new IfStatement(identicalExpr(getter, new VariableExpression("this")),
                    EmptyStatement.INSTANCE,
                    assignStatement(result, current)));

        }
        for (FieldNode fNode : fList) {
            if (shouldSkip(fNode.getName(), excludes, includes)) continue;
            // _result = HashCodeHelper.updateHash(_result, field) // plus self-reference checking
            final Expression fieldExpr = new VariableExpression(fNode);
            final Expression args = new TupleExpression(result, fieldExpr);
            final Expression current = new StaticMethodCallExpression(HASHUTIL_TYPE, "updateHash", args);
            body.addStatement(new IfStatement(identicalExpr(fieldExpr, new VariableExpression("this")),
                    EmptyStatement.INSTANCE,
                    assignStatement(result, current)));
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

    private static boolean shouldSkip(String name, List<String> excludes, List<String> includes) {
        return (excludes != null && excludes.contains(name)) || name.contains("$") || (includes != null && !includes.isEmpty() && !includes.contains(name));
    }

    private static void createCanEqual(ClassNode cNode) {
        boolean hasExistingCanEqual = hasDeclaredMethod(cNode, "canEqual", 1);
        if (hasExistingCanEqual && hasDeclaredMethod(cNode, "_canEqual", 1)) return;

        final BlockStatement body = new BlockStatement();
        VariableExpression other = new VariableExpression("other");
        body.addStatement(new ReturnStatement(isInstanceof(cNode, other)));
        Parameter[] params = {new Parameter(OBJECT_TYPE, other.getName())};
        cNode.addMethod(new MethodNode(hasExistingCanEqual ? "_canEqual" : "canEqual", hasExistingCanEqual ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.boolean_TYPE, params, ClassNode.EMPTY_ARRAY, body));

    }

    public static void createEquals(ClassNode cNode, boolean includeFields, boolean callSuper, boolean useCanEqual, List<String> excludes, List<String> includes) {
        if (useCanEqual) createCanEqual(cNode);
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingEquals = hasDeclaredMethod(cNode, "equals", 1);
        if (hasExistingEquals && hasDeclaredMethod(cNode, "_equals", 1)) return;

        final BlockStatement body = new BlockStatement();
        VariableExpression other = new VariableExpression("other");

        // some short circuit cases for efficiency
        body.addStatement(returnFalseIfNull(other));
        body.addStatement(returnTrueIfIdentical(VariableExpression.THIS_EXPRESSION, other));

        if (useCanEqual) {
            body.addStatement(returnFalseIfNotInstanceof(cNode, other));
            body.addStatement(new IfStatement(
                    new BooleanExpression(new MethodCallExpression(other, "canEqual", VariableExpression.THIS_EXPRESSION)),
                    new EmptyStatement(),
                    new ReturnStatement(ConstantExpression.FALSE)
            ));
        } else {
            body.addStatement(returnFalseIfWrongType(cNode, other));
        }

        VariableExpression otherTyped = new VariableExpression("otherTyped");
        body.addStatement(new ExpressionStatement(new DeclarationExpression(otherTyped, ASSIGN, new CastExpression(cNode, other))));

        List<PropertyNode> pList = getInstanceProperties(cNode);
        for (PropertyNode pNode : pList) {
            if (shouldSkip(pNode.getName(), excludes, includes)) continue;
            body.addStatement(
                    new IfStatement(differentPropertyExpr(pNode, otherTyped),
                            new IfStatement(differentSelfRecursivePropertyExpr(pNode, otherTyped),
                                    new ReturnStatement(ConstantExpression.FALSE),
                                    new IfStatement(bothSelfRecursivePropertyExpr(pNode, otherTyped),
                                            EmptyStatement.INSTANCE,
                                            returnFalseIfPropertyNotEqual(pNode, otherTyped))),
                            EmptyStatement.INSTANCE));
        }
        List<FieldNode> fList = new ArrayList<FieldNode>();
        if (includeFields) {
            fList.addAll(getInstanceNonPropertyFields(cNode));
        }
        for (FieldNode fNode : fList) {
            if (shouldSkip(fNode.getName(), excludes, includes)) continue;
            body.addStatement(
                    new IfStatement(differentFieldExpr(fNode, otherTyped),
                            new IfStatement(differentSelfRecursiveFieldExpr(fNode, otherTyped),
                                    new ReturnStatement(ConstantExpression.FALSE),
                                    new IfStatement(bothSelfRecursiveFieldExpr(fNode, otherTyped),
                                            EmptyStatement.INSTANCE,
                                            returnFalseIfFieldNotEqual(fNode, otherTyped))),
                            EmptyStatement.INSTANCE));
        }
        if (callSuper) {
            body.addStatement(new IfStatement(
                    isTrueExpr(new MethodCallExpression(VariableExpression.SUPER_EXPRESSION, "equals", other)),
                    new EmptyStatement(),
                    new ReturnStatement(ConstantExpression.FALSE)
            ));
        }

        // default
        body.addStatement(new ReturnStatement(ConstantExpression.TRUE));

        Parameter[] params = {new Parameter(OBJECT_TYPE, other.getName())};
        cNode.addMethod(new MethodNode(hasExistingEquals ? "_equals" : "equals", hasExistingEquals ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.boolean_TYPE, params, ClassNode.EMPTY_ARRAY, body));
    }

    private static BooleanExpression differentSelfRecursivePropertyExpr(PropertyNode pNode, Expression other) {
        String getterName = "get" + Verifier.capitalize(pNode.getName());
        Expression selfGetter = new MethodCallExpression(new VariableExpression("this"), getterName, MethodCallExpression.NO_ARGUMENTS);
        Expression otherGetter = new MethodCallExpression(other, getterName, MethodCallExpression.NO_ARGUMENTS);
        return new BooleanExpression(new BinaryExpression(new BinaryExpression(
                identicalExpr(selfGetter, new VariableExpression("this")), LOGICAL_AND, differentExpr(otherGetter, other)), LOGICAL_OR, new BinaryExpression(
                differentExpr(selfGetter, new VariableExpression("this")), LOGICAL_AND, identicalExpr(otherGetter, other))));
    }

    private static BooleanExpression bothSelfRecursivePropertyExpr(PropertyNode pNode, Expression other) {
        String getterName = "get" + Verifier.capitalize(pNode.getName());
        Expression selfGetter = new MethodCallExpression(new VariableExpression("this"), getterName, MethodCallExpression.NO_ARGUMENTS);
        Expression otherGetter = new MethodCallExpression(other, getterName, MethodCallExpression.NO_ARGUMENTS);
        return new BooleanExpression(new BinaryExpression(
                identicalExpr(selfGetter, new VariableExpression("this")), LOGICAL_AND, identicalExpr(otherGetter, other)));
    }

    private static BooleanExpression differentSelfRecursiveFieldExpr(FieldNode fNode, Expression other) {
        final Expression fieldExpr = new VariableExpression(fNode);
        final Expression otherExpr = new PropertyExpression(other, fNode.getName());
        return new BooleanExpression(new BinaryExpression(new BinaryExpression(
                identicalExpr(fieldExpr, new VariableExpression("this")), LOGICAL_AND, differentExpr(otherExpr, other)), LOGICAL_OR, new BinaryExpression(
                differentExpr(fieldExpr, new VariableExpression("this")), LOGICAL_AND, identicalExpr(otherExpr, other))));
    }

    private static BooleanExpression bothSelfRecursiveFieldExpr(FieldNode fNode, Expression other) {
        final Expression fieldExpr = new VariableExpression(fNode);
        final Expression otherExpr = new PropertyExpression(other, fNode.getName());
        return new BooleanExpression(new BinaryExpression(
                identicalExpr(fieldExpr, new VariableExpression("this")), LOGICAL_AND, identicalExpr(otherExpr, other)));
    }
}
