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
package org.codehaus.groovy.ast.tools;

import groovy.lang.Tuple2;
import groovy.transform.stc.IncorrectTypeHintException;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.memoize.ConcurrentSoftCache;
import org.codehaus.groovy.runtime.memoize.EvictableCache;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.apache.groovy.util.SystemUtil.getSystemPropertySafe;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.plus;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isUnboundedWildcard;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.resolveClassNodeGenerics;

/**
 * Utility methods to deal with parameterized types.
 */
public class GenericsUtils {

    /**
     * @since 2.0.0
     */
    public static final GenericsType[] EMPTY_GENERICS_ARRAY = GenericsType.EMPTY_ARRAY;

    /**
     * @since 3.0.0
     */
    public static final String JAVA_LANG_OBJECT = ClassHelper.OBJECT;

    /**
     * Given a parameterized type and a generic type information, aligns actual type parameters. For example, if a
     * class uses generic type <pre>&lt;T,U,V&gt;</pre> (redirectGenericTypes), is used with actual type parameters
     * <pre>&lt;java.lang.String, U,V&gt;</pre>, then a class or interface using generic types <pre>&lt;T,V&gt;</pre>
     * will be aligned to <pre>&lt;java.lang.String,V&gt;</pre>
     *
     * @param redirectGenericTypes the type arguments or the redirect class node
     * @param parameterizedTypes   the actual type arguments used on this class node
     * @param alignmentTarget      the generic type arguments to which we want to align to
     * @return aligned type arguments
     *
     * @since 2.0.0
     * @deprecated You shouldn't call this method because it is inherently unreliable.
     */
    @Deprecated(forRemoval = true, since = "2.3.0")
    public static GenericsType[] alignGenericTypes(final GenericsType[] redirectGenericTypes, final GenericsType[] parameterizedTypes, final GenericsType[] alignmentTarget) {
        if (alignmentTarget == null) return EMPTY_GENERICS_ARRAY;
        if (parameterizedTypes == null || parameterizedTypes.length == 0) return alignmentTarget;
        GenericsType[] generics = new GenericsType[alignmentTarget.length];
        for (int i = 0, scgtLength = alignmentTarget.length; i < scgtLength; i++) {
            final GenericsType currentTarget = alignmentTarget[i];
            GenericsType match = null;
            if (redirectGenericTypes != null) {
                for (int j = 0; j < redirectGenericTypes.length && match == null; j++) {
                    GenericsType redirectGenericType = redirectGenericTypes[j];
                    if (redirectGenericType.isCompatibleWith(currentTarget.getType())) {
                        if (currentTarget.isPlaceholder() && redirectGenericType.isPlaceholder() && !currentTarget.getName().equals(redirectGenericType.getName())) {
                            // check if there's a potential better match
                            boolean skip = false;
                            for (int k = j + 1; k < redirectGenericTypes.length && !skip; k++) {
                                GenericsType ogt = redirectGenericTypes[k];
                                if (ogt.isPlaceholder() && ogt.isCompatibleWith(currentTarget.getType()) && ogt.getName().equals(currentTarget.getName())) {
                                    skip = true;
                                }
                            }
                            if (skip) continue;
                        }
                        match = parameterizedTypes[j];
                        if (currentTarget.isWildcard()) {
                            // if alignment target is a wildcard type
                            // then we must make best effort to return a parameterized
                            // wildcard
                            ClassNode lower = currentTarget.getLowerBound() != null ? match.getType() : null;
                            ClassNode[] currentUpper = currentTarget.getUpperBounds();
                            ClassNode[] upper = currentUpper != null ? new ClassNode[currentUpper.length] : null;
                            if (upper != null) {
                                for (int k = 0; k < upper.length; k++) {
                                    upper[k] = currentUpper[k].isGenericsPlaceHolder() ? match.getType() : currentUpper[k];
                                }
                            }
                            match = new GenericsType(ClassHelper.makeWithoutCaching("?"), upper, lower);
                            match.setWildcard(true);
                        }
                    }
                }
            }
            if (match == null) {
                match = currentTarget;
            }
            generics[i] = match;
        }
        return generics;
    }

    /**
     * Generates a wildcard generic type in order to be used for checks against
     * class nodes. See {@link GenericsType#isCompatibleWith(ClassNode)}.
     *
     * @since 2.0.0
     */
    public static GenericsType buildWildcardType(final ClassNode... upperBounds) {
        GenericsType gt = new GenericsType(ClassHelper.makeWithoutCaching("?"), upperBounds, null);
        gt.setWildcard(true);
        return gt;
    }

    /**
     * Returns the type parameter/argument relationships of the specified type.
     *
     * @since 2.0.0
     */
    public static Map<GenericsType.GenericsTypeName, GenericsType> extractPlaceholders(final ClassNode type) {
        Map<GenericsType.GenericsTypeName, GenericsType> placeholders = new HashMap<>();
        extractPlaceholders(type, placeholders);
        return placeholders;
    }

