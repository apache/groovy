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
package org.apache.groovy.parser.antlr4;

import org.apache.groovy.util.Maps;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModifierNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.groovy.parser.antlr4.GroovyLangParser.ABSTRACT;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.FINAL;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.NATIVE;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.STATIC;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VOLATILE;

/**
 * Process modifiers for AST nodes
 */
class ModifierManager {
    private static final Map<Class, List<Integer>> INVALID_MODIFIERS_MAP = Maps.of(
            ConstructorNode.class, Arrays.asList(STATIC, FINAL, ABSTRACT, NATIVE),
            MethodNode.class, Arrays.asList(VOLATILE/*, TRANSIENT*/) // Transient is left open for properties for legacy reasons but should be removed before ClassCompletionVerifier runs (CLASSGEN)
    );
    private AstBuilder astBuilder;
    private List<ModifierNode> modifierNodeList;

    /**
     * Checks {@code modifierNodeList} and keeps an unmodifiable copy.
     * A modifier that is not repeatable may occur once, and the list may
     * contain only one visibility modifier.
     *
     * @param astBuilder builds the parsing failure raised when the list is illegal
     * @param modifierNodeList modifiers and annotations for one declaration, in source order
     * @throws org.codehaus.groovy.control.CompilationFailedException when a non-repeatable modifier is repeated,
     *         or a second visibility modifier appears
     */
    public ModifierManager(AstBuilder astBuilder, List<ModifierNode> modifierNodeList) {
        this.astBuilder = astBuilder;
        this.validate(modifierNodeList);
        this.modifierNodeList = Collections.unmodifiableList(modifierNodeList);
    }

    /**
     * Returns how many modifier nodes are held, including annotations
     * stored as modifiers.
     *
     * @return the number of modifier nodes
     */
    public int getModifierCount() {
        return modifierNodeList.size();
    }

    private void validate(List<ModifierNode> modifierNodeList) {
        Map<ModifierNode, Integer> modifierNodeCounter = new LinkedHashMap<>(modifierNodeList.size());
        int visibilityModifierCount = 0;

        for (ModifierNode modifierNode : modifierNodeList) {
            var count = modifierNodeCounter.get(modifierNode);
            if (count == null) {
                modifierNodeCounter.put(modifierNode, 1);
            } else if (count == 1 && !modifierNode.isRepeatable()) {
                throw astBuilder.createParsingFailedException("Cannot repeat modifier '" + modifierNode.getText() + "'", modifierNode);
            }

            if (modifierNode.isVisibilityModifier()) {
                visibilityModifierCount += 1;
                if (visibilityModifierCount > 1) {
                    throw astBuilder.createParsingFailedException("Cannot specify modifier '" + modifierNode.getText() + "' when the access scope has already been defined", modifierNode);
                }
            }
        }
    }

    /**
     * Rejects a held modifier that is illegal on a method.
     * {@code volatile} is rejected. {@code transient} is allowed here
     * and is removed later for properties.
     *
     * @param methodNode method the failure is reported against
     * @throws org.codehaus.groovy.control.CompilationFailedException when a held modifier is not allowed on a method
     */
    public void validate(MethodNode methodNode) {
        validate(INVALID_MODIFIERS_MAP.get(MethodNode.class), methodNode);
    }

    /**
     * Rejects a held modifier that is illegal on a constructor:
     * {@code static}, {@code final}, {@code abstract}, or {@code native}.
     *
     * @param constructorNode constructor the failure is reported against
     * @throws org.codehaus.groovy.control.CompilationFailedException when a held modifier is not allowed on a constructor
     */
    public void validate(ConstructorNode constructorNode) {
        validate(INVALID_MODIFIERS_MAP.get(ConstructorNode.class), constructorNode);
    }

    private void validate(List<Integer> invalidModifierList, MethodNode methodNode) {
        modifierNodeList.forEach(e -> {
            if (invalidModifierList.contains(e.getType())) {
                throw astBuilder.createParsingFailedException("Modifier '" + e + "' is not allowed on a " + methodNode.getClass().getSimpleName().replace("Node", "").toLowerCase(Locale.ROOT), methodNode);
            }
        });
    }

    // t    1: class modifiers value; 2: class member modifiers value
    private int calcModifiersOpValue(int t) {
        int result = 0;

        for (ModifierNode modifierNode : modifierNodeList) {
            result |= modifierNode.getOpcode();
        }

        if (!this.containsVisibilityModifier()) {
            if (1 == t) {
                result |= Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PUBLIC;
            } else if (2 == t) {
                result |= Opcodes.ACC_PUBLIC;
            }
        }

        return result;
    }

    /**
     * Combines the held modifier opcodes into a class access mask.
     * When no visibility modifier is present, the public and synthetic
     * flags are set as well.
     *
     * @return the ASM access flags for the class being declared
     */
    public int getClassModifiersOpValue() {
        return this.calcModifiersOpValue(1);
    }

