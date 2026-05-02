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

import org.apache.groovy.util.Maps;
import org.objectweb.asm.Opcodes;

import java.util.Map;
import java.util.Objects;

import static org.apache.groovy.parser.antlr4.GroovyParser.ABSTRACT;
import static org.apache.groovy.parser.antlr4.GroovyParser.DEF;
import static org.apache.groovy.parser.antlr4.GroovyParser.DEFAULT;
import static org.apache.groovy.parser.antlr4.GroovyParser.FINAL;
import static org.apache.groovy.parser.antlr4.GroovyParser.NATIVE;
import static org.apache.groovy.parser.antlr4.GroovyParser.NON_SEALED;
import static org.apache.groovy.parser.antlr4.GroovyParser.PRIVATE;
import static org.apache.groovy.parser.antlr4.GroovyParser.PROTECTED;
import static org.apache.groovy.parser.antlr4.GroovyParser.PUBLIC;
import static org.apache.groovy.parser.antlr4.GroovyParser.SEALED;
import static org.apache.groovy.parser.antlr4.GroovyParser.STATIC;
import static org.apache.groovy.parser.antlr4.GroovyParser.STRICTFP;
import static org.apache.groovy.parser.antlr4.GroovyParser.SYNCHRONIZED;
import static org.apache.groovy.parser.antlr4.GroovyParser.TRANSIENT;
import static org.apache.groovy.parser.antlr4.GroovyParser.VAL;
import static org.apache.groovy.parser.antlr4.GroovyParser.VAR;
import static org.apache.groovy.parser.antlr4.GroovyParser.VOLATILE;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;

/**
 * Represents a modifier keyword or annotation in Groovy source code.
 * ModifierNode wraps language modifiers (public, private, static, final, etc.)
 * and treats annotations as pseudo-modifiers for consistent AST handling.
 * Each modifier maps to an ASM opcode for bytecode generation.
 */
public class ModifierNode extends ASTNode {
    private Integer type;
    private Integer opcode; // ASM opcode
    private String text;
    private AnnotationNode annotationNode;
    private boolean repeatable;

    /**
     * Pseudo-modifier type constant for annotations. Annotations are treated as
     * modifiers in the AST for unified processing, distinguished by this sentinel type.
     */
    public static final int ANNOTATION_TYPE = -999;

    /**
     * Maps modifier types (parser token types) to their corresponding ASM opcodes.
     * Enables conversion from source-level modifiers to bytecode-level access flags.
     * Some modifiers (SEALED, NON_SEALED, DEFAULT, DEF, VAL, VAR) have no direct
     * bytecode equivalent and map to 0.
     */
    public static final Map<Integer, Integer> MODIFIER_OPCODE_MAP = Maps.of(
            ANNOTATION_TYPE, 0,
            DEF, 0,
            VAL, Opcodes.ACC_FINAL,
            VAR, 0,

            NATIVE, Opcodes.ACC_NATIVE,
            SYNCHRONIZED, Opcodes.ACC_SYNCHRONIZED,
            TRANSIENT, Opcodes.ACC_TRANSIENT,
            VOLATILE, Opcodes.ACC_VOLATILE,

            PUBLIC, Opcodes.ACC_PUBLIC,
            PROTECTED, Opcodes.ACC_PROTECTED,
            PRIVATE, Opcodes.ACC_PRIVATE,
            STATIC, Opcodes.ACC_STATIC,
            ABSTRACT, Opcodes.ACC_ABSTRACT,
            SEALED, 0,
            NON_SEALED, 0,
            FINAL, Opcodes.ACC_FINAL,
            STRICTFP, Opcodes.ACC_STRICT,
            DEFAULT, 0 // no flag for specifying a default method in the JVM spec, hence no ACC_DEFAULT flag in ASM
    );

    /**
     * Creates a modifier node for the specified modifier type.
     * The type corresponds to a parser token type and is mapped to an ASM opcode
     * via {@link #MODIFIER_OPCODE_MAP}.
     *
     * @param type the modifier type (parser token type)
     * @throws IllegalArgumentException if the type has no valid ASM opcode mapping
     */
    public ModifierNode(Integer type) {
        this.type = type;
        this.opcode = MODIFIER_OPCODE_MAP.get(type);
        this.repeatable = ANNOTATION_TYPE == type; // Only annotations are repeatable

        if (!asBoolean((Object) this.opcode)) {
            throw new IllegalArgumentException("Unsupported modifier type: " + type);
        }
    }

    /**
     * Creates a modifier node with a specific text representation.
     * Useful for preserving the exact source text of the modifier.
     *
     * @param type the modifier type (parser token type)
     * @param text text of the ast node (source representation)
     * @throws IllegalArgumentException if the type has no valid ASM opcode mapping
     */
    public ModifierNode(Integer type, String text) {
        this(type);
        this.text = text;
    }