    /**
     * Populates the supplied map with the type parameter/argument relationships
     * of the specified type.
     *
     * @since 2.0.0
     */
    public static void extractPlaceholders(final ClassNode type, final Map<GenericsType.GenericsTypeName, GenericsType> placeholders) {
        if (type == null) return;

        if (type.isArray()) {
            extractPlaceholders(type.getComponentType(), placeholders);
            return;
        }

        if (!type.isUsingGenerics() || !type.isRedirectNode()) return;
        GenericsType[] genericsTypes = type.getGenericsTypes(); int n;
        if (genericsTypes == null || (n = genericsTypes.length) == 0) return;

        // GROOVY-8609, GROOVY-10067, etc.
        if (type.isGenericsPlaceHolder()) {
            GenericsType gt = genericsTypes[0];
            placeholders.putIfAbsent(new GenericsType.GenericsTypeName(gt.getName()), gt);
            return;
        }

        GenericsType[] redirectGenericsTypes = type.redirect().getGenericsTypes();
        if (redirectGenericsTypes == null) {
            redirectGenericsTypes = genericsTypes;
        } else if (redirectGenericsTypes.length != n) {
            throw new GroovyBugError("Expected earlier checking to detect generics parameter arity mismatch" +
                    "\nExpected: " + type.getName() + toGenericTypesString(redirectGenericsTypes) +
                    "\nSupplied: " + type.getName() + toGenericTypesString(genericsTypes));
        }

        List<GenericsType> typeArguments = new ArrayList<>(n);
        for (int i = 0; i < n; i += 1) {
            GenericsType rgt = redirectGenericsTypes[i];
            if (rgt.isPlaceholder()) { // type parameter
                GenericsType typeArgument = genericsTypes[i];
                placeholders.computeIfAbsent(new GenericsType.GenericsTypeName(rgt.getName()), x -> {
                    typeArguments.add(typeArgument);
                    return typeArgument;
                });
            }
        }

        // examine non-placeholder type args
        for (GenericsType gt : typeArguments) {
            if (gt.isWildcard()) {
                ClassNode lowerBound = gt.getLowerBound();
                if (lowerBound != null) {
                    extractPlaceholders(lowerBound, placeholders);
                } else {
                    ClassNode[] upperBounds = gt.getUpperBounds();
                    if (upperBounds != null) {
                        for (ClassNode upperBound : upperBounds) {
                            extractPlaceholders(upperBound, placeholders);
                        }
                    }
                }
            } else if (!gt.isPlaceholder()) {
                extractPlaceholders(gt.getType(), placeholders);
            }
        }
    }

    /**
     * @since 3.0.0
     */
    public static String toGenericTypesString(final GenericsType[] genericsTypes) {
        if (genericsTypes == null) return "";
        StringJoiner sj = new StringJoiner(",","<","> ");
        for (GenericsType genericsType : genericsTypes) {
            sj.add(genericsType.toString());
        }
        return sj.toString();
    }

    /**
     * Interface class nodes retrieved from {@link org.codehaus.groovy.ast.ClassNode#getInterfaces()}
     * or {@link org.codehaus.groovy.ast.ClassNode#getAllInterfaces()} are returned with generic type
     * arguments. This method allows returning a parameterized interface given the parameterized class
     * node which implements this interface.
     *
     * @param hint   the class node where generics types are parameterized
     * @param target the interface we want to parameterize generics types
     * @return a parameterized interface class node
     *
     * @since 2.0.0
     * @deprecated Use #parameterizeType instead
     */
    @Deprecated(forRemoval = true, since = "2.5.0")
    public static ClassNode parameterizeInterfaceGenerics(final ClassNode hint, final ClassNode target) {
        return parameterizeType(hint, target);
    }

    /**
     * Interface class nodes retrieved from {@link org.codehaus.groovy.ast.ClassNode#getInterfaces()}
     * or {@link org.codehaus.groovy.ast.ClassNode#getAllInterfaces()} are returned with generic type
     * arguments. This method allows returning a parameterized interface given the parameterized class
     * node which implements this interface.
     *
     * @param hint   the ClassNode where generics types are parameterized
     * @param target the interface we want to parameterize generics types
     * @return a parameterized interface ClassNode
     *
     * @since 2.1.0
     */
    public static ClassNode parameterizeType(ClassNode hint, final ClassNode target) {
        if (hint.isArray()) {
            if (target.isArray()) {
                return parameterizeType(hint.getComponentType(), target.getComponentType()).makeArray();
            }
            return target;
        }
        if (hint.isGenericsPlaceHolder()) {
            ClassNode bound = hint.redirect();
            return parameterizeType(bound, target);
        }
        if (target.redirect().getGenericsTypes() == null) {
            return target;
        }

        ClassNode cn = target;
        Map<String, ClassNode> gt;

        // relationship may be reversed for cases like "Iterable<String> x = []"
        if (!cn.equals(hint) && implementsInterfaceOrIsSubclassOf(target, hint)) {
            do { // walk target type hierarchy towards hint
                cn = ClassHelper.getNextSuperClass(cn, hint);
                if (hasUnresolvedGenerics(cn)) {
                    gt = createGenericsSpec(hint);
                    extractSuperClassGenerics(hint, cn, gt);
                    cn = correctToGenericsSpecRecurse(gt, cn);
                }
            } while (!cn.equals(hint));

            hint = cn;
        }

        cn = target.redirect();
        gt = createGenericsSpec(hint);
        gt = createGenericsSpec(cn, gt);
        extractSuperClassGenerics(hint, cn, gt);
        return correctToGenericsSpecRecurse(gt, cn);
    }

    /**
     * @since 2.3.0
     */
    public static ClassNode nonGeneric(final ClassNode type) {
        int dims = 0;
        ClassNode temp = type;
        while (temp.isArray()) { dims += 1;
            temp = temp.getComponentType();
        }
        if (temp instanceof DecompiledClassNode // GROOVY-10461: check without resolving supers
                        ? ((DecompiledClassNode) temp).isParameterized() : temp.isUsingGenerics()) {
            ClassNode result = temp.getPlainNodeReference();
            result.setGenericsTypes(null);
            result.setUsingGenerics(false);
            while (dims > 0) { dims -= 1;
                result = result.makeArray();
            }
            return result;
        }
        return type;
    }

