/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.classgen;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.Script;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Constants;
/**
 * Verifies the AST node and adds any defaulted AST code before
 * bytecode generation occurs.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Verifier implements GroovyClassVisitor, Constants {

    private ClassNode classNode;
    private MethodNode methodNode;

    public ClassNode getClassNode() {
        return classNode;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public void visitClass(ClassNode node) {
        this.classNode = node;
        node.addInterface(GroovyObject.class.getName());

        // lets add a new field for the metaclass
        StaticMethodCallExpression initMetaClassCall =
            new StaticMethodCallExpression(
                InvokerHelper.class.getName(),
                "getMetaClass",
                VariableExpression.THIS_EXPRESSION);

        PropertyNode metaClassProperty =
            node.addProperty("metaClass", ACC_PUBLIC, MetaClass.class.getName(), initMetaClassCall, null, null);
        FieldNode metaClassField = metaClassProperty.getField();
        metaClassField.setModifiers(metaClassField.getModifiers() | ACC_TRANSIENT);

        FieldExpression metaClassVar = new FieldExpression(metaClassField);
        IfStatement initMetaClassField =
            new IfStatement(
                new BooleanExpression(
                    new BinaryExpression(metaClassVar, Token.compareEqual(-1, -1), ConstantExpression.NULL)),
                new ExpressionStatement(new BinaryExpression(metaClassVar, Token.equal(-1, -1), initMetaClassCall)),
                EmptyStatement.INSTANCE);

        node.addMethod(
            "getMetaClass",
            ACC_PUBLIC,
            MetaClass.class.getName(),
            Parameter.EMPTY_ARRAY,
            new BlockStatement(new Statement[] { initMetaClassField, new ReturnStatement(metaClassVar)}));

        // @todo we should check if the base class implements the invokeMethod method

        // lets add the invokeMethod implementation
        String superClass = node.getSuperClass();
        boolean addDelegateObject =
            (node instanceof InnerClassNode && superClass.equals(Closure.class.getName()))
                || superClass.equals(GString.class.getName());

        // don't do anything as the base class implements the invokeMethod
        if (!addDelegateObject) {
            node.addMethod(
                "invokeMethod",
                ACC_PUBLIC,
                Object.class.getName(),
                new Parameter[] {
                    new Parameter(String.class.getName(), "method"),
                    new Parameter(Object.class.getName(), "arguments")},
                new BlockStatement(
                    new Statement[] {
                        initMetaClassField,
                        new ReturnStatement(
                            new MethodCallExpression(
                                metaClassVar,
                                "invokeMethod",
                                new ArgumentListExpression(
                                    new Expression[] {
                                        VariableExpression.THIS_EXPRESSION,
                                        new VariableExpression("method"),
                                        new VariableExpression("arguments")})))
            }));

            if (!superClass.equals(Script.class.getName())) {
                node.addMethod(
                    "getProperty",
                    ACC_PUBLIC,
                    Object.class.getName(),
                    new Parameter[] { new Parameter(String.class.getName(), "property")},
                    new BlockStatement(
                        new Statement[] {
                            initMetaClassField,
                            new ReturnStatement(
                                new MethodCallExpression(
                                    metaClassVar,
                                    "getProperty",
                                    new ArgumentListExpression(
                                        new Expression[] {
                                            VariableExpression.THIS_EXPRESSION,
                                            new VariableExpression("property")})))
                }));

                node.addMethod(
                    "setProperty",
                    ACC_PUBLIC,
                    "void",
                    new Parameter[] {
                        new Parameter(String.class.getName(), "property"),
                        new Parameter(Object.class.getName(), "value")},
                    new BlockStatement(
                        new Statement[] {
                            initMetaClassField,
                            new ExpressionStatement(
                                new MethodCallExpression(
                                    metaClassVar,
                                    "setProperty",
                                    new ArgumentListExpression(
                                        new Expression[] {
                                            VariableExpression.THIS_EXPRESSION,
                                            new VariableExpression("property"),
                                            new VariableExpression("value")})))
                }));
            }
        }

        if (node.getConstructors().isEmpty()) {
            node.addConstructor(new ConstructorNode(ACC_PUBLIC, null));
        }

        addFieldInitialization(node);

        node.visitContents(this);
    }

    public void visitConstructor(ConstructorNode node) {
    }

    public void visitMethod(MethodNode node) {
        this.methodNode = node;

        Statement statement = node.getCode();
        if (!node.isVoidMethod()) {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expStmt = (ExpressionStatement) statement;
                node.setCode(new ReturnStatement(expStmt.getExpression()));
            }
            else if (statement instanceof BlockStatement) {
                BlockStatement block = (BlockStatement) statement;

                // lets copy the list so we create a new block
                List list = new ArrayList(block.getStatements());
                if (!list.isEmpty()) {
                    int idx = list.size() - 1;
                    Statement last = (Statement) list.get(idx);
                    if (last instanceof ExpressionStatement) {
                        ExpressionStatement expStmt = (ExpressionStatement) last;
                        list.set(idx, new ReturnStatement(expStmt.getExpression()));
                    }
                    else if (!(last instanceof ReturnStatement)) {
                        list.add(new ReturnStatement(ConstantExpression.NULL));
                    }
                }
                else {
                    list.add(new ReturnStatement(ConstantExpression.NULL));
                }

                node.setCode(new BlockStatement(filterStatements(list)));
            }
        }
        else {
            if (statement instanceof BlockStatement) {
                BlockStatement block = (BlockStatement) statement;
                node.setCode(new BlockStatement(filterStatements(block.getStatements())));
           }
            else {
                node.setCode(filterStatement(statement));
            }
        }
        if (node.getName().equals("main") && node.isStatic()) {
            Parameter[] params = node.getParameters();
            if (params.length == 1) {
                Parameter param = params[0];
                if (param.getType() == null || param.getType().equals("java.lang.Object")) {
                    param.setType("java.lang.String[]");
                }
            }
        }
        node.getCode().visit(new VerifierCodeVisitor(this));
    }

    public void visitField(FieldNode node) {
    }

    public void visitProperty(PropertyNode node) {
        String name = node.getName();
        FieldNode field = node.getField();

        String getterName = "get" + capitalize(name);
        String setterName = "set" + capitalize(name);

        Statement getterBlock = node.getGetterBlock();
        if (getterBlock == null) {
            if (!node.isPrivate() && classNode.getGetterMethod(getterName) == null) {
                getterBlock = createGetterBlock(node, field);
            }
        }
        Statement setterBlock = node.getGetterBlock();
        if (setterBlock == null) {
            if (!node.isPrivate() && classNode.getSetterMethod(setterName) == null) {
                setterBlock = createSetterBlock(node, field);
            }
        }

        if (getterBlock != null) {
            MethodNode getter =
                new MethodNode(getterName, node.getModifiers(), node.getType(), Parameter.EMPTY_ARRAY, getterBlock);
            classNode.addMethod(getter);
            visitMethod(getter);
        }
        if (setterBlock != null) {
            Parameter[] setterParameterTypes = { new Parameter(node.getType(), "value")};
            MethodNode setter =
                new MethodNode(setterName, node.getModifiers(), "void", setterParameterTypes, setterBlock);
            classNode.addMethod(setter);
            visitMethod(setter);
        }
    }

    // Implementation methods

    protected void addClosureCode(InnerClassNode node) {
        // add a new invoke
    }

    protected void addFieldInitialization(ClassNode node) {
        for (Iterator iter = node.getConstructors().iterator(); iter.hasNext();) {
            addFieldInitialization(node, (ConstructorNode) iter.next());
        }
    }

    protected void addFieldInitialization(ClassNode node, ConstructorNode constructorNode) {
        List statements = new ArrayList();
        List staticStatements = new ArrayList();
        for (Iterator iter = node.getFields().iterator(); iter.hasNext();) {
            addFieldInitialization(statements, staticStatements, constructorNode, (FieldNode) iter.next());
        }
        if (!statements.isEmpty()) {
            Statement code = constructorNode.getCode();
            List otherStatements = new ArrayList();
            if (code instanceof BlockStatement) {
                BlockStatement block = (BlockStatement) code;
                otherStatements.addAll(block.getStatements());
            }
            else if (code != null) {
                otherStatements.add(code);
            }
            if (!otherStatements.isEmpty()) {
                Statement first = (Statement) otherStatements.get(0);
                if (isSuperMethodCall(first)) {
                    otherStatements.remove(0);
                    statements.add(0, first);
                }
                statements.addAll(otherStatements);
            }
            constructorNode.setCode(new BlockStatement(statements));
        }

        if (!staticStatements.isEmpty()) {
            node.addStaticInitializerStatements(staticStatements);
        }
    }

    protected void addFieldInitialization(
        List list,
        List staticList,
        ConstructorNode constructorNode,
        FieldNode fieldNode) {
        Expression expression = fieldNode.getInitialValueExpression();
        if (expression != null) {
            ExpressionStatement statement =
                new ExpressionStatement(
                    new BinaryExpression(
                        new FieldExpression(fieldNode),
                        Token.equal(fieldNode.getLineNumber(), fieldNode.getColumnNumber()),
                        expression));
            if (fieldNode.isStatic()) {
                staticList.add(statement);
            }
            else {
                list.add(statement);
            }
        }
    }

    protected boolean isSuperMethodCall(Statement first) {
        if (first instanceof ExpressionStatement) {
            ExpressionStatement exprStmt = (ExpressionStatement) first;
            Expression expr = exprStmt.getExpression();
            if (expr instanceof MethodCallExpression) {
                return MethodCallExpression.isSuperMethodCall((MethodCallExpression) expr);
            }
        }
        return false;
    }

    /**
     * Capitalizes the start of the given bean property name
     */
    public static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    protected Statement createGetterBlock(PropertyNode propertyNode, FieldNode field) {
        return new ReturnStatement(new FieldExpression(field));
    }

    protected Statement createSetterBlock(PropertyNode propertyNode, FieldNode field) {
        return new ExpressionStatement(
            new BinaryExpression(new FieldExpression(field), Token.equal(0, 0), new VariableExpression("value")));
    }

    /**
     * Filters the given statements
     */
    protected List filterStatements(List list) {
        List answer = new ArrayList(list.size());
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            answer.add(filterStatement((Statement) iter.next()));
        }
        return answer;
    }

    protected Statement filterStatement(Statement statement) {
        if (statement instanceof ExpressionStatement) {
            ExpressionStatement expStmt = (ExpressionStatement) statement;
            Expression expression = expStmt.getExpression();
            if (expression instanceof ClosureExpression) {
                ClosureExpression closureExp = (ClosureExpression) expression;
                if (!closureExp.isParameterSpecified()) {
                    return closureExp.getCode();
                }
            }
        }
        return statement;
    }
}
