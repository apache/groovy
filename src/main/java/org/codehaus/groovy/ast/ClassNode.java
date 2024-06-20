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

import org.apache.groovy.ast.tools.AnnotatedNodeUtils;
import org.apache.groovy.ast.tools.ClassNodeUtils;
import org.apache.groovy.lang.annotation.Incubating;
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
import org.codehaus.groovy.runtime.ArrayGroovyMethods;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getCodeAsBlock;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.recordNative;
import static org.codehaus.groovy.transform.trait.Traits.isTrait;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_ANNOTATION;
import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

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
 * are placeholders, its redirect points to the real structure, which can
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
public class ClassNode extends AnnotatedNode {

    private static class MapOfLists {
        Map<Object, List<MethodNode>> map;

        List<MethodNode> get(Object key) {
            return Optional.ofNullable(map)
                .map(m -> m.get(key)).orElseGet(Collections::emptyList);
        }

        void put(Object key, MethodNode value) {
            if (map == null) map = new LinkedHashMap<>();
            map.computeIfAbsent(key, k -> new ArrayList<>(2)).add(value);
        }

        void remove(Object key, MethodNode value) {
            get(key).remove(value);
        }
    }

    public static final ClassNode[] EMPTY_ARRAY = new ClassNode[0];
    public static final ClassNode THIS = new ClassNode(Object.class);
    public static final ClassNode SUPER = new ClassNode(Object.class);

    private String name;
    private int modifiers;
    private ClassNode[] interfaces;
    private MixinNode[] mixins;
    private List<Statement> objectInitializers;
    private List<ConstructorNode> constructors;
    // TODO: initialize for primary nodes only!
    private final MapOfLists methods = new MapOfLists();
    private List<MethodNode> methodsList = Collections.emptyList();
    private List<FieldNode> fields;
    private List<PropertyNode> properties;
    private Map<String, FieldNode> fieldIndex;
    private ClassNode superClass;
    protected boolean isPrimaryNode;
    // TODO: initialize for primary nodes only!
    private List<ClassNode> permittedSubclasses = new ArrayList<>();
    private List<RecordComponentNode> recordComponents = Collections.emptyList();

    // use this to synchronize access for the lazy init
    protected  final  Object lazyInitLock = new Object();
    // only false when this instance is constructed from a Class
    private volatile boolean lazyInitDone = true;
    /**
     * Initializes the complete class structure.
     */
    private void lazyClassInit() {
        if (lazyInitDone) return;
        synchronized (lazyInitLock) {
            if (redirect != null) {
                throw new GroovyBugError("lazyClassInit called on a proxy ClassNode. " +
                                         "A redirect() call is missing somewhere!");
            }
            if (lazyInitDone) return;
            VMPluginFactory.getPlugin().configureClassNode(getCompileUnit(), this);
            lazyInitDone = true;
        }
    }

    // if not null then this instance is resolved
    protected Class<?> clazz;
    // if not null then this instance is an array
    private ClassNode componentType;
    // if not null then this instance is handled as proxy for the redirect
    private ClassNode redirect;

    //--------------------------------------------------------------------------

    /**
     * @param name       the fully-qualified name of the class
     * @param modifiers  the modifiers; see {@link java.lang.reflect.Modifier Modifier} or {@link org.objectweb.asm.Opcodes Opcodes}
     * @param superClass the base class; use "java.lang.Object" if no direct base class
     * @param interfaces the interfaces
     * @param mixins     the mixins
     */
    public ClassNode(final String name, final int modifiers, final ClassNode superClass, final ClassNode[] interfaces, final MixinNode[] mixins) {
        this.name = name;
        this.modifiers = modifiers;

        this.isPrimaryNode = true;
        setSuperClass(superClass);
        setInterfaces(interfaces);
        setMixins(mixins);
    }

    /**
     * @param name       the fully-qualified name of the class
     * @param modifiers  the modifiers; see {@link java.lang.reflect.Modifier Modifier} or {@link org.objectweb.asm.Opcodes Opcodes}
     * @param superClass the base class; use "java.lang.Object" if no direct base class
     */
    public ClassNode(final String name, final int modifiers, final ClassNode superClass) {
        this(name, modifiers, superClass, ClassNode.EMPTY_ARRAY, MixinNode.EMPTY_ARRAY);
    }

    /**
     * Creates a non-primary {@code ClassNode} from a real class.
     */
    public ClassNode(final Class<?> c) {
        this(c.getName(), c.getModifiers(), null, null, MixinNode.EMPTY_ARRAY);
        this.clazz = c;
        this.lazyInitDone = false;
        this.isPrimaryNode = false;
    }

    /**
     * Constructor used by {@code makeArray()} if a real class is available.
     */
    private ClassNode(final Class<?> c, final ClassNode componentType) {
        this(c);
        this.componentType = componentType;
    }

