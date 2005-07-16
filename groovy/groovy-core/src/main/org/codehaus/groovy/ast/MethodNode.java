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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.VariableScopeCodeVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Represents a method declaration
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MethodNode extends AnnotatedNode implements Opcodes {

    private String name;
    private int modifiers;
    private String returnType;
    private Parameter[] parameters;
    private boolean hasDefaultValue = false;
    private Statement code;
    private boolean dynamicReturnType;
    private VariableScope variableScope;

    public MethodNode(String name, int modifiers, String returnType, Parameter[] parameters, Statement code) {
        this.name = name;
        this.modifiers = modifiers;
        this.parameters = parameters;
        this.code = code;

        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].hasDefaultValue()) {
                    this.hasDefaultValue = true;
                    break;
                }
            }
        }

        if (returnType == null || returnType.length() == 0) {
            this.returnType = "java.lang.Object";
            this.dynamicReturnType = true;
        }
        else {
            this.returnType = ensureJavaTypeNameSyntax(returnType);
        }
    }

    /**
     * The type descriptor for a method node is a string containing the name of the method, its return type,
     * and its parameter types in a canonical form. For simplicity, I'm using the format of a Java declaration
     * without parameter names, and with $dynamic as the type for any dynamically typed values.
     *
     * @return
     */
    // TODO: add test case for type descriptor
    public String getTypeDescriptor() {
        StringBuffer buf = new StringBuffer();
        // buf.append(dynamicReturnType ? "$dynamic" : cleanupTypeName(returnType));
        //
        buf.append(ensureJavaTypeNameSyntax(returnType)); // br  to replace the above. Dynamic type returns Object.
        //
        buf.append(' ');
        buf.append(name);
        buf.append('(');
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                buf.append(',');
            }
            Parameter param = parameters[i];
            buf.append(ensureJavaTypeNameSyntax(param.getType()));
        }
        buf.append(')');
        return buf.toString();
    }

    public static String ensureJavaTypeNameSyntax(String typename) {
        // if the typename begins with "[", ends with ";", or is
        // one character long, it's in .class syntax.
        if (typename.charAt(0) == '[') {
            return ensureJavaTypeNameSyntax(typename.substring(1)) + "[]";
        }
        if (typename.length() == 1) {
            switch (typename.charAt(0)) {
                case 'B':
                    return "byte";
                case 'C':
                    return "char";
                case 'D':
                    return "double";
                case 'F':
                    return "float";
                case 'J':
                    return "long";
                case 'I':
                    return "int";
                case 'S':
                    return "short";
                case 'V':
                    return "void";
                case 'Z':
                    return "boolean";
            }
        }
        if (typename.endsWith(";")) {
            // Type should be "Lclassname;"
            return typename.substring(1, typename.length() - 1);
        }
        return typename;

    }

    public boolean isVoidMethod() {
        return "void".equals(returnType);
    }

    public Statement getCode() {
        return code;
    }

    public void setCode(Statement code) {
        this.code = code;
    }

    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public VariableScope getVariableScope() {
        if (variableScope == null) {
            variableScope = createVariableScope();
        }
        return variableScope;
    }

    public void setVariableScope(VariableScope variableScope) {
        this.variableScope = variableScope;
    }

    public boolean isDynamicReturnType() {
        return dynamicReturnType;
    }

    public boolean isAbstract() {
        return (modifiers & ACC_ABSTRACT) != 0;
    }

    public boolean isStatic() {
        return (modifiers & ACC_STATIC) != 0;
    }

    public boolean hasDefaultValue() {
        return this.hasDefaultValue;
    }

    public String toString() {
        return super.toString() + "[name: " + name + "]";
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }


    protected VariableScope createVariableScope() {
        VariableScope variableScope = new VariableScope();
        VariableScopeCodeVisitor visitor = new VariableScopeCodeVisitor(variableScope);
        visitor.setParameters(getParameters());
        Statement code = getCode();
        if (code != null) {
            code.visit(visitor);
        }
        addFieldsToVisitor(variableScope);
        return variableScope;
    }
}