    /**
     * Creates a modifier node wrapping an annotation.
     * Treats annotations as pseudo-modifiers for unified AST processing.
     *
     * @param annotationNode the annotation node to wrap
     * @param text text of the ast node (source representation)
     * @throws IllegalArgumentException if annotationNode is null
     */
    public ModifierNode(AnnotationNode annotationNode, String text) {
        this(ModifierNode.ANNOTATION_TYPE, text);
        this.annotationNode = annotationNode;

        if (!asBoolean(annotationNode)) {
            throw new IllegalArgumentException("annotationNode can not be null");
        }
    }

    /**
     * Checks whether this node represents a true modifier (not annotation or def).
     * Distinguishes real modifiers (public, static, final, etc.) from pseudo-modifiers
     * (annotations, def, val, var) that are treated as modifiers in the AST.
     *
     * @return true if this is a real modifier, false for annotations or def declarations
     */
    public boolean isModifier() {
        return !this.isAnnotation() && !this.isDef();
    }

    /**
     * Checks whether this modifier controls visibility (public, protected, private).
     * Visibility modifiers are mutually exclusive and determine class/member access scope.
     *
     * @return true if this is a visibility modifier
     */
    public boolean isVisibilityModifier() {
        return Objects.equals(PUBLIC, this.type)
                || Objects.equals(PROTECTED, this.type)
                || Objects.equals(PRIVATE, this.type);
    }

    /**
     * Checks whether this is a non-visibility modifier (static, final, abstract, etc.).
     * These modifiers can coexist with visibility modifiers.
     *
     * @return true if this is a non-visibility modifier
     */
    public boolean isNonVisibilityModifier() {
        return this.isModifier() && !this.isVisibilityModifier();
    }

    /**
     * Checks whether this node represents an annotation (pseudo-modifier).
     *
     * @return true if this node wraps an annotation
     */
    public boolean isAnnotation() {
        return Objects.equals(ANNOTATION_TYPE, this.type);
    }

    /**
     * Checks whether this node represents a property or variable declaration modifier
     * (def, val, or var). These are Groovy-specific pseudo-modifiers.
     *
     * @return true if this is a def/val/var declaration modifier
     */
    public boolean isDef() {
        return Objects.equals(DEF, this.type) || Objects.equals(VAL, this.type) || Objects.equals(VAR, this.type);
    }

    /**
     * Checks whether this node specifically represents the 'val' modifier.
     * In Groovy, 'val' declares a final variable (shorthand for final var).
     *
     * @return true if this is the 'val' modifier
     *
     * @since 6.0.0
     */
    public boolean isVal() {
        return Objects.equals(VAL, this.type);
    }

    /**
     * Returns the modifier type constant (parser token type).
     *
     * @return the type identifier for this modifier
     */
    public Integer getType() {
        return type;
    }

    /**
     * Returns the ASM opcode corresponding to this modifier.
     * Used during bytecode generation to set appropriate access flags.
     * Returns 0 for modifiers without direct bytecode representation.
     *
     * @return the ASM Opcodes constant for this modifier
     */
    public Integer getOpcode() {
        return opcode;
    }

    /**
     * Checks whether this modifier supports repetition (stacking).
     * Only annotations are repeatable in Groovy; modifiers are mutually exclusive.
     *
     * @return true if this modifier can appear multiple times (only for annotations)
     */
    public boolean isRepeatable() {
        return repeatable;
    }

    /**
     * Returns the source text representation of this modifier.
     * Preserves the exact text as it appeared in the source code.
     *
     * @return the source text of this modifier, or null if not set
     */
    @Override
    public String getText() {
        return text;
    }

    /**
     * Returns the wrapped annotation node if this modifier represents an annotation.
     * Only non-null for pseudo-modifiers created via {@link #ModifierNode(AnnotationNode, String)}.
     *
     * @return the {@link AnnotationNode} wrapped by this modifier, or null if this is a true modifier
     */
    public AnnotationNode getAnnotationNode() {
        return annotationNode;
    }

    /**
     * Compares this modifier with another object for equality.
     * Two modifiers are equal if they have the same type, text representation, and wrapped annotation.
     * Used for deduplicating modifier nodes during AST processing.
     *
     * @param o the object to compare
     * @return true if both modifiers represent the same language construct
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModifierNode that = (ModifierNode) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(text, that.text) &&
                Objects.equals(annotationNode, that.annotationNode);
    }

    /**
     * Returns the hash code for this modifier.
     * Consistent with equals(): identical modifiers have identical hash codes.
     * Used for efficient storage in hash-based collections.
     *
     * @return hash code based on type, text, and annotation
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, text, annotationNode);
    }

    /**
     * Returns the string representation of this modifier.
     * Returns the source text if available, otherwise delegates to {@link Object#toString()}.
     *
     * @return the source text of this modifier
     */
    @Override
    public String toString() {
        return this.text;
    }
}
