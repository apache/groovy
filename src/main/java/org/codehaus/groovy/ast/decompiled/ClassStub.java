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
package org.codehaus.groovy.ast.decompiled;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data container holding bytecode-extracted class metadata for efficient decompilation.
 *
 * <p>This class serves as an intermediary representation of class information extracted from compiled
 * bytecode by {@link AsmDecompiler}. It holds raw class attributes (access modifiers, class names,
 * signatures) and collections of method and field stubs that can be lazily reconstructed into
 * full AST nodes by {@link DecompiledClassNode}.
 *
 * <p>ClassStub instances are not AST nodes themselves; they are data containers that facilitate
 * lazy loading of class members. The actual {@link org.codehaus.groovy.ast.ClassNode} representation
 * is created by {@link DecompiledClassNode}, which uses this stub to populate fields and methods
 * on demand via {@link LazyFieldNode}, {@link LazyMethodNode}, and {@link LazyConstructorNode}.
 *
 * <p><strong>Thread Safety:</strong> ClassStub instances are typically constructed once and then
 * treated as immutable. They are safe for concurrent read access after initialization.
 *
 * @see AsmDecompiler#parseClass(java.net.URL)
 * @see DecompiledClassNode#DecompiledClassNode(ClassStub, AsmReferenceResolver)
 */
public class ClassStub extends MemberStub {
    /**
     * The fully qualified class name in standard dot notation (e.g., {@code "java.lang.String"}).
     */
    final String className;

    /**
     * JVM access modifiers for the class (combination of ASM {@link org.objectweb.asm.Opcodes} flags
     * such as {@code ACC_PUBLIC}, {@code ACC_FINAL}, {@code ACC_ABSTRACT}, etc.).
     * For inner classes, see {@link #innerClassModifiers}.
     */
    final int accessModifiers;

    /**
     * Java type signature in JVMS 4.7.9 format (e.g., {@code "&lt;T:Ljava/lang/Object;&gt;..."}),
     * or {@code null} if the class is not generic. Used for generic type parameter resolution.
     *
     * @see org.codehaus.groovy.ast.decompiled.ClassSignatureParser#configureClass(DecompiledClassNode, ClassStub, AsmReferenceResolver)
     */
    final String signature;

    /**
     * Internal class name of the direct superclass in JVMS 4.2 format (e.g., {@code "java/lang/Object"}),
     * or {@code null} if this is the Object class. Converted to dot notation by the resolver.
     */
    final String superName;

    /**
     * Array of internal class names of interfaces implemented by this class in JVMS 4.2 format
     * (e.g., {@code "java/io/Serializable"}), or empty array if no interfaces.
     */
    final String[] interfaceNames;

    /**
     * List of method stubs representing constructors and methods declared by this class.
     * Populated lazily by {@link AsmDecompiler} during bytecode parsing.
     * {@code null} indicates methods not yet parsed.
     */
    List<MethodStub> methods;

    /**
     * List of field stubs representing fields declared by this class.
     * Populated lazily by {@link AsmDecompiler} during bytecode parsing.
     * {@code null} indicates fields not yet parsed.
     */
    List<FieldStub> fields;

    /**
     * List of permitted subclass names for sealed classes (Java 17+).
     * Populated from the {@code PermittedSubclasses} JVMS attribute.
     * Empty if not a sealed class.
     */
    final List<String> permittedSubclasses = new ArrayList<>(1);

    /**
     * List of record component stubs for record classes (Java 16+).
     * Empty if not a record class.
     *
     * @see RecordComponentStub
     */
    final List<RecordComponentStub> recordComponents  = new ArrayList<>(1);

    /**
     * JVM access modifiers extracted from the INNERCLASS bytecode attribute for inner classes.
     * When present ({@code != -1}), these modifiers reflect the true visibility of a nested class
     * and should override {@link #accessModifiers}. The INNERCLASS attribute provides accurate
     * inner class modifiers because the top-level class file access flags do not directly reflect
     * nested class visibility.
     *
     * @see DecompiledClassNode#getModifiers(ClassStub)
     */
    int innerClassModifiers = -1;

