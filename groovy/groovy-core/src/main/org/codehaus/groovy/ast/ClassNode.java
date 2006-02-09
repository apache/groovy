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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a class in the AST.<br/>
 * A ClassNode should be created using the methods in ClassHelper. 
 * This ClassNode may be used to represent a class declaration or
 * any other type. This class uses a proxy meschanism allowing to
 * create a class for a plain name at ast creation time. In another 
 * phase of the compiler the real ClassNode for the plain name may be
 * found. To avoid the need of exchanging this ClassNode with an 
 * instance of the correct ClassNode the correct ClassNode is set as 
 * redirect. All method calls are then redirected to that ClassNode.
 * <br>
 * Note: the proxy mechanism is only allowed for classes being marked
 * as primary ClassNode which means they represent no actual class. 
 * The redirect itself can be any type of ClassNode
 *
 * @see org.codehaus.groovy.ast.ClassHelper
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Jochen Theodorou
 * @version $Revision$
 */
public class ClassNode extends AnnotatedNode implements Opcodes {

	public static ClassNode[] EMPTY_ARRAY = new ClassNode[0];
	
    private String name;
    private int modifiers;
    private ClassNode[] interfaces;
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
    private ClassNode superClass;
    boolean isPrimaryNode;
    
    // use this to synchronize access for the lazy intit
    protected Object lazyInitLock = new Object();

    // clazz!=null when resolved
    protected Class clazz;
    // only false when this classNode is constructed from a class 
    private boolean lazyInitDone=true;
    // not null if if the ClassNode is an array 
    private ClassNode componentType = null;
    // if not null this instance is handled as proxy 
    // for the redirect
    private ClassNode redirect=null; 
    
    /**
     * Returns the ClassNode this ClassNode is redirecting to.
     */
    protected ClassNode redirect(){
        if (redirect==null) return this;
        return redirect.redirect();
    }
    
    /**
     * Sets this instance as proxy for the given ClassNode
     */
    public void setRedirect(ClassNode cn) {
        if (isPrimaryNode) throw new GroovyBugError("tried to set a redirect for a primary ClassNode ("+getName()+"->"+cn.getName()+").");
        redirect = cn.redirect();        
    }
    
    /**
     * Returns a ClassNode representing an array of the class
     * represented by this ClassNode
     */
    public ClassNode makeArray() {
        if (redirect!=null) return redirect().makeArray();
        ClassNode cn;
        if (clazz!=null) {
            Class ret = Array.newInstance(clazz,0).getClass();
            // don't use the ClassHelper here!
            cn = new ClassNode(ret,this);
        } else {
            cn = new ClassNode(this);
        }
        return cn;
    }
    
    /**
     * Returns if this instance is a primary ClassNode
     */
    public boolean isPrimaryClassNode(){
    	return redirect().isPrimaryNode || (componentType!= null && componentType.isPrimaryClassNode());
    }
    
