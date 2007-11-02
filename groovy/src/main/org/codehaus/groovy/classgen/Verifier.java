/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Verifies the AST node and adds any defaulted AST code before
 * bytecode generation occurs.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Verifier implements GroovyClassVisitor, Opcodes {

    public static final String __TIMESTAMP = "__timeStamp";
    public static final String __TIMESTAMP__ = "__timeStamp__239_neverHappen";
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
                                            VariableExpression.THIS_EXPRESSION,
                                            vMethods,
                                            vArguments
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
                                                VariableExpression.THIS_EXPRESSION,
                                                new VariableExpression("property"))))
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
                                                VariableExpression.THIS_EXPRESSION,
                                                vProp,
                                                vValue)))
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

            timeTagField = new FieldNode(
                    Verifier.__TIMESTAMP__ + String.valueOf(System.currentTimeMillis()),
                    Modifier.PUBLIC | Modifier.STATIC,
                    ClassHelper.Long_TYPE,
                    //"",
                    node,
                    new ConstantExpression(new Long(0)));
            // alternatively , FieldNode timeTagField = SourceUnit.createFieldNode("public static final long __timeStamp = " + System.currentTimeMillis() + "L");
            timeTagField.setSynthetic(true);
            node.addField(timeTagField);
        }
        
        addInitialization(node);
        checkReturnInObjectInitializer(node.getObjectInitializerStatements());
        node.getObjectInitializerStatements().clear();
        addCovariantMethods(node);
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
                // the name might not be null if the method name is a GString for example
                if (name==null) return;
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
        void call(ArgumentListExpression arguments, Parameter[] newParams, MethodNode method);
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
                List paramValues = new ArrayList();
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

    public void visitGenericType(GenericsType genericsType) {

    }

    public static long getTimestamp (Class clazz) {
        final Field[] fields = clazz.getFields();
        for (int i = 0; i != fields.length; ++i ) {
           if (Modifier.isStatic(fields[i].getModifiers())) {
               final String name = fields[i].getName();
               if (name.startsWith(__TIMESTAMP__)) {
                 try {
                     return Long.decode(name.substring(__TIMESTAMP__.length())).longValue();
                 }
                 catch (NumberFormatException e) {
                     return Long.MAX_VALUE;
                 }
             }
           }
        }
        return Long.MAX_VALUE;
    }
    
    private void addCovariantMethods(ClassNode classNode) {
        ClassNode sn = classNode.getSuperClass();
        if (sn==null) return;
        List declaredMethods = classNode.getMethods();
        List methodsToAdd = new ArrayList();
        
        for (Iterator it = declaredMethods.iterator(); it.hasNext();) {
            MethodNode method = (MethodNode) it.next();
            if ((method.getModifiers()&ACC_STATIC)!=0) continue;
            addCovariantMethodInSuper(sn,method,methodsToAdd);
        }
        for (Iterator it = methodsToAdd.iterator(); it.hasNext();) {
            MethodNode method = (MethodNode) it.next();
            classNode.addMethod(method);
        }
    }
    
    private void addCovariantMethodInSuper(ClassNode sn, final MethodNode method, List methodsToAdd) {
        for (ClassNode current=sn; current!=null; current=current.getSuperClass()) {
            List superClassMethods = current.getMethods();
            for (Iterator sit = superClassMethods.iterator(); sit.hasNext();) {
                final MethodNode superClassMethod = (MethodNode) sit.next();
                if ((superClassMethod.getModifiers()&ACC_STATIC)!=0) continue;
                if (!superClassMethod.getName().equals(method.getName())) continue;
                Map genericsSpec = createGenericsSpec(current);
                if (!equalParameters(method,superClassMethod,genericsSpec)) continue;
                ClassNode mr = method.getReturnType();
                ClassNode smr = superClassMethod.getReturnType();
                if (mr.equals(smr)) continue;
                ClassNode testmr = correctToGenericsSpec(genericsSpec,smr);
                if (!mr.isDerivedFrom(testmr)) {
                    throw new RuntimeParserException(
                            "the return type is incompatible with "+
                             superClassMethod.getTypeDescriptor()+
                            " in "+current.getName(),method);
                }
                if ((superClassMethod.getModifiers()&ACC_FINAL)!=0) {
                    throw new RuntimeParserException(
                            "cannot override final method "+
                             superClassMethod.getTypeDescriptor()+
                            " in "+current.getName(),method);
                }
                if ((superClassMethod.getModifiers()&ACC_STATIC) !=
                    (method.getModifiers()&ACC_STATIC)
                ) {
                    throw new RuntimeParserException(
                            "cannot override method "+
                             superClassMethod.getTypeDescriptor()+
                            " in "+current.getName()+" with disparate static modifier"
                            ,method);
                }
                
                MethodNode newMethod = new MethodNode(
                        superClassMethod.getName(),
                        method.getModifiers() | ACC_SYNTHETIC | ACC_BRIDGE,
                        superClassMethod.getReturnType(),
                        superClassMethod.getParameters(),
                        superClassMethod.getExceptions(),
                        null
                );
                List instructions = new ArrayList(1);
                instructions.add (
                        new BytecodeInstruction() {
                            public void visit(MethodVisitor mv) {
                                BytecodeHelper helper = new BytecodeHelper(mv);
                                mv.visitVarInsn(ALOAD,0);
                                Parameter[] para = superClassMethod.getParameters();
                                Parameter[] goal = method.getParameters();
                                for (int i = 0; i < para.length; i++) {
                                    helper.load(para[i].getType(), i+1);
                                    if (!para[i].getType().equals(goal[i].getType())) {
                                        helper.doCast(goal[i].getType());
                                    }
                                }
                                mv.visitMethodInsn(
                                        INVOKEVIRTUAL, 
                                        BytecodeHelper.getClassInternalName(classNode),
                                        method.getName(),
                                        BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameters()));
                                helper.doReturn(superClassMethod.getReturnType());
                            }
                        }

                );
                newMethod.setCode(new BytecodeSequence(instructions));
                methodsToAdd.add(newMethod);
                return;
            }
        }
    }
    
    private ClassNode correctToGenericsSpec(Map genericsSpec, ClassNode type) {
        if (type.isGenericsPlaceHolder()){
            String name = type.getGenericsTypes()[0].getName();
            type = (ClassNode) genericsSpec.get(name);
        }
        if (type==null) type = ClassHelper.OBJECT_TYPE;
        return type;
    }
    
    private boolean equalParameters(MethodNode m1, MethodNode m2, Map genericsSpec) {
        Parameter[] p1 = m1.getParameters();
        Parameter[] p2 = m2.getParameters();
        if (p1.length!=p2.length) return false;
        for (int i = 0; i < p2.length; i++) {
            ClassNode type = p2[i].getType();
            type = correctToGenericsSpec(genericsSpec,type);
            if (!p1[i].getType().equals(type)) return false;
        }
        return true;
    }
    
    private Map createGenericsSpec(ClassNode sn) {
        Map ret = new HashMap();
        ClassNode stop = sn.getSuperClass();
        for (ClassNode current = classNode; current!=null && current.redirect()!=stop; current=current.getUnresolvedSuperClass(false)) {
            // ret contains the type specs for the child class, what we now need
            // is the type spec for the super class. To get that we first apply the 
            // type parameters to the super class and then use the type names of the super
            // class to reset the map. Example:
            //   class A<V,W,X>{}
            //   class B<T extends Number> extends A<T,Long,String> {}
            // first we have:    T->Number
            // we apply it to A<T,Long,String> -> A<Number,Long,String>
            // resulting in:     V->Number,W->Long,X->String
            
            GenericsType[] sgts = current.getGenericsTypes();
            if (sgts==null) {
                ret.clear();
                continue;
            }
            ClassNode[] spec = new ClassNode[sgts.length];
            for (int i = 0; i < spec.length; i++) {
                spec[i]=correctToGenericsSpec(ret, sgts[i].getType());
            }
            GenericsType[] newGts = current.redirect().getGenericsTypes();
            ret.clear();
            for (int i = 0; i < spec.length; i++) {
                ret.put(newGts[i].getName(), spec[i]);
            }            
        }
        
        return ret;
    }

}
