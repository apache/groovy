/*
 * $Id$
 *
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 *
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
package org.codehaus.groovy.classgen;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingClassException;
import org.codehaus.groovy.ast.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;

import java.util.*;

/**
 * To generate a class that has all the fields and methods, except that fields are not initilized
 * and methods are empty. It's intended for being used as a place holder during code generation
 * of reference to the "this" class itself.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="mailto:b55r@sina.com">Bing Ran</a>
 *
 * @version $Revision$
 */
public class DummyClassGenerator extends ClassGenerator {

    private ClassVisitor cw;
    private CodeVisitor cv;
    private GeneratorContext context;

    private String sourceFile;

    // current class details
    private ClassNode classNode;
    private String internalClassName;
    private String internalBaseClassName;


    public DummyClassGenerator(
        GeneratorContext context,
        ClassVisitor classVisitor,
        ClassLoader classLoader,
        String sourceFile) {
        super(classLoader);
        this.context = context;
        this.cw = classVisitor;
        this.sourceFile = sourceFile;
    }

    // GroovyClassVisitor interface
    //-------------------------------------------------------------------------
    public void visitClass(ClassNode classNode) {
        try {
            this.classNode = classNode;
            this.internalClassName = BytecodeHelper.getClassInternalName(classNode.getName());

            //System.out.println("Generating class: " + classNode.getName());

            // lets check that the classes are all valid
            classNode.setSuperClass(checkValidType(classNode.getSuperClass(), classNode, "Must be a valid base class"));
            String[] interfaces = classNode.getInterfaces();
            for (int i = 0; i < interfaces.length; i++ ) {
                interfaces[i] = checkValidType(interfaces[i], classNode, "Must be a valid interface name");
            }

            this.internalBaseClassName = BytecodeHelper.getClassInternalName(classNode.getSuperClass());

            cw.visit(
                classNode.getModifiers(),
                internalClassName,
                internalBaseClassName,
                BytecodeHelper.getClassInternalNames(classNode.getInterfaces()),
                sourceFile);

            classNode.visitContents(this);

            for (Iterator iter = innerClasses.iterator(); iter.hasNext();) {
                ClassNode innerClass = (ClassNode) iter.next();
                String innerClassName = innerClass.getName();
                String innerClassInternalName = BytecodeHelper.getClassInternalName(innerClassName);
                String outerClassName = internalClassName; // default for inner classes
                MethodNode enclosingMethod = innerClass.getEnclosingMethod();
                if (enclosingMethod != null) {
                    // local inner classes do not specify the outer class name
                    outerClassName = null;
                }
                cw.visitInnerClass(
                    innerClassInternalName,
                    outerClassName,
                    innerClassName,
                    innerClass.getModifiers());
            }
            cw.visitEnd();
        }
        catch (GroovyRuntimeException e) {
            e.setModule(classNode.getModule());
            throw e;
        }
    }

    public void visitConstructor(ConstructorNode node) {

        visitParameters(node, node.getParameters());

        String methodType = BytecodeHelper.getMethodDescriptor("void", node.getParameters());
        cv = cw.visitMethod(node.getModifiers(), "<init>", methodType, null, null);
        cv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        cv.visitInsn(DUP);
        cv.visitLdcInsn("not intended for execution");
        cv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
        cv.visitInsn(ATHROW);
        cv.visitMaxs(0, 0);
    }

    public void visitMethod(MethodNode node) {

        visitParameters(node, node.getParameters());
        node.setReturnType(checkValidType(node.getReturnType(), node, "Must be a valid return type"));

        String methodType = BytecodeHelper.getMethodDescriptor(node.getReturnType(), node.getParameters());
        cv = cw.visitMethod(node.getModifiers(), node.getName(), methodType, null, null);

        cv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        cv.visitInsn(DUP);
        cv.visitLdcInsn("not intended for execution");
        cv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
        cv.visitInsn(ATHROW);

        cv.visitMaxs(0, 0);
    }

