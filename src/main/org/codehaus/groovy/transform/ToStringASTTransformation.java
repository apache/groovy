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
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.transform.AbstractASTTransformUtil.*;

/**
 * Handles generation of code for the @ToString annotation.
 *
 * @author Paul King
 * @author Andre Steingress
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ToStringASTTransformation extends AbstractASTTransformation {

    static final Class MY_CLASS = ToString.class;
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode STRINGBUILDER_TYPE = ClassHelper.make(StringBuilder.class);
    private static final ClassNode INVOKER_TYPE = ClassHelper.make(InvokerHelper.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            checkNotInterface(cNode, MY_TYPE_NAME);
            boolean includeSuper = memberHasValue(anno, "includeSuper", true);
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
            if (includes != null && !includes.isEmpty() && excludes != null && !excludes.isEmpty()) {
                addError("Error during " + MY_TYPE_NAME + " processing: Only one of 'includes' and 'excludes' should be supplied not both.", anno);
            }
            createToString(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, cacheToString);
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
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingToString = hasDeclaredMethod(cNode, "toString", 0);
        if (hasExistingToString && hasDeclaredMethod(cNode, "_toString", 0)) return;

        final BlockStatement body = new BlockStatement();
        Expression tempToString;
        if (cache) {
            final FieldNode cacheField = cNode.addField("$to$string", ACC_PRIVATE | ACC_SYNTHETIC, ClassHelper.STRING_TYPE, null);
            final Expression savedToString = new VariableExpression(cacheField);
            body.addStatement(new IfStatement(
                    equalsNullExpr(savedToString),
                    assignStatement(savedToString, calculateToStringStatements(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, body)),
                    new EmptyStatement()
            ));
            tempToString = savedToString;
        } else {
            tempToString = calculateToStringStatements(cNode, includeSuper, includeFields, excludes, includes, includeNames, ignoreNulls, includePackage, body);
        }
        body.addStatement(new ReturnStatement(tempToString));

        cNode.addMethod(new MethodNode(hasExistingToString ? "_toString" : "toString", hasExistingToString ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body));
    }

    private static Expression calculateToStringStatements(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean includeNames, boolean ignoreNulls, boolean includePackage, BlockStatement body) {
        // def _result = new StringBuilder()
        final Expression result = new VariableExpression("_result");
        final Expression init = new ConstructorCallExpression(STRINGBUILDER_TYPE, MethodCallExpression.NO_ARGUMENTS);
        body.addStatement(declStatement(result, init));

        // def $toStringFirst = true
        final VariableExpression first = new VariableExpression("$toStringFirst");
        body.addStatement(declStatement(first, ConstantExpression.TRUE));

        // <class_name>(
        String className = (includePackage) ? cNode.getName() : cNode.getNameWithoutPackage();
        body.addStatement(append(result, new ConstantExpression(className + "(")));

        // append properties
        List<PropertyNode> pList = getInstanceProperties(cNode);
        for (PropertyNode pNode : pList) {
            if (shouldSkip(pNode.getName(), excludes, includes)) continue;
            String getterName = "get" + Verifier.capitalize(pNode.getName());
            Expression getter = new MethodCallExpression(VariableExpression.THIS_EXPRESSION, getterName, MethodCallExpression.NO_ARGUMENTS);
            appendValue(body, result, first, getter, pNode.getName(), includeNames, ignoreNulls);
        }

        // append fields if needed
        if (includeFields) {
            List<FieldNode> fList = new ArrayList<FieldNode>();
            fList.addAll(getInstanceNonPropertyFields(cNode));
            for (FieldNode fNode : fList) {
                if (shouldSkip(fNode.getName(), excludes, includes)) continue;
                appendValue(body, result, first, new VariableExpression(fNode), fNode.getName(), includeNames, ignoreNulls);
            }
        }

        // append super if needed
        if (includeSuper) {
            appendCommaIfNotFirst(body, result, first);
            appendPrefix(body, result, "super", includeNames);
            // not through MOP to avoid infinite recursion
            body.addStatement(append(result, new MethodCallExpression(VariableExpression.SUPER_EXPRESSION, "toString", MethodCallExpression.NO_ARGUMENTS)));
        }

        // wrap up
        body.addStatement(append(result, new ConstantExpression(")")));
        MethodCallExpression toString = new MethodCallExpression(result, "toString", MethodCallExpression.NO_ARGUMENTS);
        toString.setImplicitThis(false);
        return toString;
    }

    private static void appendValue(BlockStatement body, Expression result, VariableExpression first, Expression value, String name, boolean includeNames, boolean ignoreNulls) {
        final BlockStatement thenBlock = new BlockStatement();
        final Statement appendValue = ignoreNulls ? new IfStatement(notNullExpr(value), thenBlock, EmptyStatement.INSTANCE) : thenBlock;
        appendCommaIfNotFirst(thenBlock, result, first);
        appendPrefix(thenBlock, result, name, includeNames);
        thenBlock.addStatement(new IfStatement(identicalExpr(value, VariableExpression.THIS_EXPRESSION),
                append(result, new ConstantExpression("(this)")),
                append(result, new StaticMethodCallExpression(INVOKER_TYPE, "toString", value))));
        body.addStatement(appendValue);
    }

    private static void appendCommaIfNotFirst(BlockStatement body, Expression result, VariableExpression first) {
        // if ($toStringFirst) $toStringFirst = false else result.append(", ")
        body.addStatement(new IfStatement(new BooleanExpression(first),
                assignStatement(first, ConstantExpression.FALSE),
                append(result, new ConstantExpression(", "))));
    }

    private static void appendPrefix(BlockStatement body, Expression result, String name, boolean includeNames) {
        if (includeNames) body.addStatement(toStringPropertyName(result, name));
    }

    private static Statement toStringPropertyName(Expression result, String fName) {
        final BlockStatement body = new BlockStatement();
        body.addStatement(append(result, new ConstantExpression(fName + ":")));
        return body;
    }

    private static ExpressionStatement append(Expression result, Expression expr) {
        MethodCallExpression append = new MethodCallExpression(result, "append", expr);
        append.setImplicitThis(false);
        return new ExpressionStatement(append);
    }

    private static boolean shouldSkip(String name, List<String> excludes, List<String> includes) {
        return (excludes != null && excludes.contains(name)) || name.contains("$") || (includes != null && !includes.isEmpty() && !includes.contains(name));
    }

}