    /**
     * @since 2.3.0
     */
    public static ClassNode newClass(ClassNode type) {
        return type.getPlainNodeReference();
    }

    /**
     * @since 2.3.0
     */
    public static ClassNode makeClassSafe(Class klass) {
        return makeClassSafeWithGenerics(ClassHelper.make(klass));
    }

    /**
     * @since 2.4.0
     */
    public static ClassNode makeClassSafeWithGenerics(Class klass, ClassNode genericsType) {
        GenericsType[] genericsTypes = new GenericsType[1];
        genericsTypes[0] = new GenericsType(genericsType);
        return makeClassSafeWithGenerics(ClassHelper.make(klass), genericsTypes);
    }

    /**
     * @since 2.3.0
     */
    public static ClassNode makeClassSafe0(ClassNode type, GenericsType... genericTypes) {
        ClassNode plainNodeReference = newClass(type);
        if (genericTypes != null && genericTypes.length > 0) {
            plainNodeReference.setGenericsTypes(genericTypes);
            if (type.isGenericsPlaceHolder()) plainNodeReference.setGenericsPlaceHolder(true);
        }
        return plainNodeReference;
    }

    /**
     * @since 2.3.0
     */
    public static ClassNode makeClassSafeWithGenerics(ClassNode type, GenericsType... genericTypes) {
        if (type.isArray()) {
            return makeClassSafeWithGenerics(type.getComponentType(), genericTypes).makeArray();
        }
        int nTypes = (genericTypes == null ? 0 : genericTypes.length);
        GenericsType[] gTypes;
        if (nTypes == 0) {
            gTypes = EMPTY_GENERICS_ARRAY;
        } else {
            gTypes = new GenericsType[nTypes];
            System.arraycopy(genericTypes, 0, gTypes, 0, nTypes);
        }
        return makeClassSafe0(type, gTypes);
    }

    /**
     * @since 2.3.0
     */
    public static MethodNode correctToGenericsSpec(Map<String, ClassNode> genericsSpec, MethodNode mn) {
        if (genericsSpec == null) return mn;
        if (mn.getGenericsTypes() != null) genericsSpec = addMethodGenerics(mn, genericsSpec);
        ClassNode returnType = correctToGenericsSpecRecurse(genericsSpec, mn.getReturnType());
        Parameter[] oldParameters = mn.getParameters(); int nParameters= oldParameters.length;
        Parameter[] newParameters = new Parameter[nParameters];
        for (int i = 0; i < nParameters; i += 1) {
            Parameter oldParameter = oldParameters[i];
            newParameters[i] = new Parameter(correctToGenericsSpecRecurse(genericsSpec, oldParameter.getType()), oldParameter.getName(), oldParameter.getInitialExpression());
        }
        MethodNode newMethod = new MethodNode(mn.getName(), mn.getModifiers(), returnType, newParameters, mn.getExceptions(), mn.getCode());
        newMethod.setGenericsTypes(mn.getGenericsTypes());
        return newMethod;
    }

    /**
     * @since 2.3.0
     */
    public static ClassNode correctToGenericsSpecRecurse(Map<String, ClassNode> genericsSpec, ClassNode type) {
        return correctToGenericsSpecRecurse(genericsSpec, type, Collections.emptyList());
    }

    /**
     * @since 2.4.1
     */
    public static ClassNode[] correctToGenericsSpecRecurse(Map<String, ClassNode> genericsSpec, ClassNode[] types) {
        if (types == null || types.length == 1) return types;
        ClassNode[] newTypes = new ClassNode[types.length];
        boolean modified = false;
        for (int i = 0; i < types.length; i++) {
            newTypes[i] = correctToGenericsSpecRecurse(genericsSpec, types[i], Collections.emptyList());
            modified = modified || (types[i] != newTypes[i]);
        }
        if (!modified) return types;
        return newTypes;
    }

    /**
     * @since 2.4.1
     */
    public static ClassNode correctToGenericsSpecRecurse(Map<String, ClassNode> genericsSpec, ClassNode type, List<String> exclusions) {
        if (type.isArray()) {
            return correctToGenericsSpecRecurse(genericsSpec, type.getComponentType(), exclusions).makeArray();
        }
        String name = type.getUnresolvedName();
        if (type.isGenericsPlaceHolder() && !exclusions.contains(name)) {
            exclusions = plus(exclusions, name); // GROOVY-7722
            type = genericsSpec.get(name);
            if (type != null && type.isGenericsPlaceHolder()) {
                if (type.getGenericsTypes() == null) {
                    ClassNode placeholder = ClassHelper.makeWithoutCaching(type.getUnresolvedName());
                    placeholder.setGenericsPlaceHolder(true);
                    return makeClassSafeWithGenerics(type, new GenericsType(placeholder));
                } else if (!name.equals(type.getUnresolvedName())) {
                    return correctToGenericsSpecRecurse(genericsSpec, type, exclusions);
                }
            }
        }
        if (type == null) type = ClassHelper.OBJECT_TYPE.getPlainNodeReference();
        GenericsType[] oldgTypes = type.getGenericsTypes();
        GenericsType[] newgTypes = EMPTY_GENERICS_ARRAY;
        if (oldgTypes != null) {
            newgTypes = new GenericsType[oldgTypes.length];
            for (int i = 0; i < newgTypes.length; i++) {
                GenericsType oldgType = oldgTypes[i];
                if (oldgType.isWildcard()) {
                    ClassNode[] oldUpper = oldgType.getUpperBounds();
                    ClassNode[] upper = null;
                    if (oldUpper != null) {
                        // correct "? extends T" or "? extends T & I"
                        upper = new ClassNode[oldUpper.length];
                        for (int j = 0; j < oldUpper.length; j++) {
                            upper[j] = correctToGenericsSpecRecurse(genericsSpec, oldUpper[j], exclusions);
                        }
                    }
                    ClassNode oldLower = oldgType.getLowerBound();
                    ClassNode lower = null;
                    if (oldLower != null) {
                        // correct "? super T"
                        lower = correctToGenericsSpecRecurse(genericsSpec, oldLower, exclusions);
                    }
                    GenericsType fixed = new GenericsType(oldgType.getType(), upper, lower);
                    fixed.setWildcard(true);
                    newgTypes[i] = fixed;
                } else if (oldgType.isPlaceholder()) {
                    // correct "T"
                    newgTypes[i] = genericsSpec.containsKey(oldgType.getName())? new GenericsType(genericsSpec.get(oldgType.getName())): erasure(oldgType);
                } else {
                    // correct "List<T>", etc.
                    newgTypes[i] = new GenericsType(correctToGenericsSpecRecurse(genericsSpec, correctToGenericsSpec(genericsSpec, oldgType), exclusions));
                }
            }
        }
        return makeClassSafeWithGenerics(type, newgTypes);
    }

