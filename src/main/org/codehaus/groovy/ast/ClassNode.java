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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.syntax.Token;
import org.objectweb.asm.Constants;

/**
 * Represents a class declaration
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ClassNode extends MetadataNode implements Constants {

    private String name;
    private int modifiers;
    private String superClass;
    private String[] interfaces;
    private MixinNode[] mixins;
    private List constructors = new ArrayList();
    private List methods = new ArrayList();
    private List fields = new ArrayList();
    private List properties = new ArrayList();
    private Map fieldIndex = new HashMap();

    /**
     * @param name is the full name of the class
     * @param modifiers the modifiers, @see org.objectweb.asm.Constants
     * @param superClass the base class name - use "java.lang.Object" if no direct base class
     */
    public ClassNode(String name, int modifiers, String superClass) {
        this(name, modifiers, superClass, EMPTY_STRING_ARRAY, MixinNode.EMPTY_ARRAY);
    }

    /**
     * @param name is the full name of the class
     * @param modifiers the modifiers, @see org.objectweb.asm.Constants
     * @param superClass the base class name - use "java.lang.Object" if no direct base class
     */
    public ClassNode(String name, int modifiers, String superClass, String[] interfaces, MixinNode[] mixins) {
        this.name = name;
        this.modifiers = modifiers;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.mixins = mixins;
    }

    public String getSuperClass() {
        return superClass;
    }

    public List getFields() {
        return fields;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public MixinNode[] getMixins() {
        return mixins;
    }

    public List getMethods() {
        return methods;
    }

    public String getName() {
        return name;
    }

    public int getModifiers() {
        return modifiers;
    }

    public List getProperties() {
        return properties;
    }

    public List getConstructors() {
        return constructors;
    }

    public void addField(FieldNode node) {
        fields.add(node);
        fieldIndex.put(node.getName(), node);
    }

    public void addProperty(PropertyNode node) {
        FieldNode field = node.getField();
        addField(field);

        String name = node.getName();
        String getterName = "get" + capitalize(name);
        String setterName = "set" + capitalize(name);

        Statement getterBlock = node.getGetterBlock();
        if (getterBlock == null) {
            getterBlock = createGetterBlock(node, field);
        }
        Statement setterBlock = node.getGetterBlock();
        if (setterBlock == null) {
            setterBlock = createSetterBlock(node, field);
        }

        MethodNode getter =
            new MethodNode(getterName, node.getModifiers(), node.getType(), Parameter.EMPTY_ARRAY, getterBlock);

        addMethod(getter);

        Parameter[] setterParameterTypes = { new Parameter(node.getType(), "value")};
        MethodNode setter = new MethodNode(setterName, node.getModifiers(), "void", setterParameterTypes, setterBlock);
        addMethod(setter);

        properties.add(node);
    }

    public PropertyNode addProperty(
        String name,
        int modifiers,
        String type,
        Expression initialValueExpression,
        Statement getterBlock,
        Statement setterBlock) {
        PropertyNode node =
            new PropertyNode(name, modifiers, type, getName(), initialValueExpression, getterBlock, setterBlock);
        addProperty(node);
        return node;
    }

    public void addConstructor(ConstructorNode node) {
        constructors.add(node);
    }

    public ConstructorNode addConstructor(int modifiers, Parameter[] parameters, Statement code) {
        ConstructorNode node = new ConstructorNode(modifiers, parameters, code);
        addConstructor(node);
        return node;
    }

    public void addMethod(MethodNode node) {
        methods.add(node);
    }

    public MethodNode addMethod(
        String name,
        int modifiers,
        String returnType,
        Parameter[] parameters,
        Statement code) {
        MethodNode node = new MethodNode(name, modifiers, returnType, parameters, code);
        addMethod(node);
        return node;
    }

    public FieldNode addField(String name, int modifiers, String type, Expression initialValue) {
        FieldNode node = new FieldNode(name, modifiers, type, getName(), initialValue);
        addField(node);
        return node;
    }

    public void addInterface(String name) {
        // lets check if it already implements an interface
        boolean skip = false;
        for (int i = 0; i < interfaces.length; i++) {
            if (name.equals(interfaces[i])) {
                skip = true;
            }
        }
        if (!skip) {
            String[] newInterfaces = new String[interfaces.length + 1];
            System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
            newInterfaces[interfaces.length] = name;
            interfaces = newInterfaces;
        }
    }

    public void addMixin(MixinNode mixin) {
        // lets check if it already uses a mixin
        boolean skip = false;
        String name = mixin.getName();
        for (int i = 0; i < mixins.length; i++) {
            if (name.equals(mixins[i].getName())) {
                skip = true;
            }
        }
        if (!skip) {
            MixinNode[] newMixins = new MixinNode[mixins.length + 1];
            System.arraycopy(mixins, 0, newMixins, 0, mixins.length);
            newMixins[mixins.length] = mixin;
            mixins = newMixins;
        }
    }

    public FieldNode getField(String name) {
        return (FieldNode) fieldIndex.get(name);
    }

    /**
     * @return the field node on the outer class or null if this is not an inner class
     */
    public FieldNode getOuterField(String name) {
        return null;
    }

    /**
     * Helper method to avoid casting to inner class
     * @return
     */
    public ClassNode getOuterClass() {
        return null;
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
        String name = propertyNode.getName();
        return new ExpressionStatement(
            new BinaryExpression(new FieldExpression(field), Token.equal(0, 0), new VariableExpression("value")));
    }

    public void addStaticInitializerStatements(List staticStatements) {
        MethodNode method = getMethod("<clinit>");
        if (method == null) {
            method = addMethod("<clinit>", ACC_PUBLIC | ACC_STATIC, "void", Parameter.EMPTY_ARRAY, new BlockStatement());
        }
        BlockStatement block = null;
        Statement statement = method.getCode();
        if (statement == null) {
            block = new BlockStatement();
        }
        else if (statement instanceof BlockStatement) {
            block = (BlockStatement) statement;
        }
        else {
            block = new BlockStatement();
            block.addStatement(statement);
        }
        block.addStatements(staticStatements);
    }

    public MethodNode getMethod(String name) {
        for (Iterator iter = methods.iterator(); iter.hasNext(); ) {
            MethodNode method = (MethodNode) iter.next();
            if (name.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }

    public void visitContents(GroovyClassVisitor visitor) {
        // now lets visit the contents of the class
        for (Iterator iter = getProperties().iterator(); iter.hasNext();) {
            visitor.visitProperty((PropertyNode) iter.next());
        }
    
        for (Iterator iter = getFields().iterator(); iter.hasNext();) {
            visitor.visitField((FieldNode) iter.next());
        }
    
        for (Iterator iter = getConstructors().iterator(); iter.hasNext();) {
            visitor.visitConstructor((ConstructorNode) iter.next());
        }
    
        for (Iterator iter = getMethods().iterator(); iter.hasNext();) {
            visitor.visitMethod((MethodNode) iter.next());
        }
    }

}
