package org.codehaus.groovy.tools.javac;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.ResolveVisitor;
import org.objectweb.asm.Opcodes;

import groovy.lang.GroovyObjectSupport;

import java.io.*;
import java.util.*;

public class JavaStubGenerator {
    private File outputPath;
    private ArrayList toCompile = new ArrayList();

    private ResolveVisitor resolver;

    public JavaStubGenerator(JavaAwareCompilationUnit cu, File outputPath) {
        this.outputPath = outputPath;
    }
    
    private void mkdirs(File parent, String relativeFile) {
        int index = relativeFile.lastIndexOf('/');
        if (index==-1) return;
        File dir = new File(parent,relativeFile.substring(0,index));
        dir.mkdirs();
    }
    
    public void generateClass(ClassNode classNode) throws FileNotFoundException {
        String fileName = classNode.getName().replace('.', '/');
        mkdirs(outputPath,fileName);
        toCompile.add(fileName);

        File file = new File(outputPath, fileName + ".java");
        FileOutputStream fos = new FileOutputStream(file);
        PrintWriter out = new PrintWriter(fos);

        try {
            String packageName = classNode.getPackageName();
            if (packageName != null) {
                out.println("package " + packageName + ";\n");
            }

            genImports(classNode, out);

            boolean isInterface = classNode.isInterface();

            printModifiers(out, classNode.getModifiers()
                    & ~(isInterface ? Opcodes.ACC_ABSTRACT : 0));
            out.println((isInterface ? "interface " : "class ")
                    + classNode.getNameWithoutPackage());

            ClassNode superClass = classNode.getSuperClass();

            if (!isInterface) {
                if (superClass.equals(ClassHelper.OBJECT_TYPE))
                    superClass = ClassHelper.make(GroovyObjectSupport.class);
                out.println("  extends " + superClass.getName());
            } else {
                if (!superClass.equals(ClassHelper.OBJECT_TYPE))
                    out.println("  extends " + superClass.getName());
            }

            ClassNode[] interfaces = classNode.getInterfaces();
            if (interfaces != null && interfaces.length > 0) {
                out.println("  implements");
                for (int i = 0; i < interfaces.length - 1; ++i)
                    out.println("    " + interfaces[i].getName() + ",");
                out.println("    "
                        + interfaces[interfaces.length - 1].getName());
            }
            out.println("{");

            genMethods(classNode, out);
            genFields(classNode, out);
            genProps(classNode, out);

            out.println("}");
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                // ignore
            }
            try {
                fos.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void genMethods(ClassNode classNode, PrintWriter out) {
        getContructors(classNode, out);

        List methods = classNode.getMethods();
        if (methods != null)
            for (Iterator it = methods.iterator(); it.hasNext();) {
                MethodNode methodNode = (MethodNode) it.next();
                genMethod(methodNode, out);
            }
    }

    private void getContructors(ClassNode classNode, PrintWriter out) {
        List constrs = classNode.getDeclaredConstructors();
        if (constrs != null)
            for (Iterator it = constrs.iterator(); it.hasNext();) {
                ConstructorNode constrNode = (ConstructorNode) it.next();
                genConstructor(constrNode, out);
            }
    }

    private void genFields(ClassNode classNode, PrintWriter out) {
        List fields = classNode.getFields();
        if (fields != null)
            for (Iterator it = fields.iterator(); it.hasNext();) {
                FieldNode fieldNode = (FieldNode) it.next();
                genField(fieldNode, out);
            }
    }

    private void genProps(ClassNode classNode, PrintWriter out) {
        List props = classNode.getProperties();
        if (props != null)
            for (Iterator it = props.iterator(); it.hasNext();) {
                PropertyNode propNode = (PropertyNode) it.next();
                genProp(propNode, out);
            }
    }

    private void genProp(PropertyNode propNode, PrintWriter out) {
        String name = propNode.getName().substring(0, 1).toUpperCase()
                + propNode.getName().substring(1);

        String getterName = "get" + name;

        boolean skipGetter = false;
        List getterCandidates = propNode.getField().getOwner().getMethods(
                getterName);
        if (getterCandidates != null)
            for (Iterator it = getterCandidates.iterator(); it.hasNext();) {
                MethodNode method = (MethodNode) it.next();
                if (method.getParameters().length == 0) {
                    skipGetter = true;
                }
            }

        if (!skipGetter) {
            printModifiers(out, propNode.getModifiers());
            out.print(propNode.getType().getName() + " " + getterName
                    + " () { ");
            printReturn(out, propNode.getType());
            out.println(" }");
        }

        String setterName = "set" + name;

        boolean skipSetter = false;
        List setterCandidates = propNode.getField().getOwner().getMethods(
                setterName);
        if (setterCandidates != null)
            for (Iterator it = setterCandidates.iterator(); it.hasNext();) {
                MethodNode method = (MethodNode) it.next();
                if (method.getParameters().length == 1) {
                    skipSetter = true;
                }
            }

        if (!skipSetter) {
            printModifiers(out, propNode.getModifiers());
            out.println("void " + setterName + "( "
                    + propNode.getType().getName() + " value) {}");
        }
    }

    private void genField(FieldNode fieldNode, PrintWriter out) {
        printModifiers(out, fieldNode.getModifiers());
        out.println(fieldNode.getType().getName() + " "
                + fieldNode.getName() + ";");
    }

    private ConstructorCallExpression getConstructorCallExpression(
            ConstructorNode constructorNode) {
        Statement code = constructorNode.getCode();
        if (!(code instanceof BlockStatement))
            return null;

        BlockStatement block = (BlockStatement) code;
        List stats = block.getStatements();
        if (stats == null || stats.size() == 0)
            return null;

        Statement stat = (Statement) stats.get(0);
        if (!(stat instanceof ExpressionStatement))
            return null;

        Expression expr = ((ExpressionStatement) stat).getExpression();
        if (!(expr instanceof ConstructorCallExpression))
            return null;

        return (ConstructorCallExpression) expr;
    }

    private void genConstructor(ConstructorNode constructorNode, PrintWriter out) {
        // printModifiers(out, constructorNode.getModifiers());
        out.print("public "); // temporary hack
        out.print(constructorNode.getDeclaringClass().getNameWithoutPackage());

        printParams(constructorNode, out);

        ConstructorCallExpression constrCall = getConstructorCallExpression(constructorNode);
        if (constrCall == null || !constrCall.isSpecialCall())
            out.println(" {}");
        else {
            out.println(" {");
            if (constrCall.isSuperCall())
                out.print("super(");
            else
                out.print("this(");

            genSpecialContructorArgs(out, constrCall);

            out.println(");");
            out.println("}");
        }
    }

    private void genSpecialContructorArgs(PrintWriter out,
            ConstructorCallExpression constrCall) {
        Expression arguments = constrCall.getArguments();
        if (arguments instanceof ArgumentListExpression) {
            ArgumentListExpression argumentListExpression = (ArgumentListExpression) arguments;
            List args = argumentListExpression.getExpressions();
            for (Iterator it = args.iterator(); it.hasNext();) {
                Expression arg = (Expression) it.next();
                if (arg instanceof ConstantExpression) {
                    ConstantExpression constantExpression = (ConstantExpression) arg;
                    Object o = constantExpression.getValue();
                    if (o instanceof String)
                        out.print("null");
                    else
                        out.print(constantExpression.getText());
                } else {
                    printDefaultValue(out, arg.getType().getName());
                }

                if (arg != args.get(args.size() - 1))
                    out.print(", ");
            }
        }
    }

    private void genMethod(MethodNode methodNode, PrintWriter out) {
        if (!methodNode.getDeclaringClass().isInterface())
            printModifiers(out, methodNode.getModifiers());
        out.print(methodNode.getReturnType().getName() + " "
                + methodNode.getName());

        printParams(methodNode, out);

        if ((methodNode.getModifiers() & Opcodes.ACC_ABSTRACT) != 0) {
            out.println(";");
        } else {
            out.print(" { ");
            ClassNode retType = methodNode.getReturnType();
            printReturn(out, retType);
            out.println("}");
        }

    }

    private void printReturn(PrintWriter out, ClassNode retType) {
        String retName = retType.getName();
        if (!retName.equals("void")) {
            out.print("return ");

            printDefaultValue(out, retName);

            out.print(";");
        }
    }

    private void printDefaultValue(PrintWriter out, String retName) {
        if (retName.equals("int") || retName.equals("byte")
                || retName.equals("short") || retName.equals("long")
                || retName.equals("float") || retName.equals("double")
                || retName.equals("char"))
            out.print("0");
        else if (retName.equals("boolean"))
            out.print("false");
        else
            out.print("null");
    }

    private void printParams(MethodNode methodNode, PrintWriter out) {
        out.print("(");
        Parameter[] parameters = methodNode.getParameters();
        if (parameters != null && parameters.length != 0) {
            for (int i = 0; i != parameters.length - 1; ++i) {
                out.print(parameters[i].getType().getName() + " "
                        + parameters[i].getName());
                out.print(", ");
            }
            out.print(parameters[parameters.length - 1].getType().getName()
                    + " " + parameters[parameters.length - 1].getName());
        }
        out.print(")");
    }

    private void printModifiers(PrintWriter out, int modifiers) {
        if ((modifiers & Opcodes.ACC_PUBLIC) != 0)
            out.print("public ");

        if ((modifiers & Opcodes.ACC_PROTECTED) != 0)
            out.print("protected ");

        if ((modifiers & Opcodes.ACC_PRIVATE) != 0)
            out.print("private ");

        if ((modifiers & Opcodes.ACC_STATIC) != 0)
            out.print("static ");

        if ((modifiers & Opcodes.ACC_SYNCHRONIZED) != 0)
            out.print("synchronized ");

        if ((modifiers & Opcodes.ACC_ABSTRACT) != 0)
            out.print("abstract ");
    }

    private void genImports(ClassNode classNode, PrintWriter out) {
        HashSet imports = new HashSet();

        ModuleNode moduleNode = classNode.getModule();
        for (Iterator it = moduleNode.getImportPackages().iterator(); it
                .hasNext();) {
            imports.add(it.next());
        }

        for (Iterator it = moduleNode.getImports().iterator(); it.hasNext();) {
            ImportNode imp = (ImportNode) it.next();
            String name = imp.getType().getName();
            int lastDot = name.lastIndexOf('.');
            if (lastDot != -1)
                imports.add(name.substring(0, lastDot + 1));
        }

        for (Iterator it = imports.iterator(); it.hasNext();) {
            String imp = (String) it.next();
            out.println("import " + imp + "*;");
        }
        out.println();
    }

    public void clean() {
        for (Iterator it = toCompile.iterator(); it.hasNext();) {
            String path = (String) it.next();
            new File(outputPath, path + ".java").delete();
        }
    }
}