    /**
     * Constructor used by {@code makeArray()} if no real class is available.
     */
    private ClassNode(final ClassNode componentType) {
        this(componentType.getName() + "[]", ACC_ABSTRACT | ACC_FINAL | ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
          new ClassNode[]{ClassHelper.CLONEABLE_TYPE, ClassHelper.SERIALIZABLE_TYPE}, MixinNode.EMPTY_ARRAY);
        this.componentType = componentType.redirect();
        this.isPrimaryNode = false;
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the {@code ClassNode} this node is a proxy for or the node itself.
     */
    public ClassNode redirect() {
        return (redirect == null ? this : redirect.redirect());
    }

    public boolean isRedirectNode() {
        return (redirect != null);
    }

    /**
     * Sets this instance as proxy for the given {@code ClassNode}.
     *
     * @param node the class to redirect to; if {@code null} the redirect is removed
     */
    public void setRedirect(ClassNode node) {
        if (isPrimaryNode) throw new GroovyBugError("tried to set a redirect for a primary ClassNode (" + getName() + "->" + node.getName() + ").");
        if (node != null && !isGenericsPlaceHolder()) node = node.redirect();
        if (node == this) return;
        redirect = node;
    }

    /**
     * @return {@code true} if this instance is a primary {@code ClassNode}
     */
    public boolean isPrimaryClassNode() {
        return redirect().isPrimaryNode || (componentType != null && componentType.isPrimaryClassNode());
    }

    public ClassNode getPlainNodeReference() {
        return getPlainNodeReference(true);
    }

    public ClassNode getPlainNodeReference(boolean skipPrimitives) {
        if (skipPrimitives && ClassHelper.isPrimitiveType(this)) return this;
        ClassNode n = new ClassNode(name, modifiers, superClass, null, null);
        n.isPrimaryNode = false;
        n.setRedirect(redirect());
        if (isArray()) {
            n.componentType = redirect().getComponentType();
        }
        return n;
    }

    //

    public ModuleNode getModule() {
        return redirect().getNodeMetaData(ModuleNode.class);
    }

    public void setModule(ModuleNode module) {
        if (isPrimaryNode) putNodeMetaData(ModuleNode.class, module);
    }

    public CompileUnit getCompileUnit() {
        if (redirect != null) return redirect.getCompileUnit();
        return Optional.ofNullable(getModule()).map(ModuleNode::getUnit).orElse(null);
    }

    @Deprecated(forRemoval = true, since = "5.0.0")
    protected void setCompileUnit(CompileUnit cu) {
        if (redirect != null)
            redirect.setCompileUnit(cu);
    }

    public PackageNode getPackage() {
        return Optional.ofNullable(getModule()).map(ModuleNode::getPackage).orElse(null);
    }

    public String  getPackageName() {
        int idx = getName().lastIndexOf('.');
        if (idx > 0) {
            return getName().substring(0, idx);
        }
        return null;
    }

    public boolean hasPackageName() {
        return (getName().indexOf('.') > 0);
    }

    public String getNameWithoutPackage() {
        int idx = getName().lastIndexOf('.');
        if (idx > 0) {
            return getName().substring(idx + 1);
        }
        return getName();
    }

    //

    public String getUnresolvedName() {
        return name;
    }

    public ClassNode getUnresolvedSuperClass() {
        return getUnresolvedSuperClass(true);
    }

    public ClassNode getUnresolvedSuperClass(final boolean deref) {
        if (deref) {
            if (redirect != null)
                return redirect.getUnresolvedSuperClass(true);
            lazyClassInit();
        }
        return superClass;
    }

    public void setUnresolvedSuperClass(final ClassNode superClass) {
        this.superClass = superClass;
    }

    public ClassNode[] getUnresolvedInterfaces() {
        return getUnresolvedInterfaces(true);
    }

    public ClassNode[] getUnresolvedInterfaces(final boolean deref) {
        if (deref) {
            if (redirect != null)
                return redirect.getUnresolvedInterfaces(true);
            lazyClassInit();
        }
        return interfaces;
    }

    //--------------------------------------------------------------------------

    @Override
    public String getText() {
        return getName();
    }

    public String getName() {
        return redirect().name;
    }

    public String setName(final String name) {
        return redirect != null ? redirect.setName(name) : (this.name = name);
    }

    public int getModifiers() {
        return redirect().modifiers;
    }

    public void setModifiers(final int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * @return the {@code ClassNode} of the super class of this type
     */
    public ClassNode getSuperClass() {
        if (!lazyInitDone && !isResolved()) {
            throw new GroovyBugError("ClassNode#getSuperClass for " + getName() + " called before class resolving");
        }
        var sc = redirect().getUnresolvedSuperClass();
        if (sc != null) {
            sc = sc.redirect();
            if (isPrimaryClassNode() && (sc.isInterface() || isTrait(sc)))
                sc = ClassHelper.OBJECT_TYPE; // GROOVY-8272, GROOVY-11299
        }
        return sc;
    }

    public void setSuperClass(final ClassNode superClass) {
        if (redirect != null) {
            redirect.setSuperClass(superClass);
        } else {
            this.superClass = superClass;
            // GROOVY-10763: update generics indicator
            if (superClass != null && !usesGenerics && isPrimaryNode) {
                usesGenerics = superClass.isUsingGenerics();
            }
        }
    }

    /**
     * @return the interfaces implemented by this {@code ClassNode}
     */
    public ClassNode[] getInterfaces() {
        if (redirect != null)
            return redirect.getInterfaces();
        lazyClassInit();
        return interfaces;
    }

    public Set<ClassNode> getAllInterfaces() {
        Set<ClassNode> result = new LinkedHashSet<>();
        if (isInterface()) result.add(this);
        getAllInterfaces(result);
        return result;
    }

    private void getAllInterfaces(final Set<ClassNode> set) {
        for (ClassNode face : getInterfaces()) {
            if (set.add(face)) // GROOVY-11036
                face.getAllInterfaces(set);
        }
    }

    public void setInterfaces(final ClassNode[] interfaces) {
        if (redirect != null) {
            redirect.setInterfaces(interfaces);
        } else {
            this.interfaces = interfaces;
            // GROOVY-10763: update generics indicator
            if (interfaces != null && !usesGenerics && isPrimaryNode) {
                for (int i = 0, n = interfaces.length; i < n; i += 1) {
                    usesGenerics |= interfaces[i].isUsingGenerics();
                }
            }
        }
    }

    /**
     * @return the mixins associated with this {@code ClassNode}
     */
    public MixinNode[] getMixins() {
        return redirect().mixins;
    }

    public void setMixins(final MixinNode[] mixins) {
        if (redirect != null) {
            redirect.setMixins(mixins);
        } else {
            this.mixins = mixins;
        }
    }

    /**
     * @return permitted subclasses of sealed type, may initially be empty in early compiler phases
     */
    @Incubating
    public List<ClassNode> getPermittedSubclasses() {
        if (redirect != null)
            return redirect.getPermittedSubclasses();
        lazyClassInit();
        return permittedSubclasses;
    }

    @Incubating
    public void setPermittedSubclasses(final List<ClassNode> permittedSubclasses) {
        if (redirect != null) {
            redirect.setPermittedSubclasses(permittedSubclasses);
        } else {
            this.permittedSubclasses = permittedSubclasses;
        }
    }

    /**
     * Gets the record components of record type.
     *
     * @return {@code RecordComponentNode} instances
     * @since 4.0.0
     */
    @Incubating
    public List<RecordComponentNode> getRecordComponents() {
        if (redirect != null)
            return redirect.getRecordComponents();
        lazyClassInit();
        return recordComponents;
    }

    /**
     * Sets the record components for record type.
     *
     * @since 4.0.0
     */
    @Incubating
    public void setRecordComponents(final List<RecordComponentNode> recordComponents) {
        if (redirect != null) {
            redirect.setRecordComponents(recordComponents);
        } else {
            this.recordComponents = recordComponents;
        }
    }

    //--------------------------------------------------------------------------

    public void addInterface(ClassNode node) {
        ClassNode[] interfaces = getInterfaces();
        for (ClassNode face : interfaces) {
            if (face.equals(node)) return;
        }
        final int n = interfaces.length;

        System.arraycopy(interfaces, 0, interfaces = new ClassNode[n + 1], 0, n);
        interfaces[n] = node; // append interface
        setInterfaces(interfaces);
    }

    public void addMixin(MixinNode node) {
        // let's check if it already uses a mixin
        MixinNode[] mixins = getMixins();
        boolean skip = false;
        for (MixinNode existing : mixins) {
            if (node.equals(existing)) {
                skip = true;
                break;
            }
        }
        if (!skip) {
            MixinNode[] newMixins = new MixinNode[mixins.length + 1];
            System.arraycopy(mixins, 0, newMixins, 0, mixins.length);
            newMixins[mixins.length] = node;
            setMixins(newMixins);
        }
    }

    public void addField(FieldNode node) {
        addField(node, true);
    }

    private void addField(FieldNode node, boolean append) {
        ClassNode r = redirect();
        node.setDeclaringClass(r);
        node.setOwner(r);
        if (r.fields == null)
            r.fields = new ArrayList<>(4);
        if (r.fieldIndex == null)
            r.fieldIndex = new LinkedHashMap<>();

        if (append) {
            r.fields.add(node);
        } else {
            r.fields.add(0, node);
        }
        r.fieldIndex.put(node.getName(), node);
    }

    public FieldNode addField(String name, int modifiers, ClassNode type, Expression initialValue) {
        FieldNode node = new FieldNode(name, modifiers, type, redirect(), initialValue);
        addField(node);
        return node;
    }

    public void addFieldFirst(FieldNode node) {
        addField(node, false);
    }

    public FieldNode addFieldFirst(String name, int modifiers, ClassNode type, Expression initialValue) {
        FieldNode node = new FieldNode(name, modifiers, type, redirect(), initialValue);
        addFieldFirst(node);
        return node;
    }

    public void addProperty(PropertyNode node) {
        node.setDeclaringClass(redirect());
        addField(node.getField());
        getProperties().add(node);
    }

    public PropertyNode addProperty(String name, int modifiers, ClassNode type, Expression initialValue, Statement getterBlock, Statement setterBlock) {
        for (PropertyNode pn : getProperties()) {
            if (pn.getName().equals(name)) {
                if (pn.getInitialExpression() == null && initialValue != null)
                    pn.getField().setInitialValueExpression(initialValue);

                if (pn.getGetterBlock() == null && getterBlock != null)
                    pn.setGetterBlock(getterBlock);

                if (pn.getSetterBlock() == null && setterBlock != null)
                    pn.setSetterBlock(setterBlock);

                return pn;
            }
        }
        PropertyNode node = new PropertyNode(name, modifiers, type, redirect(), initialValue, getterBlock, setterBlock);
        addProperty(node);
        return node;
    }

    public void addMethod(MethodNode node) {
        ClassNode r = redirect();
        node.setDeclaringClass(r);
        if (r.methodsList.isEmpty()) {
            r.methodsList = new ArrayList<>(4);
        }
        r.methodsList.add(node);
        r.methods.put(node.getName(), node);
    }

    /**
     * If a method with the given name and parameters is already defined then it is returned
     * otherwise the given method is added to this node. This method is useful for
     * default method adding like getProperty() or invokeMethod() where there may already
     * be a method defined in a class and so the default implementations should not be added
     * if already present.
     */
    public MethodNode addMethod(String name, int modifiers, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        MethodNode other = getDeclaredMethod(name, parameters);
        // don't add duplicate methods
        if (other != null) {
            return other;
        }
        MethodNode node = new MethodNode(name, modifiers, returnType, parameters, exceptions, code);
        addMethod(node);
        return node;
    }

    /**
     * Adds a synthetic method as part of the compilation process.
     */
    public MethodNode addSyntheticMethod(String name, int modifiers, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        MethodNode node = addMethod(name, modifiers | ACC_SYNTHETIC, returnType, parameters, exceptions, code);
        node.setSynthetic(true);
        return node;
    }

    public void addConstructor(ConstructorNode node) {
        ClassNode r = redirect();
        node.setDeclaringClass(r);
        if (r.constructors == null)
            r.constructors = new ArrayList<>(4);
        r.constructors.add(node);
    }

    public ConstructorNode addConstructor(int modifiers, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        ConstructorNode node = new ConstructorNode(modifiers, parameters, exceptions, code);
        addConstructor(node);
        return node;
    }

    /**
     * Adds a statement to the object initializer.
     *
     * @param statement the statement to be added
     */
    public void addObjectInitializerStatements(Statement statement) {
        getObjectInitializerStatements().add(statement);
    }

    public void addStaticInitializerStatements(List<Statement> statements, boolean fieldInit) {
        MethodNode method = getOrAddStaticInitializer();
        BlockStatement block = getCodeAsBlock(method);

        // while anything inside a static initializer block is appended
        // we don't want to append in the case we have an initialization
        // expression of a static field. In that case we want to add
        // before the other statements
        if (!fieldInit) {
            block.addStatements(statements);
        } else {
            List<Statement> blockStatements = block.getStatements();
            statements.addAll(blockStatements);
            blockStatements.clear();
            blockStatements.addAll(statements);
        }
    }

    public void positionStmtsAfterEnumInitStmts(List<Statement> staticFieldInitializerStatements) {
        MethodNode constructor = getOrAddStaticInitializer();
        Statement statement = constructor.getCode();
        if (statement instanceof BlockStatement) {
            BlockStatement block = (BlockStatement) statement;
            // add given statements for explicitly declared static fields just after enum-special fields
            // are found - the $VALUES binary expression marks the end of such fields.
            List<Statement> blockStatements = block.getStatements();
            for (ListIterator<Statement> it = blockStatements.listIterator(); it.hasNext(); ) {
                Statement stmt = it.next();
                if (stmt instanceof ExpressionStatement &&
                        ((ExpressionStatement) stmt).getExpression() instanceof BinaryExpression) {
                    BinaryExpression bExp = (BinaryExpression) ((ExpressionStatement) stmt).getExpression();
                    if (bExp.getLeftExpression() instanceof FieldExpression) {
                        FieldExpression fExp = (FieldExpression) bExp.getLeftExpression();
                        if (fExp.getFieldName().equals("$VALUES")) {
                            for (Statement initStmt : staticFieldInitializerStatements) {
                                it.add(initStmt);
                            }
                        }
                    }
                }
            }
        }
    }

    private MethodNode getOrAddStaticInitializer() {
        MethodNode method;
        final String classInitializer = "<clinit>";
        List<MethodNode> declaredMethods = getDeclaredMethods(classInitializer);
        if (declaredMethods.isEmpty()) {
            method = addMethod(classInitializer, ACC_STATIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());
            method.setSynthetic(true);
        } else {
            method = declaredMethods.get(0);
        }
        return method;
    }

    //--------------------------------------------------------------------------

    /**
     * @return the fields associated with this {@code ClassNode}
     */
    public List<FieldNode> getFields() {
        if (redirect != null)
            return redirect.getFields();
        lazyClassInit();
        if (fields == null)
            fields = new ArrayList<>();
        return fields;
    }

    public List<PropertyNode> getProperties() {
        if (redirect != null)
            return redirect.getProperties();
        if (properties == null)
            properties = new ArrayList<>();
        return properties;
    }

    @Deprecated(forRemoval = true, since = "5.0.0")
    public Map<String, FieldNode> getFieldIndex() {
        return fieldIndex;
    }

    public boolean hasProperty(String name) {
        return getProperties().stream().map(PropertyNode::getName).anyMatch(name::equals);
    }

    public PropertyNode getProperty(String name) {
        return getProperties().stream().filter(pn -> pn.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * @return the methods associated with this {@code ClassNode}
     */
    public List<MethodNode> getMethods() {
        if (redirect != null)
            return redirect.getMethods();
        lazyClassInit();
        return methodsList;
    }

    /**
     * @return the abstract methods associated with this {@code ClassNode}
     */
    public List<MethodNode> getAbstractMethods() {
        return getDeclaredMethodsMap().values().stream()
            .filter(MethodNode::isAbstract).collect(toList());
    }

    public List<MethodNode> getAllDeclaredMethods() {
        return new ArrayList<>(getDeclaredMethodsMap().values());
    }

    public Map<String, MethodNode> getDeclaredMethodsMap() {
        Map<String, MethodNode> result = ClassNodeUtils.getDeclaredMethodsFromSuper(this);
        ClassNodeUtils.addDeclaredMethodsFromInterfaces(this, result);
        // add in the methods implemented in this class
        for (MethodNode method : getMethods()) {
            result.put(method.getTypeDescriptor(), method);
        }
        return result;
    }

    public List<ConstructorNode> getDeclaredConstructors() {
        if (redirect != null)
            return redirect.getDeclaredConstructors();
        lazyClassInit();
        if (constructors == null)
            constructors = new ArrayList<>();
        return constructors;
    }

    /**
     * @return the constructor matching the given parameters or {@code null}
     */
    public ConstructorNode getDeclaredConstructor(Parameter[] parameters) {
        for (ConstructorNode constructor : getDeclaredConstructors()) {
            if (parametersEqual(constructor.getParameters(), parameters)) {
                return constructor;
            }
        }
        return null;
    }

    /**
     * @see #getMethod(String, Parameter[])
     */
    public boolean hasMethod(String name, Parameter[] parameters) {
        return (getMethod(name, parameters) != null);
    }

    /**
     * @see #getDeclaredMethod(String, Parameter[])
     */
    public boolean hasDeclaredMethod(String name, Parameter[] parameters) {
        return (getDeclaredMethod(name, parameters) != null);
    }

    /**
     * Finds a field matching the given name in this class.
     *
     * @param name the name of the field of interest
     * @return the method matching the given name and parameters or null
     */
    public FieldNode getDeclaredField(String name) {
        if (redirect != null)
            return redirect.getDeclaredField(name);
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

    public List<Statement> getObjectInitializerStatements() {
        if (objectInitializers == null)
            objectInitializers = new ArrayList<>();
        return objectInitializers;
    }

    /**
     * Returns a list of all methods with the given name from this class.
     *
     * @return method list (possibly empty)
     * @see #getMethods(String)
     */
    public List<MethodNode> getDeclaredMethods(String name) {
        if (redirect != null)
            return redirect.getDeclaredMethods(name);
        lazyClassInit();
        return methods.get(name);
    }

    /**
     * Returns a list of all methods with the given name from this class and its
     * super class(es).
     *
     * @return method list (possibly empty)
     * @see #getDeclaredMethods(String)
     */
    public List<MethodNode> getMethods(String name) {
        List<MethodNode> list = new ArrayList<>(4);
        ClassNode node = this;
        while (node != null) {
            list.addAll(node.getDeclaredMethods(name));
            node = node.getSuperClass();
        }
        return list;
    }

    /**
     * Finds a method matching the given name and parameters in this class.
     *
     * @return method node or null
     */
    public MethodNode getDeclaredMethod(String name, Parameter[] parameters) {
        boolean zeroParameters = !ArrayGroovyMethods.asBoolean(parameters);
        for (MethodNode method : getDeclaredMethods(name)) {
            if (zeroParameters ? method.getParameters().length == 0
                    : parametersEqual(method.getParameters(), parameters)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Finds a method matching the given name and parameters in this class
     * or any super class.
     *
     * @return method node or null
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
        if (ClassHelper.isPrimitiveVoid(this)) {
            return ClassHelper.isPrimitiveVoid(type);
        }
        if (ClassHelper.isObjectType(type)) {
            return true;
        }
        if (this.isArray() && type.isArray()
                && ClassHelper.isObjectType(type.getComponentType())
                && !ClassHelper.isPrimitiveType(this.getComponentType())) {
            return true;
        }
        for (ClassNode node = this; node != null; node = node.getSuperClass()) {
            if (type.equals(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return {@code true} if this type implements {@code GroovyObject}
     */
    public boolean isDerivedFromGroovyObject() {
        return implementsInterface(ClassHelper.GROOVY_OBJECT_TYPE);
    }

    /**
     * @param classNodes the class nodes for the interfaces
     * @return {@code true} if this type implements any of the given interfaces
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
     * @return {@code true} if this type implements the given interface
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
     * @return {@code true} if this type declares that it implements any of the
     * given interfaces or if one of its interfaces extends directly/indirectly
     * any of the given interfaces
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
     * @return {@code true} if this class declares that it implements the given
     * interface or if one of its interfaces extends directly/indirectly the interface
     *
     * NOTE: Doesn't consider an interface to implement itself.
     * I think this is intended to be called on ClassNodes representing
     * classes, not interfaces.
     *
     * @see org.codehaus.groovy.ast.tools.GeneralUtils#isOrImplements
     */
    public boolean declaresInterface(ClassNode classNode) {
        ClassNode[] interfaces = getInterfaces();
        for (ClassNode face : interfaces) {
            if (face.equals(classNode)) {
                return true;
            }
        }
        for (ClassNode face : interfaces) {
            if (face.declaresInterface(classNode)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated(forRemoval = true, since = "4.0.0")
    protected boolean parametersEqual(Parameter[] a, Parameter[] b) {
        return ParameterUtils.parametersEqual(a, b);
    }

    public MethodNode getGetterMethod(String getterName) {
        return getGetterMethod(getterName, true);
    }

    public MethodNode getGetterMethod(String getterName, boolean searchSupers) {
        MethodNode getterMethod = null;

        java.util.function.Predicate<MethodNode> isNullOrSynthetic = (method) ->
                (method == null || (method.getModifiers() & ACC_SYNTHETIC) != 0);

        boolean booleanReturnOnly = getterName.startsWith("is");
        for (MethodNode method : getDeclaredMethods(getterName)) {
            if (booleanReturnOnly ? ClassHelper.isPrimitiveBoolean(method.getReturnType()) : !method.isVoidMethod()) {
                if (method.getParameters().length == 0) {
                    // GROOVY-7363, GROOVY-11341: There can be multiple matches if a method returns a non-final type due to
                    // the generation of a bridge method. The real getter is really the non-bridge, non-synthetic one as it
                    // has the most specific and exact return type of the two. Picking the bridge method results in loss of
                    // type information, as it down-casts the return type to the lower bound of the generic parameter.
                    if (isNullOrSynthetic.test(getterMethod)) {
                        getterMethod = method;
                    }
                } else if (method.hasDefaultValue() && Stream.of(method.getParameters()).allMatch(Parameter::hasInitialExpression)) {
                    // GROOVY-11380: getter generated later by default arguments
                    if (isNullOrSynthetic.test(getterMethod)) {
                        getterMethod = new MethodNode(method.getName(), method.getModifiers() & ~ACC_ABSTRACT, method.getReturnType(), Parameter.EMPTY_ARRAY, method.getExceptions(), null);
                        getterMethod.setSynthetic(true);
                        getterMethod.setDeclaringClass(this);
                        getterMethod.addAnnotations(method.getAnnotations());
                        AnnotatedNodeUtils.markAsGenerated(this, getterMethod);
                        getterMethod.setGenericsTypes(method.getGenericsTypes());
                    }
                }
            }
        }

        if (searchSupers && isNullOrSynthetic.test(getterMethod)) {
            ClassNode superClass = getSuperClass();
            if (superClass != null) {
                MethodNode method = superClass.getGetterMethod(getterName);
                if (getterMethod == null || !isNullOrSynthetic.test(method)) {
                    getterMethod = method;
                }
            }
            // GROOVY-11381:
            if (getterMethod == null && ArrayGroovyMethods.asBoolean(getInterfaces())) {
                for (ClassNode anInterface : getAllInterfaces()) {
                    MethodNode method = anInterface.getDeclaredMethod(getterName, Parameter.EMPTY_ARRAY);
                    if (method != null && method.isDefault() && (booleanReturnOnly ? ClassHelper.isPrimitiveBoolean(method.getReturnType()) : !method.isVoidMethod())) {
                        getterMethod = method;
                        break;
                    }
                }
            }
        }

        return getterMethod;
    }

    public MethodNode getSetterMethod(String setterName) {
        return getSetterMethod(setterName, true);
    }

    public MethodNode getSetterMethod(String setterName, boolean voidOnly) {
        for (MethodNode method : getDeclaredMethods(setterName)) {
            if (setterName.equals(method.getName())
                    && method.getParameters().length == 1
                    && (!voidOnly || method.isVoidMethod())) {
                return method;
            }
        }
        ClassNode parent = getSuperClass();
        if (parent != null) {
            return parent.getSetterMethod(setterName, voidOnly);
        }
        return null;
    }

    /**
     * Determines if the type has a possibly-matching instance method with the given name and arguments.
     *
     * @param name      the name of the method of interest
     * @param arguments the arguments to match against
     * @return true if a matching method was found
     */
    public boolean hasPossibleMethod(final String name, final Expression arguments) {
        int count;
        if (arguments instanceof TupleExpression) {
            // TODO: this won't strictly be true when using list expansion in argument calls
            count = ((TupleExpression) arguments).getExpressions().size();
        } else {
            count = 0;
        }

        for (ClassNode cn = this; cn != null; cn = cn.getSuperClass()) {
            for (MethodNode mn : cn.getDeclaredMethods(name)) {
                if (!mn.isStatic() && hasCompatibleNumberOfArgs(mn, count)) {
                    return true;
                }
            }
            for (ClassNode in : cn.getAllInterfaces()) {
                for (MethodNode mn : in.getDeclaredMethods(name)) {
                    if (mn.isDefault() && hasCompatibleNumberOfArgs(mn, count)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public MethodNode tryFindPossibleMethod(final String name, final Expression arguments) {
        List<Expression> args = arguments instanceof TupleExpression ? ((TupleExpression) arguments).getExpressions() : Collections.singletonList(arguments);
        int nArgs = args.size(); // TODO: this isn't strictly accurate when using spread argument expansion
        MethodNode method = null;

        for (ClassNode cn = this; cn != null; cn = cn.getSuperClass()) {
            for (MethodNode mn : cn.getDeclaredMethods(name)) {
                if (hasCompatibleNumberOfArgs(mn, nArgs)) {
                    boolean match = true;
                    for (int i = 0; i < nArgs; i += 1) {
                        if (!hasCompatibleType(args.get(i), mn, i)) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        if (method == null) {
                            method = mn;
                        } else if (cn.equals(this)
                                || method.getParameters().length != nArgs) {
                            return null;
                        } else {
                            for (int i = 0; i < nArgs; i += 1) {
                                // prefer super method if it matches better
                                if (!hasExactMatchingCompatibleType(method, mn, i)) {
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
        }

faces:  if (method == null && ArrayGroovyMethods.asBoolean(getInterfaces())) { // GROOVY-11323
            for (ClassNode cn : getAllInterfaces()) {
                for (MethodNode mn : cn.getDeclaredMethods(name)) {
                    if (mn.isPublic() && !mn.isStatic() && hasCompatibleNumberOfArgs(mn, nArgs) && (nArgs == 0
                            || IntStream.range(0,nArgs).allMatch(i -> hasCompatibleType(args.get(i),mn,i)))) {
                        method = mn;
                        break faces;
                    }
                }
            }
        }

        return method;
    }

    private static boolean hasExactMatchingCompatibleType(final MethodNode match, final MethodNode maybe, final int i) {
        int lastParamIndex = maybe.getParameters().length - 1;
        return (i <= lastParamIndex && match.getParameters()[i].getType().equals(maybe.getParameters()[i].getType()))
                || (i >= lastParamIndex && isPotentialVarArg(maybe, lastParamIndex) && match.getParameters()[i].getType().equals(maybe.getParameters()[lastParamIndex].getType().getComponentType()));
    }

    private static boolean hasCompatibleType(final Expression arg, final MethodNode method, final int i) {
        int lastParamIndex = method.getParameters().length - 1;
        return (i <= lastParamIndex && arg.getType().isDerivedFrom(method.getParameters()[i].getType()))
                || (i >= lastParamIndex && isPotentialVarArg(method, lastParamIndex) && arg.getType().isDerivedFrom(method.getParameters()[lastParamIndex].getType().getComponentType()));
    }

    private static boolean hasCompatibleNumberOfArgs(final MethodNode method, final int nArgs) {
        int lastParamIndex = method.getParameters().length - 1;
        return nArgs == method.getParameters().length || (nArgs >= lastParamIndex && isPotentialVarArg(method, lastParamIndex));
    }

    private static boolean isPotentialVarArg(final MethodNode method, final int lastParamIndex) {
        return lastParamIndex >= 0 && method.getParameters()[lastParamIndex].getType().isArray();
    }

    /**
     * Checks if the given method has a possibly matching static method with the
     * given name and arguments.
     *
     * @param name      the name of the method of interest
     * @param arguments the arguments to match against
     * @return {@code true} if a matching method was found
     */
    public boolean hasPossibleStaticMethod(final String name, final Expression arguments) {
        return ClassNodeUtils.hasPossibleStaticMethod(this, name, arguments, false);
    }

    //--------------------------------------------------------------------------

    public void renameField(String oldName, String newName) {
        ClassNode r = redirect();
        var index = r.fieldIndex;
        if (index != null)
            index.put(newName, index.remove(oldName));
    }

    public void removeField(String oldName) {
        ClassNode r = redirect();
        var index = r.fieldIndex;
        if (index != null)
            r.fields.remove(index.remove(oldName));
    }

    public void removeMethod(MethodNode node) {
        ClassNode r = redirect();
        if (!r.methodsList.isEmpty()) {
            r.methodsList.remove(node);
        }
        r.methods.remove(node.getName(), node);
    }

    public void removeConstructor(ConstructorNode node) {
        getDeclaredConstructors().remove(node);
    }

    //--------------------------------------------------------------------------

    @Override
    public boolean equals(Object that) {
        if (that == this) return true;
        if (!(that instanceof ClassNode)) return false;
        if (redirect != null) return redirect.equals(that);
        if (componentType != null) return componentType.equals(((ClassNode) that).componentType);
        return ((ClassNode) that).getText().equals(getText()); // arrays could be "T[]" or "[LT;"
    }

    @Override
    public     int hashCode() {
        return (redirect != null ? redirect.hashCode() : getText().hashCode());
    }

    @Override
    public  String toString() {
        return toString(true);
    }

    public  String toString(boolean showRedirect) {
        if (isArray()) {
            return getComponentType().toString(showRedirect) + "[]";
        }
        boolean placeholder = isGenericsPlaceHolder();
        StringBuilder ret = new StringBuilder(!placeholder ? getName() : getUnresolvedName());
        GenericsType[] genericsTypes = getGenericsTypes();
        if (!placeholder && genericsTypes != null) {
            ret.append('<');
            for (int i = 0, n = genericsTypes.length; i < n; i += 1) {
                if (i != 0) ret.append(", ");
                ret.append(genericsTypes[i]);
            }
            ret.append('>');
        }
        if (showRedirect && redirect != null) {
            ret.append(" -> ").append(redirect);
        }
        return ret.toString();
    }

    public void visitContents(GroovyClassVisitor visitor) {
        // now let's visit the contents of the class
        for (PropertyNode pn : getProperties()) {
            visitor.visitProperty(pn);
        }

        for (FieldNode fn : getFields()) {
            visitor.visitField(fn);
        }

        for (ConstructorNode cn : getDeclaredConstructors()) {
            visitor.visitConstructor(cn);
        }

        visitMethods(visitor);
    }

    private void visitMethods(GroovyClassVisitor visitor) {
        // create snapshot of the method list to avoid ConcurrentModificationException
        List<MethodNode> methodList = new ArrayList<>(getMethods());
        for (MethodNode mn : methodList) {
            visitor.visitMethod(mn);
        }

        // visit the method nodes added while iterating,
        // e.g. synthetic method for constructor reference
        final List<MethodNode> newMethodList = getMethods();
        if (newMethodList.size() > methodList.size()) { // if the newly added method nodes found, visit them
            List<MethodNode> changedMethodList = new ArrayList<>(newMethodList);
            boolean changed = changedMethodList.removeAll(methodList);
            if (changed) {
                for (MethodNode mn : changedMethodList) {
                    visitor.visitMethod(mn);
                }
            }
        }
    }

    //--------------------------------------------------------------------------

    public boolean isAbstract() {
        return (getModifiers() & ACC_ABSTRACT) != 0;
    }

    public boolean isInterface() {
        return (getModifiers() & ACC_INTERFACE) != 0;
    }

    public boolean isAnnotationDefinition() {
        return isInterface() && (getModifiers() & ACC_ANNOTATION) != 0;
    }

    public boolean isEnum() {
        return (getModifiers() & ACC_ENUM) != 0;
    }

    /**
     * Checks if the {@link ClassNode} instance represents a native {@code record}.
     * Check instead for the {@code RecordBase} annotation if looking for records and
     * record-like classes currently being compiled.
     *
     * @return {@code true} if the instance represents a native {@code record}
     * @since 4.0.0
     */
    @Incubating
    public boolean isRecord() {
        return recordNative(this);
    }

    /**
     * @return {@code true} for native and emulated (annotation based) sealed classes
     * @since 4.0.0
     */
    @Incubating
    public boolean isSealed() {
        if (redirect != null) return redirect.isSealed();
        return !getAnnotations(ClassHelper.SEALED_TYPE).isEmpty() || !getPermittedSubclasses().isEmpty();
    }

    public boolean isResolved() {
        if (clazz != null) return true;
        if (redirect != null) return redirect.isResolved();
        return (componentType != null && componentType.isResolved());
    }

    /**
     * Returns the concrete class this classnode relates to. However, this method
     * is inherently unsafe as it may return null depending on the compile phase you are
     * using. AST transformations should never use this method directly, but rather obtain
     * a new class node using {@link #getPlainNodeReference()}.
     * @return the class this classnode relates to. May return null.
     */
    public Class getTypeClass() {
        if (clazz != null) return clazz;
        if (redirect != null) return redirect.getTypeClass();

        ClassNode component = redirect().componentType;
        if (component != null && component.isResolved()) {
            return Array.newInstance(component.getTypeClass(), 0).getClass();
        }
        throw new GroovyBugError("ClassNode#getTypeClass for " + getName() + " called before the type class is set");
    }

    //

    public boolean isArray() {
        return (componentType != null);
    }

    /**
     * Returns a {@code ClassNode} representing an array of the type represented
     * by this.
     */
    public ClassNode makeArray() {
        ClassNode node;
        if (redirect != null) {
            node = redirect.makeArray();
            node.componentType = this;
        } else if (clazz != null) {
            Class<?> type = Array.newInstance(clazz, 0).getClass();
            // don't use the ClassHelper here!
            node = new ClassNode(type, this);
        } else {
            node = new ClassNode(this);
        }
        return node;
    }

    public ClassNode getComponentType() {
        return componentType;
    }

    //

    public ClassNode getOuterClass() {
        if (redirect != null) {
            return redirect.getOuterClass();
        }
        return null;
    }

    public List<ClassNode> getOuterClasses() {
        ClassNode outer = getOuterClass();
        if (outer == null) {
            return Collections.emptyList();
        }
        List<ClassNode> result = new ArrayList<>(4);
        do {
            result.add(outer);
        } while ((outer = outer.getOuterClass()) != null);

        return result;
    }

    /**
     * @return the field on the outer class or {@code null} if this is not an inner class
     */
    public FieldNode getOuterField(String name) {
        if (redirect != null) {
            return redirect.getOuterField(name);
        }
        return null;
    }

    //

    private List<InnerClassNode> innerClasses;

    void addInnerClass(InnerClassNode innerClass) {
        if (redirect != null) {
            redirect.addInnerClass(innerClass);
        } else {
            if (innerClasses == null)
                innerClasses = new ArrayList<>(4);
            innerClasses.add(innerClass);
        }
    }

    /**
     * @return iterator of inner classes defined inside this one
     */
    public Iterator<InnerClassNode> getInnerClasses() {
        if (innerClasses == null) return Collections.emptyIterator();
        return Collections.unmodifiableList(innerClasses).iterator();
    }

    //

    private MethodNode enclosingMethod;

    /**
     * The enclosing method of local inner class.
     */
    public MethodNode getEnclosingMethod() {
        return redirect().enclosingMethod;
    }

    public void setEnclosingMethod(MethodNode enclosingMethod) {
        this.enclosingMethod = enclosingMethod;
    }

    //

    private GenericsType[] genericsTypes;

    public GenericsType asGenericsType() {
        if (!isGenericsPlaceHolder()) {
            return new GenericsType(this);
        } else if (genericsTypes != null
                && genericsTypes[0].getUpperBounds() != null) {
            return genericsTypes[0];
        } else {
            ClassNode upper = (redirect != null ? redirect : this);
            return new GenericsType(this, new ClassNode[]{upper}, null);
        }
    }

    public GenericsType[] getGenericsTypes() {
        return genericsTypes;
    }

    public void setGenericsTypes(GenericsType[] genericsTypes) {
        usesGenerics = usesGenerics || genericsTypes != null;
        this.genericsTypes = genericsTypes;
    }

    //

    private boolean placeholder;

    public boolean isGenericsPlaceHolder() {
        return placeholder;
    }

    public void setGenericsPlaceHolder(boolean placeholder) {
        usesGenerics = usesGenerics || placeholder;
        this.placeholder = placeholder;
    }

    //

    private boolean usesGenerics;

    public boolean isUsingGenerics() {
        return usesGenerics;
    }

    public void setUsingGenerics(boolean usesGenerics) {
        this.usesGenerics = usesGenerics;
    }

    //

    private boolean script;

    public boolean isScript() {
        return redirect().script || isDerivedFrom(ClassHelper.SCRIPT_TYPE);
    }

    public void setScript(boolean script) {
        this.script = script;
    }

    //

    private boolean scriptBody;

    /**
     * @return {@code true} if this inner class or closure was declared inside a script body
     */
    public boolean isScriptBody() {
        return redirect().scriptBody;
    }

    public void setScriptBody(boolean scriptBody) {
        this.scriptBody = scriptBody;
    }

    //

    private boolean staticClass;

    /**
     * Is this class declared in a static method (such as a closure / inner class declared in a static method)
     */
    public boolean isStaticClass() {
        return redirect().staticClass;
    }

    public void setStaticClass(boolean staticClass) {
        this.staticClass = staticClass;
    }

    //

    private boolean syntheticPublic;

    /**
     * Indicates that this class has been "promoted" to public by Groovy when in
     * fact there was no public modifier explicitly in the source code. That is,
     * it remembers that it has applied Groovy's "public classes by default" rule.
     * This property is typically only of interest to AST transform writers.
     *
     * @return {@code true} if node is public but had no explicit public modifier
     */
    public boolean isSyntheticPublic() {
        return syntheticPublic;
    }

    public void setSyntheticPublic(boolean syntheticPublic) {
        this.syntheticPublic = syntheticPublic;
    }

    //

    private boolean annotated;

    public boolean isAnnotated() {
        return this.annotated;
    }

    public void setAnnotated(boolean annotated) {
        this.annotated = annotated;
    }

    //

    @Override
    public List<AnnotationNode> getAnnotations() {
        if (redirect != null)
            return redirect.getAnnotations();
        lazyClassInit();
        return super.getAnnotations();
    }

    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        if (redirect != null)
            return redirect.getAnnotations(type);
        lazyClassInit();
        return super.getAnnotations(type);
    }

    //

    private List<AnnotationNode> typeAnnotations = Collections.emptyList();

    public List<AnnotationNode> getTypeAnnotations() {
        return new ArrayList<>(typeAnnotations);
    }

    public List<AnnotationNode> getTypeAnnotations(ClassNode type) {
        List<AnnotationNode> annotations = new ArrayList<>();
        for (AnnotationNode node : typeAnnotations) {
            if (type.equals(node.getClassNode())) {
                annotations.add(node);
            }
        }
        return annotations;
    }

    public void addTypeAnnotation(final AnnotationNode annotation) {
        if (!isRedirectNode() && (isResolved() || isPrimaryClassNode())) {
            throw new GroovyBugError("Adding type annotation @" + annotation.getClassNode().getNameWithoutPackage() + " to non-redirect node: " + getName());
        }
        if (typeAnnotations == Collections.EMPTY_LIST) {
            typeAnnotations = new ArrayList<>(3);
        }
        typeAnnotations.add(requireNonNull(annotation));
        setAnnotated(true);
    }

    public void addTypeAnnotations(final List<AnnotationNode> annotations) {
        for (AnnotationNode annotation : annotations) {
            addTypeAnnotation(annotation);
        }
    }

    //

    public void addTransform(Class<? extends ASTTransformation> transform, ASTNode node) {
        GroovyASTTransformation annotation = transform.getAnnotation(GroovyASTTransformation.class);
        if (annotation != null) {
            Map<Class<? extends ASTTransformation>, Set<ASTNode>> transforms = getTransforms(annotation.phase());
            Set<ASTNode> nodes = transforms.computeIfAbsent(transform, k -> new LinkedHashSet<>());
            nodes.add(node);
        }
    }

    public Map<Class<? extends ASTTransformation>, Set<ASTNode>> getTransforms(CompilePhase phase) {
        return getTransformInstances().get(phase);
    }

    /**
     * The AST Transformations to be applied during compilation.
     */
    private Map<CompilePhase, Map<Class<? extends ASTTransformation>, Set<ASTNode>>> transformInstances;

    private Map<CompilePhase, Map<Class<? extends ASTTransformation>, Set<ASTNode>>> getTransformInstances() {
        if (transformInstances == null) {
            transformInstances = new EnumMap<>(CompilePhase.class);
            for (CompilePhase phase : CompilePhase.values()) {
                transformInstances.put(phase, new LinkedHashMap<>());
            }
        }
        return transformInstances;
    }
}
