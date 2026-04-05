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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;

import groovy.lang.annotation.ExtendedElementType;
import groovy.lang.annotation.ExtendedTarget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * Represents an annotation which can be attached to interfaces, classes, methods, fields, parameters, and other places.
 */
public class AnnotationNode extends ASTNode {

    public static final int CONSTRUCTOR_TARGET      = 1 << 1;
    public static final int METHOD_TARGET           = 1 << 2;
    public static final int FIELD_TARGET            = 1 << 3;
    public static final int PARAMETER_TARGET        = 1 << 4;
    public static final int LOCAL_VARIABLE_TARGET   = 1 << 5;
    public static final int ANNOTATION_TARGET       = 1 << 6;
    public static final int PACKAGE_TARGET          = 1 << 7;
    public static final int TYPE_PARAMETER_TARGET   = 1 << 8;
    public static final int TYPE_USE_TARGET         = 1 << 9;
    public static final int RECORD_COMPONENT_TARGET = 1 << 10;
    /** Groovy-only target for statement-level annotations (e.g. on {@code for}/{@code while} loops). */
    public static final int STATEMENT_TARGET        = 1 << 11;
    /** Groovy-only target for annotations on import statements. */
    public static final int IMPORT_TARGET           = 1 << 12;
    public static final int TYPE_TARGET             = ANNOTATION_TARGET | 1; // GROOVY-7151

    private final ClassNode classNode;
    private Map<String, Expression> members;

    public AnnotationNode(final ClassNode type) {
        classNode = requireNonNull(type);
    }

    public void addMember(final String name, final Expression value) {
        ensureMembers();
        Expression oldValue = members.get(name);
        if (oldValue == null) {
            members.put(name, value);
        } else {
            throw new GroovyBugError(String.format("Annotation member %s has already been added", name));
        }
    }

    public void setMember(final String name, final Expression value) {
        ensureMembers();
        members.put(name, value);
    }

    private void ensureMembers() {
        if (members == null) {
            members = new LinkedHashMap<>();
        }
    }

    @Deprecated(since = "6.0.0")
    public void setAllowedTargets(final int ignored) {
    }

    @Deprecated(since = "6.0.0")
    public void setClassRetention(final boolean ignored) {
    }

    @Deprecated(since = "6.0.0")
    public void setSourceRetention(final boolean ignored) {
    }

    @Deprecated(since = "6.0.0")
    public void setRuntimeRetention(final boolean ignored) {
    }

    //--------------------------------------------------------------------------

    public ClassNode getClassNode() {
        return classNode;
    }

    public Map<String, Expression> getMembers() {
        if (members == null) {
            return Collections.emptyMap();
        }
        return members;
    }

    public Expression getMember(final String name) {
        if (members == null) {
            return null;
        }
        return members.get(name);
    }

    @Deprecated(since = "6.0.0")
    public boolean isBuiltIn() {
        return false;
    }

    public boolean isTargetAllowed(final int target) {
        if (!(classNode.isPrimaryClassNode() || classNode.isResolved()))
            throw new IllegalStateException("cannot check target at this time");

        // GROOVY-6526: check class for @Target
        int allowedTargets = classNode.redirect().getNodeMetaData(Target.class, (k) -> {
            for (AnnotationNode an : classNode.getAnnotations()) {
                if ("java.lang.annotation.Target".equals(an.getClassNode().getName())
                        && an.getMember("value") instanceof ListExpression list) {
                    int bits = 0;
                    for (Expression e : list.getExpressions()) {
                        if (e instanceof PropertyExpression item) {
                            String name = item.getPropertyAsString();
                            bits |= switch (ElementType.valueOf(name)) {
                                case TYPE             -> TYPE_TARGET;
                                case FIELD            -> FIELD_TARGET;
                                case METHOD           -> METHOD_TARGET;
                                case PARAMETER        -> PARAMETER_TARGET;
                                case CONSTRUCTOR      -> CONSTRUCTOR_TARGET;
                                case LOCAL_VARIABLE   -> LOCAL_VARIABLE_TARGET;
                                case ANNOTATION_TYPE  -> ANNOTATION_TARGET;
                                case PACKAGE          -> PACKAGE_TARGET;
                                case TYPE_PARAMETER   -> TYPE_PARAMETER_TARGET;
                                case TYPE_USE         -> TYPE_USE_TARGET;
                                case MODULE           -> TYPE_TARGET; //TODO
                                case RECORD_COMPONENT -> RECORD_COMPONENT_TARGET;
                                default -> throw new GroovyBugError("unsupported Target " + name);
                            };
                        }
                    }
                    return bits;
                }
            }
            return 0x4FF; // GROOVY-11838: JLS 9.6.4.1
        });

        // check @ExtendedTarget for Groovy-specific element types
        allowedTargets |= classNode.redirect().getNodeMetaData(ExtendedTarget.class, (k) -> {
            for (AnnotationNode an : classNode.getAnnotations()) {
                if ("groovy.lang.annotation.ExtendedTarget".equals(an.getClassNode().getName())) {
                    Expression member = an.getMember("value");
                    int bits = 0;
                    if (member instanceof ListExpression list) {
                        for (Expression e : list.getExpressions()) {
                            bits |= extendedElementTypeBits(e);
                        }
                    } else {
                        bits |= extendedElementTypeBits(member);
                    }
                    return bits;
                }
            }
            return 0;
        });

        return (target & allowedTargets) == target;
    }

