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

import groovy.transform.ToString;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

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

    private static final ClassNode STRINGBUFFER_TYPE = ClassHelper.make(StringBuffer.class);
    private static final ClassNode INVOKER_TYPE = ClassHelper.make(InvokerHelper.class);

    private static final Token ASSIGN = Token.newSymbol(Types.ASSIGN, -1, -1);
    private static final Token LOGICAL_OR = Token.newSymbol(Types.LOGICAL_OR, -1, -1);
    private static final Token COMPARE_NOT_EQUAL = Token.newSymbol(Types.COMPARE_NOT_EQUAL, -1, -1);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            checkNotInterface(cNode, MY_TYPE_NAME);
            boolean includeSuper = memberHasValue(anno, "includeSuper", true);
            if (includeSuper && cNode.getSuperClass().getName().equals("java.lang.Object")) {
                addError("Error during " + MY_TYPE_NAME + " processing: includeSuper=true but '" + cNode.getName() + "' has no super class.", anno);
            }
            boolean includeNames = memberHasValue(anno, "includeNames", true);
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            List<String> excludes = tokenize((String) getMemberValue(anno, "excludes"));
            List<String> includes = tokenize((String) getMemberValue(anno, "includes"));
            boolean ignoreNullValues = memberHasValue(anno, "ignoreNullValues", true);

            if (hasAnnotation(cNode, CanonicalASTTransformation.MY_TYPE)) {
                AnnotationNode canonical = cNode.getAnnotations(CanonicalASTTransformation.MY_TYPE).get(0);
                if (excludes == null || excludes.isEmpty()) excludes = tokenize((String) getMemberValue(canonical, "excludes"));
                if (includes == null || includes.isEmpty()) includes = tokenize((String) getMemberValue(canonical, "includes"));
            }
            if (includes != null && !includes.isEmpty() && excludes != null && !excludes.isEmpty()) {
                addError("Error during " + MY_TYPE_NAME + " processing: Only one of 'includes' and 'excludes' should be supplied not both.", anno);
            }
            toStringInit(cNode, new ConstantExpression(includeNames));
            createToString(cNode, includeSuper, includeFields, excludes, includes, ignoreNullValues);
        }
    }

    public static void createToString(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes) {
        createToString(cNode, includeSuper, includeFields, excludes, includes, false);
    }

    public static void createToString(ClassNode cNode, boolean includeSuper, boolean includeFields, List<String> excludes, List<String> includes, boolean ignoreNullValues) {
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingToString = hasDeclaredMethod(cNode, "toString", 0);
        if (hasExistingToString && hasDeclaredMethod(cNode, "_toString", 0)) return;

        final BlockStatement body = new BlockStatement();

        // def $toStringLocalVar = null
        final VariableExpression localVar = new VariableExpression("$toStringLocalVar");
        body.addStatement(new ExpressionStatement(new DeclarationExpression(localVar, ASSIGN, ConstantExpression.NULL)));

        // def $toStringLocalVarFirst = true
        final VariableExpression localVarFirst = new VariableExpression("$toStringLocalVarFirst");
        body.addStatement(new ExpressionStatement(new DeclarationExpression(localVarFirst, ASSIGN, ConstantExpression.TRUE)));

        // def _result = new StringBuffer()
        final Expression result = new VariableExpression("_result");
        final Expression init = new ConstructorCallExpression(STRINGBUFFER_TYPE, MethodCallExpression.NO_ARGUMENTS);
        body.addStatement(new ExpressionStatement(new DeclarationExpression(result, ASSIGN, init)));

        // <class_name>(
        body.addStatement(append(result, new ConstantExpression(cNode.getName())));
        body.addStatement(append(result, new ConstantExpression("(")));

        // append properties, append fields, append super
        List<PropertyNode> pList = getInstanceProperties(cNode);
        for (PropertyNode pNode : pList) {
            if (shouldSkip(pNode.getName(), excludes, includes)) continue;
            String getterName = "get" + Verifier.capitalize(pNode.getName());
            Expression getter = new MethodCallExpression(VariableExpression.THIS_EXPRESSION, getterName, MethodCallExpression.NO_ARGUMENTS);

            assignExpressionToVar(body, localVar, getter);
            appendValue(cNode, body, localVar, result, pNode.getName(), localVarFirst, ignoreNullValues);
        }

        List<FieldNode> fList = new ArrayList<FieldNode>();
        if (includeFields) {
            fList.addAll(getInstanceNonPropertyFields(cNode));
        }
        for (FieldNode fNode : fList) {
            if (shouldSkip(fNode.getName(), excludes, includes)) continue;

            assignExpressionToVar(body, localVar, new VariableExpression(fNode));
            appendValue(cNode, body, localVar, result, fNode.getName(), localVarFirst, ignoreNullValues);
        }

        if (includeSuper) {
            appendCommaIfNotFirst(result, localVarFirst, body);
            appendPrefix(cNode, body, result, "super");
            // not through MOP to avoid infinite recursion
            body.addStatement(append(result, new MethodCallExpression(VariableExpression.SUPER_EXPRESSION, "toString", MethodCallExpression.NO_ARGUMENTS)));
        }

        // )
        body.addStatement(append(result, new ConstantExpression(")")));

        // return result.toString()
        body.addStatement(new ReturnStatement(new MethodCallExpression(result, "toString", ArgumentListExpression.EMPTY_ARGUMENTS)));
        cNode.addMethod(new MethodNode(hasExistingToString ? "_toString" : "toString", hasExistingToString ? ACC_PRIVATE : ACC_PUBLIC,
                ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body));
    }

    private static void appendValue(ClassNode cNode, BlockStatement body, VariableExpression localVar, Expression memberList, String nodeName, VariableExpression localVarFirst, boolean ignoreNullValues) {
        final BlockStatement ifBlock = new BlockStatement();
        
        final IfStatement ifNotNull = new IfStatement(
                // if (!ignoreValues || (value != null))
                new BooleanExpression(new BinaryExpression(new NotExpression(new ConstantExpression(ignoreNullValues)), LOGICAL_OR, new BinaryExpression(localVar, COMPARE_NOT_EQUAL, ConstantExpression.NULL))),
                ifBlock, 
                EmptyStatement.INSTANCE
        );

        appendCommaIfNotFirst(memberList, localVarFirst, ifBlock);
        appendPrefix(cNode, ifBlock, memberList, nodeName);

        ifBlock.addStatement(append(memberList, new StaticMethodCallExpression(INVOKER_TYPE, "toString", localVar)));
        ifBlock.addStatement(new IfStatement(new BooleanExpression(localVarFirst), assignStatement(localVarFirst, ConstantExpression.FALSE), EmptyStatement.INSTANCE));

        body.addStatement(ifNotNull);
    }

    private static void appendCommaIfNotFirst(Expression memberList, VariableExpression localVarFirst, BlockStatement statements) {
        // if (!$toStringLocalVarFirst) result.append(", ");
        statements.addStatement(new IfStatement(new NotExpression(localVarFirst), append(memberList, new ConstantExpression(", ")), EmptyStatement.INSTANCE));
    }

    private static void assignExpressionToVar(BlockStatement body, VariableExpression localVar, Expression expression) {
        body.addStatement(assignStatement(localVar, expression));
    }

    private static void appendPrefix(ClassNode cNode, BlockStatement body, Expression result, String name) {
        body.addStatement(new IfStatement(
                new BooleanExpression(new VariableExpression(cNode.getField("$print$names"))),
                toStringPropertyName(result, name),
                new EmptyStatement()
        ));
    }

    private static Statement toStringPropertyName(Expression result, String fName) {
        final BlockStatement body = new BlockStatement();
        body.addStatement(append(result, new ConstantExpression(fName + ":")));
        return body;
    }

    private static ExpressionStatement append(Expression result, Expression expr) {
        return new ExpressionStatement(new MethodCallExpression(result, "append", expr));
    }

    private static boolean shouldSkip(String name, List<String> excludes, List<String> includes) {
        return (excludes != null && excludes.contains(name)) || name.contains("$") || (includes != null && !includes.isEmpty() && !includes.contains(name));
    }

    public static void toStringInit(ClassNode cNode, ConstantExpression fieldValue) {
        cNode.addField("$print$names", ACC_PRIVATE | ACC_SYNTHETIC, ClassHelper.boolean_TYPE, fieldValue);
    }

}
