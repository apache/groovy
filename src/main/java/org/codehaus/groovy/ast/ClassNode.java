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
import static org.codehaus.groovy.runtime.ArrayGroovyMethods.asBoolean;
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
            if (map != null) {
                List<MethodNode> result = map.get(key);
                if (result != null) return result;
            }
            return Collections.emptyList();
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
     * Constructs a primary {@code ClassNode} representing a class declaration with all components.
     * This constructor creates a new ClassNode that will be compiled by Groovy. The node represents
     * an actual source class definition with specified interfaces and mixins.
     *
     * @param name       the fully-qualified name of the class (e.g., "com.example.MyClass")
     * @param modifiers  the bytecode modifiers (flags from {@link java.lang.reflect.Modifier} or {@link org.objectweb.asm.Opcodes})
     * @param superClass the base class ({@link ClassNode} representing the parent type); use {@link ClassHelper#OBJECT_TYPE} if no direct base class
     * @param interfaces array of {@link ClassNode} instances for interfaces implemented by this class; may be empty
     * @param mixins     array of {@link MixinNode} instances to be mixed into this class; may be empty
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
     * Constructs a primary {@code ClassNode} representing a class declaration with superclass.
     * This is a convenience constructor for creating classes with a single superclass and no interfaces or mixins.
     * It delegates to the full constructor with empty interface and mixin arrays.
     *
     * @param name       the fully-qualified name of the class (e.g., "com.example.MyClass")
     * @param modifiers  the bytecode modifiers (flags from {@link java.lang.reflect.Modifier} or {@link org.objectweb.asm.Opcodes})
     * @param superClass the base class ({@link ClassNode} representing the parent type); use {@link ClassHelper#OBJECT_TYPE} if no direct base class
     */
    public ClassNode(final String name, final int modifiers, final ClassNode superClass) {
        this(name, modifiers, superClass, ClassNode.EMPTY_ARRAY, MixinNode.EMPTY_ARRAY);
    }

    /**
     * Constructs a non-primary {@code ClassNode} from a real Java class.
     * This constructor creates a ClassNode that represents an actual Java class loaded at runtime (not a Groovy source class).
     * The class information is lazily initialized from reflection, and the node will not be compiled by Groovy.
     *
     * @param c the Java class ({@link Class}) to wrap; must not be {@code null}
     */
    public ClassNode(final Class<?> c) {
        this(c.getName(), c.getModifiers(), null, null, MixinNode.EMPTY_ARRAY);
        this.clazz = c;
        this.lazyInitDone = false;
        this.isPrimaryNode = false;
    }

    /**
     * Constructs a non-primary array {@code ClassNode} from a real Java array class.
     * This internal constructor is used by {@code makeArray()} when a real array class is available.
     * It wraps an array class and associates it with its component type.
     *
     * @param c the Java array class; must not be {@code null}
     * @param componentType the {@link ClassNode} representing the element type of the array
     */
    private ClassNode(final Class<?> c, final ClassNode componentType) {
        this(c);
        this.componentType = componentType;
    }

    /**
     * Constructs a non-primary array {@code ClassNode} when no real array class is available.
     * This internal constructor is used by {@code makeArray()} to create a synthetic array ClassNode.
     * The resulting node represents an array type with appropriate interfaces and modifiers.
     *
     * @param componentType the {@link ClassNode} representing the element type of the array
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
     * If this ClassNode has been set as a proxy to another ClassNode via {@link #setRedirect(ClassNode)},
     * recursively follows the redirect chain to return the ultimate target ClassNode. If no redirect is set,
     * returns {@code this}. This mechanism allows lazy resolution of class references during compilation.
     *
     * @return the final {@link ClassNode} in the redirect chain; never {@code null}
     */
    public ClassNode redirect() {
        return (redirect == null ? this : redirect.redirect());
    }

    /**
     * Returns whether this {@link ClassNode} is a redirect (proxy) node for another ClassNode.
     * A redirect node acts as a placeholder that forwards most method calls to its target ClassNode.
     * This mechanism is used during compilation to defer class resolution until the actual target
     * class is available. The actual target can be found using {@link #redirect()}.
     *
     * @return {@code true} if this node redirects to another ClassNode; {@code false} if it is a standalone node
     * @see #redirect()
     * @see #setRedirect(ClassNode)
     * @see #isPrimaryClassNode()
     */
    public boolean isRedirectNode() {
        return (redirect != null);
    }

    /**
     * Sets this instance as a proxy for the given {@code ClassNode}, enabling deferred class resolution.
     * After calling this method, most operations on this ClassNode are forwarded to the target ClassNode.
     * Redirect is only allowed for non-primary ClassNodes (see {@link #isPrimaryClassNode()}).
     * If the target is {@code null}, the redirect is removed, making this node standalone again.
     *
     * @param node the {@link ClassNode} to redirect to; if {@code null} the redirect is cleared
     * @throws GroovyBugError if this is a primary ClassNode, as redirects are only for proxy nodes
     */
    public void setRedirect(ClassNode node) {
        if (isPrimaryNode) throw new GroovyBugError("tried to set a redirect for a primary ClassNode (" + getName() + "->" + node.getName() + ").");
        if (node != null && !isGenericsPlaceHolder()) node = node.redirect();
        if (node == this) return;
        redirect = node;
    }

    /**
     * Determines whether this ClassNode is a primary node or redirects to one.
     * A primary ClassNode represents actual Groovy source code that will be compiled into bytecode.
     * Returns {@code true} if this node is primary or if its redirect target (or array component type)
     * is a primary ClassNode. Non-primary nodes are typically loaded via reflection.
     *
     * @return {@code true} if this is a primary ClassNode or redirects to one; {@code false} if it only represents a reflected class
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

    /**
     * Returns the unresolved super class of this {@link ClassNode}, using redirect resolution by default.
     * The unresolved super class represents the raw class hierarchy without applying redirect
     * resolution. Returns {@code null} if this class has no explicit superclass.
     *
     * @return the unresolved {@link ClassNode} representing the super class, or {@code null}
     * @see #getUnresolvedSuperClass(boolean)
     */
    public ClassNode getUnresolvedSuperClass() {
        return getUnresolvedSuperClass(true);
    }

    /**
     * Returns the unresolved super class of this {@link ClassNode} with optional redirect resolution.
     * When {@code deref} is {@code true}, resolves redirects recursively; otherwise returns the
     * raw superclass stored in this ClassNode. Resolving may trigger lazy class initialization.
     *
     * @param deref {@code true} to apply redirect resolution, {@code false} to return raw superclass
     * @return the unresolved {@link ClassNode} representing the super class, or {@code null}
     * @see #getUnresolvedSuperClass()
     */
    public ClassNode getUnresolvedSuperClass(final boolean deref) {
        if (deref) {
            if (redirect != null)
                return redirect.getUnresolvedSuperClass(true);
            lazyClassInit();
        }
        return superClass;
    }

    /**
     * Sets the unresolved super class for this {@link ClassNode}.
     * This method sets the raw superclass without applying any redirect resolution.
     *
     * @param superClass the {@link ClassNode} representing the super class, or {@code null}
     */
    public void setUnresolvedSuperClass(final ClassNode superClass) {
        this.superClass = superClass;
    }

    /**
     * Returns the unresolved interfaces implemented by this {@link ClassNode}, using redirect resolution by default.
     * The unresolved interfaces represent the raw interface hierarchy without applying redirect resolution.
     * Returns an empty array if this class implements no interfaces.
     *
     * @return an array of {@link ClassNode} representing the interfaces, never {@code null}
     * @see #getUnresolvedInterfaces(boolean)
     */
    public ClassNode[] getUnresolvedInterfaces() {
        return getUnresolvedInterfaces(true);
    }

    /**
     * Returns the unresolved interfaces implemented by this {@link ClassNode} with optional redirect resolution.
     * When {@code deref} is {@code true}, resolves redirects recursively; otherwise returns the
     * raw interfaces stored in this ClassNode. Resolving may trigger lazy class initialization.
     *
     * @param deref {@code true} to apply redirect resolution, {@code false} to return raw interfaces
     * @return an array of {@link ClassNode} representing the interfaces, never {@code null}
     * @see #getUnresolvedInterfaces()
     */
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

    /**
     * Returns the fully-qualified name of this class after following any redirects.
     * If this ClassNode is a proxy for another ClassNode, the name of the target is returned.
     * The name remains consistent across all compilation phases for the redirected type.
     *
     * @return the fully-qualified class name (e.g., "com.example.MyClass" or "int[]")
     */
    public String getName() {
        return redirect().name;
    }

    /**
     * Sets the fully-qualified name of this class, delegating to the redirect if one exists.
     * Allows the class name to be updated after construction, which is occasionally used during
     * AST transformation phases. Changes are propagated to the redirect target if present.
     *
     * @param name the new fully-qualified class name to set
     * @return the name that was set
     */
    public String setName(final String name) {
        return redirect != null ? redirect.setName(name) : (this.name = name);
    }

    /**
     * Returns the modifier flags for this {@link ClassNode}, combining visibility and behavioral modifiers.
     * The flags are represented as a bitmask using ASM/bytecode conventions (e.g., {@code ACC_PUBLIC},
     * {@code ACC_ABSTRACT}, {@code ACC_INTERFACE}, {@code ACC_ENUM}). This method follows redirects
     * if present, ensuring the modifiers of the actual target ClassNode are returned.
     * Individual modifiers can be tested using bitwise AND operations with the ASM Opcodes constants.
     *
     * @return the modifier flags as an integer bitmask
     * @see #setModifiers(int)
     * @see #isAbstract()
     * @see #isInterface()
     * @see #isEnum()
     */
    public int getModifiers() {
        return redirect().modifiers;
    }

    /**
     * Sets the modifier flags for this {@link ClassNode}.
     * The modifiers are a bitmask of ASM/bytecode convention flags (e.g., {@code ACC_PUBLIC},
     * {@code ACC_ABSTRACT}, {@code ACC_INTERFACE}, {@code ACC_ENUM}). Calling this method
     * directly on a redirect node updates the redirect's modifiers instead of this node's.
     * Use this method to control visibility and structural attributes like abstractness or finality.
     *
     * @param modifiers the new modifier flags as a bitmask
     * @see #getModifiers()
     * @see #isAbstract()
     * @see #isInterface()
     */
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

    /**
     * Sets the super class of this {@link ClassNode}. If this ClassNode has a redirect,
     * the super class is set on the redirect instead. Updates the generics usage indicator
     * if the super class uses generics and this is a primary ClassNode.
     *
     * @param superClass the {@link ClassNode} representing the super class, or {@code null}
     * @see #getSuperClass()
     * @see #isUsingGenerics()
     */
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

    /**
     * Sets the interfaces implemented by this {@link ClassNode}. If this ClassNode has a redirect,
     * the interfaces are set on the redirect instead. Updates the generics usage indicator
     * if any interface uses generics and this is a primary ClassNode.
     *
     * @param interfaces an array of {@link ClassNode} representing the interfaces, or {@code null}
     * @see #getInterfaces()
     * @see #addInterface(ClassNode)
     * @see #isUsingGenerics()
     */
    public void setInterfaces(final ClassNode[] interfaces) {
        if (redirect != null) {
            redirect.setInterfaces(interfaces);
        } else {
            this.interfaces = interfaces;
            // GROOVY-10763: update generics indicator
            if (interfaces != null && !usesGenerics && isPrimaryNode) {
                for (ClassNode anInterface : interfaces) {
                    usesGenerics |= anInterface.isUsingGenerics();
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

    /**
     * Adds an interface to the list of interfaces implemented by this {@link ClassNode}.
     * If the interface is already implemented, this method has no effect. The interface
     * is appended to the end of the current interfaces array.
     *
     * @param node the {@link ClassNode} representing the interface to add
     * @see #getInterfaces()
     * @see #setInterfaces(ClassNode[])
     */
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

    /**
     * Adds a {@link FieldNode} to this ClassNode. The field is added to the end of the field list
     * and its declaring class is set to this node's redirect target. The field is registered in
     * the internal field index for fast lookup by name.
     *
     * @param node the {@link FieldNode} to add, must not be {@code null}
     * @see #addFieldFirst(FieldNode)
     * @see #removeField(String)
     */
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

    /**
     * Creates and adds a {@link FieldNode} to this ClassNode with the specified properties.
     * The field is initialized with the given name, modifiers, type, and optional initial value expression.
     * This is a convenience method that creates a FieldNode and adds it via {@link #addField(FieldNode)}.
     *
     * @param name the name of the field
     * @param modifiers the access modifiers for the field (e.g., ACC_PUBLIC, ACC_PRIVATE)
     * @param type the {@link ClassNode} representing the field's type
     * @param initialValue the initial value {@link Expression}, or {@code null} if no initializer
     * @return the newly created {@link FieldNode}
     * @see #addFieldFirst(String, int, ClassNode, Expression)
     */
    public FieldNode addField(String name, int modifiers, ClassNode type, Expression initialValue) {
        FieldNode node = new FieldNode(name, modifiers, type, redirect(), initialValue);
        addField(node);
        return node;
    }

    /**
     * Adds a {@link FieldNode} to this ClassNode at the beginning of the field list.
     * This method is similar to {@link #addField(FieldNode)} but prepends the field instead of appending it,
     * which can be useful when field declaration order matters.
     *
     * @param node the {@link FieldNode} to add first, must not be {@code null}
     * @see #addField(FieldNode)
     */
    public void addFieldFirst(FieldNode node) {
        addField(node, false);
    }

    /**
     * Creates and adds a {@link FieldNode} to the beginning of this ClassNode's field list
     * with the specified properties. This is a convenience method for adding fields where
     * declaration order is significant.
     *
     * @param name the name of the field
     * @param modifiers the access modifiers for the field (e.g., ACC_PUBLIC, ACC_PRIVATE)
     * @param type the {@link ClassNode} representing the field's type
     * @param initialValue the initial value {@link Expression}, or {@code null} if no initializer
     * @return the newly created {@link FieldNode}
     * @see #addField(String, int, ClassNode, Expression)
     */
    public FieldNode addFieldFirst(String name, int modifiers, ClassNode type, Expression initialValue) {
        FieldNode node = new FieldNode(name, modifiers, type, redirect(), initialValue);
        addFieldFirst(node);
        return node;
    }

    /**
     * Adds a {@link PropertyNode} to this ClassNode. The property's associated field is also added
     * to the field list, and the property itself is registered in the properties list. The property's
     * declaring class is set to this node's redirect target.
     *
     * @param node the {@link PropertyNode} to add, must not be {@code null}
     * @see #addField(FieldNode)
     * @see PropertyNode
     */
    public void addProperty(PropertyNode node) {
        node.setDeclaringClass(redirect());
        addField(node.getField());
        getProperties().add(node);
    }

    /**
     * Creates and adds a {@link PropertyNode} to this ClassNode with the specified properties,
     * or returns an existing property with the same name if one is already present. If a property
     * with the given name exists, the method updates its getter block, setter block, and initial
     * value expression if they are currently {@code null}. This prevents duplicate properties while
     * allowing partial initialization across multiple calls.
     *
     * @param name the name of the property
     * @param modifiers the access modifiers for the property (e.g., ACC_PUBLIC, ACC_PRIVATE)
     * @param type the {@link ClassNode} representing the property's type
     * @param initialValue the initial value {@link Expression}, or {@code null} if no initializer
     * @param getterBlock the getter method's {@link Statement} body, or {@code null}
     * @param setterBlock the setter method's {@link Statement} body, or {@code null}
     * @return the newly created {@link PropertyNode} or the existing property if one with this name is already present
     * @see #addProperty(PropertyNode)
     * @see PropertyNode
     */
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

    /**
     * Adds a {@link MethodNode} to this ClassNode. The method is added to the internal methods list
     * and registered by name in the methods map for fast lookup. The method's declaring class is set
     * to this node's redirect target. This method does not check for duplicates.
     *
     * @param node the {@link MethodNode} to add, must not be {@code null}
     * @see #removeMethod(MethodNode)
     * @see #getMethod(String, Parameter[])
     * @see MethodNode
     */
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
     * Creates and adds a {@link MethodNode} to this ClassNode with the specified properties,
     * but only if a method with the same name and parameter signature does not already exist.
     * If a matching method is already declared in this class, it is returned without adding a new one.
     * This method is useful for adding default method implementations (e.g., {@code getProperty()},
     * {@code invokeMethod()}) where the class may already have a user-defined version.
     *
     * @param name the name of the method
     * @param modifiers the access modifiers (e.g., ACC_PUBLIC, ACC_STATIC)
     * @param returnType the {@link ClassNode} representing the return type
     * @param parameters array of {@link Parameter}s, or an empty array for no parameters
     * @param exceptions array of exception {@link ClassNode}s that the method declares, or an empty array
     * @param code the method body as a {@link Statement}, or {@code null}
     * @return the newly created {@link MethodNode} or an existing method if one matches the name and parameters
     * @see #addMethod(MethodNode)
     * @see #getDeclaredMethod(String, Parameter[])
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
        if (statement instanceof BlockStatement block) {
            // add given statements for explicitly declared static fields just after enum-special fields
            // are found - the $VALUES binary expression marks the end of such fields.
            List<Statement> blockStatements = block.getStatements();
            for (ListIterator<Statement> it = blockStatements.listIterator(); it.hasNext(); ) {
                Statement stmt = it.next();
                if (stmt instanceof ExpressionStatement &&
                        ((ExpressionStatement) stmt).getExpression() instanceof BinaryExpression bExp) {
                    if (bExp.getLeftExpression() instanceof FieldExpression fExp) {
                        if ("$VALUES".equals(fExp.getFieldName())) {
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
     * Returns all {@link FieldNode}s declared in this ClassNode, including inherited fields.
     * If this node is a proxy (has a redirect), the call is forwarded to the redirect target.
     * The returned list includes fields from this class only, not from super classes.
     *
     * @return an unmodifiable or mutable list of {@link FieldNode}s associated with this ClassNode;
     *         an empty list if this class declares no fields
     * @see #getDeclaredField(String)
     * @see FieldNode
     */
    public List<FieldNode> getFields() {
        if (redirect != null)
            return redirect.getFields();
        lazyClassInit();
        if (fields == null)
            fields = new ArrayList<>();
        return fields;
    }

    /**
     * Returns all {@link PropertyNode}s declared in this ClassNode. If this node is a proxy
     * (has a redirect), the call is forwarded to the redirect target. Properties are distinct
     * from fields and represent a higher-level abstraction with getter/setter semantics.
     *
     * @return a mutable list of {@link PropertyNode}s associated with this ClassNode;
     *         an empty list if this class declares no properties
     * @see #getProperty(String)
     * @see PropertyNode
     */
    public List<PropertyNode> getProperties() {
        if (redirect != null)
            return redirect.getProperties();
        if (properties == null)
            properties = new ArrayList<>();
        return properties;
    }

    /**
     * Returns the internal field index map for fast field lookup by name. This method is deprecated
     * as field access should go through {@link #getField(String)} or {@link #getDeclaredField(String)}.
     * The field index is an internal implementation detail and may change in future versions.
     *
     * @return a map from field name to {@link FieldNode}, or {@code null} if no field index exists
     * @deprecated for removal since 5.0.0; use {@link #getField(String)} or {@link #getDeclaredField(String)} instead
     * @see #getField(String)
     * @see #getDeclaredField(String)
     */
    @Deprecated(forRemoval = true, since = "5.0.0")
    public Map<String, FieldNode> getFieldIndex() {
        return fieldIndex;
    }

    /**
     * Tests whether a property with the given name exists in this ClassNode.
     * This method searches through the properties list, comparing by name.
     *
     * @param name the name of the property to check
     * @return {@code true} if a property with the given name exists, {@code false} otherwise
     * @see #getProperty(String)
     * @see #getProperties()
     */
    public boolean hasProperty(String name) {
        return getProperties().stream().map(PropertyNode::getName).anyMatch(name::equals);
    }

    /**
     * Finds a {@link PropertyNode} matching the given name in this ClassNode.
     * This method searches through the properties list, comparing by name. Only direct
     * properties of this class are searched, not inherited ones.
     *
     * @param name the name of the property to find
     * @return the {@link PropertyNode} with the given name, or {@code null} if not found
     * @see #hasProperty(String)
     * @see #getProperties()
     */
    public PropertyNode getProperty(String name) {
        return getProperties().stream().filter(pn -> pn.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Returns all {@link MethodNode}s declared in this ClassNode, including inherited and synthetic methods.
     * If this node is a proxy (has a redirect), the call is forwarded to the redirect target.
     * This list includes constructors and all instance and static methods defined for this class.
     *
     * @return a list of all {@link MethodNode}s associated with this ClassNode;
     *         an empty list if this class declares no methods
     * @see #getDeclaredMethods(String)
     * @see #getMethod(String, Parameter[])
     * @see MethodNode
     */
    public List<MethodNode> getMethods() {
        if (redirect != null)
            return redirect.getMethods();
        lazyClassInit();
        return methodsList;
    }

    /**
     * Returns a list of all abstract {@link MethodNode}s declared in this ClassNode.
     * Abstract methods are obtained from the declared methods map and filtered to return only
     * those with the abstract modifier set. This includes abstract methods from superclasses
     * and implemented interfaces.
     *
     * @return a list of abstract {@link MethodNode}s (possibly empty);
     *         returns an empty list if there are no abstract methods
     * @see #getDeclaredMethodsMap()
     * @see MethodNode#isAbstract()
     */
    public List<MethodNode> getAbstractMethods() {
        return getDeclaredMethodsMap().values().stream()
            .filter(MethodNode::isAbstract).collect(toList());
    }

    /**
     * Returns all declared {@link MethodNode}s from this ClassNode and its superclasses and interfaces.
     * The methods are collected in a single map with method type descriptors as keys, allowing
     * resolution of method overrides across the inheritance hierarchy. This method provides
     * a complete view of all accessible methods.
     *
     * @return a map from method type descriptor (name and signature) to {@link MethodNode};
     *         includes methods from superclasses and interfaces
     * @see #getDeclaredMethods(String)
     * @see #getMethod(String, Parameter[])
     * @see MethodNode#getTypeDescriptor()
     */
    public Map<String, MethodNode> getDeclaredMethodsMap() {
        Map<String, MethodNode> result = ClassNodeUtils.getDeclaredMethodsFromSuper(this);
        ClassNodeUtils.addDeclaredMethodsFromInterfaces(this, result);
        // add in the methods implemented in this class
        for (MethodNode method : getMethods()) {
            result.put(method.getTypeDescriptor(), method);
        }
        return result;
    }

    /**
     * Returns all declared {@link MethodNode}s from this ClassNode and its superclasses and interfaces
     * as a list. This is a convenience method that collects all values from the declared methods map
     * into a single list.
     *
     * @return a list of all {@link MethodNode}s declared in this class, superclasses, and interfaces;
     *         an empty list if no methods are found
     * @see #getDeclaredMethodsMap()
     */
    public List<MethodNode> getAllDeclaredMethods() {
        return new ArrayList<>(getDeclaredMethodsMap().values());
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
     * Tests whether a method with the given name and parameter signature exists in this class
     * or any of its superclasses. This is a convenience method for checking method existence.
     *
     * @param name the name of the method to check
     * @param parameters an array of {@link Parameter}s representing the method signature,
     *                   or {@code null}/{@code empty} for methods with no parameters
     * @return {@code true} if a method with the given name and parameters exists, {@code false} otherwise
     * @see #getMethod(String, Parameter[])
     * @see #hasDeclaredMethod(String, Parameter[])
     */
    public boolean hasMethod(String name, Parameter[] parameters) {
        return (getMethod(name, parameters) != null);
    }

    /**
     * Tests whether a method with the given name and parameter signature is declared directly
     * in this class (not inherited from a superclass). This is a convenience method for checking
     * method existence.
     *
     * @param name the name of the method to check
     * @param parameters an array of {@link Parameter}s representing the method signature,
     *                   or {@code null}/{@code empty} for methods with no parameters
     * @return {@code true} if a method with the given name and parameters is declared in this class,
     *         {@code false} otherwise
     * @see #getDeclaredMethod(String, Parameter[])
     * @see #hasMethod(String, Parameter[])
     */
    public boolean hasDeclaredMethod(String name, Parameter[] parameters) {
        return (getDeclaredMethod(name, parameters) != null);
    }

    /**
     * Finds a {@link FieldNode} matching the given name declared directly in this class.
     * This method does not search superclasses; to include inherited fields, use {@link #getField(String)}.
     * If this node is a proxy (has a redirect), the call is forwarded to the redirect target.
     *
     * @param name the name of the field to find
     * @return the {@link FieldNode} with the given name declared in this class, or {@code null} if not found
     * @see #getField(String)
     * @see #getFields()
     */
    public FieldNode getDeclaredField(String name) {
        if (redirect != null)
            return redirect.getDeclaredField(name);
        lazyClassInit();
        return fieldIndex == null ? null : fieldIndex.get(name);
    }

    /**
     * Finds a {@link FieldNode} matching the given name in this class or any of its superclasses.
     * This method searches the inheritance hierarchy starting from this ClassNode and moving up
     * through superclasses until a field with the matching name is found.
     *
     * @param name the name of the field to find
     * @return the {@link FieldNode} with the given name, or {@code null} if not found in this class
     *         or any superclass
     * @see #getDeclaredField(String)
     * @see #getFields()
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
     * Returns a list of all {@link MethodNode}s with the given name declared directly in this class.
     * This method does not search superclasses; to include inherited methods, use {@link #getMethods(String)}.
     * If this node is a proxy (has a redirect), the call is forwarded to the redirect target.
     * The list may be empty if no methods with the given name are declared in this class.
     *
     * @param name the name of the methods to find
     * @return a list of {@link MethodNode}s with the given name (possibly empty)
     * @see #getMethods(String)
     * @see #getDeclaredMethod(String, Parameter[])
     */
    public List<MethodNode> getDeclaredMethods(String name) {
        if (redirect != null)
            return redirect.getDeclaredMethods(name);
        lazyClassInit();
        return methods.get(name);
    }

    /**
     * Returns a list of all {@link MethodNode}s with the given name from this class and its superclasses.
     * This method searches the inheritance hierarchy starting from this ClassNode and moving up
     * through superclasses, collecting all methods with matching names. The list may be empty if
     * no methods with the given name are found in the class hierarchy.
     *
     * @param name the name of the methods to find
     * @return a list of {@link MethodNode}s with the given name from this class and its superclasses (possibly empty)
     * @see #getDeclaredMethods(String)
     * @see #getMethod(String, Parameter[])
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
     * Finds a {@link MethodNode} matching the given name and parameter signature declared directly
     * in this class. This method does not search superclasses; to include inherited methods, use
     * {@link #getMethod(String, Parameter[])}. Parameter matching is done by comparing parameter
     * types using the {@link #parametersEqual(Parameter[], Parameter[])} method.
     *
     * @param name the name of the method to find
     * @param parameters an array of {@link Parameter}s representing the method signature,
     *                   or {@code null}/{@code empty} for methods with no parameters
     * @return the {@link MethodNode} with the given name and parameters, or {@code null} if not found
     * @see #getMethod(String, Parameter[])
     * @see #getDeclaredMethods(String)
     */
    public MethodNode getDeclaredMethod(String name, Parameter[] parameters) {
        boolean zeroParameters = !asBoolean(parameters);
        for (MethodNode method : getDeclaredMethods(name)) {
            Parameter[] methodParameters = method.getParameters();
            if (zeroParameters ? methodParameters.length == 0
                    : parametersEqual(methodParameters, parameters)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Finds a {@link MethodNode} matching the given name and parameter signature in this class
     * or any of its superclasses. This method searches the inheritance hierarchy starting from
     * this ClassNode. Parameter matching is done by comparing parameter types using the
     * {@link #parametersEqual(Parameter[], Parameter[])} method.
     *
     * @param name the name of the method to find
     * @param parameters an array of {@link Parameter}s representing the method signature,
     *                   or {@code null}/{@code empty} for methods with no parameters
     * @return the {@link MethodNode} with the given name and parameters, or {@code null} if not found
     *         in this class or any superclass
     * @see #getDeclaredMethod(String, Parameter[])
     * @see #getMethods(String)
     */
    public MethodNode getMethod(String name, Parameter[] parameters) {
        boolean zeroParameters = !asBoolean(parameters);
        for (MethodNode method : getMethods(name)) {
            Parameter[] methodParameters = method.getParameters();
            if (zeroParameters ? methodParameters.length == 0
                    : parametersEqual(methodParameters, parameters)) {
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
     * Returns whether this {@link ClassNode} represents a type that implements the {@code GroovyObject} interface.
     * GroovyObject is the core interface that all Groovy objects implement, providing methods like
     * {@code getProperty()}, {@code setProperty()}, and {@code invokeMethod()} for dynamic behavior.
     * This method checks the class hierarchy to determine if GroovyObject is in the list of implemented interfaces.
     *
     * @return {@code true} if this type directly or indirectly implements {@code GroovyObject}; {@code false} otherwise
     * @see #implementsInterface(ClassNode)
     * @see #isDerivedFrom(ClassNode)
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

    /**
     * Finds a getter {@link MethodNode} matching the given name, optionally searching superclasses.
     * A getter method is defined as a method with no parameters that returns a non-void type.
     * For methods starting with "is", the return type must be boolean. Synthetic and bridge methods
     * are disregarded in favor of the most specific real getter.
     *
     * @param getterName the name of the getter method to find
     * @return the getter {@link MethodNode}, or {@code null} if not found
     * @see #getGetterMethod(String, boolean)
     * @see #getSetterMethod(String)
     */
    public MethodNode getGetterMethod(String getterName) {
        return getGetterMethod(getterName, true);
    }

    /**
     * Finds a getter {@link MethodNode} matching the given name, optionally searching superclasses
     * and interfaces. A getter method is defined as a method with no parameters that returns a non-void type.
     * For methods starting with "is", the return type must be boolean. Synthetic and bridge methods
     * are disregarded in favor of the most specific real getter.
     *
     * @param getterName the name of the getter method to find
     * @param searchSupers if {@code true}, searches superclasses and interfaces for the getter;
     *                     if {@code false}, searches only methods declared in this class
     * @return the getter {@link MethodNode}, or {@code null} if not found
     * @see #getSetterMethod(String, boolean)
     * @see MethodNode
     */
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
            if (getterMethod == null && asBoolean(getInterfaces())) {
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

    /**
     * Finds a setter {@link MethodNode} matching the given name, optionally searching superclasses.
     * A setter method is defined as a method with exactly one parameter that returns void.
     * This method searches from this class up the inheritance hierarchy until a matching setter is found.
     *
     * @param setterName the name of the setter method to find
     * @return the setter {@link MethodNode}, or {@code null} if not found
     * @see #getSetterMethod(String, boolean)
     * @see #getGetterMethod(String)
     */
    public MethodNode getSetterMethod(String setterName) {
        return getSetterMethod(setterName, true);
    }

    /**
     * Finds a setter {@link MethodNode} matching the given name. A setter method is defined as a method
     * with exactly one parameter. If {@code voidOnly} is {@code true}, the method must also return void.
     * The method searches this class and superclasses for the first matching setter.
     *
     * @param setterName the name of the setter method to find
     * @param voidOnly if {@code true}, only returns setter methods that return void;
     *                 if {@code false}, accepts setters that return any type
     * @return the setter {@link MethodNode}, or {@code null} if not found
     * @see #getGetterMethod(String, boolean)
     * @see MethodNode
     */
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

faces:  if (method == null && asBoolean(getInterfaces())) { // GROOVY-11323
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

    /**
     * Removes the {@link FieldNode} with the given name from this ClassNode.
     * This method removes the field from the internal field list and index.
     * If this node is a proxy (has a redirect), the operation is performed on the redirect target.
     *
     * @param oldName the name of the field to remove
     * @see #addField(FieldNode)
     * @see #getField(String)
     * @see FieldNode
     */
    public void removeField(String oldName) {
        ClassNode r = redirect();
        var index = r.fieldIndex;
        if (index != null)
            r.fields.remove(index.remove(oldName));
    }

    /**
     * Removes the given {@link MethodNode} from this ClassNode. The method is removed from the
     * internal methods list and the methods map. If this node is a proxy (has a redirect),
     * the operation is performed on the redirect target.
     *
     * @param node the {@link MethodNode} to remove
     * @see #addMethod(MethodNode)
     * @see #getMethod(String, Parameter[])
     * @see MethodNode
     */
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

    /**
     * Compares this {@link ClassNode} with another object for equality.
     * Two ClassNodes are considered equal if:
     * <ul>
     * <li>They are the same object (identity check)</li>
     * <li>Both are ClassNode instances and either have identical redirects, identical component types (for arrays),
     * or have matching text representations</li>
     * </ul>
     * If this ClassNode is a redirect node, the comparison is delegated to the redirect target.
     * Array ClassNodes are compared based on their component types.
     *
     * @param that the object to compare with this ClassNode
     * @return {@code true} if the objects represent the same class; {@code false} otherwise
     * @see #hashCode()
     * @see #redirect()
     */
    @Override
    public boolean equals(Object that) {
        if (that == this) return true;
        if (!(that instanceof ClassNode)) return false;
        if (redirect != null) return redirect.equals(that);
        if (componentType != null) return componentType.equals(((ClassNode) that).componentType);
        return ((ClassNode) that).getText().equals(getText()); // arrays could be "T[]" or "[LT;"
    }

    /**
     * Returns the hash code for this {@link ClassNode}.
     * The hash code is based on the redirect's hash code if a redirect exists,
     * or the text representation's hash code if this is a primary or array ClassNode.
     * This ensures that equal ClassNodes (by {@link #equals(Object)}) have equal hash codes,
     * maintaining the hash code contract for use in hash-based collections.
     *
     * @return a hash code value for this ClassNode
     * @see #equals(Object)
     */
    @Override
    public  int hashCode() {
        return (redirect != null ? redirect.hashCode() : getText().hashCode());
    }

    /**
     * Returns a string representation of this ClassNode for debugging and display purposes.
     * For array types, appends "[]" to the component type's string representation.
     * For generic types, includes generic type parameters (e.g., "List<String>").
     * If this is a redirect node, includes arrow notation showing the redirect target.
     *
     * @return a string representation of this ClassNode, including fully-qualified name and type information
     */
    @Override
    public  String toString() {
        return toString(true);
    }

    /**
     * Returns a string representation of this ClassNode with optional redirect information.
     * For array types, appends "[]" to the component type's string representation.
     * For generic types, includes generic type parameters (e.g., "List<String>").
     * If {@code showRedirect} is {@code true} and this is a redirect node, includes arrow notation
     * showing the redirect target; otherwise the redirect information is omitted.
     *
     * @param showRedirect if {@code true}, includes redirect information in the output; if {@code false}, omits it
     * @return a string representation of this ClassNode
     */
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

    /**
     * Returns whether this {@link ClassNode} represents an abstract class.
     * A class is abstract if the {@code ACC_ABSTRACT} modifier flag is set in its modifiers.
     * Abstract classes cannot be instantiated directly; they serve as base types for subclasses.
     * This method checks the actual modifier bits, independent of the source code syntax.
     *
     * @return {@code true} if this class has the abstract modifier set; {@code false} otherwise
     * @see #getModifiers()
     * @see #setModifiers(int)
     */
    public boolean isAbstract() {
        return (getModifiers() & ACC_ABSTRACT) != 0;
    }

    /**
     * Returns whether this {@link ClassNode} represents an interface type.
     * A class is an interface if the {@code ACC_INTERFACE} modifier flag is set in its modifiers.
     * Interfaces define contracts for implementing classes without providing implementation details.
     * Note that annotations are also interfaces (with the {@code ACC_ANNOTATION} flag also set).
     *
     * @return {@code true} if this class is an interface; {@code false} if it is a regular class
     * @see #isAnnotationDefinition()
     * @see #getModifiers()
     */
    public boolean isInterface() {
        return (getModifiers() & ACC_INTERFACE) != 0;
    }

    /**
     * Returns whether this {@link ClassNode} represents an annotation type (annotation definition).
     * Annotation types are always interfaces with both the {@code ACC_INTERFACE} and {@code ACC_ANNOTATION}
     * modifier flags set. They define metadata that can be attached to program elements.
     * All annotation types are interfaces but not all interfaces are annotations.
     *
     * @return {@code true} if this class is an annotation type; {@code false} otherwise
     * @see #isInterface()
     * @see #getModifiers()
     */
    public boolean isAnnotationDefinition() {
        return isInterface() && (getModifiers() & ACC_ANNOTATION) != 0;
    }

    /**
     * Returns whether this {@link ClassNode} represents an enumeration type.
     * An enum class has the {@code ACC_ENUM} modifier flag set in its modifiers.
     * Enumerations define a fixed set of named constants that can be iterated over at compile time.
     *
     * @return {@code true} if this class is an enumeration; {@code false} otherwise
     * @see #getModifiers()
     * @see #setModifiers(int)
     */
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

    /**
     * Determines whether this ClassNode has been resolved to an actual Java class.
     * A ClassNode is considered resolved if it represents a real {@link Class} object loaded at runtime,
     * or if it has been redirected to a resolved ClassNode, or if it represents an array of resolved component types.
     * During early compilation phases, unresolved ClassNodes may reference types that haven't been loaded yet.
     *
     * @return {@code true} if this ClassNode has been resolved to a real class; {@code false} if it still represents an unresolved reference
     */
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

    /**
     * Determines whether this ClassNode represents an array type.
     * A ClassNode is an array if its component type has been set. The component type represents
     * the element type of the array (e.g., "int" for "int[]", or "String" for "String[]").
     *
     * @return {@code true} if this ClassNode represents an array type; {@code false} if it represents a scalar type
     */
    public boolean isArray() {
        return (componentType != null);
    }

    /**
     * Returns a {@code ClassNode} representing an array of the type represented by this.
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

    /**
     * Returns the component type of this array ClassNode.
     * If this ClassNode does not represent an array type, returns {@code null}.
     * For array types, this returns the {@link ClassNode} representing the element type
     * (e.g., for "String[]", returns the ClassNode for "String").
     *
     * @return the {@link ClassNode} representing the array's element type, or {@code null} if not an array
     */
    public ClassNode getComponentType() {
        return componentType;
    }

    /**
     * Returns the outer class of this {@link ClassNode}, or {@code null} if this is not an inner class.
     * If this ClassNode has a redirect, returns the outer class from the redirect.
     * This method traverses the class hierarchy upward to find the immediately enclosing class.
     *
     * @return the outer {@link ClassNode}, or {@code null} if not an inner class
     * @see #getOuterClasses()
     */
    public ClassNode getOuterClass() {
        if (redirect != null) {
            return redirect.getOuterClass();
        }
        return null;
    }

    /**
     * Returns a list of all outer classes enclosing this {@link ClassNode} in hierarchical order.
     * The innermost outer class appears first in the list. Returns an empty list if this is not
     * an inner class. If this ClassNode has a redirect, the list is derived from the redirect.
     *
     * @return a {@link List} of enclosing {@link ClassNode}s in order from innermost to outermost,
     *         never {@code null}
     * @see #getOuterClass()
     */
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

    private List<InnerClassNode> innerClasses;

    /**
     * Adds an inner class to this {@link ClassNode}. If this ClassNode has a redirect,
     * the inner class is added to the redirect instead. Inner classes are lazily initialized
     * in a list and become visible through the iteration provided by {@link #getInnerClasses()}.
     *
     * @param innerClass the {@link InnerClassNode} to add
     * @see #getInnerClasses()
     */
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
     * Returns an iterator over the inner classes defined within this {@link ClassNode}.
     * The iterator is backed by an unmodifiable view of the inner classes list.
     * Returns an empty iterator if this class has no inner classes defined.
     *
     * @return an {@link Iterator} of {@link InnerClassNode}s defined in this class, never {@code null}
     * @see #addInnerClass(InnerClassNode)
     */
    public Iterator<InnerClassNode> getInnerClasses() {
        if (innerClasses == null) return Collections.emptyIterator();
        return Collections.unmodifiableList(innerClasses).iterator();
    }

    //

    private MethodNode enclosingMethod;

    /**
     * Returns the enclosing {@link MethodNode} of this local inner class, or {@code null}
     * if this is not a local inner class. Local inner classes are those declared within method bodies.
     * If this ClassNode has a redirect, returns the enclosing method from the redirect.
     *
     * @return the {@link MethodNode} enclosing this class, or {@code null}
     * @see #setEnclosingMethod(MethodNode)
     * @see #getOuterClass()
     */
    public MethodNode getEnclosingMethod() {
        return redirect().enclosingMethod;
    }

    /**
     * Sets the enclosing {@link MethodNode} for this local inner class.
     * This is typically called when processing classes declared within method bodies.
     *
     * @param enclosingMethod the {@link MethodNode} enclosing this class, or {@code null}
     * @see #getEnclosingMethod()
     */
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

    /**
     * Returns the generic type parameters for this {@link ClassNode}, or {@code null} if none are defined.
     * Generic types represent type parameters and their bounds as declared in class definitions.
     * For example, in {@code class List<T>}, the {@link GenericsType} array would contain a single
     * element representing the type parameter {@code T}.
     *
     * @return an array of {@link GenericsType} representing the type parameters, or {@code null}
     * @see #setGenericsTypes(GenericsType[])
     * @see #isUsingGenerics()
     */
    public GenericsType[] getGenericsTypes() {
        return genericsTypes;
    }

    /**
     * Sets the generic type parameters for this {@link ClassNode}.
     * Setting generics types updates the generics usage indicator for this class.
     * This method is typically called during compiler phases when type information becomes available.
     *
     * @param genericsTypes an array of {@link GenericsType} representing the type parameters, or {@code null}
     * @see #getGenericsTypes()
     * @see #isUsingGenerics()
     */
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

    private boolean script;

    /**
     * Returns whether this {@link ClassNode} represents a Groovy script body.
     * A script is a special class that executes at the top level rather than requiring instantiation.
     * This method checks both the direct flag and whether the class derives from {@code Script}.
     * During compilation, script bodies are compiled into classes that extend the {@code Script} base class.
     *
     * @return {@code true} if this class represents a script; {@code false} otherwise
     * @see #setScript(boolean)
     * @see #isScriptBody()
     * @see #isDerivedFrom(ClassNode)
     */
    public boolean isScript() {
        return redirect().script || isDerivedFrom(ClassHelper.SCRIPT_TYPE);
    }

    /**
     * Marks this {@link ClassNode} as a script or removes the script flag.
     * Setting this to {@code true} indicates that this class represents a Groovy script
     * that should be executed at the top level. This is used internally during compilation
     * to identify script classes that need special treatment (e.g., extending the Script base class).
     *
     * @param script {@code true} to mark as a script; {@code false} to unmark
     * @see #isScript()
     * @see #setScriptBody(boolean)
     */
    public void setScript(boolean script) {
        this.script = script;
    }

    private boolean scriptBody;

    /**
     * Returns whether this inner class or closure was declared inside a script body.
     * This flag distinguishes between inner classes/closures defined within a script's top-level code
     * versus those defined within regular class methods. Script body context affects how local variable
     * access and scoping rules are applied. This method follows redirects if present.
     *
     * @return {@code true} if this inner class or closure is inside a script body; {@code false} otherwise
     * @see #setScriptBody(boolean)
     * @see #isScript()
     * @see #isStaticClass()
     */
    public boolean isScriptBody() {
        return redirect().scriptBody;
    }

    /**
     * Marks this inner class or closure as being declared inside a script body.
     * When set to {@code true}, indicates that the class definition occurs at the top level
     * of a Groovy script (outside of any method or class definition). This affects variable
     * scoping and how the compiler generates code for accessing enclosing scope variables.
     * Typically used by the compiler during script class generation.
     *
     * @param scriptBody {@code true} to mark as defined in script body; {@code false} otherwise
     * @see #isScriptBody()
     * @see #setStaticClass(boolean)
     */
    public void setScriptBody(boolean scriptBody) {
        this.scriptBody = scriptBody;
    }

    private boolean staticClass;

    /**
     * Returns whether this inner class or closure was declared inside a static method context.
     * This flag identifies classes that are nested within static methods (as opposed to instance methods
     * or top-level definitions). Inner classes in static contexts have different scoping rules and cannot
     * access instance variables of the enclosing class. This method follows redirects if present.
     *
     * @return {@code true} if this class is declared in a static method; {@code false} otherwise
     * @see #setStaticClass(boolean)
     * @see #isScriptBody()
     */
    public boolean isStaticClass() {
        return redirect().staticClass;
    }

    /**
     * Marks this inner class or closure as being declared in a static method context.
     * When set to {@code true}, indicates that this class definition occurs within a static method,
     * affecting how the compiler generates access to enclosing class members. Static inner classes
     * have restricted access to the enclosing class (only to static members).
     *
     * @param staticClass {@code true} to mark as defined in a static context; {@code false} otherwise
     * @see #isStaticClass()
     * @see #setScriptBody(boolean)
     */
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

    /**
     * Marks this {@link ClassNode} as having synthetic public visibility.
     * When set to {@code true}, indicates that this class was implicitly made public by Groovy's
     * default visibility rule rather than having an explicit {@code public} modifier in the source code.
     * This distinction is important for AST transformations that need to preserve the original intent
     * regarding visibility and access control.
     *
     * @param syntheticPublic {@code true} if public was added by the compiler; {@code false} if explicitly declared
     * @see #isSyntheticPublic()
     * @see #getModifiers()
     */
    public void setSyntheticPublic(boolean syntheticPublic) {
        this.syntheticPublic = syntheticPublic;
    }

    private boolean annotated;

    /**
     * Returns whether this {@link ClassNode} has been marked as annotated.
     * This flag is set to {@code true} when annotations are added to the class via
     * {@link #addAnnotation(AnnotationNode)} or related methods. It serves as a quick
     * indicator that the class carries metadata annotations, without requiring iteration
     * through the full annotations list. The flag does not distinguish between different
     * types of annotations (type annotations, method annotations, etc.).
     *
     * @return {@code true} if this class has been marked as having annotations; {@code false} otherwise
     * @see #setAnnotated(boolean)
     * @see #getAnnotations()
     */
    public boolean isAnnotated() {
        return this.annotated;
    }

    /**
     * Marks this {@link ClassNode} as having annotations attached to it.
     * This flag is used internally by the compiler to track whether annotations have been added
     * to the class. It should be set to {@code true} when annotations are added and may be used
     * as an optimization to avoid scanning the annotations list for empty cases.
     *
     * @param annotated {@code true} to mark as having annotations; {@code false} to clear the flag
     * @see #isAnnotated()
     * @see #addAnnotation(AnnotationNode)
     * @see #addTypeAnnotation(AnnotationNode)
     */
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
