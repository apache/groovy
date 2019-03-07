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

import groovy.transform.ToString;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getAllProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getterThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasDeclaredMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.sameX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the @ToString annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ToStringASTTransformation extends AbstractASTTransformation {

    static final Class MY_CLASS = ToString.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode STRINGBUILDER_TYPE = make(StringBuilder.class);
    private static final ClassNode INVOKER_TYPE = make(InvokerHelper.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
            boolean includeSuper = memberHasValue(anno, "includeSuper", true);
            boolean includeSuperProperties = memberHasValue(anno, "includeSuperProperties", true);
            boolean includeSuperFields = memberHasValue(anno, "includeSuperFields", true);
            boolean cacheToString = memberHasValue(anno, "cache", true);
            List<String> excludes = getMemberStringList(anno, "excludes");
            List<String> includes = getMemberStringList(anno, "includes");
            if (includes != null && includes.contains("super")) {
                includeSuper = true;
            }
            if (includeSuper && cNode.getSuperClass().getName().equals("java.lang.Object")) {
                addError("Error during " + MY_TYPE_NAME + " processing: includeSuper=true but '" + cNode.getName() + "' has no super class.", anno);
            }
            boolean includeNames = memberHasValue(anno, "includeNames", true);
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            boolean ignoreNulls = memberHasValue(anno, "ignoreNulls", true);
            boolean includePackage = !memberHasValue(anno, "includePackage", false);
            boolean allProperties = !memberHasValue(anno, "allProperties", false);
            boolean allNames = memberHasValue(anno, "allNames", true);

            if (!checkIncludeExcludeUndefinedAware(anno, excludes, includes, MY_TYPE_NAME)) return;
            if (!checkPropertyList(cNode, includes != null ? DefaultGroovyMethods.minus(includes, "super") : null, "includes", anno, MY_TYPE_NAME, includeFields, includeSuperProperties, allProperties)) return;
            if (!checkPropertyList(cNode, excludes, "excludes", anno, MY_TYPE_NAME, includeFields, includeSuperProperties, allProperties)) return;
            createToString(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, cacheToString, includeSuperProperties, allProperties, allNames, includeSuperFields);
        }
    }

    public static void createToString(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean includeNames) {
        createToString(cNode, includeSuper, includeFields, excludes, includes, includeNames, false);
    }

    public static void createToString(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean includeNames, boolean ignoreNulls) {
        createToString(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, true);
    }

    public static void createToString(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean includeNames, boolean ignoreNulls, boolean includePackage) {
        createToString(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, false);
    }

    public static void createToString(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean includeNames, boolean ignoreNulls, boolean includePackage, boolean cache) {
        createToString(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, cache, false);
    }

    public static void createToString(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean includeNames, boolean ignoreNulls, boolean includePackage, boolean cache, boolean includeSuperProperties) {
        createToString(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, cache, includeSuperProperties, true);
    }

    public static void createToString(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean includeNames, boolean ignoreNulls, boolean includePackage, boolean cache, boolean includeSuperProperties, boolean allProperties) {
        createToString(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, cache, includeSuperProperties, allProperties, false, false);
    }

    public static void createToString(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean includeNames, boolean ignoreNulls, boolean includePackage, boolean cache, boolean includeSuperProperties, boolean allProperties, boolean allNames, boolean includeSuperFields) {
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingToString = hasDeclaredMethod(cNode, "toString", 0);
        if (hasExistingToString && hasDeclaredMethod(cNode, "_toString", 0)) return;

        final BlockStatement body = new BlockStatement();
        Expression tempToString;
        if (cache) {
            final FieldNode cacheField = cNode.addField("$to$string", ACC_PRIVATE | ACC_SYNTHETIC, ClassHelper.STRING_TYPE, null);
            final Expression savedToString = varX(cacheField);
            body.addStatement(ifS(
                    equalsNullX(savedToString),
                    assignS(savedToString, calculateToStringStatements(cNode, includeSuper, includeFields, includeSuperFields, excludes, includes, includeNames, ignoreNulls, includePackage, includeSuperProperties, allProperties, body, allNames))
            ));
            tempToString = savedToString;
        } else {
            tempToString = calculateToStringStatements(cNode, includeSuper, includeFields, includeSuperFields, excludes, includes, includeNames, ignoreNulls, includePackage, includeSuperProperties, allProperties, body, allNames);
        }
        body.addStatement(returnS(tempToString));

        addGeneratedMethod(cNode, hasExistingToString ? "_toString" : "toString", hasExistingToString ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body);
    }

    private static class ToStringElement {
        ToStringElement(Expression value, String name, boolean canBeSelf) {
            this.value = value;
            this.name = name;
            this.canBeSelf = canBeSelf;
        }

        Expression value;
        String name;
        boolean canBeSelf;
    }

    private static Expression calculateToStringStatements(ClassNode cNode, boolean includeSuper, boolean includeFields, boolean includeSuperFields, List<String> excludes, final List<String> includes, boolean includeNames, boolean ignoreNulls, boolean includePackage, boolean includeSuperProperties, boolean allProperties, BlockStatement body, boolean allNames) {
        // def _result = new StringBuilder()
        final Expression result = localVarX("_result");
        body.addStatement(declS(result, ctorX(STRINGBUILDER_TYPE)));
        List<ToStringElement> elements = new ArrayList<ToStringElement>();

        // def $toStringFirst = true
        final VariableExpression first = localVarX("$toStringFirst");
        body.addStatement(declS(first, constX(Boolean.TRUE)));

        // <class_name>(
        String className = (includePackage) ? cNode.getName() : cNode.getNameWithoutPackage();
        body.addStatement(appendS(result, constX(className + "(")));

        Set<String> names = new HashSet<String>();
        List<PropertyNode> superList;
        if (includeSuperProperties || includeSuperFields) {
            superList = getAllProperties(names, cNode, cNode.getSuperClass(), includeSuperProperties, includeSuperFields, allProperties, false, true, true, true, allNames, false);
        } else {
            superList = new ArrayList<PropertyNode>();
        }
        List<PropertyNode> list = getAllProperties(names, cNode, cNode,true, includeFields, allProperties, false, false, true, false, allNames, false);
        list.addAll(superList);

        for (PropertyNode pNode : list) {
            String name = pNode.getName();
            if (shouldSkipUndefinedAware(name, excludes, includes, allNames)) continue;
            FieldNode fNode = pNode.getField();
            if (!cNode.hasProperty(name) && fNode.getDeclaringClass() != null) {
                // it's really just a field
                elements.add(new ToStringElement(varX(fNode), name, canBeSelf(cNode, fNode.getType())));
            } else {
                Expression getter = getterThisX(cNode, pNode);
                elements.add(new ToStringElement(getter, name, canBeSelf(cNode, pNode.getType())));
            }
        }

        // append super if needed
        if (includeSuper) {
            // not through MOP to avoid infinite recursion
            elements.add(new ToStringElement(callSuperX("toString"), "super", false));
        }

        if (includes != null) {
            Comparator<ToStringElement> includeComparator = new Comparator<ToStringElement>() {
                public int compare(ToStringElement tse1, ToStringElement tse2) {
                    return Integer.compare(includes.indexOf(tse1.name), includes.indexOf(tse2.name));
                }
            };
            Collections.sort(elements, includeComparator);
        }

        for (ToStringElement el : elements) {
            appendValue(body, result, first, el.value, el.name, includeNames, ignoreNulls, el.canBeSelf);
        }

        // wrap up
        body.addStatement(appendS(result, constX(")")));
        MethodCallExpression toString = callX(result, "toString");
        toString.setImplicitThis(false);
        return toString;
    }

    private static void appendValue(BlockStatement body, Expression result, VariableExpression first, Expression value, String name, boolean includeNames, boolean ignoreNulls, boolean canBeSelf) {
        final BlockStatement thenBlock = new BlockStatement();
        final Statement appendValue = ignoreNulls ? ifS(notNullX(value), thenBlock) : thenBlock;
        appendCommaIfNotFirst(thenBlock, result, first);
        appendPrefix(thenBlock, result, name, includeNames);
        if (canBeSelf) {
            thenBlock.addStatement(ifElseS(
                    sameX(value, new VariableExpression("this")),
                    appendS(result, constX("(this)")),
                    appendS(result, callX(INVOKER_TYPE, "toString", value))));
        } else {
            thenBlock.addStatement(appendS(result, callX(INVOKER_TYPE, "toString", value)));

        }
        body.addStatement(appendValue);
    }

    private static boolean canBeSelf(ClassNode cNode, ClassNode valueType) {
        return StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(valueType, cNode);
    }

    private static void appendCommaIfNotFirst(BlockStatement body, Expression result, VariableExpression first) {
        // if ($toStringFirst) $toStringFirst = false else result.append(", ")
        body.addStatement(ifElseS(
                first,
                assignS(first, ConstantExpression.FALSE),
                appendS(result, constX(", "))));
    }

    private static void appendPrefix(BlockStatement body, Expression result, String name, boolean includeNames) {
        if (includeNames) body.addStatement(toStringPropertyName(result, name));
    }

    private static Statement toStringPropertyName(Expression result, String fName) {
        final BlockStatement body = new BlockStatement();
        body.addStatement(appendS(result, constX(fName + ":")));
        return body;
    }

    private static Statement appendS(Expression result, Expression expr) {
        MethodCallExpression append = callX(result, "append", expr);
        append.setImplicitThis(false);
        return stmt(append);
    }
}
