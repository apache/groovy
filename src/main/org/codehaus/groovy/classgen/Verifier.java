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

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
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
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.objectweb.asm.Opcodes;

/**
 * Verifies the AST node and adds any defaulted AST code before
 * bytecode generation occurs.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Verifier implements GroovyClassVisitor, Opcodes {

    public static final String __TIMESTAMP = "__timeStamp";
	private ClassNode classNode;
    private MethodNode methodNode;

    public ClassNode getClassNode() {
        return classNode;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    /**
     * add code to implement GroovyObject
     * @param node
     */
    public void visitClass(ClassNode node) {
        this.classNode = node;
                
        if ((classNode.getModifiers() & Opcodes.ACC_INTERFACE) >0) {
            //interfaces have no construcotrs, but this code expects one, 
            //so create a dummy and don't add it to the class node
            ConstructorNode dummy = new ConstructorNode(0,null);
            addInitialization(node, dummy);
            node.visitContents(this);
            return;
        }
        
        addDefaultParameterMethods(node);
        addDefaultParameterConstructors(node);

        if (!node.isDerivedFromGroovyObject()) {
            node.addInterface(ClassHelper.make(GroovyObject.class));

            // lets add a new field for the metaclass
            StaticMethodCallExpression initMetaClassCall =
                new StaticMethodCallExpression(
                    ClassHelper.make(ScriptBytecodeAdapter.class),
                    "initMetaClass",
                    VariableExpression.THIS_EXPRESSION);

            PropertyNode metaClassProperty =
                node.addProperty("metaClass", ACC_PUBLIC, ClassHelper.make(MetaClass.class), initMetaClassCall, null, null);
            metaClassProperty.setSynthetic(true);
            FieldNode metaClassField = metaClassProperty.getField();
            metaClassField.setModifiers(metaClassField.getModifiers() | ACC_TRANSIENT);

            FieldExpression metaClassVar = new FieldExpression(metaClassField);
            IfStatement initMetaClassField =
                new IfStatement(
                    new BooleanExpression(
                        new BinaryExpression(metaClassVar, Token.newSymbol( Types.COMPARE_EQUAL, -1, -1), ConstantExpression.NULL)),
                    new ExpressionStatement(new BinaryExpression(metaClassVar, Token.newSymbol( Types.EQUAL, -1, -1), initMetaClassCall)),
                    EmptyStatement.INSTANCE);

            node.addSyntheticMethod(
                "getMetaClass",
                ACC_PUBLIC,
                ClassHelper.make(MetaClass.class),
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                new BlockStatement(new Statement[] { initMetaClassField, new ReturnStatement(metaClassVar)}, new VariableScope())
            );

            // @todo we should check if the base class implements the invokeMethod method

            // lets add the invokeMethod implementation
            ClassNode superClass = node.getSuperClass();
            boolean addDelegateObject =
                (node instanceof InnerClassNode && superClass.equals(ClassHelper.CLOSURE_TYPE))
                    || superClass.equals(ClassHelper.GSTRING_TYPE);

            // don't do anything as the base class implements the invokeMethod
            if (!addDelegateObject) {
                
                VariableExpression vMethods = new VariableExpression("method");
                VariableExpression vArguments = new VariableExpression("arguments");
                VariableScope blockScope = new VariableScope();
                blockScope.getReferencedLocalVariables().put("method",vMethods);
                blockScope.getReferencedLocalVariables().put("arguments",vArguments);
                
                node.addSyntheticMethod(
                    "invokeMethod",
                    ACC_PUBLIC,
                    ClassHelper.OBJECT_TYPE,
                    new Parameter[] {
                        new Parameter(ClassHelper.STRING_TYPE, "method"),
                        new Parameter(ClassHelper.OBJECT_TYPE, "arguments")
                    },
                    ClassNode.EMPTY_ARRAY,    
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
                                            vMethods,
                                            vArguments}
                                        )
                                    )
                                )
                        },
                        blockScope
                    )
                );
                

                if (!node.isScript()) {
                    node.addSyntheticMethod(
                        "getProperty",
                        ACC_PUBLIC,
                        ClassHelper.OBJECT_TYPE,
                        new Parameter[] { new Parameter(ClassHelper.STRING_TYPE, "property")},
                        ClassNode.EMPTY_ARRAY,
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
                            },
                            new VariableScope()
                        ));
                    VariableExpression vProp = new VariableExpression("property");
                    VariableExpression vValue = new VariableExpression("value");
                    blockScope = new VariableScope();
                    blockScope.getReferencedLocalVariables().put("property",vProp);
                    blockScope.getReferencedLocalVariables().put("value",vValue);
                    
                    node.addSyntheticMethod(
                        "setProperty",
                        ACC_PUBLIC,
                        ClassHelper.VOID_TYPE,
                        new Parameter[] {
                            new Parameter(ClassHelper.STRING_TYPE, "property"),
                            new Parameter(ClassHelper.OBJECT_TYPE, "value")
                        },
                        ClassNode.EMPTY_ARRAY,
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
                                                vProp,
                                                vValue})))
                            },
                            blockScope
                    ));
                }
            }
        }

        if (node.getDeclaredConstructors().isEmpty()) {
            ConstructorNode constructor = new ConstructorNode(ACC_PUBLIC, null);
            constructor.setSynthetic(true);
            node.addConstructor(constructor);
        }
        
        if (!(node instanceof InnerClassNode)) {// add a static timestamp field to the class
            FieldNode timeTagField = new FieldNode(
                    Verifier.__TIMESTAMP,
                    Modifier.PUBLIC | Modifier.STATIC,
                    ClassHelper.Long_TYPE,
                    //"",
                    node,
                    new ConstantExpression(new Long(System.currentTimeMillis())));
            // alternatively , FieldNode timeTagField = SourceUnit.createFieldNode("public static final long __timeStamp = " + System.currentTimeMillis() + "L");
            timeTagField.setSynthetic(true);
            node.addField(timeTagField);
        }
        
        addInitialization(node);
        checkReturnInObjectInitializer(node.getObjectInitializerStatements());
        node.getObjectInitializerStatements().clear();
        node.visitContents(this);
    }
    private void checkReturnInObjectInitializer(List init) {
        CodeVisitorSupport cvs = new CodeVisitorSupport() {
            public void visitReturnStatement(ReturnStatement statement) {
                throw new RuntimeParserException("'return' is not allowed in object initializer",statement);
            }
        };
        for (Iterator iterator = init.iterator(); iterator.hasNext();) {
            Statement stm = (Statement) iterator.next();
            stm.visit(cvs);
        }
    }

    public void visitConstructor(ConstructorNode node) {
        CodeVisitorSupport checkSuper = new CodeVisitorSupport() {
            boolean firstMethodCall = true;
            String type=null;
            public void visitMethodCallExpression(MethodCallExpression call) {
                if (!firstMethodCall) return;
                firstMethodCall = false;
                String name = call.getMethodAsString();
                if (!name.equals("super") && !name.equals("this")) return;
                type=name;
                call.getArguments().visit(this);
                type=null;
            }
            public void visitVariableExpression(VariableExpression expression) {
                if (type==null) return;
                String name = expression.getName();
                if (!name.equals("this") && !name.equals("super")) return;
                throw new RuntimeParserException("cannot reference "+name+" inside of "+type+"(....) before supertype constructor has been called",expression);
            }            
        };
        Statement s = node.getCode();
        //todo why can a statement can be null?
        if (s == null) return;
        s.visit(checkSuper);
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
                        list.set(idx, new ReturnStatement(expStmt));
                    }
                    else if (!(last instanceof ReturnStatement)) {
                        list.add(new ReturnStatement(ConstantExpression.NULL));
                    }
                }
                else {
                    list.add(new ReturnStatement(ConstantExpression.NULL));
                }

                node.setCode(new BlockStatement(filterStatements(list),block.getVariableScope()));
            }
        }
        else if (!node.isAbstract()) {
        	BlockStatement newBlock = new BlockStatement();
            if (statement instanceof BlockStatement) {
                newBlock.addStatements(filterStatements(((BlockStatement)statement).getStatements()));
            }
            else {
                newBlock.addStatement(filterStatement(statement));
            }
            newBlock.addStatement(ReturnStatement.RETURN_NULL_OR_VOID);
            node.setCode(newBlock);
        }
        if (node.getName().equals("main") && node.isStatic()) {
            Parameter[] params = node.getParameters();
            if (params.length == 1) {
                Parameter param = params[0];
                if (param.getType() == null || param.getType()==ClassHelper.OBJECT_TYPE) {
                    param.setType(ClassHelper.STRING_TYPE.makeArray());
                }
            }
        }
        statement = node.getCode();
        if (statement!=null) statement.visit(new VerifierCodeVisitor(this));
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
        Statement setterBlock = node.getSetterBlock();
        if (setterBlock == null) {
            if (!node.isPrivate() && (node.getModifiers()&ACC_FINAL)==0 && classNode.getSetterMethod(setterName) == null) {
                setterBlock = createSetterBlock(node, field);
            }
        }

        if (getterBlock != null) {
            MethodNode getter =
                new MethodNode(getterName, node.getModifiers(), node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
            getter.setSynthetic(true);
            classNode.addMethod(getter);
            visitMethod(getter);

            if (ClassHelper.boolean_TYPE==node.getType() || ClassHelper.Boolean_TYPE==node.getType()) {
                String secondGetterName = "is" + capitalize(name);
                MethodNode secondGetter =
                    new MethodNode(secondGetterName, node.getModifiers(), node.getType(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, getterBlock);
                secondGetter.setSynthetic(true);
                classNode.addMethod(secondGetter);
                visitMethod(secondGetter);
            }
        }
        if (setterBlock != null) {
            Parameter[] setterParameterTypes = { new Parameter(node.getType(), "value")};
            MethodNode setter =
                new MethodNode(setterName, node.getModifiers(), ClassHelper.VOID_TYPE, setterParameterTypes, ClassNode.EMPTY_ARRAY, setterBlock);
            setter.setSynthetic(true);
            classNode.addMethod(setter);
            visitMethod(setter);
        }
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    
    private interface DefaultArgsAction {
        public void call(ArgumentListExpression arguments, Parameter[] newParams, MethodNode method);
    }
    
    /**
     * Creates a new helper method for each combination of default parameter expressions 
     */
    protected void addDefaultParameterMethods(final ClassNode node) {
        List methods = new ArrayList(node.getMethods());
        addDefaultParameters(methods, new DefaultArgsAction(){
            public void call(ArgumentListExpression arguments, Parameter[] newParams, MethodNode method) {
                MethodCallExpression expression = new MethodCallExpression(VariableExpression.THIS_EXPRESSION, method.getName(), arguments);
                expression.setImplicitThis(true);
                Statement code = null;
                if (method.isVoidMethod()) {
                    code = new ExpressionStatement(expression);
                } else {
                    code = new ReturnStatement(expression);
                }
                node.addMethod(method.getName(), method.getModifiers(), method.getReturnType(), newParams, method.getExceptions(), code);
            }
        });
    }
    
    protected void addDefaultParameterConstructors(final ClassNode node) {
        List methods = new ArrayList(node.getDeclaredConstructors());
        addDefaultParameters(methods, new DefaultArgsAction(){
            public void call(ArgumentListExpression arguments, Parameter[] newParams, MethodNode method) {
                ConstructorNode ctor = (ConstructorNode) method;
                ConstructorCallExpression expression = new ConstructorCallExpression(ClassNode.THIS, arguments);
                Statement code = new ExpressionStatement(expression);
                node.addConstructor(ctor.getModifiers(), newParams, ctor.getExceptions(), code);
            }
        });
    }

    /**
     * Creates a new helper method for each combination of default parameter expressions 
     */
    protected void addDefaultParameters(List methods, DefaultArgsAction action) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (method.hasDefaultValue()) {
                Parameter[] parameters = method.getParameters();
                int counter = 0;
                ArrayList paramValues = new ArrayList();
                int size = parameters.length;
                for (int i = size - 1; i >= 0; i--) {
                    Parameter parameter = parameters[i];
                    if (parameter != null && parameter.hasInitialExpression()) {
                        paramValues.add(new Integer(i));
                        paramValues.add(parameter.getInitialExpression());
                        counter++;
                    }
                }

                for (int j = 1; j <= counter; j++) {
                    Parameter[] newParams =  new Parameter[parameters.length - j];
                    ArgumentListExpression arguments = new ArgumentListExpression();
                    int index = 0;
                    int k = 1;
                    for (int i = 0; i < parameters.length; i++) {
                        if (k > counter - j && parameters[i] != null && parameters[i].hasInitialExpression()) {
                            arguments.addExpression(parameters[i].getInitialExpression());
                            k++;
                        }
                        else if (parameters[i] != null && parameters[i].hasInitialExpression()) {
                            newParams[index++] = parameters[i];
                            arguments.addExpression(new VariableExpression(parameters[i].getName()));
                            k++;
                        }
                        else {
                            newParams[index++] = parameters[i];
                            arguments.addExpression(new VariableExpression(parameters[i].getName()));
                        }
                    }
                    action.call(arguments,newParams,method);
                }
            }
        }
    }

    protected void addClosureCode(InnerClassNode node) {
        // add a new invoke
    }

    protected void addInitialization(ClassNode node) {
        for (Iterator iter = node.getDeclaredConstructors().iterator(); iter.hasNext();) {
            addInitialization(node, (ConstructorNode) iter.next());
        }
    }

    protected void addInitialization(ClassNode node, ConstructorNode constructorNode) {
        Statement firstStatement = constructorNode.getFirstStatement();
        ConstructorCallExpression first = getFirstIfSpecialConstructorCall(firstStatement);
        
        // in case of this(...) let the other constructor do the intit
        if (first!=null && first.isThisCall()) return;
        
        List statements = new ArrayList();
        List staticStatements = new ArrayList();
        for (Iterator iter = node.getFields().iterator(); iter.hasNext();) {
            addFieldInitialization(statements, staticStatements, (FieldNode) iter.next());
        }
        statements.addAll(node.getObjectInitializerStatements());
        if (!statements.isEmpty()) {
            Statement code = constructorNode.getCode();
            BlockStatement block = new BlockStatement();
            List otherStatements = block.getStatements();
            if (code instanceof BlockStatement) {
                block = (BlockStatement) code;
                otherStatements=block.getStatements();
            }
            else if (code != null) {
                otherStatements.add(code);
            }
            if (!otherStatements.isEmpty()) {
                if (first!=null) {
                    // it is super(..) since this(..) is already covered
                    otherStatements.remove(0);
                    statements.add(0, firstStatement);
                } 
                statements.addAll(otherStatements);
            }
            constructorNode.setCode(new BlockStatement(statements, block.getVariableScope()));
        }

        if (!staticStatements.isEmpty()) {
            node.addStaticInitializerStatements(staticStatements,true);
        }
    }

    private ConstructorCallExpression getFirstIfSpecialConstructorCall(Statement code) {
        if (code == null || !(code instanceof ExpressionStatement)) return null;

        Expression expression = ((ExpressionStatement)code).getExpression();
        if (!(expression instanceof ConstructorCallExpression)) return null;
        ConstructorCallExpression cce = (ConstructorCallExpression) expression;
        if (cce.isSpecialCall()) return cce;
        return null;
    }

    protected void addFieldInitialization(
        List list,
        List staticList,
        FieldNode fieldNode) {
        Expression expression = fieldNode.getInitialExpression();
        if (expression != null) {
            ExpressionStatement statement =
                new ExpressionStatement(
                    new BinaryExpression(
                        new FieldExpression(fieldNode),
                        Token.newSymbol(Types.EQUAL, fieldNode.getLineNumber(), fieldNode.getColumnNumber()),
                        expression));
            if (fieldNode.isStatic()) {
                staticList.add(statement);
            }
            else {
                list.add(statement);
            }
        }
    }

    /**
     * Capitalizes the start of the given bean property name
     */
    public static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
    }

    protected Statement createGetterBlock(PropertyNode propertyNode, FieldNode field) {
        Expression expression = new FieldExpression(field);
        return new ReturnStatement(expression);
    }

    protected Statement createSetterBlock(PropertyNode propertyNode, FieldNode field) {
        Expression expression = new FieldExpression(field);
        return new ExpressionStatement(
            new BinaryExpression(expression, Token.newSymbol(Types.EQUAL, 0, 0), new VariableExpression("value")));
    }

    /**
     * Filters the given statements
     */
    protected List filterStatements(List list) {
        List answer = new ArrayList(list.size());
        for (Iterator iter = list.iterator(); iter.hasNext();) {
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
