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
package org.codehaus.groovy.ast;

import groovy.lang.GroovyObject;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Represents a class in the AST.<br/>
 * A ClassNode should be created using the methods in ClassHelper. 
 * This ClassNode may be used to represent a class declaration or
 * any other type. This class uses a proxy meschanism allowing to
 * create a class for a plain name at ast creation time. In another 
 * phase of the compiler the real ClassNode for the plain name may be
 * found. To avoid the need of exchanging this ClassNode with an 
 * instance of the correct ClassNode the correct ClassNode is set as 
 * redirect. Most method calls are then redirected to that ClassNode.
 * <br>
 * <b>Note:</b> the proxy mechanism is only allowed for classes being marked
 * as primary ClassNode which means they represent no actual class. 
 * The redirect itself can be any type of ClassNode
 * <br>
 * To descirbe generic type signature see {@link #getGenericsTypes()} and
 * {@link #setGenericsTypes(GenericsType[])}. These emthods are not proxied,
 * they describe the type signature used at the point of declaration or the
 * type signatures provided by the class. If the type signatures provided
 * by the class are needed, then a call to {@link #redirect()} will help.
 *
 * @see org.codehaus.groovy.ast.ClassHelper
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Jochen Theodorou
 * @version $Revision$
 */
public class ClassNode extends AnnotatedNode implements Opcodes {

    private static class MapOfLists {
        private Map map = new HashMap();
        public List get(Object key) {
            return (List) map.get(key);
        }
        public List getNotNull(Object key) {
            List ret = get(key);
            if (ret==null) ret = Collections.EMPTY_LIST;
            return ret;
        }
        public void put(Object key, Object value) {
            if (map.containsKey(key)) {
                get(key).add(value);
            } else {
                ArrayList list = new ArrayList(2);
                list.add(value);
                map.put(key, list);
            }
        }        
    }
    
    public static ClassNode[] EMPTY_ARRAY = new ClassNode[0];
    
    public static ClassNode THIS = new ClassNode(Object.class);
    public static ClassNode SUPER = new ClassNode(Object.class);
    
    private String name;
    private final int modifiers;
    private ClassNode[] interfaces;
    private MixinNode[] mixins;
    private List constructors = new ArrayList();
    private List  objectInitializers = new ArrayList();
    private MapOfLists methods;
    private List methodsList;
    private List fields = new ArrayList();
    private List properties = new ArrayList();
    private Map fieldIndex = new HashMap();
    private ModuleNode module;
    private CompileUnit compileUnit;
    private boolean staticClass = false;
    private boolean scriptBody = false;
    private boolean script;
    private ClassNode superClass;
    protected boolean isPrimaryNode;
    
    // use this to synchronize access for the lazy init
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
    // flag if the classes or its members are annotated
    private boolean annotated;
    
    // type spec for generics
    private GenericsType[] genericsTypes=null;
    private boolean usesGenerics=false;
    
    // if set to true the name getGenericsTypes consists
    // of 1 element describing the name of the placeholder
    private boolean placeholder;
    
    /**
     * Returns the ClassNode this ClassNode is redirecting to.
     */
    public ClassNode redirect(){
        if (redirect==null) return this;
        return redirect.redirect();
    }
    
    /**
     * Sets this instance as proxy for the given ClassNode. 
     * @param cn the class to redirect to. If set to null the redirect will be removed
     */
    public void setRedirect(ClassNode cn) {
        if (isPrimaryNode) throw new GroovyBugError("tried to set a redirect for a primary ClassNode ("+getName()+"->"+cn.getName()+").");
        if (cn!=null) cn = cn.redirect();
        if (cn==this) return;
        redirect = cn;
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
            VMPluginFactory.getPlugin().configureClassNode(compileUnit,this);
            lazyInitDone = true;
        }
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
        this(name, modifiers, superClass, EMPTY_ARRAY, MixinNode.EMPTY_ARRAY);
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
        if (superClass!=null) {
            usesGenerics = superClass.isUsingGenerics();
        }
        if (!usesGenerics && interfaces!=null) {
            for (int i = 0; i < interfaces.length; i++) {
                usesGenerics = usesGenerics || interfaces[i].isUsingGenerics();
            }
        }
        this.methods = new MapOfLists();
        this.methodsList = new ArrayList();
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

    public void setInterfaces(ClassNode[] interfaces) {
        if (redirect!=null) {
            redirect().setInterfaces(interfaces);
        } else {
            this.interfaces = interfaces;
        }
    }

    public MixinNode[] getMixins() {
        return redirect().mixins;
    }

    /**
     * Returns a list containing MethodNode objects for
     * each method in the class represented by this ClassNode
     */
    public List getMethods() {
        if (!lazyInitDone) lazyClassInit();
        if (redirect!=null) return redirect().getMethods();
        return methodsList;
    }

    /**
     * Returns a list containing MethodNode objects for
     * each abstract method in the class represented by 
     * this ClassNode
     */   
    public List getAbstractMethods() {
        List result = new ArrayList(3);
        Map declaredMethods = getDeclaredMethodsMap();
        for (Iterator it = declaredMethods.values().iterator(); it.hasNext();) {
            MethodNode method = (MethodNode) it.next();
            if (method.isAbstract()) {
                result.add(method);
            }
        }
        
        if (result.isEmpty()) {
            return null;
        } else {
            return result;
        }
    }

    public List getAllDeclaredMethods() {
        return new ArrayList(getDeclaredMethodsMap().values());
    }


    public Map getDeclaredMethodsMap() {
        // Start off with the methods from the superclass.
        ClassNode parent = getSuperClass();
        Map result = null;
        if (parent != null) {
            result = parent.getDeclaredMethodsMap();
        } else {
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
            result.put(sig, method);
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

    public boolean hasProperty(String name) {
    	return getProperty(name)!=null;
    }
    
    public PropertyNode getProperty(String name) {
    	for (Iterator iter = getProperties().iterator(); iter.hasNext();) {
            PropertyNode pn = (PropertyNode) iter.next();
            if (pn.getName().equals(name)) return pn;
        }
        return null;   	
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
        redirect().methodsList.add(node);
        redirect().methods.put(node.getName(), node);
    }

    /**
     * If a method with the given name and parameters is already defined then it is returned
     * otherwise the given method is added to this node. This method is useful for
     * default method adding like getProperty() or invokeMethod() where there may already
     * be a method defined in a class and so the default implementations should not be added
     * if already present.
     */
    public MethodNode addMethod(String name,
                                int modifiers,
                                ClassNode returnType,
                                Parameter[] parameters,
                                ClassNode[] exceptions,
                                Statement code) {
        MethodNode other = getDeclaredMethod(name, parameters);
        // let's not add duplicate methods
        if (other != null) {
            return other;
        }
        MethodNode node = new MethodNode(name, modifiers, returnType, parameters, exceptions, code);
        addMethod(node);
        return node;
    }

    /**
     * @see #getDeclaredMethod(String, Parameter[])
     */
    public boolean hasDeclaredMethod(String name, Parameter[] parameters) {
        MethodNode other = getDeclaredMethod(name, parameters);
        return other != null;
    }

    /**
     * @see #getMethod(String, Parameter[])
     */
    public boolean hasMethod(String name, Parameter[] parameters) {
        MethodNode other = getMethod(name, parameters);
        return other != null;
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

    /**
     * Finds a field matching the given name in this class.
     *
     * @param name the name of the field of interest
     * @return the method matching the given name and parameters or null
     */
    public FieldNode getDeclaredField(String name) {
        return (FieldNode) redirect().fieldIndex.get(name);
    }

    /**
     * Finds a field matching the given name in this class or a parent class.
     *
     * @param name the name of the field of interest
     * @return the method matching the given name and parameters or null
     */
    public FieldNode getField(String name) {
        ClassNode node = this;
        while (node != null) {
            FieldNode fn = node.getDeclaredField(name);
            if (fn != null) return fn;
            node = node.getSuperClass();
        }
        return null;
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
     */
    public ClassNode getOuterClass() {
        return null;
    }
    
    public void addObjectInitializerStatements(Statement statements) {
        objectInitializers.add(statements);
    }
    
    public List getObjectInitializerStatements() {
        return objectInitializers;
    }

    public void addStaticInitializerStatements(List staticStatements, boolean fieldInit) {
        MethodNode method = null;
        List declaredMethods = getDeclaredMethods("<clinit>");
        if (declaredMethods.isEmpty()) {
            method =
                    addMethod("<clinit>", ACC_STATIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
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
     * This methods returns a list of all methods of the given name
     * defined in the current class
     * @return the method list
     * @see #getMethods(String)
     */
    public List getDeclaredMethods(String name) {
        if (!lazyInitDone) lazyClassInit();
        if (redirect!=null) return redirect().getDeclaredMethods(name);
        return methods.getNotNull(name);
    }

    /**
     * This methods creates a list of all methods with this name of the
     * current class and of all super classes
     * @return the methods list
     * @see #getDeclaredMethods(String)
     */
    public List getMethods(String name) {
        List answer = new ArrayList(getDeclaredMethods(name));
        ClassNode parent = getSuperClass();
        if (parent!=null) answer.addAll(parent.getMethods(name));
        return answer;
    }

    /**
     * Finds a method matching the given name and parameters in this class.
     *
     * @return the method matching the given name and parameters or null
     */
    public MethodNode getDeclaredMethod(String name, Parameter[] parameters) {
        List list = getDeclaredMethods(name);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (parametersEqual(method.getParameters(), parameters)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Finds a method matching the given name and parameters in this class
     * or any parent class.
     *
     * @return the method matching the given name and parameters or null
     */
    public MethodNode getMethod(String name, Parameter[] parameters) {
        List list = getMethods(name);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (parametersEqual(method.getParameters(), parameters)) {
                return method;
            }
        }
        return null;
    }

    /**
     * @return true if this node is derived from the given class node
     */
    public boolean isDerivedFrom(ClassNode type) {
        if (type.equals(ClassHelper.OBJECT_TYPE)) return true;
        ClassNode node = this;
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
        return implementsInterface(ClassHelper.make(GroovyObject.class));
    }

    /**
     * @param classNode the class node for the interface
     * @return true if this class or any base class implements the given interface
     */
    public boolean implementsInterface(ClassNode classNode) {
        ClassNode node = redirect();
        do {
            if (node.declaresInterface(classNode)) {
                return true;
            }
            node = node.getSuperClass();
        }
        while (node != null);
        return false;
    }

    /**
     * @param classNode the class node for the interface
     * @return true if this class declares that it implements the given interface
     * or if one of its interfaces extends directly or indirectly the interface
     */
    public boolean declaresInterface(ClassNode classNode) {
        ClassNode[] interfaces = redirect().getInterfaces();
        if (declaresInterfaceDirect(interfaces, classNode)) return true;
        List superInterfaces = Arrays.asList(interfaces);
        while (superInterfaces.size() > 0) {
            List keep = new ArrayList();
            for (int i = 0; i < superInterfaces.size(); i++) {
                ClassNode cn = (ClassNode) superInterfaces.get(i);
                if (cn.declaresInterface(classNode)) return true;
                keep.addAll(Arrays.asList(cn.getInterfaces()));
            }
            superInterfaces = keep;
        }
        return false;
    }

    private boolean declaresInterfaceDirect(ClassNode[] interfaces, ClassNode classNode) {
        int size = interfaces.length;
        for (int i = 0; i < size; i++) {
            if (interfaces[i].equals(classNode)) {
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
        ClassNode sn = redirect().getUnresolvedSuperClass();
        if (sn!=null) sn=sn.redirect();
        return sn;
    }
    
    public ClassNode getUnresolvedSuperClass() {
        return getUnresolvedSuperClass(true);
    }

    public ClassNode getUnresolvedSuperClass(boolean useRedirect) {
        if (!useRedirect) return superClass;
        if (!lazyInitDone) {
            lazyClassInit();
        }
        return redirect().superClass;
    }

    public void setUnresolvedSuperClass(ClassNode sn) {
        superClass = sn;
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
        for (Iterator iter = getDeclaredMethods(getterName).iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (getterName.equals(method.getName())
                    && ClassHelper.VOID_TYPE!=method.getReturnType()
                    && method.getParameters().length == 0) {
                return method;
            }
        }
        ClassNode parent = getSuperClass(); 
        if (parent!=null) return parent.getGetterMethod(getterName); 
        return null;
    }

    public MethodNode getSetterMethod(String setterName) {
        for (Iterator iter = getDeclaredMethods(setterName).iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (setterName.equals(method.getName())
                    && ClassHelper.VOID_TYPE==method.getReturnType()
                    && method.getParameters().length == 1) {
                return method;
            }
        }
        ClassNode parent = getSuperClass(); 
        if (parent!=null) return parent.getSetterMethod(setterName); 
        return null;
    }

    /**
     * Is this class delcared in a static method (such as a closure / inner class declared in a static method)
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
        String ret = getName(); 
        if (genericsTypes!=null) {
            ret += " <";
            for (int i = 0; i < genericsTypes.length; i++) {
                if (i!=0) ret+=", ";
                ret += genericsTypes[i];
            }
            ret += ">";
        }
        if (redirect!=null) {
            ret += " -> "+redirect().toString();
        }
        return ret;
    }

    /**
     * Returns true if the given method has a possibly matching method with the given name and arguments
     */
    public boolean hasPossibleMethod(String name, Expression arguments) {
        int count = 0;

        if (arguments instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) arguments;
            // TODO this won't strictly be true when using list expansion in argument calls
            count = tuple.getExpressions().size();
        }
        ClassNode node = this;
        do {
            for (Iterator iter = getMethods(name).iterator(); iter.hasNext();) {
                MethodNode method = (MethodNode) iter.next();
                if (method.getParameters().length == count) {
                    return true;
                }
            }
            node = node.getSuperClass();
        }
        while (node != null);
        return false;
    }
    
    /**
     * Returns true if the given method has a possibly matching static method with the given name and arguments
     */
    public boolean hasPossibleStaticMethod(String name, Expression arguments) {
        int count = 0;

        if (arguments instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) arguments;
            // TODO this won't strictly be true when using list expansion in argument calls
            count = tuple.getExpressions().size();
        }
        for (Iterator iter = getMethods(name).iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            
            if(method.isStatic()) {
                Parameter[] parameters = method.getParameters(); 
                if (parameters.length == count) return true;

                // handle varargs case
                if (parameters.length > 0 && parameters[parameters.length - 1].getType().isArray()) {
                    if (count >= parameters.length - 1) return true;
                }
                
                // handle parameters with default values
                int nonDefaultParameters = 0;
                for(int i = 0; i < parameters.length; i++) {
                    if(parameters[i].hasInitialExpression() == false) {
                        nonDefaultParameters++;
                    }
                }
                
                if(count < parameters.length && nonDefaultParameters <= count) {
                    return true;
                }
            }
        }
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

    /**
     * Marks if the current class uses annotations or not
     * @param flag
     */
    public void setAnnotated(boolean flag) {
        this.annotated = flag;
    }
    
    public boolean isAnnotated() {
        return this.annotated;
    }

    public GenericsType[] getGenericsTypes() {
        return genericsTypes;
    }

    public void setGenericsTypes(GenericsType[] genericsTypes) {
        usesGenerics = usesGenerics || genericsTypes!=null;
        this.genericsTypes = genericsTypes;
    }

    public void setGenericsPlaceHolder(boolean b) {
        usesGenerics = usesGenerics || b;
        placeholder = b;
    }
    
    public boolean isGenericsPlaceHolder() {
        return placeholder;
    }
    
    public boolean isUsingGenerics() {
        return usesGenerics;
    }
    
    public void setUsingGenerics(boolean b) {
        usesGenerics = b;
    }
    
    public ClassNode getPlainNodeReference() {
        if (ClassHelper.isPrimitiveType(this)) return this;
        ClassNode n = new ClassNode(getName(),getModifiers(),getSuperClass(),null,null);
        n.isPrimaryNode = false;
        n.setRedirect(this.redirect);
        return n;
    }
}
