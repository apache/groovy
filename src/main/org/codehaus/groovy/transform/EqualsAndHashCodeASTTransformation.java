/*
 * Copyright 2008-2014 the original author or authors.
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
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.util.HashCodeHelper;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.*;
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafe;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class EqualsAndHashCodeASTTransformation extends AbstractASTTransformation {
    static final Class MY_CLASS = EqualsAndHashCode.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode HASHUTIL_TYPE = make(HashCodeHelper.class);
    private static final ClassNode INVOKERHELPER_TYPE = make(InvokerHelper.class);
    private static final ClassNode OBJECT_TYPE = makeClassSafe(Object.class);

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
            checkIncludeExclude(anno, excludes, includes, MY_TYPE_NAME);
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
            final Expression hash = var(hashField);
            body.addStatement(ifS(
                    isZeroX(hash),
                    calculateHashStatements(cNode, hash, includeFields, callSuper, excludes, includes)
            ));
            body.addStatement(returnS(hash));
        } else {
            body.addStatement(calculateHashStatements(cNode, null, includeFields, callSuper, excludes, includes));
        }

        cNode.addMethod(new MethodNode(
                hasExistingHashCode ? "_hashCode" : "hashCode",
                hasExistingHashCode ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.int_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                body));
    }

    private static Statement calculateHashStatements(ClassNode cNode, Expression hash, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes) {
        final List<PropertyNode> pList = getInstanceProperties(cNode);
        final List<FieldNode> fList = new ArrayList<FieldNode>();
        if (includeFields) {
            fList.addAll(getInstanceNonPropertyFields(cNode));
        }
        final BlockStatement body = new BlockStatement();
        // def _result = HashCodeHelper.initHash()
        final Expression result = var("_result");
        body.addStatement(declS(result, callX(HASHUTIL_TYPE, "initHash")));

        for (PropertyNode pNode : pList) {
            if (shouldSkip(pNode.getName(), excludes, includes)) continue;
            // _result = HashCodeHelper.updateHash(_result, getProperty()) // plus self-reference checking
            Expression getter = callX(INVOKERHELPER_TYPE, "getProperty", args(var("this"), constX(pNode.getName())));
            final Expression current = callX(HASHUTIL_TYPE, "updateHash", args(result, getter));
            body.addStatement(ifS(
                    not(identicalX(getter, var("this"))),
                    assignS(result, current)));

        }
        for (FieldNode fNode : fList) {
            if (shouldSkip(fNode.getName(), excludes, includes)) continue;
            // _result = HashCodeHelper.updateHash(_result, field) // plus self-reference checking
            final Expression fieldExpr = var(fNode);
            final Expression current = callX(HASHUTIL_TYPE, "updateHash", args(result, fieldExpr));
            body.addStatement(ifS(
                    not(identicalX(fieldExpr, var("this"))),
                    assignS(result, current)));
        }
        if (callSuper) {
            // _result = HashCodeHelper.updateHash(_result, super.hashCode())
            final Expression current = callX(HASHUTIL_TYPE, "updateHash", args(result, callSuperX("hashCode")));
            body.addStatement(assignS(result, current));
        }
        // $hash$code = _result
        if (hash != null) {
            body.addStatement(assignS(hash, result));
        } else {
            body.addStatement(returnS(result));
        }
        return body;
    }

    private static void createCanEqual(ClassNode cNode) {
        boolean hasExistingCanEqual = hasDeclaredMethod(cNode, "canEqual", 1);
        if (hasExistingCanEqual && hasDeclaredMethod(cNode, "_canEqual", 1)) return;

        final BlockStatement body = new BlockStatement();
        VariableExpression other = var("other");
        body.addStatement(returnS(isInstanceOf(other, cNode)));
        cNode.addMethod(new MethodNode(
                hasExistingCanEqual ? "_canEqual" : "canEqual",
                hasExistingCanEqual ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.boolean_TYPE,
                params(param(OBJECT_TYPE, other.getName())),
                ClassNode.EMPTY_ARRAY,
                body));

    }

    public static void createEquals(ClassNode cNode, boolean includeFields, boolean callSuper, boolean useCanEqual, List<String> excludes, List<String> includes) {
        if (useCanEqual) createCanEqual(cNode);
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingEquals = hasDeclaredMethod(cNode, "equals", 1);
        if (hasExistingEquals && hasDeclaredMethod(cNode, "_equals", 1)) return;

        final BlockStatement body = new BlockStatement();
        VariableExpression other = var("other");

        // some short circuit cases for efficiency
        body.addStatement(ifS(equalsNullX(other), returnS(constX(Boolean.FALSE))));
        body.addStatement(ifS(identicalX(VariableExpression.THIS_EXPRESSION, other), returnS(constX(Boolean.TRUE))));

        if (useCanEqual) {
            body.addStatement(ifS(not(isInstanceOf(other, cNode)), returnS(constX(Boolean.FALSE))));
            body.addStatement(ifS(not(callX(other, "canEqual", var("this"))), returnS(constX(Boolean.FALSE))));
        } else {
            body.addStatement(ifS(not(hasClassX(other, cNode)), returnS(constX(Boolean.FALSE))));
        }

        VariableExpression otherTyped = var("otherTyped");
        body.addStatement(declS(otherTyped, new CastExpression(cNode, other)));

        List<PropertyNode> pList = getInstanceProperties(cNode);
        for (PropertyNode pNode : pList) {
            if (shouldSkip(pNode.getName(), excludes, includes)) continue;
            body.addStatement(
                    ifS(differentPropertyX(pNode, otherTyped),
                            ifElseS(differentSelfRecursivePropertyX(pNode, otherTyped),
                                    returnS(constX(Boolean.FALSE)),
                                    ifS(not(bothSelfRecursivePropertyX(pNode, otherTyped)),
                                            ifS(nePropertyX(pNode, otherTyped), returnS(constX(Boolean.FALSE))))
                            )
                    )
            );
        }
        List<FieldNode> fList = new ArrayList<FieldNode>();
        if (includeFields) {
            fList.addAll(getInstanceNonPropertyFields(cNode));
        }
        for (FieldNode fNode : fList) {
            if (shouldSkip(fNode.getName(), excludes, includes)) continue;
            body.addStatement(
                    ifS(differentFieldX(fNode, otherTyped),
                            ifElseS(differentSelfRecursiveFieldX(fNode, otherTyped),
                                    returnS(constX(Boolean.FALSE)),
                                    ifS(not(bothSelfRecursiveFieldX(fNode, otherTyped)),
                                            ifS(neFieldX(otherTyped, fNode), returnS(constX(Boolean.FALSE)))))
                    ));
        }
        if (callSuper) {
            body.addStatement(ifS(
                    not(isTrueX(callSuperX("equals", other))),
                    returnS(constX(Boolean.FALSE))
            ));
        }

        // default
        body.addStatement(returnS(constX(Boolean.TRUE)));

        cNode.addMethod(new MethodNode(
                hasExistingEquals ? "_equals" : "equals",
                hasExistingEquals ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.boolean_TYPE,
                params(param(OBJECT_TYPE, other.getName())),
                ClassNode.EMPTY_ARRAY,
                body));
    }

    private static BinaryExpression differentSelfRecursivePropertyX(PropertyNode pNode, Expression other) {
        String getterName = getGetterName(pNode);
        Expression selfGetter = callThisX(getterName);
        Expression otherGetter = callX(other, getterName);
        return or(
                and(identicalX(selfGetter, var("this")), differentX(otherGetter, other)),
                and(differentX(selfGetter, var("this")), identicalX(otherGetter, other))
        );
    }

    private static BinaryExpression bothSelfRecursivePropertyX(PropertyNode pNode, Expression other) {
        String getterName = getGetterName(pNode);
        Expression selfGetter = callThisX(getterName);
        Expression otherGetter = callX(other, getterName);
        return and(
                identicalX(selfGetter, var("this")),
                identicalX(otherGetter, other)
        );
    }

    private static BinaryExpression differentSelfRecursiveFieldX(FieldNode fNode, Expression other) {
        final Expression fieldExpr = var(fNode);
        final Expression otherExpr = prop(other, fNode.getName());
        return or(
                and(identicalX(fieldExpr, var("this")), differentX(otherExpr, other)),
                and(differentX(fieldExpr, var("this")), identicalX(otherExpr, other))
        );
    }

    private static BinaryExpression bothSelfRecursiveFieldX(FieldNode fNode, Expression other) {
        final Expression fieldExpr = var(fNode);
        final Expression otherExpr = prop(other, fNode.getName());
        return and(
                identicalX(fieldExpr, var("this")),
                identicalX(otherExpr, other)
        );
    }
}