    /**
     * Constructor used by makeArray() if no real class is available
     */
    private ClassNode(ClassNode componentType) {
        this(componentType.getName()+"[]", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        this.componentType = componentType.redirect();
        isPrimaryNode=false;
    }
    
    /**
     * Constructor used by makeArray() if a real class is available
     */
    private ClassNode(Class c, ClassNode componentType) {
        this(c);
        this.componentType = componentType;
        isPrimaryNode=false;
    }
    
    /**
     * Creates a ClassNode from a real class. The resulting 
     * ClassNode will be no primary ClassNode.
     */
    public ClassNode(Class c) {
        this(c.getName(), c.getModifiers(), null, null ,MixinNode.EMPTY_ARRAY);
        clazz=c;
        lazyInitDone=false;
        CompileUnit cu = getCompileUnit();
        if (cu!=null) cu.addClass(this);
        isPrimaryNode=false;
    }    
    
    /**
     * The complete class structure will be initialized only when really
     * needed to avoid having too much objects during compilation
     */
    private void lazyClassInit() {       
        synchronized (lazyInitLock) {
            if (lazyInitDone) return;
            
            Field[] fields = clazz.getDeclaredFields();
            for (int i=0;i<fields.length;i++){
                addField(fields[i].getName(),fields[i].getModifiers(),this,null);
            }
            Method[] methods = clazz.getDeclaredMethods();
            for (int i=0;i<methods.length;i++){
                Method m = methods[i];
                MethodNode mn = new MethodNode(m.getName(), m.getModifiers(), ClassHelper.make(m.getReturnType()), createParameters(m.getParameterTypes()), ClassHelper.make(m.getExceptionTypes()), null);
                addMethod(mn);
            }
            Constructor[] constructors = clazz.getConstructors();
            for (int i=0;i<constructors.length;i++){
                Constructor ctor = constructors[i];
                addConstructor(ctor.getModifiers(),createParameters(ctor.getParameterTypes()),ClassHelper.make(ctor.getExceptionTypes()),null);
            }
            Class sc = clazz.getSuperclass();
            if (sc!=null) superClass = ClassHelper.make(sc);
            buildInterfaceTypes(clazz);       
            lazyInitDone=true;
        }
    }
    
    private void buildInterfaceTypes(Class c) {
        Class[] interfaces = c.getInterfaces();
        ClassNode[] ret = new ClassNode[interfaces.length];
        for (int i=0;i<interfaces.length;i++){
            ret[i] = ClassHelper.make(interfaces[i]);
        }
        this.interfaces = ret;
    }
    
    
    // added to track the enclosing method for local inner classes
    private MethodNode enclosingMethod = null;

    public MethodNode getEnclosingMethod() {
        return redirect().enclosingMethod;
    }

    public void setEnclosingMethod(MethodNode enclosingMethod) {
        redirect().enclosingMethod = enclosingMethod;
    }


    /**
     * @param name       is the full name of the class
     * @param modifiers  the modifiers,
     * @param superClass the base class name - use "java.lang.Object" if no direct
     *                   base class
     * @see org.objectweb.asm.Opcodes
     */
    public ClassNode(String name, int modifiers, ClassNode superClass) {
        this(name, modifiers, superClass, ClassHelper.EMPTY_TYPE_ARRAY, MixinNode.EMPTY_ARRAY);
    }

    /**
     * @param name       is the full name of the class
     * @param modifiers  the modifiers,
     * @param superClass the base class name - use "java.lang.Object" if no direct
     *                   base class
     * @see org.objectweb.asm.Opcodes
     */
    public ClassNode(String name, int modifiers, ClassNode superClass, ClassNode[] interfaces, MixinNode[] mixins) {
        this.name = name;
        this.modifiers = modifiers;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.mixins = mixins;
        isPrimaryNode = true;
    }

    
    /**
     * Sets the superclass of this ClassNode
     */
    public void setSuperClass(ClassNode superClass) {
        redirect().superClass = superClass;
    }

    /**
     * Returns a list containing FieldNode objects for
     * each field in the class represented by this ClassNode
     */
    public List getFields() {
        if (!lazyInitDone) {
            lazyClassInit();
        }
        if (redirect!=null) return redirect().getFields();
        return fields;
    }

    /**
     * Returns an array of ClassNodes representing the
     * interfaces the class implements
     */
    public ClassNode[] getInterfaces() {
        if (!lazyInitDone) {
            lazyClassInit();
        }
        if (redirect!=null) return redirect().getInterfaces();
        return interfaces;
    }

    public MixinNode[] getMixins() {
        return redirect().mixins;
    }

    /**
     * Returns a list containing MethodNode objects for
     * each method in the class represented by this ClassNode
     */    
    public List getMethods() {
        if (!lazyInitDone) {
            lazyClassInit();
        }
        if (redirect!=null) return redirect().getMethods();
        return methods;
    }

    /**
     * Returns a list containing MethodNode objects for
     * each abstract method in the class represented by 
     * this ClassNode
     */   
    public List getAbstractMethods() {
        
        HashSet abstractNodes = new HashSet();
        // let us collect the abstract super classes and stop at the
        // first non abstract super class. If such a class still 
        // contains abstract methods, then loading that class will fail.
        // No need to be extra carefull here for that.
        abstractNodes.add(this.redirect());
        ClassNode parent = this.getSuperClass().redirect();
        while (parent!=null && ((parent.getModifiers() & Opcodes.ACC_ABSTRACT) != 0)) {
            abstractNodes.add(parent);
            parent = parent.getSuperClass().redirect();
        }

        List result = new ArrayList();
        for (Iterator methIt = getAllDeclaredMethods().iterator(); methIt.hasNext();) {
            MethodNode method = (MethodNode) methIt.next();
            // add only abstract methods from abtract classes that
            // are not overwritten
            if ( abstractNodes.contains(method.getDeclaringClass().redirect()) && 
                 (method.getModifiers() & Opcodes.ACC_ABSTRACT) != 0
               ) {
                result.add(method);
            }
        }
        if (result.size() == 0) {
            return null;
        }
        else {
            return result;
        }
    }

    public List getAllDeclaredMethods() {
        return new ArrayList(getDeclaredMethodsMap().values());
    }


    protected Map getDeclaredMethodsMap() {
        // Start off with the methods from the superclass.
        ClassNode parent = getSuperClass();
        Map result = null;
        if (parent != null) {
            result = parent.getDeclaredMethodsMap();
        }
        else {
            result = new HashMap();
        }

        // add in unimplemented abstract methods from the interfaces
        ClassNode[] interfaces = getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            ClassNode iface = interfaces[i];
            Map ifaceMethodsMap = iface.getDeclaredMethodsMap();
            for (Iterator iter = ifaceMethodsMap.keySet().iterator(); iter.hasNext();) {
                String methSig = (String) iter.next();
                if (!result.containsKey(methSig)) {
                    MethodNode methNode = (MethodNode) ifaceMethodsMap.get(methSig);
                    result.put(methSig, methNode);
                }
            }
        }

        // And add in the methods implemented in this class.
        for (Iterator iter = getMethods().iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            String sig = method.getTypeDescriptor();
            if (result.containsKey(sig)) {
                MethodNode inheritedMethod = (MethodNode) result.get(sig);
                if (inheritedMethod.isAbstract()) {
                    result.put(sig, method);
                }
            }
            else {
                result.put(sig, method);
            }
        }
        return result;
    }

