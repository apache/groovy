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
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.parser.antlr4.GroovyLangParser.ABSTRACT;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.FINAL;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.NATIVE;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.STATIC;
import static org.apache.groovy.parser.antlr4.GroovyLangParser.VOLATILE;

/**
 * Process modifiers for AST nodes
 * <p>
 * Created by Daniel.Sun on 2016/08/27.
 */
class ModifierManager {
    private static final Map<Class, List<Integer>> INVALID_MODIFIERS_MAP = Maps.<Class, List<Integer>>of(
            ConstructorNode.class, Arrays.asList(STATIC, FINAL, ABSTRACT, NATIVE),
            MethodNode.class, Arrays.asList(VOLATILE/*, TRANSIENT*/)
    );
    private AstBuilder astBuilder;
    private List<ModifierNode> modifierNodeList;

    public ModifierManager(AstBuilder astBuilder, List<ModifierNode> modifierNodeList) {
        this.astBuilder = astBuilder;
        this.validate(modifierNodeList);
        this.modifierNodeList = Collections.unmodifiableList(modifierNodeList);
    }

    public int getModifierCount() {
        return modifierNodeList.size();
    }

    private void validate(List<ModifierNode> modifierNodeList) {
        Map<ModifierNode, Integer> modifierNodeCounter = new LinkedHashMap<>(modifierNodeList.size());
        int visibilityModifierCnt = 0;

        for (ModifierNode modifierNode : modifierNodeList) {
            Integer cnt = modifierNodeCounter.get(modifierNode);

            if (null == cnt) {
                modifierNodeCounter.put(modifierNode, 1);
            } else if (1 == cnt && !modifierNode.isRepeatable()) {
                throw astBuilder.createParsingFailedException("Cannot repeat modifier[" + modifierNode.getText() + "]", modifierNode);
            }

            if (modifierNode.isVisibilityModifier()) {
                visibilityModifierCnt++;

                if (visibilityModifierCnt > 1) {
                    throw astBuilder.createParsingFailedException("Cannot specify modifier[" + modifierNode.getText() + "] when access scope has already been defined", modifierNode);
                }
            }
        }
    }

    public void validate(MethodNode methodNode) {
        validate(INVALID_MODIFIERS_MAP.get(MethodNode.class), methodNode);
    }

    public void validate(ConstructorNode constructorNode) {
        validate(INVALID_MODIFIERS_MAP.get(ConstructorNode.class), constructorNode);
    }

    private void validate(List<Integer> invalidModifierList, MethodNode methodNode) {
        for (ModifierNode e : modifierNodeList) {
            if (invalidModifierList.contains(e.getType())) {
                throw astBuilder.createParsingFailedException(methodNode.getClass().getSimpleName().replace("Node", "") + " has an incorrect modifier '" + e + "'.", methodNode);
            }
        }
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

    public int getClassModifiersOpValue() {
        return this.calcModifiersOpValue(1);
    }

    public int getClassMemberModifiersOpValue() {
        return this.calcModifiersOpValue(2);
    }

    public List<AnnotationNode> getAnnotations() {
        List<AnnotationNode> result = new ArrayList<AnnotationNode>();
        for (ModifierNode m : modifierNodeList) {
            if (m.isAnnotation()) {
                result.add(m.getAnnotationNode());
            }
        }
        return result;
    }

    public boolean containsAny(int... modifierTypes) {
        for (ModifierNode e : modifierNodeList) {
            for (int modifierType : modifierTypes) {
                if (modifierType == e.getType()) {
                    return true;
                }
            }
        }
        return false;
    }

    public ModifierNode get(int modifierType) {
        for (ModifierNode e : modifierNodeList) {
            if (modifierType == e.getType()) {
                return e;
            }
        }
        return null;
    }

    public boolean containsAnnotations() {
        for (ModifierNode e : modifierNodeList) {
            if (e.isAnnotation()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsVisibilityModifier() {
        for (ModifierNode e : modifierNodeList) {
            if (e.isVisibilityModifier()) {
                return true;
            }
        }
        return false;
    }

    public boolean containsNonVisibilityModifier() {
        for (ModifierNode e : modifierNodeList) {
            if (e.isNonVisibilityModifier()) {
                return true;
            }
        }
        return false;
    }

    public Parameter processParameter(Parameter parameter) {
        for (ModifierNode e : modifierNodeList) {
            parameter.setModifiers(parameter.getModifiers() | e.getOpcode());

            if (e.isAnnotation()) {
                parameter.addAnnotation(e.getAnnotationNode());
            }
        };

        return parameter;
    }

    public int clearVisibilityModifiers(int modifiers) {
        return modifiers & ~Opcodes.ACC_PUBLIC & ~Opcodes.ACC_PROTECTED & ~Opcodes.ACC_PRIVATE;
    }

    public MethodNode processMethodNode(MethodNode mn) {
        for (ModifierNode e : modifierNodeList) {
            mn.setModifiers((e.isVisibilityModifier() ? clearVisibilityModifiers(mn.getModifiers()) : mn.getModifiers()) | e.getOpcode());

            if (e.isAnnotation()) {
                mn.addAnnotation(e.getAnnotationNode());
            }
        };

        return mn;
    }

    public VariableExpression processVariableExpression(VariableExpression ve) {
        for (ModifierNode e : modifierNodeList) {
            ve.setModifiers(ve.getModifiers() | e.getOpcode());

            // local variable does not attach annotations
        };

        return ve;
    }

    public <T extends AnnotatedNode> T attachAnnotations(T node) {
        for (AnnotationNode an : getAnnotations()) {
            node.addAnnotation(an);
        }

        return node;
    }
}