    /**
     * @since 2.3.0
     */
    public static ClassNode correctToGenericsSpec(final Map<String, ClassNode> genericsSpec, final GenericsType type) {
        ClassNode cn = null;
        if (type.isPlaceholder()) {
            String name = type.getName();
            if (name.charAt(0) != '#') //
                cn = genericsSpec.get(name);
        }
        else if (type.isWildcard()) {
            if (type.getUpperBounds() != null)
                cn = type.getUpperBounds()[0]; // GROOVY-9891
        }
        if (cn == null) {
            cn = type.getType();
        }
        return cn;
    }

    /**
     * @since 2.3.0
     */
    public static ClassNode correctToGenericsSpec(final Map<String, ClassNode> genericsSpec, ClassNode type) {
        if (type.isArray()) {
            return correctToGenericsSpec(genericsSpec, type.getComponentType()).makeArray();
        }
        if (type.isGenericsPlaceHolder() && type.getGenericsTypes() != null) {
            String name = type.getGenericsTypes()[0].getName();
            type = genericsSpec.get(name);
            if (type != null && type.isGenericsPlaceHolder()
                    && !name.equals(type.getUnresolvedName())) {
                return correctToGenericsSpec(genericsSpec, type);
            }
        }
        return type != null ? type : ClassHelper.OBJECT_TYPE.getPlainNodeReference();
    }

    /**
     * @since 2.4.0
     */
    public static Map<String, ClassNode> createGenericsSpec(final ClassNode type) {
        return createGenericsSpec(type, Collections.emptyMap());
    }

    /**
     * @since 2.3.0
     */
    public static Map<String, ClassNode> createGenericsSpec(final ClassNode type, final Map<String, ClassNode> oldSpec) {
        // Example:
        // abstract class A<X,Y,Z> { ... }
        // class C<T extends Number> extends A<T,Object,String> { }
        // the type "A<T,Object,String> -> A<X,Y,Z>" will produce [X:Number,Y:Object,Z:String]

        ClassNode oc = type.getNodeMetaData("outer.class"); // GROOVY-10646: outer class type parameters
        Map<String, ClassNode> newSpec = oc != null ? createGenericsSpec(oc, oldSpec) : new HashMap<>();
        GenericsType[] gt = type.getGenericsTypes(), rgt = type.redirect().getGenericsTypes();
        if (gt != null && rgt != null) {
            for (int i = 0, n = gt.length; i < n; i += 1) {
                newSpec.put(rgt[i].getName(), correctToGenericsSpec(oldSpec, gt[i]));
            }
        }
        return newSpec;
    }

    /**
     * @since 2.4.1
     */
    public static Map<String, ClassNode> addMethodGenerics(final MethodNode node, final Map<String, ClassNode> oldSpec) {
        Map<String, ClassNode> newSpec = new HashMap<>(oldSpec);
        GenericsType[] tps = node.getGenericsTypes();
        if (tps != null) {
            for (GenericsType tp : tps) {
                String name = tp.getName();
                ClassNode type = tp.getType();
                ClassNode redirect;
                if (tp.getUpperBounds() != null) {
                    redirect = tp.getUpperBounds()[0];
                } else {
                    redirect = ClassHelper.OBJECT_TYPE;
                }
                if (redirect.isGenericsPlaceHolder()) {
                    // "T extends U (& Face)*"
                    type = redirect;
                } else {
                    // "T" or "T extends Type (& Face)*"
                    type = ClassHelper.makeWithoutCaching(name);
                    type.setGenericsPlaceHolder(true);
                    type.setRedirect(redirect);
                }
                newSpec.put(name, type);
            }
        }
        return newSpec;
    }

