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
import org.codehaus.groovy.ast.expr.Expression;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents an annotation which can be attached to interfaces, classes, methods, fields, parameters, and other places.
 */
public class AnnotationNode extends ASTNode {
    public static final int CONSTRUCTOR_TARGET = 1 << 1;
    public static final int METHOD_TARGET = 1 << 2;
    public static final int FIELD_TARGET = 1 << 3;
    public static final int PARAMETER_TARGET =  1 << 4;
    public static final int LOCAL_VARIABLE_TARGET = 1 << 5;
    public static final int ANNOTATION_TARGET = 1 << 6;
    public static final int PACKAGE_TARGET = 1 << 7;
    public static final int TYPE_PARAMETER_TARGET = 1 << 8;
    public static final int TYPE_USE_TARGET = 1 << 9;
    public static final int TYPE_TARGET = 1 + ANNOTATION_TARGET;    //GROOVY-7151
    private static final int ALL_TARGETS = TYPE_TARGET | CONSTRUCTOR_TARGET | METHOD_TARGET
            | FIELD_TARGET | PARAMETER_TARGET | LOCAL_VARIABLE_TARGET | ANNOTATION_TARGET
            | PACKAGE_TARGET | TYPE_PARAMETER_TARGET | TYPE_USE_TARGET;

    private final ClassNode classNode;
    private Map<String, Expression> members;
    private boolean runtimeRetention = false, sourceRetention = false, /* explicit */ classRetention = false;
    private int allowedTargets = ALL_TARGETS;

    public AnnotationNode(ClassNode classNode) {
        this.classNode = classNode;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public Map<String, Expression> getMembers() {
        if (members == null) {
            return Collections.emptyMap();
        }
        return members;
    }

    public Expression getMember(String name) {
        if (members == null) {
            return null;
        }
        return members.get(name);
    }

    private void assertMembers() {
        if (members == null) {
             members = new LinkedHashMap<String, Expression>();
        }
    }

    public void addMember(String name, Expression value) {
        assertMembers();
        Expression oldValue = members.get(name);
        if (oldValue == null) {
            members.put(name, value);
        }
        else {
            throw new GroovyBugError(String.format("Annotation member %s has already been added", name));
        }
    }

    public void setMember(String name, Expression value) {
        assertMembers();
        members.put(name, value);
    }

    public boolean isBuiltIn(){
        return false;
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
     * Sets the internal flag if the current annotation has
     * <code>RetentionPolicy.SOURCE</code>.
     *
     * @param flag if <tt>true</tt> then current annotation is marked as having
     *     <code>RetentionPolicy.RUNTIME</code>.
     */
    public void setRuntimeRetention(boolean flag) {
        this.runtimeRetention = flag;
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
     * Sets the internal flag if the current annotation has <code>RetentionPolicy.SOURCE</code>.
     *
     * @param flag if <tt>true</tt> then current annotation is marked as having
     *     <code>RetentionPolicy.SOURCE</code>.
     */
    public void setSourceRetention(boolean flag) {
        this.sourceRetention = flag;
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

    /**
     * Sets the internal flag if the current annotation has an explicit <code>RetentionPolicy.CLASS</code>.
     *
     * @param flag if <tt>true</tt> then current annotation is marked as having
     *     <code>RetentionPolicy.CLASS</code>.
     */
    public void setClassRetention(boolean flag) {
        this.classRetention = flag;
    }

    public void setAllowedTargets(int bitmap) {
        this.allowedTargets = bitmap;
    }

    public boolean isTargetAllowed(int target) {
        return (this.allowedTargets & target) == target;
    }

    public static String targetToName(int target) {
        switch(target) {
            case TYPE_TARGET:
                return "TYPE";
            case CONSTRUCTOR_TARGET:
                return "CONSTRUCTOR";
            case METHOD_TARGET:
                return "METHOD";
            case FIELD_TARGET:
                return "FIELD";
            case PARAMETER_TARGET:
                return "PARAMETER";
            case LOCAL_VARIABLE_TARGET:
                return "LOCAL_VARIABLE";
            case ANNOTATION_TARGET:
                return "ANNOTATION";
            case PACKAGE_TARGET:
                return "PACKAGE";
            case TYPE_PARAMETER_TARGET:
                return "TYPE_PARAMETER";
            case TYPE_USE_TARGET:
                return "TYPE_USE";
            default:
                return "unknown target";
        }
    }

    @Override
    public String getText() {
        String memberText = "";
        if (members != null) {
            boolean first = true;
            for (Map.Entry<String, Expression> next : members.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    memberText += ", ";
                }
                memberText += next.getKey() + ": " + next.getValue().getText();
            }
        }
        return "@" + classNode.getText() + "(" + memberText + ")";
    }
}
