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

import org.codehaus.groovy.ast.Parameter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;

/**
 * A helper class for bytecode generation
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class BytecodeHelper implements Constants {

    private CodeVisitor cv;

    public BytecodeHelper(CodeVisitor cv) {
        this.cv = cv;
    }

    /**
     * Generates the bytecode to autobox the current value on the stack
     */
    public void box(Class type) {
        if (type.isPrimitive() && type != void.class) {
            String returnString = "(" + getTypeDescription(type.getName()) + ")Ljava/lang/Object;";
            cv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/InvokerHelper", "box", returnString);
        }
    }

    /**
     * Generates the bytecode to unbox the current value on the stack
     */
    public void unbox(Class type) {
        if (type.isPrimitive() && type != void.class) {
            String returnString = "(Ljava/lang/Object;)" + getTypeDescription(type.getName());
            cv.visitMethodInsn(
                INVOKESTATIC,
                "org/codehaus/groovy/runtime/InvokerHelper",
                type.getName() + "Unbox",
                returnString);
        }
    }

    /**
     * Generates the bytecode to unbox the current value on the stack
     */
    public void unbox(String type) {
        if (isPrimitiveType(type) && !type.equals("void")) {
            String returnString = "(Ljava/lang/Object;)" + getTypeDescription(type);
            cv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/InvokerHelper", type + "Unbox", returnString);
        }
    }

    public boolean isPrimitiveType(String type) {
        return type != null
            && (type.equals("boolean")
                || type.equals("byte")
                || type.equals("char")
                || type.equals("short")
                || type.equals("int")
                || type.equals("long")
                || type.equals("float")
                || type.equals("double"));
    }

    /**
     * @return the ASM type description
     */
    public String getTypeDescription(String name) {
        // lets avoid class loading
        // return getType(name).getDescriptor();
        if (name == null) {
            return "Ljava/lang/Object;";
        }
        if (name.equals("void")) {
            return "V";
        }
        String prefix = "";
        if (name.endsWith("[]")) {
            prefix = "[";
            name = name.substring(0, name.length() - 2);
        }
        if (name.equals("int")) {
            return prefix + "I";
        }
        if (name.equals("long")) {
            return prefix + "J";
        }
        if (name.equals("short")) {
            return prefix + "S";
        }
        if (name.equals("float")) {
            return prefix + "F";
        }
        if (name.equals("double")) {
            return prefix + "D";
        }
        if (name.equals("byte")) {
            return prefix + "B";
        }
        if (name.equals("char")) {
            return prefix + "C";
        }
        if (name.equals("boolean")) {
            return prefix + "Z";
        }
        return prefix + "L" + name.replace('.', '/') + ";";
    }

    /**
     * @return the ASM internal name of the type
     */
    protected String getClassInternalName(String name) {
        if (name == null) {
            return "java/lang/Object";
        }
        String answer = name.replace('.', '/');
        if (answer.endsWith("[]")) {
            return "[" + answer.substring(0, answer.length() - 2);
        }
        return answer;
    }

    /**
     * @return the ASM method type descriptor
     */
    protected String getMethodDescriptor(String returnTypeName, Parameter[] paramTypeNames) {
        // lets avoid class loading
        StringBuffer buffer = new StringBuffer("(");
        for (int i = 0; i < paramTypeNames.length; i++) {
            buffer.append(getTypeDescription(paramTypeNames[i].getType()));
        }
        buffer.append(")");
        buffer.append(getTypeDescription(returnTypeName));
        return buffer.toString();
    }

    /**
     * @return the ASM method type descriptor
     */
    protected String getMethodDescriptor(Class returnType, Class[] paramTypes) {
        // lets avoid class loading
        StringBuffer buffer = new StringBuffer("(");
        for (int i = 0; i < paramTypes.length; i++) {
            buffer.append(getTypeDescription(paramTypes[i]));
        }
        buffer.append(")");
        buffer.append(getTypeDescription(returnType));
        return buffer.toString();
    }

    public String getTypeDescription(Class type) {
        if (type.isArray()) {
            return type.getName().replace('.', '/');
        }
        else {
            return getTypeDescription(type.getName());
        }
    }

    /**
     * @return an array of ASM internal names of the type
     */
    protected String[] getClassInternalNames(String[] names) {
        int size = names.length;
        String[] answer = new String[size];
        for (int i = 0; i < size; i++) {
            answer[i] = getClassInternalName(names[i]);
        }
        return answer;
    }

    protected void pushConstant(boolean value) {
        if (value) {
            cv.visitInsn(ICONST_1);
        }
        else {
            cv.visitInsn(ICONST_0);
        }
    }

    protected void pushConstant(int value) {
        switch (value) {
            case 0 :
                cv.visitInsn(ICONST_0);
                break;
            case 1 :
                cv.visitInsn(ICONST_1);
                break;
            case 2 :
                cv.visitInsn(ICONST_2);
                break;
            case 3 :
                cv.visitInsn(ICONST_3);
                break;
            case 4 :
                cv.visitInsn(ICONST_4);
                break;
            case 5 :
                cv.visitInsn(ICONST_5);
                break;
            default :
                cv.visitIntInsn(BIPUSH, value);
                break;
        }
    }

    public void doCast(String type) {
        if (!type.equals("java.lang.Object")) {
            cv.visitTypeInsn(CHECKCAST, type.endsWith("[]") ? getTypeDescription(type) : getClassInternalName(type));
        }
    }

    public void doCast(Class type) {
        String name = type.getName();
        if (type.isArray()) {
            name = type.getComponentType().getName() + "[]";
        }
        doCast(name);
    }
}
