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
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.util.HashCodeHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.andX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getGetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceNonPropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getterThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasClassX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasDeclaredMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasEqualFieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasEqualPropertyX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasSameFieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasSamePropertyX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isTrueX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isZeroX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notIdenticalX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.orX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.sameX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafe;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class EqualsAndHashCodeASTTransformation extends AbstractASTTransformation {
    static final Class MY_CLASS = EqualsAndHashCode.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode HASHUTIL_TYPE = make(HashCodeHelper.class);
    private static final ClassNode OBJECT_TYPE = makeClassSafe(Object.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
            boolean callSuper = memberHasValue(anno, "callSuper", true);
            boolean cacheHashCode = memberHasValue(anno, "cache", true);
            boolean useCanEqual = !memberHasValue(anno, "useCanEqual", false);
            if (callSuper && cNode.getSuperClass().getName().equals("java.lang.Object")) {
                addError("Error during " + MY_TYPE_NAME + " processing: callSuper=true but '" + cNode.getName() + "' has no super class.", anno);
            }
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            List<String> excludes = getMemberStringList(anno, "excludes");
            List<String> includes = getMemberStringList(anno, "includes");
            final boolean allNames = memberHasValue(anno, "allNames", true);
            final boolean allProperties = memberHasValue(anno, "allProperties", true);
            if (!checkIncludeExcludeUndefinedAware(anno, excludes, includes, MY_TYPE_NAME)) return;
            if (!checkPropertyList(cNode, includes, "includes", anno, MY_TYPE_NAME, includeFields)) return;
            if (!checkPropertyList(cNode, excludes, "excludes", anno, MY_TYPE_NAME, includeFields)) return;
            createHashCode(cNode, cacheHashCode, includeFields, callSuper, excludes, includes, allNames, allProperties);
            createEquals(cNode, includeFields, callSuper, useCanEqual, excludes, includes, allNames, allProperties);
        }
    }

    public static void createHashCode(ClassNode cNode, boolean cacheResult, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes) {
        createHashCode(cNode, cacheResult, includeFields, callSuper, excludes, includes, false);
    }

    public static void createHashCode(ClassNode cNode, boolean cacheResult, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes, boolean allNames) {
        createHashCode(cNode, cacheResult, includeFields, callSuper, excludes, includes, allNames,false);
    }

    public static void createHashCode(ClassNode cNode, boolean cacheResult, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties) {
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingHashCode = hasDeclaredMethod(cNode, "hashCode", 0);
        if (hasExistingHashCode && hasDeclaredMethod(cNode, "_hashCode", 0)) return;

        final BlockStatement body = new BlockStatement();
        // TODO use pList and fList
        if (cacheResult) {
            final FieldNode hashField = cNode.addField("$hash$code", ACC_PRIVATE | ACC_SYNTHETIC, ClassHelper.int_TYPE, null);
            final Expression hash = varX(hashField);
            body.addStatement(ifS(
                    isZeroX(hash),
                    calculateHashStatements(cNode, hash, includeFields, callSuper, excludes, includes, allNames, allProperties)
            ));
            body.addStatement(returnS(hash));
        } else {
            body.addStatement(calculateHashStatements(cNode, null, includeFields, callSuper, excludes, includes, allNames, allProperties));
        }

        addGeneratedMethod(cNode,
                hasExistingHashCode ? "_hashCode" : "hashCode",
                hasExistingHashCode ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.int_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                body);
    }

    private static Statement calculateHashStatements(ClassNode cNode, Expression hash, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties) {
        final Set<String> names = new HashSet<String>();
        final List<PropertyNode> pList = getAllProperties(names, cNode, true, false, allProperties, false, false, false);
        final List<FieldNode> fList = new ArrayList<FieldNode>();
        if (includeFields) {
            fList.addAll(getInstanceNonPropertyFields(cNode));
        }
        final BlockStatement body = new BlockStatement();
        // def _result = HashCodeHelper.initHash()
        final Expression result = localVarX("_result");
        body.addStatement(declS(result, callX(HASHUTIL_TYPE, "initHash")));

        for (PropertyNode pNode : pList) {
            if (shouldSkipUndefinedAware(pNode.getName(), excludes, includes, allNames)) continue;
            // _result = HashCodeHelper.updateHash(_result, getProperty()) // plus self-reference checking
            Expression getter = getterThisX(cNode, pNode);
            final Expression current = callX(HASHUTIL_TYPE, "updateHash", args(result, getter));
            body.addStatement(ifS(
                    notIdenticalX(getter, varX("this")),
                    assignS(result, current)));

        }
        for (FieldNode fNode : fList) {
            if (shouldSkipUndefinedAware(fNode.getName(), excludes, includes, allNames)) continue;
            // _result = HashCodeHelper.updateHash(_result, field) // plus self-reference checking
            final Expression fieldExpr = varX(fNode);
            final Expression current = callX(HASHUTIL_TYPE, "updateHash", args(result, fieldExpr));
            body.addStatement(ifS(
                    notIdenticalX(fieldExpr, varX("this")),
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
        VariableExpression other = varX("other");
        body.addStatement(returnS(isInstanceOfX(other, GenericsUtils.nonGeneric(cNode))));
        MethodNode canEqual = addGeneratedMethod(cNode,
                hasExistingCanEqual ? "_canEqual" : "canEqual",
                hasExistingCanEqual ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.boolean_TYPE,
                params(param(OBJECT_TYPE, other.getName())),
                ClassNode.EMPTY_ARRAY,
                body);
        // don't null check this: prefer false to IllegalArgumentException
        NullCheckASTTransformation.markAsProcessed(canEqual);
    }

    public static void createEquals(ClassNode cNode, boolean includeFields, boolean callSuper, boolean useCanEqual, List<String> excludes, List<String> includes) {
        createEquals(cNode, includeFields, callSuper, useCanEqual, excludes, includes, false);
    }

    public static void createEquals(ClassNode cNode, boolean includeFields, boolean callSuper, boolean useCanEqual, List<String> excludes, List<String> includes, boolean allNames) {
        createEquals(cNode, includeFields, callSuper, useCanEqual, excludes, includes, allNames,false);
    }

    public static void createEquals(ClassNode cNode, boolean includeFields, boolean callSuper, boolean useCanEqual, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties) {
        if (useCanEqual) createCanEqual(cNode);
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingEquals = hasDeclaredMethod(cNode, "equals", 1);
        if (hasExistingEquals && hasDeclaredMethod(cNode, "_equals", 1)) return;

        final BlockStatement body = new BlockStatement();
        VariableExpression other = varX("other");

        // some short circuit cases for efficiency
        body.addStatement(ifS(equalsNullX(other), returnS(constX(Boolean.FALSE, true))));
        body.addStatement(ifS(sameX(varX("this"), other), returnS(constX(Boolean.TRUE, true))));

        if (useCanEqual) {
            body.addStatement(ifS(notX(isInstanceOfX(other, GenericsUtils.nonGeneric(cNode))), returnS(constX(Boolean.FALSE,true))));
        } else {
            body.addStatement(ifS(notX(hasClassX(other, GenericsUtils.nonGeneric(cNode))), returnS(constX(Boolean.FALSE,true))));
        }

        VariableExpression otherTyped = localVarX("otherTyped", GenericsUtils.nonGeneric(cNode));
        CastExpression castExpression = new CastExpression(GenericsUtils.nonGeneric(cNode), other);
        castExpression.setStrict(true);
        body.addStatement(declS(otherTyped, castExpression));

        if (useCanEqual) {
            body.addStatement(ifS(notX(callX(otherTyped, "canEqual", varX("this"))), returnS(constX(Boolean.FALSE,true))));
        }

        final Set<String> names = new HashSet<String>();
        final List<PropertyNode> pList = getAllProperties(names, cNode, true, includeFields, allProperties, false, false, false);
        for (PropertyNode pNode : pList) {
            if (shouldSkipUndefinedAware(pNode.getName(), excludes, includes, allNames)) continue;
            boolean canBeSelf = StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(
                    pNode.getOriginType(), cNode
            );
            if (!canBeSelf) {
                body.addStatement(ifS(notX(hasEqualPropertyX(otherTyped.getOriginType(), pNode, otherTyped)), returnS(constX(Boolean.FALSE, true))));
            } else {
                body.addStatement(
                        ifS(notX(hasSamePropertyX(pNode, otherTyped)),
                                ifElseS(differentSelfRecursivePropertyX(pNode, otherTyped),
                                        returnS(constX(Boolean.FALSE, true)),
                                        ifS(notX(bothSelfRecursivePropertyX(pNode, otherTyped)),
                                                ifS(notX(hasEqualPropertyX(otherTyped.getOriginType(), pNode, otherTyped)), returnS(constX(Boolean.FALSE, true))))
                                )
                        )
                );
            }
        }
        List<FieldNode> fList = new ArrayList<FieldNode>();
        if (includeFields) {
            fList.addAll(getInstanceNonPropertyFields(cNode));
        }
        for (FieldNode fNode : fList) {
            if (shouldSkipUndefinedAware(fNode.getName(), excludes, includes, allNames)) continue;
            body.addStatement(
                    ifS(notX(hasSameFieldX(fNode, otherTyped)),
                            ifElseS(differentSelfRecursiveFieldX(fNode, otherTyped),
                                    returnS(constX(Boolean.FALSE,true)),
                                    ifS(notX(bothSelfRecursiveFieldX(fNode, otherTyped)),
                                            ifS(notX(hasEqualFieldX(fNode, otherTyped)), returnS(constX(Boolean.FALSE,true)))))
                    ));
        }
        if (callSuper) {
            body.addStatement(ifS(
                    notX(isTrueX(callSuperX("equals", other))),
                    returnS(constX(Boolean.FALSE,true))
            ));
        }

        // default
        body.addStatement(returnS(constX(Boolean.TRUE,true)));

        MethodNode equal = addGeneratedMethod(cNode,
                hasExistingEquals ? "_equals" : "equals",
                hasExistingEquals ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.boolean_TYPE,
                params(param(OBJECT_TYPE, other.getName())),
                ClassNode.EMPTY_ARRAY,
                body);
        // don't null check this: prefer false to IllegalArgumentException
        NullCheckASTTransformation.markAsProcessed(equal);
    }

    private static BinaryExpression differentSelfRecursivePropertyX(PropertyNode pNode, Expression other) {
        String getterName = getGetterName(pNode);
        Expression selfGetter = callThisX(getterName);
        Expression otherGetter = callX(other, getterName);
        return orX(
                andX(sameX(selfGetter, varX("this")), notX(sameX(otherGetter, other))),
                andX(notX(sameX(selfGetter, varX("this"))), sameX(otherGetter, other))
        );
    }

    private static BinaryExpression bothSelfRecursivePropertyX(PropertyNode pNode, Expression other) {
        String getterName = getGetterName(pNode);
        Expression selfGetter = callThisX(getterName);
        Expression otherGetter = callX(other, getterName);
        return andX(
                sameX(selfGetter, varX("this")),
                sameX(otherGetter, other)
        );
    }

    private static BinaryExpression differentSelfRecursiveFieldX(FieldNode fNode, Expression other) {
        final Expression fieldExpr = varX(fNode);
        final Expression otherExpr = propX(other, fNode.getName());
        return orX(
                andX(sameX(fieldExpr, varX("this")), notX(sameX(otherExpr, other))),
                andX(notX(sameX(fieldExpr, varX("this"))), sameX(otherExpr, other))
        );
    }

    private static BinaryExpression bothSelfRecursiveFieldX(FieldNode fNode, Expression other) {
        final Expression fieldExpr = varX(fNode);
        final Expression otherExpr = propX(other, fNode.getName());
        return andX(
                sameX(fieldExpr, varX("this")),
                sameX(otherExpr, other)
        );
    }
}