    private static int extendedElementTypeBits(final Expression e) {
        if (e instanceof PropertyExpression item) {
            return valueOfBits(item.getPropertyAsString());
        }
        return 0;
    }

    private static int valueOfBits(String name) {
        if (name == null) return 0;
        try {
            return switch (ExtendedElementType.valueOf(name)) {
                case IMPORT -> IMPORT_TARGET;
                case LOOP   -> STATEMENT_TARGET;
            };
        } catch (IllegalArgumentException ignore) {
            return 0;
        }
    }

    private RetentionPolicy getRetentionPolicy() {
        if (!(classNode.isPrimaryClassNode() || classNode.isResolved()))
            throw new IllegalStateException("cannot check retention at this time");

        // GROOVY-6526: check class for @Retention
        return classNode.redirect().getNodeMetaData(Retention.class, (k) -> {
            for (AnnotationNode an : classNode.getAnnotations()) {
                if ("java.lang.annotation.Retention".equals(an.getClassNode().getName())) {
                    if (an.getMember("value") instanceof PropertyExpression pe) {
                        return RetentionPolicy.valueOf(pe.getPropertyAsString());
                    }
                    break;
                }
            }
            return null;
        });
    }

    /**
     * Flag corresponding to <code>RetentionPolicy.RUNTIME</code>.
     *
     * @return <tt>true</tt> if the annotation should be visible at runtime;
     *         <tt>false</tt> otherwise
     */
    public boolean hasRuntimeRetention() {
        return RetentionPolicy.RUNTIME.equals(getRetentionPolicy());
    }

    /**
     * Flag corresponding to <code>RetentionPolicy.SOURCE</code>.
     *
     * @return <tt>true</tt> if the annotation is only allowed in sources;
     *         <tt>false</tt> otherwise
     */
    public boolean hasSourceRetention() {
        return RetentionPolicy.SOURCE.equals(getRetentionPolicy());
    }

    /**
     * Flag corresponding to <code>RetentionPolicy.CLASS</code>.
     * This is the default when no <code>Retention</code> annotation is present.
     *
     * @return <tt>true</tt> if the annotation is written in the bytecode but not visible at runtime;
     *         <tt>false</tt> otherwise
     */
    public boolean hasClassRetention() {
        RetentionPolicy retentionPolicy = getRetentionPolicy();
        return retentionPolicy == null || retentionPolicy.equals(RetentionPolicy.CLASS);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getText() + "]";
    }

    @Override
    public String getText() {
        String text = "@" + classNode.getName();
        if (members != null) {
            var memberText = new StringJoiner(", ", "(", ")");
            for (Map.Entry<String,Expression> entry : members.entrySet()) {
                memberText.add(entry.getKey() + "=" + getText(entry.getValue()));
            }
            text += memberText;
        }
        return text;
    }

    private static String getText(final Expression e) {
        String text;
        if (e instanceof ConstantExpression ce && ce.getValue() instanceof String string) {
            text = '"' + string + '"';
        } else if (e instanceof ListExpression list) {
            var listText = new StringJoiner(", ", "[", "]");
            for (var item : list.getExpressions()) {
                listText.add(getText(item));
            }
            text = listText.toString();
        } else {
            text = e.getText();
        }
        return text;
    }

    public static String targetToName(final int target) {
        if ((target & 1) == 1) return "TYPE"; // GROOVY-7151
        return switch (target) {
            case CONSTRUCTOR_TARGET      -> "CONSTRUCTOR";
            case METHOD_TARGET           -> "METHOD";
            case FIELD_TARGET            -> "FIELD";
            case PARAMETER_TARGET        -> "PARAMETER";
            case LOCAL_VARIABLE_TARGET   -> "LOCAL_VARIABLE";
            case ANNOTATION_TARGET       -> "ANNOTATION";
            case PACKAGE_TARGET          -> "PACKAGE";
            case TYPE_PARAMETER_TARGET   -> "TYPE_PARAMETER";
            case TYPE_USE_TARGET         -> "TYPE_USE";
            case RECORD_COMPONENT_TARGET -> "RECORD_COMPONENT";
            case STATEMENT_TARGET        -> "STATEMENT";
            case IMPORT_TARGET           -> "IMPORT";
            default -> "unknown target";
        };
    }
}
