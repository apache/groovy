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
import static org.apache.groovy.parser.antlr4.GroovyParser.PRIVATE;
import static org.apache.groovy.parser.antlr4.GroovyParser.PROTECTED;
import static org.apache.groovy.parser.antlr4.GroovyParser.PUBLIC;
import static org.apache.groovy.parser.antlr4.GroovyParser.STATIC;
import static org.apache.groovy.parser.antlr4.GroovyParser.STRICTFP;
import static org.apache.groovy.parser.antlr4.GroovyParser.SYNCHRONIZED;
import static org.apache.groovy.parser.antlr4.GroovyParser.TRANSIENT;
import static org.apache.groovy.parser.antlr4.GroovyParser.VAR;
import static org.apache.groovy.parser.antlr4.GroovyParser.VAL;
import static org.apache.groovy.parser.antlr4.GroovyParser.LET;
import static org.apache.groovy.parser.antlr4.GroovyParser.VOLATILE;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asBoolean;

/**
 * Represents a modifier
 * <p>
 * Created by Daniel.Sun on 2016/08/23.
 */
public class ModifierNode extends ASTNode {
    private Integer type;
    private Integer opcode; // ASM opcode
    private String text;
    private AnnotationNode annotationNode;
    private boolean repeatable;

    public static final int ANNOTATION_TYPE = -999;
    public static final Map<Integer, Integer> MODIFIER_OPCODE_MAP = Maps.of(
            ANNOTATION_TYPE, 0,
            DEF, 0,
            VAR, 0,
            VAL, Opcodes.ACC_FINAL, // -> final var
            LET, Opcodes.ACC_FINAL, // -> final var

            NATIVE, Opcodes.ACC_NATIVE,
            SYNCHRONIZED, Opcodes.ACC_SYNCHRONIZED,
            TRANSIENT, Opcodes.ACC_TRANSIENT,
            VOLATILE, Opcodes.ACC_VOLATILE,

            PUBLIC, Opcodes.ACC_PUBLIC,
            PROTECTED, Opcodes.ACC_PROTECTED,
            PRIVATE, Opcodes.ACC_PRIVATE,
            STATIC, Opcodes.ACC_STATIC,
            ABSTRACT, Opcodes.ACC_ABSTRACT,
            FINAL, Opcodes.ACC_FINAL,
            STRICTFP, Opcodes.ACC_STRICT,
            DEFAULT, 0 // no flag for specifying a default method in the JVM spec, hence no ACC_DEFAULT flag in ASM
    );

    public ModifierNode(Integer type) {
        this.type = type;
        this.opcode = MODIFIER_OPCODE_MAP.get(type);
        this.repeatable = ANNOTATION_TYPE == type; // Only annotations are repeatable

        if (!asBoolean((Object) this.opcode)) {
            throw new IllegalArgumentException("Unsupported modifier type: " + type);
        }
    }

    /**
     * @param type the modifier type, which is same as the token type
     * @param text text of the ast node
     */
    public ModifierNode(Integer type, String text) {
        this(type);
        this.text = text;
    }

    /**
     * @param annotationNode the annotation node
     * @param text           text of the ast node
     */
    public ModifierNode(AnnotationNode annotationNode, String text) {
        this(ModifierNode.ANNOTATION_TYPE, text);
        this.annotationNode = annotationNode;

        if (!asBoolean(annotationNode)) {
            throw new IllegalArgumentException("annotationNode can not be null");
        }
    }

    /**
     * Check whether the modifier is not an imagined modifier(annotation, def)
     */
    public boolean isModifier() {
        return !this.isAnnotation() && !this.isDef();
    }

    public boolean isVisibilityModifier() {
        return Objects.equals(PUBLIC, this.type)
                || Objects.equals(PROTECTED, this.type)
                || Objects.equals(PRIVATE, this.type);
    }

    public boolean isNonVisibilityModifier() {
        return this.isModifier() && !this.isVisibilityModifier();
    }

    public boolean isAnnotation() {
        return Objects.equals(ANNOTATION_TYPE, this.type);
    }

    public boolean isDef() {
        return Objects.equals(DEF, this.type) ||
        		Objects.equals(VAR, this.type) ||
        		
        		// includes "final" modifier
        		Objects.equals(VAL, this.type) ||
        		Objects.equals(LET, this.type);
    }

    public Integer getType() {
        return type;
    }

    public Integer getOpcode() {
        return opcode;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    @Override
    public String getText() {
        return text;
    }

    public AnnotationNode getAnnotationNode() {
        return annotationNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModifierNode that = (ModifierNode) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(text, that.text) &&
                Objects.equals(annotationNode, that.annotationNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, text, annotationNode);
    }

    @Override
    public String toString() {
        return this.text;
    }
}
