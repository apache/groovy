/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.ast;

import org.apache.groovy.ast.tools.ClassNodeUtils;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.ParameterUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a class in the AST.
 * <p>
 * A ClassNode should be created using the methods in ClassHelper.
 * This ClassNode may be used to represent a class declaration or
 * any other type. This class uses a proxy mechanism allowing to
 * create a class for a plain name at AST creation time. In another
 * phase of the compiler the real ClassNode for the plain name may be
 * found. To avoid the need of exchanging this ClassNode with an
 * instance of the correct ClassNode the correct ClassNode is set as
 * redirect. Most method calls are then redirected to that ClassNode.
 * <p>
 * There are three types of ClassNodes:
 * <ol>
 * <li> Primary ClassNodes:<br>
 * A primary ClassNode is one where we have a source representation
 * which is to be compiled by Groovy and which we have an AST for. 
 * The groovy compiler will output one class for each such ClassNode
 * that passes through AsmBytecodeGenerator... not more, not less.
 * That means for example Closures become such ClassNodes too at
 * some point. 
 * <li> ClassNodes create through different sources (typically created
 * from a java.lang.reflect.Class object):<br>
 * The compiler will not output classes from these, the methods
 * usually do not contain bodies. These kind of ClassNodes will be
 * used in different checks, but not checks that work on the method 
 * bodies. For example if such a ClassNode is a super class to a primary
 * ClassNode, then the abstract method test and others will be done 
 * with data based on these. Theoretically it is also possible to mix both 
 * (1 and 2) kind of classes in a hierarchy, but this probably works only
 *  in the newest Groovy versions. Such ClassNodes normally have to
 *  isResolved() returning true without having a redirect.In the Groovy 
 *  compiler the only version of this, that exists, is a ClassNode created 
 *  through a Class instance
 * <li> Labels:<br>
 * ClassNodes created through ClassHelper.makeWithoutCaching. They 
 * are place holders, its redirect points to the real structure, which can
 * be a label too, but following all redirects it should end with a ClassNode
 * from one of the other two categories. If ResolveVisitor finds such a 
 * node, it tries to set the redirects. Any such label created after 
 * ResolveVisitor has done its work needs to have a redirect pointing to 
 * case 1 or 2. If not the compiler may react strange... this can be considered 
 * as a kind of dangling pointer. 
 * </ol>
 * <b>Note:</b> the redirect mechanism is only allowed for classes 
 * that are not primary ClassNodes. Typically this is done for classes
 * created by name only.  The redirect itself can be any type of ClassNode.
 * <p>
 * To describe generic type signature see {@link #getGenericsTypes()} and
 * {@link #setGenericsTypes(GenericsType[])}. These methods are not proxied,
 * they describe the type signature used at the point of declaration or the
 * type signatures provided by the class. If the type signatures provided
 * by the class are needed, then a call to {@link #redirect()} will help.
 *
 * @see org.codehaus.groovy.ast.ClassHelper
 */
public class ClassNode extends AnnotatedNode implements Opcodes {

    private static class MapOfLists {
        private Map<Object, List<MethodNode>> map;
        public List<MethodNode> get(Object key) {
            return map == null ? null : map.get(key);
        }

        public List<MethodNode> getNotNull(Object key) {
            List<MethodNode> ret = get(key);
            if (ret==null) ret = Collections.emptyList();
            return ret;
        }

        public void put(Object key, MethodNode value) {
            if (map == null) {
                 map = new LinkedHashMap<Object, List<MethodNode>>();
            }
            if (map.containsKey(key)) {
                get(key).add(value);
            } else {
                List<MethodNode> list = new ArrayList<MethodNode>(2);
                list.add(value);
                map.put(key, list);
            }
        }

        public void remove(Object key, MethodNode value) {
            get(key).remove(value);
        }
    }

    public static final ClassNode[] EMPTY_ARRAY = new ClassNode[0];
    public static final ClassNode THIS = new ClassNode(Object.class);
    public static final ClassNode SUPER = new ClassNode(Object.class);

    private String name;
    private int modifiers;
    private boolean syntheticPublic;
    private ClassNode[] interfaces;
    private MixinNode[] mixins;
    private List<ConstructorNode> constructors;
    private List<Statement> objectInitializers;
    private MapOfLists methods;
    private List<MethodNode> methodsList;
    private LinkedList<FieldNode> fields;
    private List<PropertyNode> properties;
    private Map<String, FieldNode> fieldIndex;
    private ModuleNode module;
    private CompileUnit compileUnit;
    private boolean staticClass = false;
    private boolean scriptBody = false;
    private boolean script;
    private ClassNode superClass;
    protected boolean isPrimaryNode;
    protected List<InnerClassNode> innerClasses;

    /**
     * The ASTTransformations to be applied to the Class
     */
    private Map<CompilePhase, Map<Class<? extends ASTTransformation>, Set<ASTNode>>> transformInstances;

    // use this to synchronize access for the lazy init
    protected final Object lazyInitLock = new Object();

    // clazz!=null when resolved
    protected Class clazz;
    // only false when this classNode is constructed from a class
    private volatile boolean lazyInitDone=true;
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
        if (redirect!=null) {
            ClassNode res = redirect().makeArray();
            res.componentType = this;
            return res;
        }
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
     * @return true if this instance is a primary ClassNode
     */
    public boolean isPrimaryClassNode() {
        return redirect().isPrimaryNode || (componentType != null && componentType.isPrimaryClassNode());
    }

    /*
     * Constructor used by makeArray() if no real class is available
     */
    private ClassNode(ClassNode componentType) {
        this(componentType.getName()+"[]", ACC_PUBLIC, ClassHelper.OBJECT_TYPE);
        this.componentType = componentType.redirect();
        isPrimaryNode=false;
    }

    /*
     * Constructor used by makeArray() if a real class is available
     */
    private ClassNode(Class c, ClassNode componentType) {
        this(c);
        this.componentType = componentType;
        isPrimaryNode=false;
    }

    /**
     * Creates a ClassNode from a real class. The resulting
     * ClassNode will not be a primary ClassNode.
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
     * needed to avoid having too many objects during compilation
     */
    private void lazyClassInit() {
        if (lazyInitDone) return;
        synchronized (lazyInitLock) {
            if (redirect!=null) {
                throw new GroovyBugError("lazyClassInit called on a proxy ClassNode, that must not happen."+
                                         "A redirect() call is missing somewhere!");
            }   
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
     * Indicates that this class has been "promoted" to public by
     * Groovy when in fact there was no public modifier explicitly
     * in the source code. I.e. it remembers that it has applied
     * Groovy's "public classes by default" rule.This property is
     * typically only of interest to AST transform writers.
     *
     * @return true if this class is public but had no explicit public modifier
     */
    public boolean isSyntheticPublic() {
        return syntheticPublic;
    }

    public void setSyntheticPublic(boolean syntheticPublic) {
        this.syntheticPublic = syntheticPublic;
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
     * @param interfaces the interfaces for this class
     * @param mixins     the mixins for this class
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
            for (ClassNode anInterface : interfaces) {
                usesGenerics = usesGenerics || anInterface.isUsingGenerics();
                if (usesGenerics) break;
            }
        }
        this.methods = new MapOfLists();
        this.methodsList = Collections.emptyList();
    }

    /**
     * Sets the superclass of this ClassNode
     */
    public void setSuperClass(ClassNode superClass) {
        redirect().superClass = superClass;
    }

    /**
     * @return the list of FieldNode's associated with this ClassNode
     */
    public List<FieldNode> getFields() {
        if (redirect!=null) return redirect().getFields();
        lazyClassInit();
        if (fields == null)
            fields = new LinkedList<FieldNode> ();
        return fields;
    }

    /**
     * @return the array of interfaces which this ClassNode implements
     */
    public ClassNode[] getInterfaces() {
        if (redirect!=null) return redirect().getInterfaces();
        lazyClassInit();
        return interfaces;
    }

    public void setInterfaces(ClassNode[] interfaces) {
        if (redirect!=null) {
            redirect().setInterfaces(interfaces);
        } else {
            this.interfaces = interfaces;
        }
    }

    /**
     * @return the array of mixins associated with this ClassNode
     */
    public MixinNode[] getMixins() {
        return redirect().mixins;
    }


    public void setMixins(MixinNode[] mixins) {
        redirect().mixins = mixins;
    }

    /**
     * @return the list of methods associated with this ClassNode
     */
    public List<MethodNode> getMethods() {
        if (redirect!=null) return redirect().getMethods();
        lazyClassInit();
        return methodsList;
    }

    /**
     * @return the list of abstract methods associated with this
     * ClassNode or null if there are no such methods
     */
    public List<MethodNode> getAbstractMethods() {
        List<MethodNode> result = new ArrayList<MethodNode>(3);
        for (MethodNode method : getDeclaredMethodsMap().values()) {
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

    public List<MethodNode> getAllDeclaredMethods() {
        return new ArrayList<MethodNode>(getDeclaredMethodsMap().values());
    }

    public Set<ClassNode> getAllInterfaces () {
        Set<ClassNode> res = new LinkedHashSet<ClassNode>();
        getAllInterfaces(res);
        return res;
    }

    private void getAllInterfaces(Set<ClassNode> res) {
        if (isInterface())
          res.add(this);

        for (ClassNode anInterface : getInterfaces()) {
            res.add(anInterface);
            anInterface.getAllInterfaces(res);
        }
    }

    public Map<String, MethodNode> getDeclaredMethodsMap() {
        Map<String, MethodNode> result = ClassNodeUtils.getDeclaredMethodsFromSuper(this);
        ClassNodeUtils.addDeclaredMethodsFromInterfaces(this, result);

        // And add in the methods implemented in this class.
        for (MethodNode method : getMethods()) {
            String sig = method.getTypeDescriptor();
            result.put(sig, method);
        }
        return result;
    }

    public String getName() {
        return redirect().name;
    }

    public String getUnresolvedName() {
        return name;
    }

    public String setName(String name) {
        return redirect().name=name;
    }

    public int getModifiers() {
        return redirect().modifiers;
    }

    public void setModifiers(int modifiers) {
        redirect().modifiers = modifiers;
    }

    public List<PropertyNode> getProperties() {
        final ClassNode r = redirect();
        if (r.properties == null)
            r.properties = new ArrayList<PropertyNode> ();
        return r.properties;
    }

    public List<ConstructorNode> getDeclaredConstructors() {
        if (redirect != null) return redirect().getDeclaredConstructors();
        lazyClassInit();
        if (constructors == null)
            constructors = new ArrayList<ConstructorNode> ();
        return constructors;
    }

    /**
     * Finds a constructor matching the given parameters in this class.
     *
     * @return the constructor matching the given parameters or null
     */
    public ConstructorNode getDeclaredConstructor(Parameter[] parameters) {
        for (ConstructorNode method : getDeclaredConstructors()) {
            if (parametersEqual(method.getParameters(), parameters)) {
                return method;
            }
        }
        return null;
    }

    public void removeConstructor(ConstructorNode node) {
        redirect().constructors.remove(node);
    }

    public ModuleNode getModule() {
        return redirect().module;
    }

    public PackageNode getPackage() {
        return getModule() == null ? null : getModule().getPackage();
    }

    public void setModule(ModuleNode module) {
        redirect().module = module;
        if (module != null) {
            redirect().compileUnit = module.getUnit();
        }
    }

    public void addField(FieldNode node) {
        addField(node, false);
    }

    public void addFieldFirst(FieldNode node) {
        addField(node, true);
    }

    private void addField(FieldNode node, boolean isFirst) {
        final ClassNode r = redirect();
        node.setDeclaringClass(r);
        node.setOwner(r);
        if (r.fields == null)
            r.fields = new LinkedList<>();
        if (r.fieldIndex == null)
            r.fieldIndex = new LinkedHashMap<>();

        if (isFirst)
            r.fields.addFirst(node);
        else
            r.fields.add(node);

        r.fieldIndex.put(node.getName(), node);
    }

    public Map<String, FieldNode> getFieldIndex() {
        return fieldIndex;
    }

    public void addProperty(PropertyNode node) {
        node.setDeclaringClass(redirect());
        FieldNode field = node.getField();
        addField(field);
        final ClassNode r = redirect();
        if (r.properties == null)
            r.properties = new ArrayList<PropertyNode> ();
        r.properties.add(node);
    }

    public PropertyNode addProperty(String name,
                                    int modifiers,
                                    ClassNode type,
                                    Expression initialValueExpression,
                                    Statement getterBlock,
                                    Statement setterBlock) {
        for (PropertyNode pn : getProperties()) {
            if (pn.getName().equals(name)) {
                if (pn.getInitialExpression() == null && initialValueExpression != null)
                    pn.getField().setInitialValueExpression(initialValueExpression);

                if (pn.getGetterBlock() == null && getterBlock != null)
                    pn.setGetterBlock(getterBlock);

                if (pn.getSetterBlock() == null && setterBlock != null)
                    pn.setSetterBlock(setterBlock);

                return pn;
            }
        }
        PropertyNode node =
                new PropertyNode(name, modifiers, type, redirect(), initialValueExpression, getterBlock, setterBlock);
        addProperty(node);
        return node;
    }

    public boolean hasProperty(String name) {
        return getProperty(name) != null;
    }

    public PropertyNode getProperty(String name) {
        for (PropertyNode pn : getProperties()) {
            if (pn.getName().equals(name)) return pn;
        }
        return null;
    }

    public void addConstructor(ConstructorNode node) {
        node.setDeclaringClass(this);
        final ClassNode r = redirect();
        if (r.constructors == null)
            r.constructors = new ArrayList<ConstructorNode> ();
        r.constructors.add(node);
    }

    public ConstructorNode addConstructor(int modifiers, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        ConstructorNode node = new ConstructorNode(modifiers, parameters, exceptions, code);
        addConstructor(node);
        return node;
    }

    public void addMethod(MethodNode node) {
        node.setDeclaringClass(this);
        ClassNode base = redirect();
        if (base.methodsList.isEmpty()) {
            base.methodsList = new ArrayList<MethodNode>();
        }
        base.methodsList.add(node);
        base.methods.put(node.getName(), node);
    }

    public void removeMethod(MethodNode node) {
        ClassNode base = redirect();
        if (!base.methodsList.isEmpty()) {
            base.methodsList.remove(node);
        }
        base.methods.remove(node.getName(), node);
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
        MethodNode answer = addMethod(name, modifiers|ACC_SYNTHETIC, returnType, parameters, exceptions, code);
        answer.setSynthetic(true);
        return answer;
    }

    public FieldNode addField(String name, int modifiers, ClassNode type, Expression initialValue) {
        FieldNode node = new FieldNode(name, modifiers, type, redirect(), initialValue);
        addField(node);
        return node;
    }

    public FieldNode addFieldFirst(String name, int modifiers, ClassNode type, Expression initialValue) {
        FieldNode node = new FieldNode(name, modifiers, type, redirect(), initialValue);
        addFieldFirst(node);
        return node;
    }

    public void addInterface(ClassNode type) {
        // let's check if it already implements an interface
        boolean skip = false;
        ClassNode[] interfaces = redirect().interfaces;
        for (ClassNode existing : interfaces) {
            if (type.equals(existing)) {
                skip = true;
                break;
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
        if (!(o instanceof ClassNode)) return false;
        ClassNode cn = (ClassNode) o;
        return (cn.getText().equals(getText()));
    }

    public int hashCode() {
        if (redirect!=null) return redirect().hashCode();
        return getName().hashCode();
    }

    public void addMixin(MixinNode mixin) {
        // let's check if it already uses a mixin
        MixinNode[] mixins = redirect().mixins;
        boolean skip = false;
        for (MixinNode existing : mixins) {
            if (mixin.equals(existing)) {
                skip = true;
                break;
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
        if (redirect != null) return redirect().getDeclaredField(name);

        lazyClassInit();
        return fieldIndex == null ? null : fieldIndex.get(name);
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

    public List<ClassNode> getOuterClasses() {
        if (!(this instanceof InnerClassNode)) {
            return Collections.emptyList();
        }

        List<ClassNode> result = new LinkedList<>();
        ClassNode outestClass = ((InnerClassNode) this).getOuterMostClass();
        ClassNode cn = this;

        do {
            result.add(cn = cn.getOuterClass());
        } while (!cn.equals(outestClass));

        return result;
    }

    /**
     * Adds a statement to the object initializer.
     *
     * @param statements the statement to be added
     */
    public void addObjectInitializerStatements(Statement statements) {
        getObjectInitializerStatements().add(statements);
    }

    public List<Statement> getObjectInitializerStatements() {
        if (objectInitializers == null)
            objectInitializers = new LinkedList<Statement> ();
        return objectInitializers;
    }

    private MethodNode getOrAddStaticConstructorNode() {
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
        return method;
    }
    
    public void addStaticInitializerStatements(List<Statement> staticStatements, boolean fieldInit) {
        MethodNode method = getOrAddStaticConstructorNode();
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
            List<Statement> blockStatements = block.getStatements();
            staticStatements.addAll(blockStatements);
            blockStatements.clear();
            blockStatements.addAll(staticStatements);
        }
    }

    public void positionStmtsAfterEnumInitStmts(List<Statement> staticFieldStatements) {
        MethodNode method = getOrAddStaticConstructorNode();
        Statement statement = method.getCode();
        if (statement instanceof BlockStatement) {
            BlockStatement block = (BlockStatement) statement;
            // add given statements for explicitly declared static fields just after enum-special fields
            // are found - the $VALUES binary expression marks the end of such fields.
            List<Statement> blockStatements = block.getStatements();
            ListIterator<Statement> litr = blockStatements.listIterator();
            while (litr.hasNext()) {
                Statement stmt = litr.next();
                if (stmt instanceof ExpressionStatement &&
                        ((ExpressionStatement) stmt).getExpression() instanceof BinaryExpression) {
                    BinaryExpression bExp = (BinaryExpression) ((ExpressionStatement) stmt).getExpression();
                    if (bExp.getLeftExpression() instanceof FieldExpression) {
                        FieldExpression fExp = (FieldExpression) bExp.getLeftExpression();
                        if (fExp.getFieldName().equals("$VALUES")) {
                            for (Statement tmpStmt : staticFieldStatements) {
                                litr.add(tmpStmt);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This methods returns a list of all methods of the given name
     * defined in the current class
     * @return the method list
     * @see #getMethods(String)
     */
    public List<MethodNode> getDeclaredMethods(String name) {
        if (redirect!=null) return redirect().getDeclaredMethods(name);
        lazyClassInit();
        return methods.getNotNull(name);
    }

    /**
     * This methods creates a list of all methods with this name of the
     * current class and of all super classes
     * @return the methods list
     * @see #getDeclaredMethods(String)
     */
    public List<MethodNode> getMethods(String name) {
        List<MethodNode> answer = new ArrayList<MethodNode>();
        ClassNode node = this;
        while (node != null) {
            answer.addAll(node.getDeclaredMethods(name));
            node = node.getSuperClass();
        }
        return answer;
    }

    /**
     * Finds a method matching the given name and parameters in this class.
     *
     * @return the method matching the given name and parameters or null
     */
    public MethodNode getDeclaredMethod(String name, Parameter[] parameters) {
        for (MethodNode method :  getDeclaredMethods(name)) {
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
        for (MethodNode method : getMethods(name)) {
            if (parametersEqual(method.getParameters(), parameters)) {
                return method;
            }
        }
        return null;
    }

    /**
     * @param type the ClassNode of interest
     * @return true if this node is derived from the given ClassNode
     */
    public boolean isDerivedFrom(ClassNode type) {
        if (this.equals(ClassHelper.VOID_TYPE)) {
            return type.equals(ClassHelper.VOID_TYPE);
        }
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
        return implementsInterface(ClassHelper.GROOVY_OBJECT_TYPE);
    }

    /**
     *
     * @param classNodes the class nodes for the interfaces
     * @return true if this class or any base class implements any of the given interfaces
     */
    public boolean implementsAnyInterfaces(ClassNode... classNodes) {
        for (ClassNode classNode : classNodes) {
            if (implementsInterface(classNode)) {
                return true;
            }
        }

        return false;
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
     *
     * @param classNodes the class nodes for the interfaces
     * @return true if this class declares that it implements any of the given interfaces
     * or if one of its interfaces extends directly or indirectly any of the given interfaces
     */
    public boolean declaresAnyInterfaces(ClassNode... classNodes) {
        for (ClassNode classNode : classNodes) {
            if (declaresInterface(classNode)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param classNode the class node for the interface
     * @return true if this class declares that it implements the given interface
     * or if one of its interfaces extends directly or indirectly the interface
     *
     * NOTE: Doesn't consider an interface to implement itself.
     * I think this is intended to be called on ClassNodes representing
     * classes, not interfaces.
     * 
     */
    public boolean declaresInterface(ClassNode classNode) {
        ClassNode[] interfaces = redirect().getInterfaces();
        for (ClassNode cn : interfaces) {
            if (cn.equals(classNode)) return true;
        }
        for (ClassNode cn : interfaces) {
            if (cn.declaresInterface(classNode)) return true;
        }
        return false;
    }

    /**
     * @return the ClassNode of the super class of this type
     */
    public ClassNode getSuperClass() {
        if (!lazyInitDone && !isResolved()) {
            throw new GroovyBugError("ClassNode#getSuperClass for "+getName()+" called before class resolving");
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
        if (redirect != null) return redirect().getUnresolvedSuperClass(true);
        lazyClassInit();
        return superClass;
    }

    public void setUnresolvedSuperClass(ClassNode sn) {
        superClass = sn;
    }

    public ClassNode [] getUnresolvedInterfaces() {
        return getUnresolvedInterfaces(true);
    }

    public ClassNode [] getUnresolvedInterfaces(boolean useRedirect) {
        if (!useRedirect) return interfaces;
        if (redirect != null) return redirect().getUnresolvedInterfaces(true);
        lazyClassInit();
        return interfaces;
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
        return ParameterUtils.parametersEqual(a, b);
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
        // now let's accept the contents of the class
        for (PropertyNode pn : getProperties()) {
            visitor.visitProperty(pn);
        }

        for (FieldNode fn : getFields()) {
            visitor.visitField(fn);
        }

        for (ConstructorNode cn : getDeclaredConstructors()) {
            visitor.visitConstructor(cn);
        }

        for (MethodNode mn : getMethods()) {
            visitor.visitMethod(mn);
        }
    }

    public MethodNode getGetterMethod(String getterName) {
        return getGetterMethod(getterName, true);
    }

    public MethodNode getGetterMethod(String getterName, boolean searchSuperClasses) {
        MethodNode getterMethod = null;
        boolean booleanReturnOnly = getterName.startsWith("is");
        for (MethodNode method : getDeclaredMethods(getterName)) {
            if (getterName.equals(method.getName())
                    && ClassHelper.VOID_TYPE!=method.getReturnType()
                    && method.getParameters().length == 0
                    && (!booleanReturnOnly || ClassHelper.Boolean_TYPE.equals(ClassHelper.getWrapper(method.getReturnType())))) {
                // GROOVY-7363: There can be multiple matches for a getter returning a generic parameter type, due to
                // the generation of a bridge method. The real getter is really the non-bridge, non-synthetic one as it
                // has the most specific and exact return type of the two. Picking the bridge method results in loss of
                // type information, as it down-casts the return type to the lower bound of the generic parameter.
                if (getterMethod == null || getterMethod.isSynthetic()) {
                    getterMethod = method;
                }
            }
        }
        if (getterMethod != null) return getterMethod;
        if (searchSuperClasses) {
            ClassNode parent = getSuperClass();
            if (parent != null) return parent.getGetterMethod(getterName);
        }
        return null;
    }

    public MethodNode getSetterMethod(String setterName) {
        return getSetterMethod(setterName, true);
    }

    public MethodNode getSetterMethod(String setterName, boolean voidOnly) {
        for (MethodNode method : getDeclaredMethods(setterName)) {
            if (setterName.equals(method.getName())
                    && (!voidOnly || ClassHelper.VOID_TYPE==method.getReturnType())
                    && method.getParameters().length == 1) {
                return method;
            }
        }
        ClassNode parent = getSuperClass();
        if (parent!=null) return parent.getSetterMethod(setterName, voidOnly);
        return null;
    }

    /**
     * Is this class declared in a static method (such as a closure / inner class declared in a static method)
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
        return toString(true);
    }

    public String toString(boolean showRedirect) {
        if (isArray()) {
            return componentType.toString(showRedirect)+"[]";
        }
        StringBuilder ret = new StringBuilder(getName());
        if (placeholder) ret = new StringBuilder(getUnresolvedName());
        if (!placeholder && genericsTypes != null) {
            ret.append(" <");
            for (int i = 0; i < genericsTypes.length; i++) {
                if (i != 0) ret.append(", ");
                GenericsType genericsType = genericsTypes[i];
                ret.append(genericTypeAsString(genericsType));
            }
            ret.append(">");
        }
        if (redirect != null && showRedirect) {
            ret.append(" -> ").append(redirect().toString());
        }
        return ret.toString();
    }

    /**
     * This exists to avoid a recursive definition of toString. The default toString
     * in GenericsType calls ClassNode.toString(), which calls GenericsType.toString(), etc. 
     * @param genericsType
     * @return the string representing the generic type
     */
    private String genericTypeAsString(GenericsType genericsType) {
        StringBuilder ret = new StringBuilder(genericsType.getName());
        if (genericsType.getUpperBounds() != null) {
            ret.append(" extends ");
            for (int i = 0; i < genericsType.getUpperBounds().length; i++) {
                ClassNode classNode = genericsType.getUpperBounds()[i];
                if (classNode.equals(this)) {
                    ret.append(classNode.getName());
                } else {
                    ret.append(classNode.toString(false));
                }
                if (i + 1 < genericsType.getUpperBounds().length) ret.append(" & ");
            }
        } else if (genericsType.getLowerBound() !=null) {
            ClassNode classNode = genericsType.getLowerBound();
            if (classNode.equals(this)) {
                ret.append(" super ").append(classNode.getName());
            } else {
                ret.append(" super ").append(classNode);
            }
        }
        return ret.toString();
    }

    /**
     * Returns true if the given method has a possibly matching instance method with the given name and arguments.
     *
     * @param name      the name of the method of interest
     * @param arguments the arguments to match against
     * @return true if a matching method was found
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
            for (MethodNode method : getMethods(name)) {
                if (hasCompatibleNumberOfArgs(method, count) && !method.isStatic()) {
                    return true;
                }
            }
            node = node.getSuperClass();
        }
        while (node != null);
        return false;
    }

    public MethodNode tryFindPossibleMethod(String name, Expression arguments) {
        int count = 0;

        if (arguments instanceof TupleExpression) {
            TupleExpression tuple = (TupleExpression) arguments;
            // TODO this won't strictly be true when using list expansion in argument calls
            count = tuple.getExpressions().size();
        } else
            return null;

        MethodNode res = null;
        ClassNode node = this;
        TupleExpression args = (TupleExpression) arguments;
        do {
            for (MethodNode method : node.getMethods(name)) {
                if (hasCompatibleNumberOfArgs(method, count)) {
                    boolean match = true;
                    for (int i = 0; i != count; ++i)
                        if (!hasCompatibleType(args, method, i)) {
                            match = false;
                            break;
                        }

                    if (match) {
                        if (res == null)
                            res = method;
                        else {
                            if (res.getParameters().length != count)
                                return null;
                            if (node.equals(this))
                                return null;

                            match = true;
                            for (int i = 0; i != count; ++i)
                                // prefer super method if it matches better
                                if (!hasExactMatchingCompatibleType(res, method, i)) {
                                    match = false;
                                    break;
                                }
                            if (!match)
                                return null;
                        }
                    }
                }
            }
            node = node.getSuperClass();
        }
        while (node != null);

        return res;
    }

    private boolean hasExactMatchingCompatibleType(MethodNode current, MethodNode newCandidate, int i) {
        int lastParamIndex = newCandidate.getParameters().length - 1;
        return current.getParameters()[i].getType().equals(newCandidate.getParameters()[i].getType())
                || (isPotentialVarArg(newCandidate, lastParamIndex) && i >= lastParamIndex && current.getParameters()[i].getType().equals(newCandidate.getParameters()[lastParamIndex].getType().componentType));
    }

    private boolean hasCompatibleType(TupleExpression args, MethodNode method, int i) {
        int lastParamIndex = method.getParameters().length - 1;
        return (i <= lastParamIndex && args.getExpression(i).getType().isDerivedFrom(method.getParameters()[i].getType()))
                || (isPotentialVarArg(method, lastParamIndex) && i >= lastParamIndex  && args.getExpression(i).getType().isDerivedFrom(method.getParameters()[lastParamIndex].getType().componentType));
    }

    private boolean hasCompatibleNumberOfArgs(MethodNode method, int count) {
        int lastParamIndex = method.getParameters().length - 1;
        return method.getParameters().length == count || (isPotentialVarArg(method, lastParamIndex) && count >= lastParamIndex);
    }

    private boolean isPotentialVarArg(MethodNode newCandidate, int lastParamIndex) {
        return lastParamIndex >= 0 && newCandidate.getParameters()[lastParamIndex].getType().isArray();
    }

    /**
     * Returns true if the given method has a possibly matching static method with the given name and arguments.
     *
     * @param name      the name of the method of interest
     * @param arguments the arguments to match against
     * @return true if a matching method was found
     */
    public boolean hasPossibleStaticMethod(String name, Expression arguments) {
        return ClassNodeUtils.hasPossibleStaticMethod(this, name, arguments, false);
    }

    public boolean isInterface(){
        return (getModifiers() & Opcodes.ACC_INTERFACE) > 0;
    }

    public boolean isAbstract(){
        return (getModifiers() & Opcodes.ACC_ABSTRACT) > 0;
    }

    public boolean isResolved() {
        if (clazz != null) return true;
        if (redirect != null) return redirect.isResolved();
        return componentType != null && componentType.isResolved();
    }

    public boolean isArray(){
        return componentType!=null;
    }

    public ClassNode getComponentType() {
        return componentType;
    }

    /**
     * Returns the concrete class this classnode relates to. However, this method
     * is inherently unsafe as it may return null depending on the compile phase you are
     * using. AST transformations should never use this method directly, but rather obtain
     * a new class node using {@link #getPlainNodeReference()}.
     * @return the class this classnode relates to. May return null.
     */
    public Class getTypeClass(){
        if (clazz != null) return clazz;
        if (redirect != null) return redirect.getTypeClass();

        ClassNode component = redirect().componentType;
        if (component!=null && component.isResolved()){
            return Array.newInstance(component.getTypeClass(), 0).getClass();
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
        ClassNode n = new ClassNode(name, modifiers, superClass,null,null);
        n.isPrimaryNode = false;
        n.setRedirect(redirect());
        if (isArray()) {
            n.componentType = redirect().getComponentType();
        } 
        return n;
    }

    public boolean isAnnotationDefinition() {
        return /* redirect().isPrimaryNode && */
                isInterface() && (getModifiers() & Opcodes.ACC_ANNOTATION) != 0;
    }

    public List<AnnotationNode> getAnnotations() {
        if (redirect!=null) return redirect.getAnnotations();
        lazyClassInit();
        return super.getAnnotations();
    }

    public List<AnnotationNode> getAnnotations(ClassNode type) {
        if (redirect!=null) return redirect.getAnnotations(type);
        lazyClassInit();
        return super.getAnnotations(type);
    }

    public void addTransform(Class<? extends ASTTransformation> transform, ASTNode node) {
        GroovyASTTransformation annotation = transform.getAnnotation(GroovyASTTransformation.class);
        if (annotation == null) return;

        Set<ASTNode> nodes = getTransformInstances().get(annotation.phase()).get(transform);
        if (nodes == null) {
            nodes = new LinkedHashSet<ASTNode>();
            getTransformInstances().get(annotation.phase()).put(transform, nodes);
        }
        nodes.add(node);
    }

    public Map<Class <? extends ASTTransformation>, Set<ASTNode>> getTransforms(CompilePhase phase) {
        return getTransformInstances().get(phase);
    }

    public void renameField(String oldName, String newName) {
        ClassNode r = redirect ();
        if (r.fieldIndex == null)
            r.fieldIndex = new LinkedHashMap<String,FieldNode> ();
        final Map<String,FieldNode> index = r.fieldIndex;
        index.put(newName, index.remove(oldName));
    }
    
    public void removeField(String oldName) {
        ClassNode r = redirect ();
        if (r.fieldIndex == null)
            r.fieldIndex = new LinkedHashMap<String,FieldNode> ();
        final Map<String,FieldNode> index = r.fieldIndex;
        r.fields.remove(index.get(oldName));
        index.remove(oldName);
    }

    public boolean isEnum() {
        return (getModifiers()&Opcodes.ACC_ENUM) != 0;
     }

    /**
     * @return iterator of inner classes defined inside this one
     */
    public Iterator<InnerClassNode> getInnerClasses() {
        return (innerClasses == null ? Collections.<InnerClassNode>emptyList() : innerClasses).iterator();
    }

    private Map<CompilePhase, Map<Class<? extends ASTTransformation>, Set<ASTNode>>> getTransformInstances() {
        if(transformInstances == null){
            transformInstances = new EnumMap<CompilePhase, Map<Class <? extends ASTTransformation>, Set<ASTNode>>>(CompilePhase.class);
            for (CompilePhase phase : CompilePhase.values()) {
                transformInstances.put(phase, new LinkedHashMap<Class <? extends ASTTransformation>, Set<ASTNode>>());
            }
        }
        return transformInstances;
    }
    
    public boolean isRedirectNode() {
        return redirect!=null;
    }

    @Override
    public String getText() {
        return getName();
    }
}