    public String getName() {
        return redirect().name;
    }
    
    public String setName(String name) {
        return redirect().name=name;
    }

    public int getModifiers() {
        return redirect().modifiers;
    }

    public List getProperties() {
        return redirect().properties;
    }

    public List getDeclaredConstructors() {
        if (!lazyInitDone) {
            lazyClassInit();
        }
        return redirect().constructors;
    }

    public ModuleNode getModule() {
        return redirect().module;
    }

    public void setModule(ModuleNode module) {
        redirect().module = module;
        if (module != null) {
            redirect().compileUnit = module.getUnit();
        }
    }

    public void addField(FieldNode node) {
        node.setDeclaringClass(redirect());
        node.setOwner(redirect());
        redirect().fields.add(node);
        redirect().fieldIndex.put(node.getName(), node);
    }

    public void addProperty(PropertyNode node) {
        node.setDeclaringClass(redirect());
        FieldNode field = node.getField();
        addField(field);

        redirect().properties.add(node);
    }

    public PropertyNode addProperty(String name,
                                    int modifiers,
                                    ClassNode type,
                                    Expression initialValueExpression,
                                    Statement getterBlock,
                                    Statement setterBlock) {
    	for (Iterator iter = getProperties().iterator(); iter.hasNext();) {
			PropertyNode pn = (PropertyNode) iter.next();
			if (pn.getName().equals(name)) return pn;
		}
        PropertyNode node =
                new PropertyNode(name, modifiers, type, redirect(), initialValueExpression, getterBlock, setterBlock);
        addProperty(node);
        return node;
    }

