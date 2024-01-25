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
package org.apache.groovy.ast.tools;

import groovy.transform.NonSealed;
import org.apache.groovy.util.BeanUtils;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.decompiled.DecompiledClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.reflection.ReflectionUtils;
import org.codehaus.groovy.transform.AbstractASTTransformation;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.reflect.Modifier.isFinal;
import static org.codehaus.groovy.ast.ClassHelper.isObjectType;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOrImplements;
import static org.codehaus.groovy.runtime.ArrayGroovyMethods.asBoolean;
import static org.codehaus.groovy.runtime.ArrayTypeUtils.dimension;
import static org.codehaus.groovy.runtime.ArrayTypeUtils.elementType;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * Utility class for working with ClassNodes
 */
public class ClassNodeUtils {

    /**
     * Formats a type name into a human-readable version. For arrays, appends "[]" to the formatted
     * type name of the component. For unit class nodes, uses the class node name.
     *
     * @param cNode the type to format
     * @return a human-readable version of the type name (java.lang.String[] for example)
     */
    public static String formatTypeName(final ClassNode cNode) {
        if (cNode.isArray()) {
            ClassNode it = cNode;
            int dim = 0;
            while (it.isArray()) {
                dim++;
                it = it.getComponentType();
            }
            StringBuilder sb = new StringBuilder(it.getName().length() + 2 * dim);
            sb.append(it.getName());
            for (int i = 0; i < dim; i++) {
                sb.append("[]");
            }
            return sb.toString();
        }
        return cNode.getName();
    }

    /**
     * Returns an existing method if one exists or else create a new method and mark it as {@code @Generated}.
     *
     * @see ClassNode#addMethod(String, int, ClassNode, Parameter[], ClassNode[], Statement)
     */
    public static MethodNode addGeneratedMethod(final ClassNode cNode, final String name, final int modifiers,
            final ClassNode returnType, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        MethodNode result = cNode.getDeclaredMethod(name, parameters);
        if (result == null) {
            result = new MethodNode(name, modifiers, returnType, parameters, exceptions, code);
            addGeneratedMethod(cNode, result);
        }
        return result;
    }

    /**
     * Adds a method and mark it as {@code @Generated}.
     *
     * @see ClassNode#addMethod(MethodNode)
     */
    public static void addGeneratedMethod(final ClassNode cNode, final MethodNode mNode) {
        cNode.addMethod(mNode);
        AnnotatedNodeUtils.markAsGenerated(cNode, mNode);
    }

    /**
     * Adds a method and mark it as {@code @Generated}.
     *
     * @see ClassNode#addMethod(MethodNode)
     */
    public static void addGeneratedMethod(final ClassNode cNode, final MethodNode mNode, final boolean skipChecks) {
        cNode.addMethod(mNode);
        AnnotatedNodeUtils.markAsGenerated(cNode, mNode, skipChecks);
    }

    /**
     * Adds an inner class that is marked as {@code @Generated}.
     *
     * @see org.codehaus.groovy.ast.ModuleNode#addClass(ClassNode)
     */
    public static void addGeneratedInnerClass(final ClassNode cNode, final ClassNode inner) {
        cNode.getModule().addClass(inner);
        AnnotatedNodeUtils.markAsGenerated(cNode, inner);
    }

    /**
     * Adds a method that is marked as {@code @Generated}.
     *
     * @see ClassNode#addConstructor(int, Parameter[], ClassNode[], Statement)
     */
    public static ConstructorNode addGeneratedConstructor(final ClassNode classNode, final int modifiers, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        ConstructorNode ctorNode = new ConstructorNode(modifiers, parameters, exceptions, code);
        addGeneratedConstructor(classNode, ctorNode);
        return ctorNode;
    }

    /**
     * Adds a method that is marked as {@code @Generated}.
     *
     * @see ClassNode#addConstructor(ConstructorNode)
     */
    public static void addGeneratedConstructor(final ClassNode classNode, final ConstructorNode ctorNode) {
        classNode.addConstructor(ctorNode);
        AnnotatedNodeUtils.markAsGenerated(classNode, ctorNode);
    }

    /**
     * Adds methods from the super class.
     *
     * @param cNode The ClassNode
     * @return A map of methods
     */
    public static Map<String, MethodNode> getDeclaredMethodsFromSuper(final ClassNode cNode) {
        ClassNode parent = cNode.getSuperClass();
        if (parent == null) {
            return new LinkedHashMap<>();
        }
        return parent.getDeclaredMethodsMap();
    }