    /**
     * @since 2.3.1
     */
    public static void extractSuperClassGenerics(final ClassNode type, final ClassNode target, final Map<String, ClassNode> spec) {
    // TODO: this is very similar to StaticTypesCheckingSupport#extractGenericsConnections, using ClassNode instead of GenericsType
        if (target == null || target == type) return;
        if (target.isGenericsPlaceHolder()) {
            spec.put(target.getUnresolvedName(), type);
        } else if (type.isArray() && target.isArray()) {
            extractSuperClassGenerics(type.getComponentType(), target.getComponentType(), spec);
        } else if (type.isArray() && target.getName().equals(JAVA_LANG_OBJECT)) {
            // Object is the superclass of an array, but no generics are involved
        } else if (type.equals(target) || !implementsInterfaceOrIsSubclassOf(type, target)) {
            extractSuperClassGenerics(type.getGenericsTypes(), target.getGenericsTypes(), spec);
        } else {
            ClassNode superClass = getSuperClass(type, target);
            if (superClass != null) {
                if (hasUnresolvedGenerics(superClass)) {
                    GenericsType[] tp = type.redirect().getGenericsTypes();
                    if (tp != null) {
                        GenericsType[] ta = type.getGenericsTypes();
                        boolean noTypeArguments = ta == null || ta.length == 0 || !type.isRedirectNode();
                        Map<String, ClassNode> genericsSpec = new HashMap<>();
                        for (int i = 0, n = tp.length; i < n; i += 1) {
                            ClassNode cn;
                            if (noTypeArguments || isUnboundedWildcard(ta[i])) { // GROOVY-10651
                                GenericsType gt = tp[i];
                                cn = gt.getUpperBounds() != null ? gt.getUpperBounds()[0] : gt.getType().redirect();
                            } else {
                                GenericsType gt = ta[i];
                                cn = gt.isWildcard() && gt.getUpperBounds() != null ? gt.getUpperBounds()[0] : gt.getType();
                            }
                            genericsSpec.put(tp[i].getName(), cn);
                        }
                        superClass = correctToGenericsSpecRecurse(genericsSpec, superClass);
                    }
                }
                extractSuperClassGenerics(superClass, target, spec);
            } else {
                // if we reach here, we have an unhandled case
                throw new GroovyBugError("The type " + type + " seems not to normally extend " + target + ". Sorry, I cannot handle this.");
            }
        }
    }

    /**
     * @since 3.0.0
     */
    public static ClassNode getSuperClass(final ClassNode type, final ClassNode target) {
        return ClassHelper.getNextSuperClass(ClassHelper.isPrimitiveType(type) ? ClassHelper.getWrapper(type) : type, target);
    }

    private static void extractSuperClassGenerics(final GenericsType[] usage, final GenericsType[] declaration, final Map<String, ClassNode> spec) {
        // if declaration does not provide generics, there is no connection to make
        if (declaration == null || declaration.length == 0) return;

        // if usage is a raw type, remove type parameters from spec
        if (usage == null) {
            for (GenericsType dt : declaration) {
                String name = dt.getName();
                ClassNode type = spec.get(name);
                if (type != null && type.isGenericsPlaceHolder()
                        && type.getUnresolvedName().equals(name)) {
                    type = type.asGenericsType().getUpperBounds()[0];
                    spec.put(name, type);
                }
            }
            return;
        }

        if (usage.length != declaration.length) return;

        for (int i = 0, n = usage.length; i < n; i += 1) {
            GenericsType ui = usage[i];
            GenericsType di = declaration[i];
            if (di.isPlaceholder()) {
                spec.put(di.getName(), ui.getType());
            } else if (di.isWildcard()) {
                if (ui.isWildcard()) {
                    extractSuperClassGenerics(ui.getLowerBound(), di.getLowerBound(), spec);
                    extractSuperClassGenerics(ui.getUpperBounds(), di.getUpperBounds(), spec);
                } else {
                    ClassNode cu = ui.getType();
                    extractSuperClassGenerics(cu, di.getLowerBound(), spec);
                    ClassNode[] upperBounds = di.getUpperBounds();
                    if (upperBounds != null) {
                        for (ClassNode cn : upperBounds) {
                            extractSuperClassGenerics(cu, cn, spec);
                        }
                    }
                }
            } else {
                extractSuperClassGenerics(ui.getType(), di.getType(), spec);
            }
        }
    }

    private static void extractSuperClassGenerics(final ClassNode[] usage, final ClassNode[] declaration, final Map<String, ClassNode> spec) {
        if (usage == null || declaration == null || declaration.length == 0) return;
        // both have generics
        for (int i = 0; i < usage.length; i++) {
            ClassNode ui = usage[i];
            ClassNode di = declaration[i];
            if (di.isGenericsPlaceHolder()) {
                spec.put(di.getGenericsTypes()[0].getName(), di);
            } else if (di.isUsingGenerics()) {
                extractSuperClassGenerics(ui.getGenericsTypes(), di.getGenericsTypes(), spec);
            }
        }
    }

    /**
     * @since 2.4.0
     */
    public static ClassNode[] parseClassNodesFromString(final String option, final SourceUnit sourceUnit, final CompilationUnit compilationUnit, final MethodNode mn, final ASTNode usage) {
        try {
            ModuleNode moduleNode = ParserPlugin.buildAST("Dummy<" + option + "> dummy;", compilationUnit.getConfiguration(), compilationUnit.getClassLoader(), null);
            DeclarationExpression dummyDeclaration = (DeclarationExpression) ((ExpressionStatement) moduleNode.getStatementBlock().getStatements().get(0)).getExpression();

            // the returned node is DummyNode<Param1, Param2, Param3, ...)
            ClassNode dummyNode = dummyDeclaration.getLeftExpression().getType();
            GenericsType[] dummyNodeGenericsTypes = dummyNode.getGenericsTypes();
            if (dummyNodeGenericsTypes != null) {
                int n = dummyNodeGenericsTypes.length;
                ClassNode[] signature = new ClassNode[n];
                for (int i = 0; i < n; i += 1) {
                    GenericsType genericsType = dummyNodeGenericsTypes[i];
                    signature[i] = genericsType.isWildcard() ? ClassHelper.dynamicType()
                                    : resolveClassNode(sourceUnit, compilationUnit, mn, usage, genericsType.getType());
                }
                return signature;
            }
        } catch (Exception | LinkageError e) {
            sourceUnit.addError(new IncorrectTypeHintException(mn, e, usage.getLineNumber(), usage.getColumnNumber()));
        }
        return null;
    }