    public void visitField(FieldNode fieldNode) {

        // lets check that the classes are all valid
        fieldNode.setType(checkValidType(fieldNode.getType(), fieldNode, "Must be a valid field class for field: " + fieldNode.getName()));

        cw.visitField(
            fieldNode.getModifiers(),
            fieldNode.getName(),
            BytecodeHelper.getTypeDescription(fieldNode.getType()),
            null, //fieldValue,  //br  all the sudden that one cannot init the field here. init is done in static initilizer and instace intializer.
            null);
    }

    /**
     * Creates a getter, setter and field
     */
    public void visitProperty(PropertyNode statement) {
    }


    protected String checkValidType(String type, ASTNode node, String message) {
        if (type!= null && type.length() == 0)
            return "java.lang.Object";
        if (type.endsWith("[]")) {
            String postfix = "[]";
            String prefix = type.substring(0, type.length() - 2);
            return checkValidType(prefix, node, message) + postfix;
        }
        int idx = type.indexOf('$');
        if (idx > 0) {
            String postfix = type.substring(idx);
            String prefix = type.substring(0, idx);
            return checkValidType(prefix, node, message) + postfix;
        }
        if (BytecodeHelper.isPrimitiveType(type) || "void".equals(type)) {
            return type;
        }
        String original = type;
        type = resolveClassName(type);
        if (type != null) {
            return type;
        }

        throw new MissingClassException(original, node, message + " for class: " + classNode.getName());
    }
    protected String resolveClassName(String type) {
        return classNode.resolveClassName(type);
    }

    protected static boolean isPrimitiveFieldType(String type) {
        return type.equals("java.lang.String")
            || type.equals("java.lang.Integer")
            || type.equals("java.lang.Double")
            || type.equals("java.lang.Long")
            || type.equals("java.lang.Float");
    }
    protected Class loadClass(String name) {
        if (name.equals(this.classNode.getName())) {
            return Object.class;
        }

        if (name == null) {
            return null;
        }
        else if (name.length() == 0) {
            return Object.class;
        }

        else if ("void".equals(name)) {
            return void.class;
        }
        else if ("boolean".equals(name)) {
            return boolean.class;
        }
        else if ("byte".equals(name)) {
            return byte.class;
        }
        else if ("short".equals(name)) {
            return short.class;
        }
        else if ("char".equals(name)) {
            return char.class;
        }
        else if ("int".equals(name)) {
            return int.class;
        }
        else if ("long".equals(name)) {
            return long.class;
        }
        else if ("float".equals(name)) {
            return float.class;
        }
        else if ("double".equals(name)) {
            return double.class;
        }

        name = BytecodeHelper.formatNameForClassLoading(name);

    	try {
    		Class cls = (Class)classCache.get(name);
    		if (cls != null)
    			return cls;

    		CompileUnit compileUnit = getCompileUnit();
            if (compileUnit != null) {
            	cls = compileUnit.loadClass(name);
                classCache.put(name, cls);
            	return cls;
            }
            else {
                throw new ClassGeneratorException("Could not load class: " + name);
            }
        }
        catch (ClassNotFoundException e) {
            throw new ClassGeneratorException("Error when compiling class: " + classNode.getName() + ". Reason: could not load class: " + name + " reason: " + e, e);
        }
    }

    Map classCache = new HashMap();
    {
        classCache.put("int", Integer.TYPE);
        classCache.put("byte", Byte.TYPE);
        classCache.put("short", Short.TYPE);
        classCache.put("char", Character.TYPE);
        classCache.put("boolean", Boolean.TYPE);
        classCache.put("long", Long.TYPE);
        classCache.put("double", Double.TYPE);
        classCache.put("float", Float.TYPE);
    }
    protected CompileUnit getCompileUnit() {
        CompileUnit answer = classNode.getCompileUnit();
        if (answer == null) {
            answer = context.getCompileUnit();
        }
        return answer;
    }

    protected void visitParameters(ASTNode node, Parameter[] parameters) {
        for (int i = 0, size = parameters.length; i < size; i++ ) {
            visitParameter(node, parameters[i]);
        }
    }

    protected void visitParameter(ASTNode node, Parameter parameter) {
        if (! parameter.isDynamicType()) {
            parameter.setType(checkValidType(parameter.getType(), node, "Must be a valid parameter class"));
        }
    }

}
