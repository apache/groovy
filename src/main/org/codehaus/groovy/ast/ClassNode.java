/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
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
package org.codehaus.groovy.ast;

import groovy.lang.GroovyObject;
import groovy.lang.Script;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.objectweb.asm.Constants;

/**
 * Represents a class declaration
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ClassNode extends MetadataNode implements Constants {

    private Logger log = Logger.getLogger(getClass().getName());

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
    private ModuleNode module;
    private CompileUnit compileUnit;
    private boolean staticClass = false;
    private boolean scriptBody = false;
    private boolean script;

    private ClassNode superClassNode;

    /**
     * @param name
     *            is the full name of the class
     * @param modifiers
     *            the modifiers,
     * @see org.objectweb.asm.Constants
     * @param superClass
     *            the base class name - use "java.lang.Object" if no direct
     *            base class
     */
    public ClassNode(String name, int modifiers, String superClass) {
        this(name, modifiers, superClass, EMPTY_STRING_ARRAY, MixinNode.EMPTY_ARRAY);
    }

    /**
     * @param name
     *            is the full name of the class
     * @param modifiers
     *            the modifiers,
     * @see org.objectweb.asm.Constants
     * @param superClass
     *            the base class name - use "java.lang.Object" if no direct
     *            base class
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

    public ModuleNode getModule() {
        return module;
    }

    public void setModule(ModuleNode module) {
        this.module = module;
        if (module != null) {
            this.compileUnit = module.getUnit();
        }
    }

    public void addField(FieldNode node) {
        fields.add(node);
        fieldIndex.put(node.getName(), node);
    }

    public void addProperty(PropertyNode node) {
        FieldNode field = node.getField();
        addField(field);

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
        node.declaringClass = this;
    }

    /**
     * IF a method with the given name and parameters is already defined then it is returned
     * otherwise the given method is added to this node. This method is useful for
     * default method adding like getProperty() or invokeMethod() where there may already
     * be a method defined in a class and  so the default implementations should not be added
     * if already present.
     */
    public MethodNode addMethod(
        String name,
        int modifiers,
        String returnType,
        Parameter[] parameters,
        Statement code) {
        MethodNode other = getDeclaredMethod(name, parameters);
        // lets not add duplicate methods
        if (other != null) {
            return other;
        }
        MethodNode node = new MethodNode(name, modifiers, returnType, parameters, code);
        addMethod(node);
        return node;
    }

    /** 
     * Adds a synthetic method as part of the compilation process
     */
    public MethodNode addSyntheticMethod(
        String name,
        int modifiers,
        String returnType,
        Parameter[] parameters,
        Statement code) {
        MethodNode answer = addMethod(name, modifiers, returnType, parameters, code);
        answer.setSynthetic(true);
        return answer;
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
     * @return the field node on the outer class or null if this is not an
     *         inner class
     */
    public FieldNode getOuterField(String name) {
        return null;
    }

    /**
     * Helper method to avoid casting to inner class
     * 
     * @return
     */
    public ClassNode getOuterClass() {
        return null;
    }

    public void addStaticInitializerStatements(List staticStatements) {
        MethodNode method = null;
        List methods = getDeclaredMethods("<clinit>");
        if (methods.isEmpty()) {
            method =
                addMethod("<clinit>", ACC_PUBLIC | ACC_STATIC, "void", Parameter.EMPTY_ARRAY, new BlockStatement());
        }
        else {
            method = (MethodNode) methods.get(0);
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

    /**
     * @return a list of methods which match the given name
     */
    public List getDeclaredMethods(String name) {
        List answer = new ArrayList();
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (name.equals(method.getName())) {
                answer.add(method);
            }
        }
        return answer;
    }

    /**
     * @return a list of methods which match the given name
     */
    public List getMethods(String name) {
        List answer = new ArrayList();
        ClassNode node = this;
        do {
            for (Iterator iter = node.methods.iterator(); iter.hasNext();) {
                MethodNode method = (MethodNode) iter.next();
                if (name.equals(method.getName())) {
                    answer.add(method);
                }
            }
            node = node.getSuperClassNode();
        }
        while (node != null);
        return answer;
    }

    /**
     * @return the method matching the given name and parameters or null
     */
    public MethodNode getDeclaredMethod(String name, Parameter[] parameters) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (name.equals(method.getName()) && parametersEqual(method.getParameters(), parameters)) {
                return method;
            }
        }
        return null;
    }

    /**
     * @return true if this node is derived from the given class node
     */
    public boolean isDerivedFrom(String name) {
        ClassNode node = getSuperClassNode();
        while (node != null) {
            if (name.equals(node.getName())) {
                return true;
            }
            node = node.getSuperClassNode();
        }
        return false;
    }

    /**
     * @return true if this class is derived from a groovy object
     * i.e. it implements GroovyObject
     */
    public boolean isDerivedFromGroovyObject() {
        return implementsInteface(GroovyObject.class.getName());
    }

    /**
     * @param name the fully qualified name of the interface
     * @return true if this class or any base class implements the given interface
     */
    public boolean implementsInteface(String name) {
        ClassNode node = this;
        do {
            if (node.declaresInterface(name)) {
                return true;
            }
            node = node.getSuperClassNode();
        }
        while (node != null);
        return false;
    }

    /**
     * @param name the fully qualified name of the interface
     * @return true if this class declares that it implements the given interface
     */
    public boolean declaresInterface(String name) {
        int size = interfaces.length;
        for (int i = 0; i < size; i++ ) {
            if (name.equals(interfaces[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the ClassNode of the super class of this type
     */
    public ClassNode getSuperClassNode() {
        if (superClass != null && superClassNode == null && !name.equals("java.lang.Object")) {
            // lets try find the class in the compile unit
            superClassNode = findClassNode(superClass);
        }
        return superClassNode;
    }

    /**
     * Attempts to lookup the fully qualified class name in the compile unit or classpath
     * 
     * @param type fully qulified type name
     * @return the ClassNode for this type or null if it could not be found
     */
    public ClassNode findClassNode(String type) {
        ClassNode answer = null;
        CompileUnit compileUnit = getCompileUnit();
        if (compileUnit != null) {
            answer = compileUnit.getClass(type);
            if (answer == null) {
                Class theClass;
                try {
                    theClass = compileUnit.loadClass(type);
                    answer = createClassNode(theClass);
                }
                catch (ClassNotFoundException e) {
                    // lets ignore class not found exceptions
                    log.warning("Cannot find class: " + type + " due to: " + e);
                }
            }
        }
        return answer;
    }

    protected ClassNode createClassNode(Class theClass) {
        Class[] interfaces = theClass.getInterfaces();
        int size = interfaces.length;
        String[] interfaceNames = new String[size];
        for (int i = 0; i < size; i++) {
            interfaceNames[i] = interfaces[i].getName();
        }
        
        String name = null;
        if (theClass.getSuperclass() != null) {
            name = theClass.getSuperclass().getName();
        }
        ClassNode answer =
            new ClassNode(
                theClass.getName(),
                theClass.getModifiers(),
                name,
                interfaceNames,
                MixinNode.EMPTY_ARRAY);
        answer.compileUnit = getCompileUnit();
        Method[] methods = theClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++ ) {
            answer.addMethod(createMethodNode(methods[i]));
        }
        return answer;
    }


    /**
     * Factory method to create a new MethodNode for the given Method
     */
    protected MethodNode createMethodNode(Method method) {
        Parameter[] parameters = Parameter.EMPTY_ARRAY;
        Class[] types = method.getParameterTypes();
        int size = types.length;
        if (size > 0) {
            parameters = new Parameter[size];
            for (int i = 0; i < size; i++) {
                parameters[i] = createParameter(method, types[i], i);
            }
        }
        return new MethodNode(method.getName(), method.getModifiers(), method.getReturnType().getName(), parameters, EmptyStatement.INSTANCE);
    }

    protected Parameter createParameter(Method method, Class parameterType, int idx) {
        return new Parameter(parameterType.getName(), "param" + idx);
    }

    /**
     * Tries to create a Class node for the given type name
     * 
     * @param type
     * @return
     */
    /*
    public ClassNode resolveClass(String type) {
        if (type != null) {
            if (getNameWithoutPackage().equals(type)) {
                return this;
            }
            for (int i = 0; i < 2; i++) {
                CompileUnit compileUnit = getCompileUnit();
                if (compileUnit != null) {
                    ClassNode classNode = compileUnit.getClass(type);
                    if (classNode != null) {
                        return classNode;
                    }
    
                    try {
                        Class theClass = compileUnit.loadClass(type);
                        return createClassNode(theClass);
                    }
                    catch (Throwable e) {
                        // fall through
                    }
                }
    
                // lets try class in same package
                String packageName = getPackageName();
                if (packageName == null || packageName.length() <= 0) {
                    break;
                }
                type = packageName + "." + type;
            }
        }
        return null;
    }
    */

    public String resolveClassName(String type) {
        String answer = null;
        if (type != null) {
            if (getName().equals(type) || getNameWithoutPackage().equals(type)) {
                return getName();
            }
            answer = tryResolveClassFromCompileUnit(type);
            if (answer == null) {
                // lets try class in same package
                String packageName = getPackageName();
                if (packageName != null && packageName.length() > 0) {
                    answer = tryResolveClassFromCompileUnit(packageName + "." + type);
                }
            }
            if (answer == null) {
                // lets try use the packages imported in the module
                if (module != null) {
                    //System.out.println("Looking up inside the imported packages: " + module.getImportPackages());
                    
                    for (Iterator iter = module.getImportPackages().iterator(); iter.hasNext(); ) {
                        String packageName = (String) iter.next();
                        answer = tryResolveClassFromCompileUnit(packageName + type);
                        if (answer != null) {
                            return answer;
                        }
                    }
                }
            }
        }
        return answer;
    }

    /**
     * @param type
     * @return
     */
    protected String tryResolveClassFromCompileUnit(String type) {
        CompileUnit compileUnit = getCompileUnit();
        if (compileUnit != null) {
            if (compileUnit.getClass(type) != null) {
                return type;
            }

            try {
                compileUnit.loadClass(type);
                return type;
            }
            catch (Throwable e) {
                // fall through
            }
        }
        return null;
    }

    public CompileUnit getCompileUnit() {
        if (compileUnit == null && module != null) {
            compileUnit = module.getUnit();
        }
        return compileUnit;
    }

    /**
     * @return true if the two arrays are of the same size and have the same contents
     */
    protected boolean parametersEqual(Parameter[] a, Parameter[] b) {
        if (a.length == b.length) {
            boolean answer = true;
            for (int i = 0; i < a.length; i++) {
                if (!a[i].getType().equals(b[i].getType())) {
                    answer = false;
                    break;
                }
            }
            return answer;
        }
        return false;
    }

    /**
     * @return the name of the class for the given identifier if it is a class
     * otherwise return null
     */
    public String getClassNameForExpression(String identifier) {
        // lets see if it really is a class name
        String className = null;
        if (module != null) {
            className = module.getImport(identifier);
            if (className == null) {
                if (module.getUnit().getClass(identifier) != null) {
                    className = identifier;
                }
                else {
                    // lets prepend the package name to see if its in our
                    // package
                    String packageName = getPackageName();
                    if (packageName != null) {
                        String guessName = packageName + "." + identifier;
                        if (module.getUnit().getClass(guessName) != null) {
                            className = guessName;
                        }
                        else if (guessName.equals(name)) {
                            className = name;
                        }
                    }
                }
            }
        }
        else {
            System.out.println("No module for class: " + getName());
        }
        return className;
    }

    /**
     * @return the package name of this class
     */
    public String getPackageName() {
        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            return name.substring(0, idx);
        }
        return null;
    }

    public String getNameWithoutPackage() {
        int idx = name.lastIndexOf('.');
        if (idx > 0) {
            return name.substring(idx + 1);
        }
        return name;
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

    public MethodNode getGetterMethod(String getterName) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (getterName.equals(method.getName())
                && !"void".equals(method.getReturnType())
                && method.getParameters().length == 0) {
                return method;
            }
        }
        return null;
    }

    public MethodNode getSetterMethod(String getterName) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (getterName.equals(method.getName())
                && "void".equals(method.getReturnType())
                && method.getParameters().length == 1) {
                return method;
            }
        }
        return null;
    }

    /**
     * Is this class delcared in a static method (such as a closure / inner class declared in a static method)
     * @return
     */
    public boolean isStaticClass() {
        return staticClass;
    }

    public void setStaticClass(boolean staticClass) {
        this.staticClass = staticClass;
    }

    /**
     * @return Returns true if this inner class or closure was declared inside a script body
     */
    public boolean isScriptBody() {
        return scriptBody;
    }

    public void setScriptBody(boolean scriptBody) {
        this.scriptBody = scriptBody;
    }

    public boolean isScript() {
        return script | isDerivedFrom(Script.class.getName());
    }

    public void setScript(boolean script) {
        this.script = script;
    }

    public String toString() {
        return super.toString() + "[name: " + name + "]";
    }

   }
