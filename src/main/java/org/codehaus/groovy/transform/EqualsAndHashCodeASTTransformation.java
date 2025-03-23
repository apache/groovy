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
import groovy.transform.stc.POJO;
import org.apache.groovy.ast.tools.AnnotatedNodeUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import org.codehaus.groovy.util.HashCodeHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.andX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.attrX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.findDeclaredMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceNonPropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getterThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getterX;
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
import static org.codehaus.groovy.ast.tools.GenericsUtils.nonGeneric;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class EqualsAndHashCodeASTTransformation extends AbstractASTTransformation {
    static final Class MY_CLASS = EqualsAndHashCode.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode HASHUTIL_TYPE = make(HashCodeHelper.class);
    private static final ClassNode POJO_TYPE = make(POJO.class);
    private static final ClassNode OBJECTS_TYPE = make(Objects.class);
    private static final ClassNode ARRAYS_TYPE = make(Arrays.class);
    private static final ClassNode OBJECT_TYPE = makeClassSafe(Object.class);
    private static final String HASH_CODE = "hashCode";
    private static final String UNDER_HASH_CODE = "_hashCode";
    private static final String UPDATE_HASH = "updateHash";
    private static final String EQUALS = "equals";
    private static final String UNDER_EQUALS = "_equals";
    private static final String CAN_EQUAL = "canEqual";
    private static final String UNDER_CAN_EQUAL = "_canEqual";

    @Override
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
            // Look for @POJO annotation by default but annotation attribute overrides
            Object pojoMember = getMemberValue(anno, "pojo");
            boolean pojo;
            if (pojoMember == null) {
                pojo = !cNode.getAnnotations(POJO_TYPE).isEmpty();
            } else {
                pojo = (boolean) pojoMember;
            }
            boolean useCanEqual = !memberHasValue(anno, "useCanEqual", false);
            if (callSuper && "java.lang.Object".equals(cNode.getSuperClass().getName())) {
                addError("Error during " + MY_TYPE_NAME + " processing: callSuper=true but '" + cNode.getName() + "' has no super class.", anno);
            }
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            boolean useGetter = !memberHasValue(anno, "useGetters", false);
            List<String> excludes = getMemberStringList(anno, "excludes");
            List<String> includes = getMemberStringList(anno, "includes");
            final boolean allNames = memberHasValue(anno, "allNames", true);
            final boolean allProperties = memberHasValue(anno, "allProperties", true);
            if (!checkIncludeExcludeUndefinedAware(anno, excludes, includes, MY_TYPE_NAME)) return;
            if (!checkPropertyList(cNode, includes, "includes", anno, MY_TYPE_NAME, includeFields)) return;
            if (!checkPropertyList(cNode, excludes, "excludes", anno, MY_TYPE_NAME, includeFields)) return;
            createHashCode(cNode, cacheHashCode, includeFields, callSuper, excludes, includes, allNames, allProperties, pojo, useGetter);
            createEquals(cNode, includeFields, callSuper, useCanEqual, excludes, includes, allNames, allProperties, pojo, useGetter);
        }
    }

    public static void createHashCode(ClassNode cNode, boolean cacheResult, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes) {
        createHashCode(cNode, cacheResult, includeFields, callSuper, excludes, includes, false);
    }

    public static void createHashCode(ClassNode cNode, boolean cacheResult, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes, boolean allNames) {
        createHashCode(cNode, cacheResult, includeFields, callSuper, excludes, includes, allNames,false);
    }

    public static void createHashCode(ClassNode cNode, boolean cacheResult, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties) {
        createHashCode(cNode, cacheResult, includeFields, callSuper, excludes, includes, allNames, allProperties, false);
    }

    public static void createHashCode(ClassNode cNode, boolean cacheResult, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties, boolean pojo) {
        createHashCode(cNode, cacheResult, includeFields, callSuper, excludes, includes, allNames, allProperties, pojo, false);
    }

    public static void createHashCode(ClassNode cNode, boolean cacheResult, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties, boolean pojo, boolean useGetter) {
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingHashCode = hasDeclaredMethod(cNode, HASH_CODE, 0);
        if (hasExistingHashCode) {
            // no point in the private method if one with that name already exists
            if (hasDeclaredMethod(cNode, UNDER_HASH_CODE, 0)) return;
            // an existing generated method also takes precedence
            MethodNode hashCode = cNode.getDeclaredMethod(HASH_CODE, Parameter.EMPTY_ARRAY);
            if (AnnotatedNodeUtils.isGenerated(hashCode)) return;
        }

        final BlockStatement body = new BlockStatement();
        // TODO use pList and fList
        if (cacheResult) {
            final FieldNode hashField = cNode.addField("$hash$code", ACC_PRIVATE | ACC_SYNTHETIC, ClassHelper.int_TYPE, null);
            final Expression hash = varX(hashField);
            body.addStatement(ifS(
                    isZeroX(hash),
                    calculateHashStatements(cNode, hash, includeFields, callSuper, excludes, includes, allNames, allProperties, pojo, useGetter)
            ));
            body.addStatement(returnS(hash));
        } else {
            body.addStatement(calculateHashStatements(cNode, null, includeFields, callSuper, excludes, includes, allNames, allProperties, pojo, useGetter));
        }

        addGeneratedMethod(cNode,
                hasExistingHashCode ? UNDER_HASH_CODE : HASH_CODE,
                hasExistingHashCode ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.int_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                body);
    }

    private static Statement calculateHashStatements(ClassNode cNode, Expression hash, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties, boolean pojo, boolean useGetter) {
        if (pojo) {
            return calculateHashStatementsPOJO(cNode, hash, includeFields, callSuper, excludes, includes, allNames, allProperties, useGetter);
        }
        return calculateHashStatementsDefault(cNode, hash, includeFields, callSuper, excludes, includes, allNames, allProperties, useGetter);
    }

    private static Statement calculateHashStatementsDefault(ClassNode cNode, Expression hash, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties, boolean useGetter) {
        final Set<String> names = new HashSet<>();
        final List<PropertyNode> pList = getAllProperties(names, cNode, true, false, allProperties, false, false, false);
        final List<FieldNode> fList = new ArrayList<>();
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
            Expression prop = useGetter ? getterThisX(cNode, pNode) : propX(varX("this"), pNode.getName());
            final Expression current = callX(HASHUTIL_TYPE, UPDATE_HASH, args(result, prop));
            body.addStatement(ifS(
                    notIdenticalX(prop, varX("this")),
                    assignS(result, current)));

        }
        for (FieldNode fNode : fList) {
            if (shouldSkipUndefinedAware(fNode.getName(), excludes, includes, allNames)) continue;
            // _result = HashCodeHelper.updateHash(_result, field) // plus self-reference checking
            final Expression fieldExpr = varX(fNode);
            final Expression current = callX(HASHUTIL_TYPE, UPDATE_HASH, args(result, fieldExpr));
            body.addStatement(ifS(
                    notIdenticalX(fieldExpr, varX("this")),
                    assignS(result, current)));
        }
        if (callSuper) {
            // _result = HashCodeHelper.updateHash(_result, super.hashCode())
            final Expression current = callX(HASHUTIL_TYPE, UPDATE_HASH, args(result, callSuperX(HASH_CODE)));
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

    private static Statement calculateHashStatementsPOJO(ClassNode cNode, Expression hash, boolean includeFields, boolean callSuper, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties, boolean useGetter) {
        final Set<String> names = new HashSet<>();
        final List<PropertyNode> pList = getAllProperties(names, cNode, true, false, allProperties, false, false, false);
        final List<FieldNode> fList = new ArrayList<>();
        if (includeFields) {
            fList.addAll(getInstanceNonPropertyFields(cNode));
        }
        final BlockStatement body = new BlockStatement();
        final ArgumentListExpression args = new ArgumentListExpression();
        for (PropertyNode pNode : pList) {
            if (shouldSkipUndefinedAware(pNode.getName(), excludes, includes, allNames)) continue;
            if (useGetter) {
                args.addExpression(getterThisX(cNode, pNode));
            } else {
                args.addExpression(propX(varX("this"), pNode.getName()));
            }
        }
        for (FieldNode fNode : fList) {
            if (shouldSkipUndefinedAware(fNode.getName(), excludes, includes, allNames)) continue;
            args.addExpression(varX(fNode));
        }
        if (callSuper) {
            args.addExpression(varX("super"));
        }
        Expression calcHash = callX(ARRAYS_TYPE, HASH_CODE, args);
        if (hash != null) {
            body.addStatement(assignS(hash, calcHash));
        } else {
            body.addStatement(returnS(calcHash));
        }
        return body;
    }

    private static void createCanEqual(ClassNode cNode) {
        boolean hasExistingCanEqual = hasDeclaredMethod(cNode, CAN_EQUAL, 1);
        if (hasExistingCanEqual && hasDeclaredMethod(cNode, UNDER_CAN_EQUAL, 1)) return;

        final BlockStatement body = new BlockStatement();
        VariableExpression other = varX("other");
        body.addStatement(returnS(isInstanceOfX(other, nonGeneric(cNode))));
        MethodNode canEqual = addGeneratedMethod(cNode,
                hasExistingCanEqual ? UNDER_CAN_EQUAL : CAN_EQUAL,
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
        createEquals(cNode, includeFields, callSuper, useCanEqual, excludes, includes, allNames, allProperties, false);
    }

    public static void createEquals(ClassNode cNode, boolean includeFields, boolean callSuper, boolean useCanEqual, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties, boolean pojo) {
        createEquals(cNode, includeFields, callSuper, useCanEqual, excludes, includes, allNames, allProperties, pojo, false);
    }
    public static void createEquals(ClassNode cNode, boolean includeFields, boolean callSuper, boolean useCanEqual, List<String> excludes, List<String> includes, boolean allNames, boolean allProperties, boolean pojo, boolean useGetter) {
        if (useCanEqual) createCanEqual(cNode);
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingEquals = hasDeclaredMethod(cNode, EQUALS, 1);
        if (hasExistingEquals) {
            // no point in the private method if one with that name already exists
            if (hasDeclaredMethod(cNode, UNDER_EQUALS, 1)) return;
            // an existing generated method also takes precedence
            MethodNode equals = findDeclaredMethod(cNode, EQUALS, 1);
            if (AnnotatedNodeUtils.isGenerated(equals)) return;
        }
        if (hasExistingEquals && hasDeclaredMethod(cNode, UNDER_EQUALS, 1)) return;

        final BlockStatement body = new BlockStatement();
        VariableExpression other = varX("other");

        // some short circuit cases for efficiency
        body.addStatement(ifS(equalsNullX(other), returnS(constX(Boolean.FALSE, true))));
        body.addStatement(ifS(sameX(varX("this"), other), returnS(constX(Boolean.TRUE, true))));

        if (useCanEqual) {
            body.addStatement(ifS(notX(isInstanceOfX(other, nonGeneric(cNode))), returnS(constX(Boolean.FALSE,true))));
        } else {
            Expression classesEqual = pojo
                    ? callX(callThisX("getClass"), EQUALS, callX(other, "getClass"))
                    : hasClassX(other, nonGeneric(cNode));
            body.addStatement(ifS(notX(classesEqual), returnS(constX(Boolean.FALSE,true))));
        }

        VariableExpression otherTyped = localVarX("otherTyped", nonGeneric(cNode));
        ClassNode originType = otherTyped.getOriginType();
        CastExpression castExpression = new CastExpression(nonGeneric(cNode), other);
        castExpression.setStrict(true);
        body.addStatement(declS(otherTyped, castExpression));

        if (useCanEqual) {
            body.addStatement(ifS(notX(callX(otherTyped, CAN_EQUAL, varX("this"))), returnS(constX(Boolean.FALSE,true))));
        }

        final Set<String> names = new HashSet<>();
        final List<PropertyNode> pList = getAllProperties(names, cNode, true, includeFields, allProperties, false, false, false);
        for (PropertyNode pNode : pList) {
            if (shouldSkipUndefinedAware(pNode.getName(), excludes, includes, allNames)) continue;
            boolean canBeSelf = StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(
                    pNode.getOriginType(), cNode
            );
            Expression thisX = useGetter ? getterThisX(originType, pNode) : propX(varX("this"), pNode.getName());
            Expression otherX = useGetter ? getterX(originType, otherTyped, pNode) : attrX(otherTyped, pNode.getName());
            Expression propsEqual = pojo
                    ? callX(OBJECTS_TYPE, EQUALS, args(thisX, otherX))
                    : hasEqualPropertyX(originType, pNode, otherTyped);
            if (!canBeSelf) {
                body.addStatement(ifS(notX(propsEqual), returnS(constX(Boolean.FALSE, true))));
            } else {
                body.addStatement(
                        ifS(notX(hasSamePropertyX(pNode, otherTyped)),
                                ifElseS(differentSelfRecursivePropertyX(pNode, otherTyped),
                                        returnS(constX(Boolean.FALSE, true)),
                                        ifS(notX(bothSelfRecursivePropertyX(pNode, otherTyped)),
                                                ifS(notX(propsEqual), returnS(constX(Boolean.FALSE, true))))
                                )
                        )
                );
            }
        }
        List<FieldNode> fList = new ArrayList<>();
        if (includeFields) {
            fList.addAll(getInstanceNonPropertyFields(cNode));
        }
        for (FieldNode fNode : fList) {
            if (shouldSkipUndefinedAware(fNode.getName(), excludes, includes, allNames)) continue;
            Expression fieldsEqual = pojo
                    ? callX(OBJECTS_TYPE, EQUALS, args(varX(fNode), propX(otherTyped, fNode.getName())))
                    : hasEqualFieldX(fNode, otherTyped);

            body.addStatement(
                    ifS(notX(hasSameFieldX(fNode, otherTyped)),
                            ifElseS(differentSelfRecursiveFieldX(fNode, otherTyped),
                                    returnS(constX(Boolean.FALSE,true)),
                                    ifS(notX(bothSelfRecursiveFieldX(fNode, otherTyped)),
                                            ifS(notX(fieldsEqual), returnS(constX(Boolean.FALSE,true)))))
                    ));
        }
        if (callSuper) {
            body.addStatement(ifS(
                    notX(isTrueX(callSuperX(EQUALS, other))),
                    returnS(constX(Boolean.FALSE,true))
            ));
        }

        // default
        body.addStatement(returnS(constX(Boolean.TRUE,true)));

        MethodNode equal = addGeneratedMethod(cNode,
                hasExistingEquals ? UNDER_EQUALS : EQUALS,
                hasExistingEquals ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.boolean_TYPE,
                params(param(OBJECT_TYPE, other.getName())),
                ClassNode.EMPTY_ARRAY,
                body);
        // don't null check this: prefer false to IllegalArgumentException
        NullCheckASTTransformation.markAsProcessed(equal);
    }

    private static BinaryExpression differentSelfRecursivePropertyX(PropertyNode pNode, Expression other) {
        String getterName = pNode.getGetterNameOrDefault();
        Expression selfGetter = callThisX(getterName);
        Expression otherGetter = callX(other, getterName);
        return orX(
                andX(sameX(selfGetter, varX("this")), notX(sameX(otherGetter, other))),
                andX(notX(sameX(selfGetter, varX("this"))), sameX(otherGetter, other))
        );
    }

    private static BinaryExpression bothSelfRecursivePropertyX(PropertyNode pNode, Expression other) {
        String getterName = pNode.getGetterNameOrDefault();
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
