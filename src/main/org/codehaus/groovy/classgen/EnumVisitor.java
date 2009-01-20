package org.codehaus.groovy.classgen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

public class EnumVisitor extends ClassCodeVisitorSupport{

    // some constants for modifiers
    private static final int FS = Opcodes.ACC_FINAL | Opcodes.ACC_STATIC;
    private static final int PS = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
    private static final int PUBLIC_FS = Opcodes.ACC_PUBLIC | FS;
    private static final int PRIVATE_FS = Opcodes.ACC_PRIVATE | FS;
    
    private final CompilationUnit compilationUnit;
    private final SourceUnit sourceUnit;
    
    
    public EnumVisitor(CompilationUnit cu, SourceUnit su) {
        compilationUnit = cu;
        sourceUnit = su;
    }    
    
    public void visitClass(ClassNode node) {
        if (!isEnum(node)) return;
        completeEnum(node);
    }

    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }
    
    private boolean isEnum(ClassNode node) {
       return (node.getModifiers()&Opcodes.ACC_ENUM) != 0;
    }

    private void completeEnum(final ClassNode enumClass) {
        ClassNode enumArray = enumClass.makeArray();
        List methods = enumClass.getMethods();
        boolean hasNext = false;
        boolean hasPrevious = false;
        for (int i = 0; i < methods.size(); i++) {
            MethodNode m = (MethodNode) methods.get(i);
            if (m.getName().equals("next") && m.getParameters().length == 0) hasNext = true;
            if (m.getName().equals("previous") && m.getParameters().length == 0) hasPrevious = true;
            if (hasNext && hasPrevious) break;
        }

        // create MIN_VALUE and MAX_VALUE fields
        FieldNode minValue = new FieldNode("MIN_VALUE", PUBLIC_FS, enumClass, enumClass, null);
        FieldNode maxValue = new FieldNode("MAX_VALUE", PUBLIC_FS, enumClass, enumClass, null);

        // create values field
        FieldNode values = new FieldNode("$VALUES",PRIVATE_FS|Opcodes.ACC_SYNTHETIC,enumArray,enumClass,null);
        values.setSynthetic(true);
        {
            // create values() method
            MethodNode valuesMethod = new MethodNode("values",PUBLIC_FS,enumArray,new Parameter[0],ClassNode.EMPTY_ARRAY,null);
            valuesMethod.setSynthetic(true);
            BlockStatement code = new BlockStatement();
            code.addStatement(
                    new ReturnStatement(
                            new MethodCallExpression(new FieldExpression(values),"clone",MethodCallExpression.NO_ARGUMENTS)
                    )
            );
            valuesMethod.setCode(code);
            enumClass.addMethod(valuesMethod);
        }

        if (!hasNext) {
            // create next() method, code:
            //     Day next() {
            //        int ordinal = ordinal().next()
            //        if (ordinal >= values().size()) ordinal = 0
            //        return values()[ordinal]
            //     }
            Token assign = Token.newSymbol(Types.ASSIGN, -1, -1);
            Token ge = Token.newSymbol(Types.COMPARE_GREATER_THAN_EQUAL, -1, -1);
            MethodNode nextMethod = new MethodNode("next", Opcodes.ACC_PUBLIC, enumClass, new Parameter[0], ClassNode.EMPTY_ARRAY, null);
            nextMethod.setSynthetic(true);
            BlockStatement code = new BlockStatement();
            BlockStatement ifStatement = new BlockStatement();
            ifStatement.addStatement(
                    new ExpressionStatement(
                            new BinaryExpression(new VariableExpression("ordinal"), assign, new ConstantExpression(Integer.valueOf(0)))
                    )
            );

            code.addStatement(
                    new ExpressionStatement(
                            new DeclarationExpression(
                                    new VariableExpression("ordinal"),
                                    assign,
                                    new MethodCallExpression(
                                            new MethodCallExpression(
                                                    VariableExpression.THIS_EXPRESSION,
                                                    "ordinal",
                                                    MethodCallExpression.NO_ARGUMENTS),
                                            "next",
                                            MethodCallExpression.NO_ARGUMENTS
                                    )
                            )
                    )
            );
            code.addStatement(
                    new IfStatement(
                            new BooleanExpression(new BinaryExpression(
                                    new VariableExpression("ordinal"),
                                    ge,
                                    new MethodCallExpression(
                                            new FieldExpression(values),
                                            "size",
                                            MethodCallExpression.NO_ARGUMENTS
                                    )
                            )),
                            ifStatement,
                            EmptyStatement.INSTANCE
                    )
            );
            code.addStatement(
                    new ReturnStatement(
                            new MethodCallExpression(new FieldExpression(values), "getAt", new VariableExpression("ordinal"))
                    )
            );
            nextMethod.setCode(code);
            enumClass.addMethod(nextMethod);
        }

        if (!hasPrevious) {
            // create previous() method, code:
            //    Day previous() {
            //        int ordinal = ordinal().previous()
            //        if (ordinal < 0) ordinal = values().size() - 1
            //        return values()[ordinal]
            //    }
            Token assign = Token.newSymbol(Types.ASSIGN, -1, -1);
            Token lt = Token.newSymbol(Types.COMPARE_LESS_THAN, -1, -1);
            MethodNode nextMethod = new MethodNode("previous", Opcodes.ACC_PUBLIC, enumClass, new Parameter[0], ClassNode.EMPTY_ARRAY, null);
            nextMethod.setSynthetic(true);
            BlockStatement code = new BlockStatement();
            BlockStatement ifStatement = new BlockStatement();
            ifStatement.addStatement(
                    new ExpressionStatement(
                            new BinaryExpression(new VariableExpression("ordinal"), assign,
                                    new MethodCallExpression(
                                            new MethodCallExpression(
                                                    new FieldExpression(values),
                                                    "size",
                                                    MethodCallExpression.NO_ARGUMENTS
                                            ),
                                            "minus",
                                            new ConstantExpression(Integer.valueOf(1))
                                    )
                            )
                    )
            );

            code.addStatement(
                    new ExpressionStatement(
                            new DeclarationExpression(
                                    new VariableExpression("ordinal"),
                                    assign,
                                    new MethodCallExpression(
                                            new MethodCallExpression(
                                                    VariableExpression.THIS_EXPRESSION,
                                                    "ordinal",
                                                    MethodCallExpression.NO_ARGUMENTS),
                                            "previous",
                                            MethodCallExpression.NO_ARGUMENTS
                                    )
                            )
                    )
            );
            code.addStatement(
                    new IfStatement(
                            new BooleanExpression(new BinaryExpression(
                                    new VariableExpression("ordinal"),
                                    lt,
                                    new ConstantExpression(Integer.valueOf(0))
                            )),
                            ifStatement,
                            EmptyStatement.INSTANCE
                    )
            );
            code.addStatement(
                    new ReturnStatement(
                            new MethodCallExpression(new FieldExpression(values), "getAt", new VariableExpression("ordinal"))
                    )
            );
            nextMethod.setCode(code);
            enumClass.addMethod(nextMethod);
        }

        {
            // create valueOf
            Parameter stringParameter = new Parameter(ClassHelper.STRING_TYPE,"name");
            MethodNode valueOfMethod = new MethodNode("valueOf",PS,enumClass,new Parameter[]{stringParameter},ClassNode.EMPTY_ARRAY,null);
            ArgumentListExpression callArguments = new ArgumentListExpression();
            callArguments.addExpression(new ClassExpression(enumClass));
            callArguments.addExpression(new VariableExpression("name"));

            BlockStatement code = new BlockStatement();
            code.addStatement(
                    new ReturnStatement(
                            new MethodCallExpression(new ClassExpression(ClassHelper.Enum_Type),"valueOf",callArguments)
                    )
            );
            valueOfMethod.setCode(code);
            valueOfMethod.setSynthetic(true);
            enumClass.addMethod(valueOfMethod);
        }
        addConstructor(enumClass);
        {
            // constructor helper
            // This method is used instead of calling the constructor as
            // calling the constructor may require a table with MetaClass
            // selecting the constructor for each enum value. So instead we
            // use this method to have a central point for constructor selection
            // and only one table. The whole construction is needed because 
            // Reflection forbids access to the enum constructor.
            // code:
            // def $INIT(Object[] para) {
            //  return this(*para)
            // }            
            Parameter[] parameter = new Parameter[]{new Parameter(ClassHelper.OBJECT_TYPE.makeArray(), "para")};
            MethodNode initMethod = new MethodNode("$INIT",PRIVATE_FS,enumClass,parameter,ClassNode.EMPTY_ARRAY,null);
            initMethod.setSynthetic(true);
            ConstructorCallExpression cce = new ConstructorCallExpression(
                    ClassNode.THIS,
                    new ArgumentListExpression(
                            new SpreadExpression(new VariableExpression("para"))
                    )
            );
            BlockStatement code = new BlockStatement();
            code.addStatement(new ReturnStatement(cce));
            initMethod.setCode(code);
            enumClass.addMethod(initMethod);
        }

        {
            // static init
            List fields = enumClass.getFields();
            List arrayInit = new ArrayList();
            int value = -1;
            Token assign = Token.newSymbol(Types.ASSIGN, -1, -1);
            List block = new ArrayList();
            FieldNode tempMin = null;
            FieldNode tempMax = null;
            for (Iterator iterator = fields.iterator(); iterator.hasNext();) {
                FieldNode field = (FieldNode) iterator.next();
                if ((field.getModifiers()&Opcodes.ACC_ENUM) == 0) continue;
                value++;
                if (tempMin == null) tempMin = field;
                tempMax = field;

                ArgumentListExpression args = new ArgumentListExpression();
                args.addExpression(new ConstantExpression(field.getName()));
                args.addExpression(new ConstantExpression(Integer.valueOf(value)));
                if (field.getInitialExpression()!=null) {
                    ListExpression oldArgs = (ListExpression) field.getInitialExpression();
                    for (Iterator oldArgsIterator = oldArgs.getExpressions().iterator(); oldArgsIterator.hasNext();) {
                        Expression exp = (Expression) oldArgsIterator.next();
                        args.addExpression(exp);
                    }
                }
                field.setInitialValueExpression(null);
                block.add(
                        new ExpressionStatement(
                                new BinaryExpression(
                                        new FieldExpression(field),
                                        assign,
                                        new MethodCallExpression(new ClassExpression(enumClass),"$INIT",args)
                                )
                        )
                );
                arrayInit.add(new FieldExpression(field));
            }
            
            if (tempMin!=null) {
                block.add(
                        new ExpressionStatement(
                                new BinaryExpression(
                                        new FieldExpression(minValue),
                                        assign,
                                        new FieldExpression(tempMin)
                                )
                        )
                );
                block.add(
                        new ExpressionStatement(
                                new BinaryExpression(
                                        new FieldExpression(maxValue),
                                        assign,
                                        new FieldExpression(tempMax)
                                )
                        )
                );
                enumClass.addField(minValue);
                enumClass.addField(maxValue);
            }

            block.add(
                    new ExpressionStatement(
                            new BinaryExpression(new FieldExpression(values),assign,new ArrayExpression(enumClass,arrayInit))
                    )
            );
            enumClass.addStaticInitializerStatements(block, true);
            enumClass.addField(values);
        }
        
        
    }

    private void addConstructor(ClassNode enumClass) {
        // first look if there are declared constructors
        List ctors = new ArrayList(enumClass.getDeclaredConstructors());
        if (ctors.size()==0) {
            // add default constructor
            ConstructorNode init = new ConstructorNode(Opcodes.ACC_PRIVATE,new Parameter[0],ClassNode.EMPTY_ARRAY,new BlockStatement());
            enumClass.addConstructor(init);
            ctors.add(init);
        } 
        
        // for each constructor:
        // if constructor does not define a call to super, then transform constructor
        // to get String,int parameters at beginning and add call super(String,int)  
        
        for (Iterator iterator = ctors.iterator(); iterator.hasNext();) {
            ConstructorNode ctor = (ConstructorNode) iterator.next();
            if (ctor.firstStatementIsSpecialConstructorCall()) continue;
            // we need to add parameters
            Parameter[] oldP = ctor.getParameters();
            Parameter[] newP = new Parameter[oldP.length+2];
            String stringParameterName = getUniqueVariableName("__str",ctor.getCode());
            newP[0] = new Parameter(ClassHelper.STRING_TYPE,stringParameterName);
            String intParameterName = getUniqueVariableName("__int",ctor.getCode());
            newP[1] = new Parameter(ClassHelper.int_TYPE,intParameterName);
            System.arraycopy(oldP, 0, newP, 2, oldP.length);
            ctor.setParameters(newP);
            // and a super call
            ConstructorCallExpression cce = new ConstructorCallExpression(
                    ClassNode.SUPER,
                    new ArgumentListExpression(
                           new VariableExpression(stringParameterName),
                           new VariableExpression(intParameterName)
                    )
            );
            BlockStatement code = new BlockStatement();
            code.addStatement(new ExpressionStatement(cce));
            Statement oldCode = ctor.getCode();
            if (oldCode!=null) code.addStatement(oldCode);
            ctor.setCode(code);
        }
    }

    private String getUniqueVariableName(final String name, Statement code) {
        if (code==null) return name;
        final Object[] found=new Object[1];
        CodeVisitorSupport cv = new CodeVisitorSupport() {
            public void visitVariableExpression(VariableExpression expression) {
                if (expression.getName().equals(name)) found[0]=Boolean.TRUE;
            }
        };
        code.visit(cv);
        if (found[0]!=null) return getUniqueVariableName("_"+name, code);
        return name;
    }

}