    /**
     * Creates a ClassStub from bytecode class metadata.
     *
     * @param className the fully qualified class name (dot notation)
     * @param accessModifiers JVM access modifiers (ASM Opcodes flags)
     * @param signature generic type signature or {@code null}
     * @param superName internal superclass name (JVMS format) or {@code null}
     * @param interfaceNames internal interface names (JVMS format) or empty array
     */
    public ClassStub(String className, int accessModifiers, String signature, String superName, String[] interfaceNames) {
        this.className = className;
        this.accessModifiers = accessModifiers;
        this.signature = signature;
        this.superName = superName;
        this.interfaceNames = interfaceNames;
    }
}

/**
 * Common interface for bytecode stubs that may carry annotations extracted from the class file.
 * Provides access to annotation metadata for classes, methods, fields, and record components.
 */
interface AnnotatedStub {
    /**
     * Returns the list of annotations attached to this class member.
     *
     * @return list of {@link AnnotationStub} objects, or {@code null} if no annotations present
     */
    List<AnnotationStub> getAnnotations();
}

/**
 * Interface for bytecode stubs that may carry type annotations (JSR 308 JVMS 4.7.20, 4.7.21).
 * Type annotations provide fine-grained type information for generic types and complex type expressions.
 */
interface AnnotatedTypeStub {
    /**
     * Returns the list of type annotations attached to this class member.
     * Type annotations apply to specific uses of types (e.g., wildcards, type bounds).
     *
     * @return list of {@link TypeAnnotationStub} objects, or {@code null} if no type annotations present
     */
    List<TypeAnnotationStub> getTypeAnnotations();
}

/**
 * Bytecode stub for a class member carrying annotation metadata.
 * Base class for {@link MethodStub} and {@link FieldStub}.
 */
class MemberStub implements AnnotatedStub {
    /**
     * List of annotations, lazily initialized when the first annotation is added.
     */
    List<AnnotationStub> annotations = null;

    /**
     * Adds an annotation to this member stub.
     *
     * @param desc the annotation descriptor (class name in JVMS format, e.g., {@code "Ljava/lang/Override;"})
     * @return the newly created {@link AnnotationStub}
     */
    AnnotationStub addAnnotation(String desc) {
        AnnotationStub stub = new AnnotationStub(desc);
        if (annotations == null) annotations = new ArrayList<AnnotationStub>(1);
        annotations.add(stub);
        return stub;
    }

    @Override
    public List<AnnotationStub> getAnnotations() {
        return annotations;
    }
}

/**
 * Bytecode stub for a method or constructor extracted from compiled bytecode.
 * Contains access modifiers, descriptors, signatures, and parameter information necessary
 * to reconstruct a {@link org.codehaus.groovy.ast.MethodNode} or {@link org.codehaus.groovy.ast.ConstructorNode}.
 */
class MethodStub extends MemberStub {
    /**
     * Method name. For constructors, this is always {@code "&lt;init&gt;"}.
     */
    final String methodName;

    /**
     * JVM access modifiers (ASM Opcodes flags) for this method.
     */
    final int accessModifiers;

    /**
     * Method descriptor in JVMS 4.3.3 format (e.g., {@code "(Ljava/lang/String;I)Z"}).
     * Encodes parameter and return types for method resolution.
     */
    final String desc;

    /**
     * Generic method signature in JVMS 4.7.9.4 format (e.g., {@code "&lt;T:Ljava/lang/Object;&gt;(TT;)TT;"}),
     * or {@code null} if the method is not generic or has no complex generic types.
     */
    final String signature;

    /**
     * Array of exception type names (JVMS 4.2 format) declared in the method's {@code throws} clause,
     * or empty array if no exceptions declared.
     */
    final String[] exceptions;

