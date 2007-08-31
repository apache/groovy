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
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

public class EnumVisitor extends ClassCodeVisitorSupport{

    // some constants for modifiers
    private static int FS = Opcodes.ACC_FINAL | Opcodes.ACC_STATIC;
    private static int PS = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;    
    private static int PUBLIC_FS = Opcodes.ACC_PUBLIC | FS; 
    private static int PRIVATE_FS = Opcodes.ACC_PRIVATE | FS;
    
    private CompilationUnit compilationUnit;
    private SourceUnit sourceUnit;
    
    
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

        // create values field
        FieldNode values = new FieldNode("$VALUES",PRIVATE_FS,enumArray,enumClass,null);
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
            enumClass.addMethod(valueOfMethod);
        }
        addConstructor(enumClass);
        {
            // constructor helper
            // This method is used instead of calling the constructor as
            // calling the constrcutor may require a table with MetaClass
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
            for (Iterator iterator = fields.iterator(); iterator.hasNext();) {
                FieldNode field = (FieldNode) iterator.next();
                if ((field.getModifiers()&Opcodes.ACC_ENUM) == 0) continue;
                value++;
                
                ArgumentListExpression args = new ArgumentListExpression();
                args.addExpression(new ConstantExpression(field.getName()));
                args.addExpression(new ConstantExpression(new Integer(value)));
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
        ArrayList ctors = new ArrayList(enumClass.getDeclaredConstructors());
        if (ctors.size()==0) {
            // add default constructor
            ConstructorNode init = new ConstructorNode(Opcodes.ACC_PRIVATE,new Parameter[0],ClassNode.EMPTY_ARRAY,new BlockStatement());
            enumClass.addConstructor(init);
            ctors.add(init);
        } 
        
        // for each constructor:
        // if constructordoes not define a call to super, then transform constructor
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