    /**
     * Adds methods from all interfaces. Existing entries in the methods map
     * take precedence. Methods from interfaces visited early take precedence
     * over later ones.
     *
     * @param cNode The ClassNode
     * @param methodsMap A map of existing methods to alter
     */
    public static void addDeclaredMethodsFromInterfaces(final ClassNode cNode, final Map<String, MethodNode> methodsMap) {
        for (ClassNode iface : cNode.getInterfaces()) {
            Map<String, MethodNode> declaredMethods = iface.getDeclaredMethodsMap();
            for (Map.Entry<String, MethodNode> entry : declaredMethods.entrySet()) {
                if (entry.getValue().getDeclaringClass().isInterface() && (entry.getValue().getModifiers() & ACC_SYNTHETIC) == 0) {
                    methodsMap.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Gets methods from all interfaces. Methods from interfaces visited early
     * take precedence over later ones.
     *
     * @param cNode The ClassNode
     * @return A map of methods
     */
    public static Map<String, MethodNode> getDeclaredMethodsFromInterfaces(final ClassNode cNode) {
        Map<String, MethodNode> methodsMap = new LinkedHashMap<>();
        addDeclaredMethodsFromInterfaces(cNode, methodsMap);
        return methodsMap;
    }

    /**
     * Adds methods from interfaces and parent interfaces. Existing entries in the methods map take precedence.
     * Methods from interfaces visited early take precedence over later ones.
     *
     * @param cNode The ClassNode
     * @param methodsMap A map of existing methods to alter
     */
    public static void addDeclaredMethodsFromAllInterfaces(final ClassNode cNode, final Map<String, MethodNode> methodsMap) {
        List<ClassNode> cnInterfaces = Arrays.asList(cNode.getInterfaces());
        for (ClassNode sc = cNode.getSuperClass(); sc != null && !isObjectType(sc); sc = sc.getSuperClass()) {
            for (ClassNode i : sc.getInterfaces()) {
                if (!cnInterfaces.contains(i)) {
                    methodsMap.putAll(i.getDeclaredMethodsMap());
                }
            }
        }
    }

    /**
     * Returns true if the given method has a possibly matching static method with the given name and arguments.
     * Handles default arguments and optionally spread expressions.
     *
     * @param cNode     the ClassNode of interest
     * @param name      the name of the method of interest
     * @param arguments the arguments to match against
     * @param trySpread whether to try to account for SpreadExpressions within the arguments
     * @return {@code true} if a matching method was found.
     */
    public static boolean hasPossibleStaticMethod(final ClassNode cNode, final String name, final Expression arguments, final boolean trySpread) {
        int count = 0; boolean foundSpread = false;
        if (arguments instanceof TupleExpression) {
            for (Expression arg : (TupleExpression) arguments) {
                if (arg instanceof SpreadExpression) {
                    foundSpread = true;
                } else {
                    count += 1;
                }
            }
        } else if (arguments instanceof MapExpression) {
            count = 1;
        }

        for (MethodNode method : cNode.getMethods(name)) {
            if (method.isStatic()) {
                Parameter[] parameters = method.getParameters();
                // do fuzzy match for spread case: count will be number of non-spread args
                if (trySpread && foundSpread && parameters.length >= count) return true;

                if (parameters.length == count) return true;

                // handle varargs case
                if (parameters.length > 0 && parameters[parameters.length - 1].getType().isArray()) {
                    if (count >= parameters.length - 1) return true;
                    // fuzzy match any spread to a varargs
                    if (trySpread && foundSpread) return true;
                }

                // handle parameters with default values
                int nonDefaultParameters = 0;
                for (Parameter parameter : parameters) {
                    if (!parameter.hasInitialExpression()) {
                        nonDefaultParameters += 1;
                    }
                }

                if (count < parameters.length && nonDefaultParameters <= count) {
                    return true;
                }
                // TODO: Handle spread with nonDefaultParams?
            }
        }

        // GROOVY-11104: generated method
        if (cNode.isPrimaryClassNode()) {
            for (PropertyNode pNode : cNode.getProperties()) {
                if (pNode.getGetterNameOrDefault().equals(name)) {
                    return pNode.isStatic() && !isPackagePrivate(pNode); // GROOVY-11180
                }
                if (pNode.getSetterNameOrDefault().equals(name)) {
                    return pNode.isStatic() && !pNode.isFinal() && !isPackagePrivate(pNode);
                }
            }
        }

        return false;
    }

    /**
     * Returns true if we have a static accessor
     */
    public static boolean hasPossibleStaticProperty(final ClassNode cNode, final String methodName) {
        // assume explicit static method call checked first so we can assume a simple check here
        if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
            return false;
        }
        String propName = getPropNameForAccessor(methodName);
        PropertyNode pNode = getStaticProperty(cNode, propName);
        return pNode != null && (methodName.startsWith("get") || isPrimitiveBoolean(pNode.getType()));
    }

    /**
     * Returns the property name, e.g. age, given an accessor name, e.g. getAge.
     * Returns the original if a valid prefix cannot be removed.
     *
     * @param accessorName the accessor name of interest, e.g. getAge
     * @return the property name, e.g. age, or original if not a valid property accessor name
     */
    public static String getPropNameForAccessor(final String accessorName) {
        if (!isValidAccessorName(accessorName)) return accessorName;
        int prefixLength = accessorName.startsWith("is") ? 2 : 3;
        return String.valueOf(accessorName.charAt(prefixLength)).toLowerCase() + accessorName.substring(prefixLength + 1);
    }

    /**
     * Detects whether the given accessor name starts with "get", "set" or "is" followed by at least one character.
     *
     * @param accessorName the accessor name of interest, e.g. getAge
     * @return true if a valid prefix is found
     */
    public static boolean isValidAccessorName(final String accessorName) {
        if (accessorName.startsWith("get") || accessorName.startsWith("is") || accessorName.startsWith("set")) {
            int prefixLength = accessorName.startsWith("is") ? 2 : 3;
            return accessorName.length() > prefixLength;
        }
        return false;
    }

    /**
     * Check if the type is declared {@code non-sealed}
     *
     * @param cn the type
     * @return the check result
     */
    public static boolean isNonSealed(final ClassNode cn) {
        if (cn instanceof DecompiledClassNode) {
            Class<?> typeClass;
            try {
                typeClass = cn.getTypeClass();
            } catch (NoClassDefFoundError e) {
                return false;
            }

            final Class<?> superclass = typeClass.getSuperclass();
            if (null == superclass) return false;
            return ReflectionUtils.isSealed(superclass) && !(Modifier.isFinal(typeClass.getModifiers()) || ReflectionUtils.isSealed(typeClass));
        }

        if (Boolean.TRUE.equals(cn.getNodeMetaData(NonSealed.class))) return true;
        final ClassNode superClass = cn.getSuperClass();
        if (null == superClass) return false;
        return superClass.isSealed() && !(isFinal(cn.getModifiers()) || cn.isSealed());
    }

    public static boolean hasStaticProperty(final ClassNode cNode, final String propName) {
        PropertyNode found = getStaticProperty(cNode, propName);
        if (found == null) {
            String otherName = BeanUtils.decapitalize(propName);
            if (!otherName.equals(propName))
                found = getStaticProperty(cNode, otherName);
        }
        return (found != null);
    }

    /**
     * Detects whether a static property with the given name is within the class
     * or a super class.
     *
     * @param cNode the ClassNode of interest
     * @param propName the property name
     * @return the static property if found or else null
     */
    public static PropertyNode getStaticProperty(final ClassNode cNode, final String propName) {
        ClassNode classNode = cNode;
        while (classNode != null) {
            for (PropertyNode pn : classNode.getProperties()) {
                if (pn.getName().equals(propName) && pn.isStatic() && !isPackagePrivate(pn)) {
                    return pn;
                }
            }
            classNode = classNode.getSuperClass();
        }
        return null;
    }

    /**
     * Detects whether a given ClassNode is an inner class (non-static).
     *
     * @param cNode the ClassNode of interest
     * @return true if the given node is a (non-static) inner class, else false
     */
    public static boolean isInnerClass(final ClassNode cNode) {
        return cNode.getOuterClass() != null && !Modifier.isStatic(cNode.getModifiers());
    }

    /**
     * Checks if the source ClassNode is compatible with the target ClassNode
     */
    public static boolean isCompatibleWith(ClassNode source, ClassNode target) {
        if (source.equals(target)) return true;

        if (source.isArray() && target.isArray() && dimension(source) == dimension(target)) {
            source = elementType(source);
            target = elementType(target);
        }

        return !isPrimitiveType(source) && !isPrimitiveType(target)
                && (source.isDerivedFrom(target) || source.implementsInterface(target));
    }

    public static boolean hasNoArgConstructor(final ClassNode cNode) {
        List<ConstructorNode> constructors = cNode.getDeclaredConstructors();
        for (ConstructorNode next : constructors) {
            if (next.getParameters().length == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if an explicit (non-generated) constructor is in the class.
     *
     * @param xform if non-null, add an error if an explicit constructor is found
     * @param cNode the type of the containing class
     * @return true if an explicit (non-generated) constructor was found
     */
    public static boolean hasExplicitConstructor(final AbstractASTTransformation xform, final ClassNode cNode) {
        List<ConstructorNode> declaredConstructors = cNode.getDeclaredConstructors();
        for (ConstructorNode constructorNode : declaredConstructors) {
            // allow constructors added by other transforms if flagged as Generated
            if (AnnotatedNodeUtils.isGenerated(constructorNode)) {
                continue;
            }
            if (xform != null) {
                xform.addError("Error during " + xform.getAnnotationName() +
                        " processing. Explicit constructors not allowed for class: " +
                        cNode.getNameWithoutPackage(), constructorNode);
            }
            return true;
        }
        return false;
    }

    /**
     * Determines if the given ClassNode values have the same package name.
     *
     * @param first a ClassNode
     * @param second a ClassNode
     * @return true if both given nodes have the same package name
     * @throws NullPointerException if either of the given nodes are null
     */
    public static boolean samePackageName(final ClassNode first, final ClassNode second) {
        return Objects.equals(first.getPackageName(), second.getPackageName());
    }

    /**
     * Searches the class for a field that matches specified name.
     *
     * @since 3.0.0
     */
    public static FieldNode getField(final ClassNode classNode, final String fieldName) {
        return getField(classNode, fieldName, fieldNode -> true);
    }

    /**
     * Searches the class for a field that matches specified name and test.
     *
     * @since 4.0.0
     */
    public static FieldNode getField(final ClassNode classNode, final String fieldName, final Predicate<? super FieldNode> acceptability) {
        Queue<ClassNode> todo = new ArrayDeque<>(Collections.singletonList(classNode));
        Set<ClassNode> done = new HashSet<>();
        ClassNode next;

        while ((next = todo.poll()) != null) {
            if (done.add(next)) {
                FieldNode fieldNode = next.getDeclaredField(fieldName);
                if (fieldNode != null && acceptability.test(fieldNode)) {
                    if (fieldNode.isPrivate() && isPackagePrivate(fieldNode)) // GROOVY-11180
                        fieldNode.setModifiers(fieldNode.getModifiers() & ~Modifier.PRIVATE);
                    return fieldNode;
                }
                Collections.addAll(todo, next.getInterfaces());
                ClassNode superType = next.getSuperClass();
                if (superType != null) todo.add(superType);
            }
        }

        return null;
    }

    /**
     * Searches the class for a method that matches specified name and test.
     *
     * @since 5.0.0
     */
    public static MethodNode getMethod(final ClassNode classNode, final String methodName, final Predicate<? super MethodNode> acceptability) {
        for (ClassNode next = classNode; next != null; next = next.getSuperClass()) {
            for (MethodNode methodNode : next.getDeclaredMethods(methodName)) {
                if (acceptability.test(methodNode)) return methodNode;
            }
        }

        if (classNode.isAbstract() && asBoolean(classNode.getInterfaces())) { // GROOVY-11071
            Queue<ClassNode> todo = new ArrayDeque<>(Arrays.asList(classNode.getInterfaces()));
            Set<ClassNode> done = new HashSet<>();
            done.add(classNode);
            ClassNode next;

            while ((next = todo.poll()) != null) {
                if (done.add(next)) {
                    for (MethodNode methodNode : next.getDeclaredMethods(methodName)) {
                        if (acceptability.test(methodNode)) return methodNode;
                    }
                    Collections.addAll(todo, next.getInterfaces());
                }
            }
        }

        return null;
    }

    public static boolean isSubtype(final ClassNode maybeSuperclassOrInterface, final ClassNode candidateChild) {
        return maybeSuperclassOrInterface.isInterface() || candidateChild.isInterface()
                ? isOrImplements(candidateChild, maybeSuperclassOrInterface)
                : candidateChild.isDerivedFrom(maybeSuperclassOrInterface);
    }

    //--------------------------------------------------------------------------

    private static boolean isPackagePrivate(final AnnotatedNode aNode) {
        return aNode.getAnnotations().stream().anyMatch(anno -> anno.getClassNode().getName().equals("groovy.transform.PackageScope"))
            || aNode.getDeclaringClass().getAnnotations().stream().anyMatch(anno -> anno.getClassNode().getName().equals("groovy.transform.PackageScope")
                                                                            && Optional.ofNullable(anno.getMember("value")).filter(expr -> expr.getText().contains("FIELDS")).isPresent());
    }

    private ClassNodeUtils() {}
}