    /**
     * Map from parameter index to list of annotations for that parameter parameter index 0 = first parameter).
     * Lazily initialized when parameter annotations are encountered.
     */
    Map<Integer, List<AnnotationStub>> parameterAnnotations;

    /**
     * List of parameter names extracted from the MethodParameters bytecode attribute (Java 8+),
     * or {@code null} if no parameter names are available from the class file.
     * Used to preserve original parameter names from compiled code.
     */
    List<String> parameterNames;

    /**
     * Annotation default value for annotation method members (non-null only for methods in {@code @interface} declarations).
     */
    Object annotationDefault;

    /**
     * Creates a method stub from bytecode method metadata.
     *
     * @param methodName the method name (or {@code "&lt;init&gt;"} for constructors)
     * @param accessModifiers JVM access modifiers
     * @param desc method descriptor (JVMS 4.3.3 format)
     * @param signature generic signature or {@code null}
     * @param exceptions array of exception type names or empty array
     */
    public MethodStub(String methodName, int accessModifiers, String desc, String signature, String[] exceptions) {
        this.methodName = methodName;
        this.accessModifiers = accessModifiers;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = exceptions;
    }
}

/**
 * Bytecode stub for a field extracted from compiled bytecode.
 * Contains type information and modifiers necessary to reconstruct a {@link org.codehaus.groovy.ast.FieldNode}.
 */
class FieldStub extends MemberStub {
    /**
     * Field name as declared in the source.
     */
    final String fieldName;

    /**
     * JVM access modifiers (ASM Opcodes flags) for this field.
     */
    final int accessModifiers;

    /**
     * Field type descriptor in JVMS 4.3.2 format (e.g., {@code "Ljava/lang/String;"}, {@code "I"}, {@code "[I"}).
     */
    final String desc;

    /**
     * Generic field signature in JVMS 4.7.9.1 format (e.g., {@code "Ljava/util/List&lt;Ljava/lang/String;&gt;;"}),
     * or {@code null} if the field type is not generic.
     */
    final String signature;

    /**
     * Static initial value for this field (if it is a compile-time constant), or {@code null}.
     * Possible types: {@link String}, {@link Number}, {@link Long}, {@link Double}, {@link Float}, {@link Boolean}.
     */
    final Object value;

    /**
     * Creates a field stub with no initial value.
     *
     * @param fieldName the field name
     * @param accessModifiers JVM access modifiers
     * @param desc field type descriptor (JVMS format)
     * @param signature generic signature or {@code null}
     */
    public FieldStub(String fieldName, int accessModifiers, String desc, String signature) {
        this(fieldName, accessModifiers, desc, signature, null);
    }

    /**
     * Creates a field stub with an optional static initial value.
     *
     * @param fieldName the field name
     * @param accessModifiers JVM access modifiers
     * @param desc field type descriptor (JVMS format)
     * @param signature generic signature or {@code null}
     * @param value compile-time constant value or {@code null}
     */
    public FieldStub(String fieldName, int accessModifiers, String desc, String signature, Object value) {
        this.fieldName = fieldName;
        this.accessModifiers = accessModifiers;
        this.desc = desc;
        this.signature = signature;
        this.value = value;
    }
}

/**
 * Bytecode stub representing an annotation extracted from a class file.
 * Holds the annotation type and its member key-value pairs.
 */
class AnnotationStub {
    /**
     * Fully qualified annotation type name (class name in dot notation).
     */
    final String className;

    /**
     * Map of annotation member names to their values.
     * Values may be primitives, Strings, enums (wrapped in {@link EnumConstantWrapper}),
     * nested annotations (nested {@link AnnotationStub}), or arrays thereof.
     */
    final Map<String, Object> members = new LinkedHashMap<>();

    /**
     * Creates an annotation stub from its type.
     *
     * @param className the fully qualified annotation class name
     */
    public AnnotationStub(String className) {
        this.className = className;
    }
}

/**
 * Bytecode stub representing a type annotation (JSR 308, JVMS 4.7.20, 4.7.21).
 * Type annotations provide precise location information for annotations on type uses
 * (e.g., annotations on wildcards, type bounds, cast expressions).
 */