    public void addConstructor(ConstructorNode node) {
        node.setDeclaringClass(this);
        redirect().constructors.add(node);
    }

    public ConstructorNode addConstructor(int modifiers, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        ConstructorNode node = new ConstructorNode(modifiers, parameters, exceptions, code);
        addConstructor(node);
        return node;
    }

    public void addMethod(MethodNode node) {
        node.setDeclaringClass(this);
        redirect().methods.add(node);
    }

    /**
     * IF a method with the given name and parameters is already defined then it is returned
     * otherwise the given method is added to this node. This method is useful for
     * default method adding like getProperty() or invokeMethod() where there may already
     * be a method defined in a class and  so the default implementations should not be added
     * if already present.
     */
    public MethodNode addMethod(String name,
                                int modifiers,
                                ClassNode returnType,
                                Parameter[] parameters,
                                ClassNode[] exceptions,
                                Statement code) {
        MethodNode other = getDeclaredMethod(name, parameters);
        // lets not add duplicate methods
        if (other != null) {
            return other;
        }
        MethodNode node = new MethodNode(name, modifiers, returnType, parameters, exceptions, code);
        addMethod(node);
        return node;
    }

    /**
     * Adds a synthetic method as part of the compilation process
     */
    public MethodNode addSyntheticMethod(String name,
                                         int modifiers,
                                         ClassNode returnType,
                                         Parameter[] parameters,
                                         ClassNode[] exceptions,
                                         Statement code) {
        MethodNode answer = addMethod(name, modifiers, returnType, parameters, exceptions, code);
        answer.setSynthetic(true);
        return answer;
    }

    public FieldNode addField(String name, int modifiers, ClassNode type, Expression initialValue) {
        FieldNode node = new FieldNode(name, modifiers, type, redirect(), initialValue);
        addField(node);
        return node;
    }

    public void addInterface(ClassNode type) {
        // lets check if it already implements an interface
        boolean skip = false;
        ClassNode[] interfaces = redirect().interfaces;
        for (int i = 0; i < interfaces.length; i++) {
            if (type.equals(interfaces[i])) {
                skip = true;
            }
        }
        if (!skip) {
            ClassNode[] newInterfaces = new ClassNode[interfaces.length + 1];
            System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
            newInterfaces[interfaces.length] = type;
            redirect().interfaces = newInterfaces;
        }
    }
    
    public boolean equals(Object o) {
        if (redirect!=null) return redirect().equals(o);
        ClassNode cn = (ClassNode) o;        
        return (cn.getName().equals(getName()));
    }

    public void addMixin(MixinNode mixin) {
        // lets check if it already uses a mixin
        MixinNode[] mixins = redirect().mixins;
        boolean skip = false;
        for (int i = 0; i < mixins.length; i++) {
            if (mixin.equals(mixins[i])) {
                skip = true;
            }
        }
        if (!skip) {
            MixinNode[] newMixins = new MixinNode[mixins.length + 1];
            System.arraycopy(mixins, 0, newMixins, 0, mixins.length);
            newMixins[mixins.length] = mixin;
            redirect().mixins = newMixins;
        }
    }