    private static ClassNode resolveClassNode(final SourceUnit sourceUnit, final CompilationUnit compilationUnit, final MethodNode mn, final ASTNode usage, final ClassNode parsedNode) {
        ClassNode dummyClass = new ClassNode("dummy", 0, ClassHelper.OBJECT_TYPE);
        dummyClass.setModule(new ModuleNode(sourceUnit));
        dummyClass.setGenericsTypes(mn.getDeclaringClass().getGenericsTypes());
        MethodNode dummyMN = new MethodNode(
                "dummy",
                0,
                parsedNode,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                EmptyStatement.INSTANCE
        );
        dummyMN.setGenericsTypes(mn.getGenericsTypes());
        dummyClass.addMethod(dummyMN);
        ResolveVisitor visitor = new ResolveVisitor(compilationUnit) {
            @Override
            public void addError(final String msg, final ASTNode expr) {
                sourceUnit.addError(new IncorrectTypeHintException(mn, msg, usage.getLineNumber(), usage.getColumnNumber()));
            }
        };
        visitor.startResolving(dummyClass, sourceUnit);
        return dummyMN.getReturnType();
    }

    /**
     * Transforms generics types from an old context to a new context using the
     * given spec. This method assumes all generics types will be placeholders.
     * WARNING: The resulting generics types may or may not be placeholders
     * after the transformation.
     *
     * @param genericsSpec    the generics context information spec
     * @param oldPlaceHolders the old placeholders
     * @return the new generics types
     *
     * @since 2.5.0
     */
    public static GenericsType[] applyGenericsContextToPlaceHolders(Map<String, ClassNode> genericsSpec, GenericsType[] oldPlaceHolders) {
        if (oldPlaceHolders == null || oldPlaceHolders.length == 0) return oldPlaceHolders;
        if (genericsSpec.isEmpty()) return oldPlaceHolders;
        GenericsType[] newTypes = new GenericsType[oldPlaceHolders.length];
        for (int i = 0; i < oldPlaceHolders.length; i++) {
            GenericsType old = oldPlaceHolders[i];
            if (!old.isPlaceholder())
                throw new GroovyBugError("Given generics type " + old + " must be a placeholder!");
            ClassNode fromSpec = genericsSpec.get(old.getName());
            if (fromSpec != null) {
                newTypes[i] = fromSpec.asGenericsType();
            } else {
                ClassNode[] upper = old.getUpperBounds();
                ClassNode[] newUpper = upper;
                if (upper != null && upper.length > 0) {
                    ClassNode[] upperCorrected = new ClassNode[upper.length];
                    for (ClassNode classNode : upper) {
                        upperCorrected[i] = correctToGenericsSpecRecurse(genericsSpec, classNode);
                    }
                    upper = upperCorrected;
                }
                ClassNode lower = old.getLowerBound();
                ClassNode newLower = correctToGenericsSpecRecurse(genericsSpec, lower);
                if (lower == newLower && upper == newUpper) {
                    newTypes[i] = oldPlaceHolders[i];
                } else {
                    ClassNode newPlaceHolder = ClassHelper.make(old.getName());
                    GenericsType gt = new GenericsType(newPlaceHolder, newUpper, newLower);
                    gt.setPlaceholder(true);
                    newTypes[i] = gt;
                }
            }
        }
        return newTypes;
    }

    private static final boolean PARAMETERIZED_TYPE_CACHE_ENABLED = Boolean.parseBoolean(getSystemPropertySafe("groovy.enable.parameterized.type.cache", "true"));

    private static final EvictableCache<ParameterizedTypeCacheKey, SoftReference<ClassNode>> PARAMETERIZED_TYPE_CACHE = new ConcurrentSoftCache<>(64);

    /**
     * Clears the parameterized type cache.
     * <p>
     * It is useful to IDE as the type being compiled are continuously being edited/altered; see GROOVY-8675
     *
     * @since 3.0.0
     */
    public static void clearParameterizedTypeCache() {
        PARAMETERIZED_TYPE_CACHE.clearAll();
    }

    /**
     * Convenience method for {@link #findParameterizedTypeFromCache(ClassNode,ClassNode,boolean)}
     * with {@code tryToFindExactType} set to {@code false}.
     *
     * @since 3.0.0
     */
    public static ClassNode findParameterizedTypeFromCache(final ClassNode genericsClass, final ClassNode actualType) {
        return findParameterizedTypeFromCache(genericsClass, actualType, false);
    }

    /**
     * Try to get the parameterized type from the cache.
     * <p>
     * If no cached item found, cache and return the result of {@link #findParameterizedType(ClassNode,ClassNode,boolean)}.
     *
     * @since 3.0.0
     */
    public static ClassNode findParameterizedTypeFromCache(final ClassNode genericsClass, final ClassNode actualType, boolean tryToFindExactType) {
        if (!PARAMETERIZED_TYPE_CACHE_ENABLED) {
            return findParameterizedType(genericsClass, actualType, tryToFindExactType);
        }

        SoftReference<ClassNode> sr = PARAMETERIZED_TYPE_CACHE.getAndPut(
                new ParameterizedTypeCacheKey(genericsClass, actualType),
                key -> new SoftReference<>(findParameterizedType(key.getGenericsClass(), key.getActualType(), tryToFindExactType)));

        return sr != null ? sr.get() : null;
    }

