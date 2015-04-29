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
import org.codehaus.groovy.ast.MethodNode;
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
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

/**
 * Handles generation of code for the @ToString annotation.
 *
 * @author Paul King
 * @author Andre Steingress
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
            boolean cacheToString = memberHasValue(anno, "cache", true);
            if (includeSuper && cNode.getSuperClass().getName().equals("java.lang.Object")) {
                addError("Error during " + MY_TYPE_NAME + " processing: includeSuper=true but '" + cNode.getName() + "' has no super class.", anno);
            }
            boolean includeNames = memberHasValue(anno, "includeNames", true);
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            List<String> excludes = getMemberList(anno, "excludes");
            List<String> includes = getMemberList(anno, "includes");
            boolean ignoreNulls = memberHasValue(anno, "ignoreNulls", true);
            boolean includePackage = !memberHasValue(anno, "includePackage", false);

            if (hasAnnotation(cNode, CanonicalASTTransformation.MY_TYPE)) {
                AnnotationNode canonical = cNode.getAnnotations(CanonicalASTTransformation.MY_TYPE).get(0);
                if (excludes == null || excludes.isEmpty()) excludes = getMemberList(canonical, "excludes");
                if (includes == null || includes.isEmpty()) includes = getMemberList(canonical, "includes");
            }
            if (!checkIncludeExclude(anno, excludes, includes, MY_TYPE_NAME)) return;
            createToString(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, cacheToString, includeSuperProperties);
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
        createToString(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, false, false);
    }

    public static void createToString(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean includeNames, boolean ignoreNulls, boolean includePackage, boolean cache, boolean includeSuperProperties) {
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
                    assignS(savedToString, calculateToStringStatements(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, includeSuperProperties, body))
            ));
            tempToString = savedToString;
        } else {
            tempToString = calculateToStringStatements(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, includeSuperProperties, body);
        }
        body.addStatement(returnS(tempToString));

        cNode.addMethod(new MethodNode(hasExistingToString ? "_toString" : "toString", hasExistingToString ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body));
    }

    private static Expression calculateToStringStatements(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean includeNames, boolean ignoreNulls, boolean includePackage, boolean includeSuperProperties, BlockStatement body) {
        // def _result = new StringBuilder()
        final Expression result = varX("_result");
        body.addStatement(declS(result, ctorX(STRINGBUILDER_TYPE)));

        // def $toStringFirst = true
        final VariableExpression first = varX("$toStringFirst");
        body.addStatement(declS(first, constX(Boolean.TRUE)));

        // <class_name>(
        String className = (includePackage) ? cNode.getName() : cNode.getNameWithoutPackage();
        body.addStatement(appendS(result, constX(className + "(")));

        // append properties
        List<PropertyNode> pList;
        if (includeSuperProperties) {
            pList = getAllProperties(cNode);
            Iterator<PropertyNode> pIterator = pList.iterator();
            while (pIterator.hasNext()) {
                if (pIterator.next().isStatic()) {
                    pIterator.remove();
                }
            }
        } else {
            pList = getInstanceProperties(cNode);
        }
        for (PropertyNode pNode : pList) {
            if (shouldSkip(pNode.getName(), excludes, includes)) continue;
            Expression getter = getterX(cNode, pNode);

            appendValue(body, result, first, getter, pNode.getName(), includeNames, ignoreNulls);
        }

        // append fields if needed
        if (includeFields) {
            List<FieldNode> fList = new ArrayList<FieldNode>();
            fList.addAll(getInstanceNonPropertyFields(cNode));
            for (FieldNode fNode : fList) {
                if (shouldSkip(fNode.getName(), excludes, includes)) continue;
                appendValue(body, result, first, varX(fNode), fNode.getName(), includeNames, ignoreNulls);
            }
        }

        // append super if needed
        if (includeSuper) {
            appendCommaIfNotFirst(body, result, first);
            appendPrefix(body, result, "super", includeNames);
            // not through MOP to avoid infinite recursion
            body.addStatement(appendS(result, callSuperX("toString")));
        }

        // wrap up
        body.addStatement(appendS(result, constX(")")));
        MethodCallExpression toString = callX(result, "toString");
        toString.setImplicitThis(false);
        return toString;
    }

    private static void appendValue(BlockStatement body, Expression result, VariableExpression first, Expression value, String name, boolean includeNames, boolean ignoreNulls) {
        final BlockStatement thenBlock = new BlockStatement();
        final Statement appendValue = ignoreNulls ? ifS(notNullX(value), thenBlock) : thenBlock;
        appendCommaIfNotFirst(thenBlock, result, first);
        appendPrefix(thenBlock, result, name, includeNames);
        thenBlock.addStatement(ifElseS(
                sameX(value, new VariableExpression("this")),
                appendS(result, constX("(this)")),
                appendS(result, callX(INVOKER_TYPE, "toString", value))));
        body.addStatement(appendValue);
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