    public FieldNode getField(String name) {
        return (FieldNode) redirect().fieldIndex.get(name);
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

    public void addStaticInitializerStatements(List staticStatements, boolean fieldInit) {
        MethodNode method = null;
        List declaredMethods = getDeclaredMethods("<clinit>");
        if (declaredMethods.isEmpty()) {
            method =
                    addMethod("<clinit>", ACC_PUBLIC | ACC_STATIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
            method.setSynthetic(true);
        }
        else {
            method = (MethodNode) declaredMethods.get(0);
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
        
        // while anything inside a static initializer block is appended 
        // we don't want to append in the case we have a initialization
        // expression of a static field. In that case we want to add
        // before the other statements
        if (!fieldInit) {
            block.addStatements(staticStatements);
        } else {
            List blockStatements = block.getStatements();
            staticStatements.addAll(blockStatements);
            blockStatements.clear();
            blockStatements.addAll(staticStatements);
        }
    }

    /**
     * @return a list of methods which match the given name
     */
    public List getDeclaredMethods(String name) {
        List answer = new ArrayList();
        for (Iterator iter = getMethods().iterator(); iter.hasNext();) {
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
            for (Iterator iter = node.getMethods().iterator(); iter.hasNext();) {
                MethodNode method = (MethodNode) iter.next();
                if (name.equals(method.getName())) {
                    answer.add(method);
                }
            }
            node = node.getSuperClass();
        }
        while (node != null);
        return answer;
    }

    /**
     * @return the method matching the given name and parameters or null
     */
    public MethodNode getDeclaredMethod(String name, Parameter[] parameters) {
        for (Iterator iter = getMethods().iterator(); iter.hasNext();) {
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
    public boolean isDerivedFrom(ClassNode type) {
        ClassNode node = getSuperClass();
        while (node != null) {
            if (type.equals(node)) {
                return true;
            }
            node = node.getSuperClass();
        }
        return false;
    }

    /**
     * @return true if this class is derived from a groovy object
     *         i.e. it implements GroovyObject
     */
    public boolean isDerivedFromGroovyObject() {
        return implementsInteface(GroovyObject.class.getName());
    }

    /**
     * @param name the fully qualified name of the interface
     * @return true if this class or any base class implements the given interface
     */
    public boolean implementsInteface(String name) {
        ClassNode node = redirect();
        do {
            if (node.declaresInterface(name)) {
                return true;
            }
            node = node.getSuperClass();
        }
        while (node != null);
        return false;
    }

    /**
     * @param name the fully qualified name of the interface
     * @return true if this class declares that it implements the given interface
     */
    public boolean declaresInterface(String name) {
        ClassNode[] interfaces = redirect().getInterfaces();
        int size = interfaces.length;
        for (int i = 0; i < size; i++) {
            if (interfaces[i].getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the ClassNode of the super class of this type
     */
    public ClassNode getSuperClass() {
        if (!lazyInitDone && !isResolved()) {
            throw new GroovyBugError("Classnode#getSuperClass for "+getName()+" called before class resolving");
        }
        return getUnresolvedSuperClass();
    }
    
    public ClassNode getUnresolvedSuperClass() {
        if (!lazyInitDone) {
            lazyClassInit();
        }
        return redirect().superClass;
    }

    /**
     * Factory method to create a new MethodNode via reflection
     */
    protected MethodNode createMethodNode(Method method) {
        Parameter[] parameters = createParameters(method.getParameterTypes());
        return new MethodNode(method.getName(), method.getModifiers(), ClassHelper.make(method.getReturnType()), parameters, ClassHelper.make(method.getExceptionTypes()), EmptyStatement.INSTANCE);
    }

    /**
     * @param types
     * @return
     */
    protected Parameter[] createParameters(Class[] types) {
        Parameter[] parameters = Parameter.EMPTY_ARRAY;
        int size = types.length;
        if (size > 0) {
            parameters = new Parameter[size];
            for (int i = 0; i < size; i++) {
                parameters[i] = createParameter(types[i], i);
            }
        }
        return parameters;
    }

    protected Parameter createParameter(Class parameterType, int idx) {
        return new Parameter(ClassHelper.make(parameterType), "param" + idx);
    }

    public CompileUnit getCompileUnit() {
        if (redirect!=null) return redirect().getCompileUnit();
        if (compileUnit == null && module != null) {
            compileUnit = module.getUnit();
        }
        return compileUnit;
    }
    
    protected void setCompileUnit(CompileUnit cu) {
        if (redirect!=null) redirect().setCompileUnit(cu);
        if (compileUnit!= null) compileUnit = cu;
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
     * @return the package name of this class
     */
    public String getPackageName() {
        int idx = getName().lastIndexOf('.');
        if (idx > 0) {
            return getName().substring(0, idx);
        }
        return null;
    }

    public String getNameWithoutPackage() {
        int idx = getName().lastIndexOf('.');
        if (idx > 0) {
            return getName().substring(idx + 1);
        }
        return getName();
    }

    public void visitContents(GroovyClassVisitor visitor) {
        
        // now lets visit the contents of the class
        for (Iterator iter = getProperties().iterator(); iter.hasNext();) {
            PropertyNode pn = (PropertyNode) iter.next();
            visitor.visitProperty(pn);
        }

        for (Iterator iter = getFields().iterator(); iter.hasNext();) {
            FieldNode fn = (FieldNode) iter.next();
            visitor.visitField(fn);
        }

        for (Iterator iter = getDeclaredConstructors().iterator(); iter.hasNext();) {
            ConstructorNode cn = (ConstructorNode) iter.next();
            visitor.visitConstructor(cn);
        }

        for (Iterator iter = getMethods().iterator(); iter.hasNext();) {
            MethodNode mn = (MethodNode) iter.next();
            visitor.visitMethod(mn);
        }
    }

    public MethodNode getGetterMethod(String getterName) {
        for (Iterator iter = getMethods().iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (getterName.equals(method.getName())
                    && ClassHelper.VOID_TYPE!=method.getReturnType()
                    && method.getParameters().length == 0) {
                return method;
            }
        }
        return null;
    }

    public MethodNode getSetterMethod(String getterName) {
        for (Iterator iter = getMethods().iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (getterName.equals(method.getName())
                    && ClassHelper.VOID_TYPE==method.getReturnType()
                    && method.getParameters().length == 1) {
                return method;
            }
        }
        return null;
    }

    /**
     * Is this class delcared in a static method (such as a closure / inner class declared in a static method)
     *
     * @return
     */
    public boolean isStaticClass() {
        return redirect().staticClass;
    }

    public void setStaticClass(boolean staticClass) {
        redirect().staticClass = staticClass;
    }

    /**
     * @return Returns true if this inner class or closure was declared inside a script body
     */
    public boolean isScriptBody() {
        return redirect().scriptBody;
    }

    public void setScriptBody(boolean scriptBody) {
        redirect().scriptBody = scriptBody;
    }

    public boolean isScript() {
        return redirect().script || isDerivedFrom(ClassHelper.SCRIPT_TYPE);
    }

    public void setScript(boolean script) {
        redirect().script = script;
    }

    public String toString() {
        return super.toString() + "[name: " + getName() + "]";
    }

    /**
     * Returns true if the given method has a possibly matching method with the given name and arguments
     */
    public boolean hasPossibleMethod(String name, Expression arguments) {
        int count = 0;

        if (arguments instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) arguments;
            // TODO this won't strictly be true when using list expension in argument calls
            count = tuple.getExpressions().size();
        }
        ClassNode node = this;
        do {
            for (Iterator iter = getMethods().iterator(); iter.hasNext();) {
                MethodNode method = (MethodNode) iter.next();
                if (name.equals(method.getName()) && method.getParameters().length == count) {
                    return true;
                }
            }
            node = node.getSuperClass();
        }
        while (node != null);
        return false;
    }
    
    public boolean isInterface(){
        return (getModifiers() & Opcodes.ACC_INTERFACE) > 0; 
    }
    
    public boolean isResolved(){
        return redirect().clazz!=null || (componentType != null && componentType.isResolved());
    }
    
    public boolean isArray(){
        return componentType!=null;
    }
    
    public ClassNode getComponentType() {
        return componentType;
    }
    
    public Class getTypeClass(){
        Class c = redirect().clazz;
        if (c!=null) return c;
        ClassNode component = redirect().componentType;
        if (component!=null && component.isResolved()){
            ClassNode cn = component.makeArray();
            setRedirect(cn);
            return redirect().clazz;
        }
        throw new GroovyBugError("ClassNode#getTypeClass for "+getName()+" is called before the type class is set ");
    }
    
    public boolean hasPackageName(){
        return redirect().name.indexOf('.')>0;
    }
}