    /**
     * Convenience method for {@link #findParameterizedType(ClassNode,ClassNode,boolean)} with {@code tryToFindExactType} set to {@code false}.
     *
     * @since 3.0.0
     */
    public static ClassNode findParameterizedType(final ClassNode genericsClass, final ClassNode actualType) {
        return findParameterizedType(genericsClass, actualType, false);
    }

    /**
     * Gets the parameterized type by searching the whole class hierarchy according to generics class and actual receiver.
     * <p>
     * {@link #findParameterizedTypeFromCache(ClassNode,ClassNode,boolean)} is strongly recommended for better performance.
     *
     * @since 3.0.0
     */
    public static ClassNode findParameterizedType(final ClassNode genericsClass, final ClassNode actualType, final boolean tryToFindExactType) {
        final GenericsType[] genericsTypes = genericsClass.getGenericsTypes();
        if (genericsTypes == null || genericsClass.isGenericsPlaceHolder()) {
            return null;
        }

        if (actualType.equals(genericsClass)) {
            return actualType;
        }

        LinkedList<ClassNode> todo = new LinkedList<>();
        Set<ClassNode> done = new HashSet<>();
        todo.add(actualType);
        ClassNode type;

        while ((type = todo.poll()) != null) {
            if (done.add(type)) {
                if (!type.isInterface()) {
                    ClassNode cn = type.getUnresolvedSuperClass();
                    if (cn != null && cn.redirect() != ClassHelper.OBJECT_TYPE) {
                        if (hasUnresolvedGenerics(cn)) {
                            cn = parameterizeType(type, cn);
                        }
                        if (cn.equals(genericsClass)) {
                            return cn;
                        }
                        todo.add(cn);
                    }
                }
                for (ClassNode cn : type.getInterfaces()) {
                    if (hasUnresolvedGenerics(cn)) {
                        cn = parameterizeType(type, cn);
                    }
                    if (cn.equals(genericsClass)) {
                        return cn;
                    }
                    todo.add(cn);
                }
            }
        }

        return null;
    }

    /**
     * map declaring generics type to actual generics type, e.g. GROOVY-7204:
     * declaring generics types:      T,      S extends Serializable
     * actual generics types   : String,      Long
     *
     * the result map is [
     *  T: String,
     *  S: Long
     * ]
     *
     * The resolved types can not help us to choose methods correctly if the argument is a string:  T: Object, S: Serializable
     * so we need actual types:  T: String, S: Long
     *
     * @since 3.0.0
     */
    @Deprecated(forRemoval = true, since = "5.0.0")
    public static Map<GenericsType, GenericsType> makeDeclaringAndActualGenericsTypeMap(final ClassNode declaringClass, final ClassNode actualReceiver) {
        return correlateTypeParametersAndTypeArguments(declaringClass, actualReceiver, false);
    }

    /**
     * The method is similar with {@link GenericsUtils#makeDeclaringAndActualGenericsTypeMap(ClassNode, ClassNode)},
     * The main difference is that the method will try to map all placeholders found to the relevant exact types,
     * but the other will not try even if the parameterized type has placeholders
     *
     * @param declaringClass the generics class node declaring the generics types
     * @param actualReceiver the subclass class node
     * @return the placeholder-to-actualtype mapping
     *
     * @since 3.0.0
     */
    @Deprecated(forRemoval = true, since = "5.0.0")
    public static Map<GenericsType, GenericsType> makeDeclaringAndActualGenericsTypeMapOfExactType(final ClassNode declaringClass, final ClassNode actualReceiver) {
        return correlateTypeParametersAndTypeArguments(declaringClass, actualReceiver, true);
    }

    @Deprecated(forRemoval = true, since = "5.0.0")
    private static Map<GenericsType, GenericsType> correlateTypeParametersAndTypeArguments(final ClassNode declaringClass, final ClassNode actualReceiver, final boolean tryToFindExactType) {
        ClassNode parameterizedType = findParameterizedTypeFromCache(declaringClass, actualReceiver, tryToFindExactType);
        if (parameterizedType != null && parameterizedType.isRedirectNode() && !parameterizedType.isGenericsPlaceHolder()) { // GROOVY-10166
            // declaringClass may be "List<T> -> List<E>" and parameterizedType may be "List<String> -> List<E>" or "List<> -> List<E>"
            final GenericsType[] typeParameters = parameterizedType.redirect().getGenericsTypes();
            if (typeParameters != null) {
                final GenericsType[] typeArguments = parameterizedType.getGenericsTypes();
                final int m = typeArguments == null ? 0 : typeArguments.length;
                final int n = typeParameters.length;

                Map<GenericsType, GenericsType> map = new LinkedHashMap<>();
                for (int i = 0; i < n; i += 1) {
                    map.put(typeParameters[i], i < m ? typeArguments[i] : erasure(typeParameters[i]));
                }
                return map;
            }
        }
        return Collections.emptyMap();
    }

    /**
     * @see org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport#extractType(GenericsType)
     */
    private static GenericsType erasure(GenericsType gt) {
        ClassNode cn = gt.getType().redirect(); // discard the placeholder

        if (gt.getType().getGenericsTypes() != null)
            gt = gt.getType().getGenericsTypes()[0];

        if (gt.getUpperBounds() != null)
            cn = gt.getUpperBounds()[0]; // TODO: if length > 1 then union type?

        return cn.asGenericsType();
    }

    /**
     * Checks if the type has any non-placeholder (aka resolved) generics.
     *
     * @since 3.0.0
     */
    public static boolean hasNonPlaceHolders(final ClassNode type) {
        return checkPlaceHolders(type, gt -> !gt.isPlaceholder());
    }

    /**
     * Checks if the type has any placeholder (aka unresolved) generics.
     *
     * @since 3.0.0
     */
    public static boolean hasPlaceHolders(final ClassNode type) {
        return checkPlaceHolders(type, gt -> gt.isPlaceholder());
    }