    /**
     * Combines the held modifier opcodes into a member access mask.
     * When no visibility modifier is present, the public flag is set.
     * The synthetic flag added by {@link #getClassModifiersOpValue()}
     * is not set for members.
     *
     * @return the ASM access flags for a field, method, or constructor
     */
    public int getClassMemberModifiersOpValue() {
        return this.calcModifiersOpValue(2);
    }

    /**
     * Collects annotations stored among the modifiers, in source order.
     * Keyword modifiers are omitted.
     *
     * @return the annotation nodes from this list
     */
    public List<AnnotationNode> getAnnotations() {
        return modifierNodeList.stream()
                .filter(ModifierNode::isAnnotation)
                .map(ModifierNode::getAnnotationNode)
                .collect(Collectors.toList());
    }

    /**
     * Reports whether any held modifier has a parser token type among
     * {@code modifierTypes}.
     *
     * @param modifierTypes parser token types to look for
     * @return {@code true} when at least one held modifier has one of those types
     */
    public boolean containsAny(final int... modifierTypes) {
        return modifierNodeList.stream().anyMatch(e -> {
            for (int modifierType : modifierTypes) {
                if (modifierType == e.getType()) {
                    return true;
                }
            }

            return false;
        });
    }

    /**
     * Finds the first held modifier whose parser token type is {@code modifierType}.
     *
     * @param modifierType parser token type to look for
     * @return that modifier, or empty when none matches
     */
    public Optional<ModifierNode> get(int modifierType) {
        return modifierNodeList.stream().filter(e -> modifierType == e.getType()).findFirst();
    }

    /**
     * Reports whether an annotation is present in the modifier list.
     *
     * @return {@code true} when any modifier node wraps an annotation
     */
    public boolean containsAnnotations() {
        return modifierNodeList.stream().anyMatch(ModifierNode::isAnnotation);
    }

    /**
     * Reports whether {@code public}, {@code protected}, or {@code private}
     * is present.
     *
     * @return {@code true} when a visibility modifier is present
     */
    public boolean containsVisibilityModifier() {
        return modifierNodeList.stream().anyMatch(ModifierNode::isVisibilityModifier);
    }

    /**
     * Reports whether a keyword modifier other than visibility is present,
     * such as {@code static} or {@code final}. Annotations and {@code def}
     * are not counted.
     *
     * @return {@code true} when such a modifier is present
     */
    public boolean containsNonVisibilityModifier() {
        return modifierNodeList.stream().anyMatch(ModifierNode::isNonVisibilityModifier);
    }

    /**
     * Applies every held modifier to {@code parameter}. Each opcode is
     * OR-ed into the parameter modifiers, and each annotation is attached.
     *
     * @param parameter parameter to update
     * @return the same parameter, with modifiers and annotations applied
     */
    public Parameter processParameter(Parameter parameter) {
        modifierNodeList.forEach(e -> {
            parameter.setModifiers(parameter.getModifiers() | e.getOpcode());

            if (e.isAnnotation()) {
                parameter.addAnnotation(e.getAnnotationNode());
            }
        });

        return parameter;
    }

    /**
     * Clears the public, protected, and private bits of {@code modifiers}.
     * Every other access flag is left as it was.
     *
     * @param modifiers access mask to update
     * @return {@code modifiers} with the visibility bits removed
     */
    public int clearVisibilityModifiers(int modifiers) {
        return modifiers & ~Opcodes.ACC_PUBLIC & ~Opcodes.ACC_PROTECTED & ~Opcodes.ACC_PRIVATE;
    }

    /**
     * Applies every held modifier to {@code mn}. A visibility modifier
     * replaces any visibility already recorded on the method; other
     * opcodes are OR-ed in. Annotations are attached to the method.
     *
     * @param mn method to update
     * @return the same method, with modifiers and annotations applied
     */
    public MethodNode processMethodNode(MethodNode mn) {
        modifierNodeList.forEach(e -> {
            mn.setModifiers((e.isVisibilityModifier() ? clearVisibilityModifiers(mn.getModifiers()) : mn.getModifiers()) | e.getOpcode());

            if (e.isAnnotation()) {
                mn.addAnnotation(e.getAnnotationNode());
            }
        });

        return mn;
    }

    /**
     * OR-s every held modifier opcode into {@code ve}. Annotations are
     * not attached, because a local variable does not carry them.
     *
     * @param ve variable expression to update
     * @return the same expression, with modifier opcodes applied
     */
    public VariableExpression processVariableExpression(VariableExpression ve) {
        modifierNodeList.forEach(e -> {
            ve.setModifiers(ve.getModifiers() | e.getOpcode());

            // local variable does not attach annotations
        });

        return ve;
    }

    /**
     * Adds every annotation from the modifier list to {@code node}.
     * Keyword modifiers are not applied to the node.
     *
     * @param <T> the annotated node type
     * @param node node that receives the annotations
     * @return the same node, with those annotations added
     */
    public <T extends AnnotatedNode> T attachAnnotations(T node) {
        this.getAnnotations().forEach(node::addAnnotation);

        return node;
    }
}
