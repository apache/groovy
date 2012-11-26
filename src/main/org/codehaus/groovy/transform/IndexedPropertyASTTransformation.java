/*
 * Copyright 2008-2012 the original author or authors.
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

import groovy.transform.IndexedProperty;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.List;


/**
 * Handles generation of code for the {@code @}IndexedProperty annotation.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class IndexedPropertyASTTransformation implements ASTTransformation, Opcodes {

    private static final Class MY_CLASS = IndexedProperty.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode LIST_TYPE = ClassHelper.makeWithoutCaching(List.class, false);
    private static final Token ASSIGN = Token.newSymbol("=", -1, -1);
    private static final Token INDEX = Token.newSymbol("[", -1, -1);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (parent instanceof FieldNode) {
            FieldNode fNode = (FieldNode) parent;
            ClassNode cNode = fNode.getDeclaringClass();
            if (cNode.getProperty(fNode.getName()) == null) {
                addError("Error during " + MY_TYPE_NAME + " processing. Field '" + fNode.getName() +
                        "' doesn't appear to be a property; incorrect visibility?", fNode, source);
                return;
            }
            ClassNode fType = fNode.getType();
            if (fType.isArray()) {
                addArraySetter(fNode);
                addArrayGetter(fNode);
            } else if (fType.isDerivedFrom(LIST_TYPE)) {
                addListSetter(fNode);
                addListGetter(fNode);
            } else {
                addError("Error during " + MY_TYPE_NAME + " processing. Non-Indexable property '" + fNode.getName() +
                        "' found. Type must be array or list but found " + fType.getName(), fNode, source);
            }
        }
    }

    private void addListGetter(FieldNode fNode) {
        addGetter(fNode, getComponentTypeForList(fNode.getType()));
    }

    private void addListSetter(FieldNode fNode) {
        addSetter(fNode, getComponentTypeForList(fNode.getType()));
    }

    private void addArrayGetter(FieldNode fNode) {
        addGetter(fNode, fNode.getType().getComponentType());
    }

    private void addArraySetter(FieldNode fNode) {
        addSetter(fNode, fNode.getType().getComponentType());
    }

    private void addGetter(FieldNode fNode, ClassNode componentType) {
        ClassNode cNode = fNode.getDeclaringClass();
        BlockStatement body = new BlockStatement();
        Parameter[] params = new Parameter[1];
        params[0] = new Parameter(ClassHelper.int_TYPE, "index");
        body.addStatement(new ExpressionStatement(
                new BinaryExpression(
                        new VariableExpression(fNode.getName()),
                        INDEX,
                        new VariableExpression(params[0]))
        ));
        cNode.addMethod(makeName(fNode, "get"), getModifiers(fNode), componentType, params, null, body);
    }

    private void addSetter(FieldNode fNode, ClassNode componentType) {
        ClassNode cNode = fNode.getDeclaringClass();
        BlockStatement body = new BlockStatement();
        Parameter[] params = new Parameter[2];
        params[0] = new Parameter(ClassHelper.int_TYPE, "index");
        params[1] = new Parameter(componentType, "value");
        body.addStatement(new ExpressionStatement(
                new BinaryExpression(
                        new BinaryExpression(
                                new VariableExpression(fNode.getName()),
                                INDEX,
                                new VariableExpression(params[0])),
                        ASSIGN,
                        new VariableExpression(params[1])
                )));
        cNode.addMethod(makeName(fNode, "set"), getModifiers(fNode), ClassHelper.VOID_TYPE, params, null, body);
    }

    private ClassNode getComponentTypeForList(ClassNode fType) {
        if (fType.isUsingGenerics() && fType.getGenericsTypes().length == 1) {
            return fType.getGenericsTypes()[0].getType();
        } else {
            return ClassHelper.OBJECT_TYPE;
        }
    }

    private int getModifiers(FieldNode fNode) {
        int mods = ACC_PUBLIC;
        if (fNode.isStatic()) mods |= ACC_STATIC;
        return mods;
    }

    private String makeName(FieldNode fNode, String prefix) {
        return prefix + MetaClassHelper.capitalize(fNode.getName());
    }

    private void addError(String msg, ASTNode expr, SourceUnit source) {
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(),
                        expr.getLastLineNumber(), expr.getLastColumnNumber()), source)
        );
    }

}