    private static boolean checkPlaceHolders(final ClassNode type, final Predicate<GenericsType> p) {
        if (type != null) {
            GenericsType[] genericsTypes = type.getGenericsTypes();
            if (genericsTypes != null) {
                for (GenericsType genericsType : genericsTypes) {
                    if (p.test(genericsType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks for any placeholder (aka unresolved) generics.
     *
     * @since 4.0.0
     */
    public static boolean hasUnresolvedGenerics(final ClassNode type) {
        if (type.isGenericsPlaceHolder()) return true;
        if (type.isArray()) {
            return hasUnresolvedGenerics(type.getComponentType());
        }
        GenericsType[] genericsTypes = type.getGenericsTypes();
        if (genericsTypes != null) {
            for (GenericsType genericsType : genericsTypes) {
                if (genericsType.isPlaceholder()) return true;
                ClassNode lowerBound = genericsType.getLowerBound();
                ClassNode[] upperBounds = genericsType.getUpperBounds();
                if (lowerBound != null) {
                    if (hasUnresolvedGenerics(lowerBound)) return true;
                } else if (upperBounds != null) {
                    for (ClassNode upperBound : upperBounds) {
                        if (hasUnresolvedGenerics(upperBound)) return true;
                    }
                } else {
                    if (hasUnresolvedGenerics(genericsType.getType())) return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the parameter and return types of the abstract method of SAM.
     *
     * If the abstract method is not parameterized, we will get generics placeholders, e.g. T, U
     * For example, the abstract method of {@link java.util.function.Function} is
     * <pre>
     *      R apply(T t);
     * </pre>
     *
     * We parameterize the above interface as {@code Function<String, Integer>}, then the abstract method will be
     * <pre>
     *      Integer apply(String t);
     * </pre>
     *
     * When we call {@code parameterizeSAM} on the ClassNode {@code Function<String, Integer>},
     * we can get parameter types and return type of the above abstract method,
     * i.e. ClassNode {@code ClassHelper.STRING_TYPE} and {@code ClassHelper.Integer_TYPE}
     *
     * @param samType the class node which contains only one abstract method
     *
     * @since 3.0.0
     */
    public static Tuple2<ClassNode[], ClassNode> parameterizeSAM(final ClassNode samType) {
        MethodNode abstractMethod = ClassHelper.findSAM(samType);
        ClassNode  declaringClass = abstractMethod.getDeclaringClass();
        Map<GenericsType.GenericsTypeName, GenericsType> spec = extractPlaceholders(
            samType.equals(declaringClass) ? samType : parameterizeType(samType, declaringClass));

        if (spec.isEmpty() && declaringClass.getGenericsTypes() != null) {
            for (GenericsType tp : declaringClass.getGenericsTypes()) // apply erasure
                spec.put(new GenericsType.GenericsTypeName(tp.getName()), erasure(tp));
        } else {
            // resolveClassNodeGenerics converts "T=? super Type" to Object, so convert "T=? super Type" to "T=Type"
            spec.replaceAll((name, type) -> type.isWildcard() && type.getLowerBound() != null ? type.getLowerBound().asGenericsType() : type);
        }

        ClassNode[] parameterTypes = Arrays.stream(abstractMethod.getParameters()).map(p -> resolveClassNodeGenerics(spec, null, p.getType())).toArray(ClassNode[]::new);
        ClassNode returnType = resolveClassNodeGenerics(spec, null, abstractMethod.getReturnType());
        return new Tuple2<>(parameterTypes, returnType);
    }

    /**
     * Gets the actual type according to the placeholder name.
     *
     * @param placeholderName the placeholder name (i.e. "T", "E", etc.)
     * @param genericsPlaceholderAndTypeMap the result of {@link #makeDeclaringAndActualGenericsTypeMap}
     *
     * @since 3.0.0
     */
    @Deprecated(forRemoval = true, since = "5.0.0")
    public static ClassNode findActualTypeByGenericsPlaceholderName(final String placeholderName, final Map<GenericsType, GenericsType> genericsPlaceholderAndTypeMap) {
        Function<GenericsType, ClassNode> resolver = gt -> {
            if (gt.isWildcard()) {
                if (gt.getLowerBound() != null) {
                    return gt.getLowerBound();
                }
                if (gt.getUpperBounds() != null) {
                    return gt.getUpperBounds()[0];
                }
            }
            return gt.getType();
        };

        return genericsPlaceholderAndTypeMap.entrySet().stream()
                .filter(e -> e.getKey().getName().equals(placeholderName))
                .map(Map.Entry::getValue).map(resolver).findFirst().orElse(null);
    }

    private static class ParameterizedTypeCacheKey {
        private ClassNode genericsClass;
        private ClassNode actualType;

        public ParameterizedTypeCacheKey(ClassNode genericsClass, ClassNode actualType) {
            this.genericsClass = genericsClass;
            this.actualType = actualType;
        }

        public ClassNode getGenericsClass() {
            return genericsClass;
        }

        @SuppressWarnings("unused")
        public void setGenericsClass(ClassNode genericsClass) {
            this.genericsClass = genericsClass;
        }

        public ClassNode getActualType() {
            return actualType;
        }

        @SuppressWarnings("unused")
        public void setActualType(ClassNode actualType) {
            this.actualType = actualType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ParameterizedTypeCacheKey cacheKey = (ParameterizedTypeCacheKey) o;

            return genericsClass == cacheKey.genericsClass &&
                    actualType == cacheKey.actualType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(genericsClass, actualType);
        }
    }
}