class TypeAnnotationStub extends AnnotationStub {
    /**
     * Creates a type annotation stub from its type.
     *
     * @param className the fully qualified annotation class name
     */
    public TypeAnnotationStub(String className) {
        super(className);
    }
}

/**
 * Wrapper for a type used as an annotation member value.
 * Stores a type reference that could not be fully resolved at annotation parsing time.
 */
class TypeWrapper {
    /**
     * Type descriptor in JVMS 4.3.2 format (e.g., {@code "Ljava/lang/String;"}).
     */
    final String desc;

    /**
     * Creates a type wrapper for a descriptor.
     *
     * @param desc the type descriptor
     */
    public TypeWrapper(String desc) {
        this.desc = desc;
    }
}

/**
 * Wrapper for an enumeration constant used as an annotation member value.
 * Stores both the enum type and the specific constant name.
 */
class EnumConstantWrapper {
    /**
     * Enumeration type descriptor in JVMS 4.3.2 format (e.g., {@code "Ljava/lang/annotation/RetentionPolicy;"}).
     */
    final String enumDesc;

    /**
     * Name of the enum constant (e.g., {@code "SOURCE"}, {@code "RUNTIME"}).
     */
    final String constant;

    /**
     * Creates a wrapper for an enum constant value.
     *
     * @param enumDesc the enumeration type descriptor
     * @param constant the constant name
     */
    public EnumConstantWrapper(String enumDesc, String constant) {
        this.enumDesc = enumDesc;
        this.constant = constant;
    }
}

/**
 * Bytecode stub representing a record component in a Java record class (Java 16+).
 * Record components define the fields and accessor methods that comprise the record's value.
 *
 * @see RecordComponentNode
 */
class RecordComponentStub implements AnnotatedStub, AnnotatedTypeStub {
    /**
     * Component name, corresponding to both the field and the accessor method name.
     */
    final String name;

    /**
     * Component type descriptor in JVMS 4.3.2 format (e.g., {@code "Ljava/lang/String;"}).
     */
    final String descriptor;

    /**
     * Generic component signature (JVMS 4.7.9.1 format) or {@code null} if not generic.
     * Used to reconstruct generic type information for the component.
     */
    final String signature;

    /**
     * List of annotations attached to this record component, lazily initialized.
     */
    List<AnnotationStub> annotations;

    /**
     * List of type annotations (JSR 308) attached to this record component, lazily initialized.
     */
    List<TypeAnnotationStub> typeAnnotations;

    /**
     * Creates a record component stub from bytecode metadata.
     *
     * @param name the component name
     * @param descriptor the type descriptor (JVMS format)
     * @param signature generic signature or {@code null}
     */
    public RecordComponentStub(String name, String descriptor, String signature) {
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
    }

    /**
     * Adds an annotation to this record component.
     *
     * @param desc the annotation descriptor (JVMS format)
     * @return the newly created {@link AnnotationStub}
     */
    AnnotationStub addAnnotation(String desc) {
        AnnotationStub stub = new AnnotationStub(desc);
        if (annotations == null) annotations = new ArrayList<AnnotationStub>(1);
        annotations.add(stub);
        return stub;
    }

    @Override
    public List<AnnotationStub> getAnnotations() {
        return annotations;
    }

    /**
     * Adds a type annotation to this record component.
     *
     * @param desc the annotation descriptor (JVMS format)
     * @return the newly created {@link TypeAnnotationStub}
     */
    public TypeAnnotationStub addTypeAnnotation(String desc) {
        TypeAnnotationStub stub = new TypeAnnotationStub(desc);
        if (typeAnnotations == null) typeAnnotations = new ArrayList<TypeAnnotationStub>(1);
        typeAnnotations.add(stub);
        return stub;
    }

    @Override
    public List<TypeAnnotationStub> getTypeAnnotations() {
        return typeAnnotations;
    }
}
