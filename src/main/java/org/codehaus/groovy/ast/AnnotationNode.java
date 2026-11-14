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

import groovy.lang.annotation.ExtendedElementType;
import groovy.lang.annotation.ExtendedTarget;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;

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

    /**
     * Bit for {@link ElementType#CONSTRUCTOR}. Combined with the other target bits by
     * {@link #isTargetAllowed(int)}.
     */
    public static final int CONSTRUCTOR_TARGET      = 1 << 1;
    /**
     * Bit for {@link ElementType#METHOD}. Combined with the other target bits by
     * {@link #isTargetAllowed(int)}.
     */
    public static final int METHOD_TARGET           = 1 << 2;
    /**
     * Bit for {@link ElementType#FIELD}. Combined with the other target bits by
     * {@link #isTargetAllowed(int)}.
     */
    public static final int FIELD_TARGET            = 1 << 3;
    /**
     * Bit for {@link ElementType#PARAMETER}. Combined with the other target bits by
     * {@link #isTargetAllowed(int)}.
     */
    public static final int PARAMETER_TARGET        = 1 << 4;
    /**
     * Bit for {@link ElementType#LOCAL_VARIABLE}. Combined with the other target bits by
     * {@link #isTargetAllowed(int)}.
     */
    public static final int LOCAL_VARIABLE_TARGET   = 1 << 5;
    /**
     * Bit for {@link ElementType#ANNOTATION_TYPE}. The low bit is clear, so {@link #targetToName(int)}
     * reports it as ANNOTATION rather than TYPE.
     */
    public static final int ANNOTATION_TARGET       = 1 << 6;
    /**
     * Bit for {@link ElementType#PACKAGE}. Combined with the other target bits by
     * {@link #isTargetAllowed(int)}.
     */
    public static final int PACKAGE_TARGET          = 1 << 7;
    /**
     * Bit for {@link ElementType#TYPE_PARAMETER}. Not part of the mask used when the annotation type has
     * no {@link Target}.
     */
    public static final int TYPE_PARAMETER_TARGET   = 1 << 8;
    /**
     * Bit for {@link ElementType#TYPE_USE}. Not part of the mask used when the annotation type has no
     * {@link Target}.
     */
    public static final int TYPE_USE_TARGET         = 1 << 9;
    /**
     * Bit for {@link ElementType#RECORD_COMPONENT}. Included in the mask used when the annotation type has
     * no {@link Target}.
     */
    public static final int RECORD_COMPONENT_TARGET = 1 << 10;
    /** Groovy-only target for statement-level annotations (e.g. on {@code for}/{@code while} loops). */
    public static final int STATEMENT_TARGET        = 1 << 11;
    /** Groovy-only target for annotations on import statements. */
    public static final int IMPORT_TARGET           = 1 << 12;
    /**
     * Mask for a type or module target. {@link ElementType#TYPE} and {@link ElementType#MODULE} both map
     * here.
     * The low bit keeps it distinct from {@link #ANNOTATION_TARGET}, and {@link #targetToName(int)}
     * reports every value with that bit set as TYPE.
     */
    public static final int TYPE_TARGET             = ANNOTATION_TARGET | 1; // GROOVY-7151

    private final ClassNode classNode;
    private Map<String, Expression> members;

    /**
     * Creates an annotation use of the given annotation type.
     * No member values are stored until {@link #addMember(String, Expression)} or
     * {@link #setMember(String, Expression)} is called.
     *
     * @param type the annotation class; stored as given and not copied
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public AnnotationNode(final ClassNode type) {
        classNode = requireNonNull(type);
    }

    /**
     * Stores a member expression the first time {@code name} has no value.
     * A missing member and a member set to {@code null} are both treated as unset.
     * Members are kept in insertion order.
     * {@link #setMember(String, Expression)} overwrites a stored value instead of rejecting it.
     *
     * @param name the annotation member name
     * @param value the member expression, stored without checking it against the annotation type
     * @throws GroovyBugError if a non-null value is already stored for {@code name}
     */
    public void addMember(final String name, final Expression value) {
        ensureMembers();
        Expression oldValue = members.get(name);
        if (oldValue == null) {
            members.put(name, value);
        } else {
            throw new GroovyBugError(String.format("Annotation member %s has already been added", name));
        }
    }

    /**
     * Stores a member expression, replacing a value already recorded for the same name.
     * The map preserves insertion order, so replacing a name does not move it.
     *
     * @param name the annotation member name
     * @param value the member expression to store
     */
    public void setMember(final String name, final Expression value) {
        ensureMembers();
        members.put(name, value);
    }

    private void ensureMembers() {
        if (members == null) {
            members = new LinkedHashMap<>();
        }
    }

    /**
     * Does nothing. Allowed targets are read from the annotation type's {@link Target} by
     * {@link #isTargetAllowed(int)}.
     *
     * @param ignored unused bitmask of targets
     * @deprecated since 6.0.0; targets are no longer stored on the annotation use
     */
    @Deprecated(since = "6.0.0")
    public void setAllowedTargets(final int ignored) {
    }

    /**
     * Does nothing. Class retention is derived from the annotation type's {@link Retention} by
     * {@link #hasClassRetention()}.
     *
     * @param ignored unused retention flag
     * @deprecated since 6.0.0; retention is no longer stored on the annotation use
     */
    @Deprecated(since = "6.0.0")
    public void setClassRetention(final boolean ignored) {
    }

    /**
     * Does nothing. Source retention is derived from the annotation type's {@link Retention} by
     * {@link #hasSourceRetention()}.
     *
     * @param ignored unused retention flag
     * @deprecated since 6.0.0; retention is no longer stored on the annotation use
     */
    @Deprecated(since = "6.0.0")
    public void setSourceRetention(final boolean ignored) {
    }

    /**
     * Does nothing. Runtime retention is derived from the annotation type's {@link Retention} by
     * {@link #hasRuntimeRetention()}.
     *
     * @param ignored unused retention flag
     * @deprecated since 6.0.0; retention is no longer stored on the annotation use
     */
    @Deprecated(since = "6.0.0")
    public void setRuntimeRetention(final boolean ignored) {
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the annotation class passed to the constructor.
     * This call does not resolve or redirect that class node.
     *
     * @return the annotation type
     */
    public ClassNode getClassNode() {
        return classNode;
    }

    /**
     * Returns the member expressions in insertion order.
     * Before any member is stored the result is an immutable empty map, and later additions are not visible
     * through it.
     * Once a member has been stored the result is the live map.
     *
     * @return the member map, never {@code null}
     */
    public Map<String, Expression> getMembers() {
        if (members == null) {
            return Collections.emptyMap();
        }
        return members;
    }

    /**
     * Returns the expression stored for one member.
     *
     * @param name the member name
     * @return the member expression, or {@code null} when that member has not been set
     */
    public Expression getMember(final String name) {
        if (members == null) {
            return null;
        }
        return members.get(name);
    }

    /**
     * Always reports that the annotation is not built in.
     * Annotation nodes no longer distinguish built-in annotations.
     *
     * @return {@code false}
     * @deprecated since 6.0.0
     */
    @Deprecated(since = "6.0.0")
    public boolean isBuiltIn() {
        return false;
    }

    /**
     * Reports whether every bit set in {@code target} is permitted for this annotation type.
     * <p>
     * The annotation class must already be a primary class node or resolved. The first check decodes
     * {@link Target} from that class and caches the mask on the resolved class.
     * Only a {@code value} that is a {@link ListExpression} is decoded. A {@link PropertyExpression} in
     * that list contributes the bit for the {@link ElementType} it names; any other entry is skipped. If no
     * {@link Target} annotation has such a list, the mask is the JLS 9.6.4.1 default: declaration contexts
     * other than type parameters, and not type-use.
     * {@link ElementType#TYPE} and {@link ElementType#MODULE} both contribute {@link #TYPE_TARGET}. An
     * {@link ElementType} constant with no mapped bit fails with {@link GroovyBugError}. A property name
     * that is not an {@link ElementType} constant aborts the lookup.
     * {@link ExtendedTarget} is then OR-ed in. {@link ExtendedElementType#IMPORT} contributes
     * {@link #IMPORT_TARGET} and {@link ExtendedElementType#LOOP} contributes {@link #STATEMENT_TARGET};
     * an unknown extended element contributes nothing.
     * A requested combination is allowed only when all of its bits are in the mask. A target of zero is
     * always allowed.
     *
     * @param target bitmask of element kinds, usually one {@code *_TARGET} constant
     * @return {@code true} when every bit set in {@code target} is allowed
     * @throws IllegalStateException if the annotation class is neither primary nor resolved
     * @throws GroovyBugError if {@link Target} names an {@link ElementType} that has no bit
     */
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
        String text = "@" + getClassNode().getName();
        Map<String, Expression> members = getMembers();
        if (!members.isEmpty()) {
            var memberText = new StringJoiner(", ", "(", ")");
            for (Map.Entry<String, Expression> entry : members.entrySet()) {
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

    /**
     * Names one target bit for error messages.
     * Any value with the low bit set is {@code TYPE}, which is how {@link #TYPE_TARGET} is told apart from
     * {@link #ANNOTATION_TARGET}.
     * Every other value must equal exactly one {@code *_TARGET} constant; a mix of bits is not split apart.
     *
     * @param target a target bitmask, normally one constant
     * @return the element-kind name, or {@code unknown target} when nothing matches
     */
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
