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
    public static final int TYPE_TARGET             = ANNOTATION_TARGET | 1; // GROOVY-7151

    private final ClassNode classNode;
    private Map<String, Expression> members;
    private int allowedTargets = 0x4FF; // GROOVY-11838: JLS 9.6.4.1
    private boolean runtimeRetention = false, sourceRetention = false, /*explicit*/ classRetention = false;

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

    public void setAllowedTargets(final int bitmap) {
        allowedTargets = bitmap;
    }

    /**
     * Sets the internal flag if the current annotation has <code>RetentionPolicy.RUNTIME</code>.
     *
     * @param value if <tt>true</tt> then current annotation is marked as having
     *     <code>RetentionPolicy.RUNTIME</code>.
     */
    public void setRuntimeRetention(final boolean value) {
        runtimeRetention = value;
    }

    /**
     * Sets the internal flag if the current annotation has <code>RetentionPolicy.SOURCE</code>.
     *
     * @param value if <tt>true</tt> then current annotation is marked as having
     *     <code>RetentionPolicy.SOURCE</code>.
     */
    public void setSourceRetention(final boolean value) {
        sourceRetention = value;
    }

    /**
     * Sets the internal flag if the current annotation has an explicit <code>RetentionPolicy.CLASS</code>.
     *
     * @param value if <tt>true</tt> then current annotation is marked as having
     *     <code>RetentionPolicy.CLASS</code>.
     */
    public void setClassRetention(final boolean value) {
        classRetention = value;
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
        return (this.allowedTargets & target) == target;
    }

    /**
     * Flag corresponding to <code>RetentionPolicy.RUNTIME</code>.
     * @return <tt>true</tt> if the annotation should be visible at runtime,
     *         <tt>false</tt> otherwise
     */
    public boolean hasRuntimeRetention() {
        return this.runtimeRetention;
    }

    /**
     * Flag corresponding to <code>RetentionPolicy.SOURCE</code>.
     * @return <tt>true</tt> if the annotation is only allowed in sources
     *         <tt>false</tt> otherwise
     */
    public boolean hasSourceRetention() {
        return this.sourceRetention;
    }

    /**
     * Flag corresponding to <code>RetentionPolicy.CLASS</code>.
     * This is the default when no <code>RetentionPolicy</code> annotations are present.
     *
     * @return <tt>true</tt> if the annotation is written in the bytecode, but not visible at runtime
     *         <tt>false</tt> otherwise
     */
    public boolean hasClassRetention() {
        if (!runtimeRetention && !sourceRetention) return true;
        return this.classRetention;
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
        return switch (target) {
            case TYPE_TARGET -> "TYPE";
            case CONSTRUCTOR_TARGET -> "CONSTRUCTOR";
            case METHOD_TARGET -> "METHOD";
            case FIELD_TARGET -> "FIELD";
            case PARAMETER_TARGET -> "PARAMETER";
            case LOCAL_VARIABLE_TARGET -> "LOCAL_VARIABLE";
            case ANNOTATION_TARGET -> "ANNOTATION";
            case PACKAGE_TARGET -> "PACKAGE";
            case TYPE_PARAMETER_TARGET -> "TYPE_PARAMETER";
            case TYPE_USE_TARGET -> "TYPE_USE";
            case RECORD_COMPONENT_TARGET -> "RECORD_COMPONENT";
            default -> "unknown target";
        };
    }
}
