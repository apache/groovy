/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.expr.Expression;


/**
 * Represents an annotation which can be attached to interfaces, classes, methods and fields.
 * 
 * @author <a href="mailto:jstrachan@protique.com">James Strachan</a>
 * @author <a href='mailto:the[dot]mindstorm[at]gmail[dot]com'>Alex Popescu</a>
 * @version $Revision$
 */
public class AnnotationNode extends ASTNode {
    public static final int TYPE_TARGET = 1;
    public static final int CONSTRUCTOR_TARGET = 1 << 1;
    public static final int METHOD_TARGET = 1 << 2;
    public static final int FIELD_TARGET = 1 << 3;
    public static final int PARAMETER_TARGET =  1 << 4;
    public static final int LOCAL_VARIABLE_TARGET = 1 << 5;
    public static final int ANNOTATION_TARGET = 1 << 6;
    public static final int PACKAGE_TARGET = 1 << 7;
    private static final int ALL_TARGETS = TYPE_TARGET | CONSTRUCTOR_TARGET | METHOD_TARGET
        | FIELD_TARGET | PARAMETER_TARGET | LOCAL_VARIABLE_TARGET | ANNOTATION_TARGET;
    
    private final ClassNode classNode;
    private Map members = new HashMap();
    private boolean runtimeRetention= false, sourceRetention= false, classRetention = false;
    private int allowedTargets = ALL_TARGETS;

    public AnnotationNode(ClassNode classNode) {
        this.classNode = classNode;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public Map getMembers() {
        return members;
    }
    
    public Expression getMember(String name) {
        return (Expression) members.get(name);
    }

    public void addMember(String name, Expression value) {
        Expression oldValue = (Expression) members.get(name);
        if (oldValue == null) {
            members.put(name, value);
        }
        else {
            List list = null;
            if (oldValue instanceof List) {
                list = (List) oldValue;
            }
            else {
                list = new ArrayList();
                list.add(oldValue);
                members.put(name, list);
            }
            list.add(value);
        }
    }

    public void setMember(String name, Expression value) {
        members.put(name, value);
    }
    
    public boolean isBuiltIn(){
        return false;
    }

    /**
     * Flag corresponding to <code>RetentionPolicy</code>.
     * @return <tt>true</tt> if the annotation should be visible at runtime, 
     *      <tt>false</tt> otherwise
     */
    public boolean hasRuntimeRetention() {
        return this.runtimeRetention;
    }

    /**
     * Sets the internal flag of this annotation runtime retention policy.
     * If the current annotation has 
     * <code>RetentionPolicy.RUNTIME</code> or if <tt>false</tt>
     * if the <code>RetentionPolicy.CLASS</code>.
     * @param flag if <tt>true</tt> then current annotation is marked as having
     *     <code>RetentionPolicy.RUNTIME</code>. If <tt>false</tt> then
     *     the annotation has <code>RetentionPolicy.CLASS</code>.
     */
    public void setRuntimeRetention(boolean flag) {
        this.runtimeRetention = flag;
    }
    
    /**
     * Flag corresponding to <code>RetentionPolicy.SOURCE</code>.
     * @return <tt>true</tt> if the annotation is only allowed in sources 
     *      <tt>false</tt> otherwise
     */
    public boolean hasSourceRetention() {
        if (!runtimeRetention && !classRetention) return true;
        return this.sourceRetention;
    }

    /** Sets the internal flag if the current annotation has 
     * <code>RetentionPolicy.SOURCE</code>.
     */ 
    public void setSourceRetention(boolean flag) {
        this.sourceRetention = flag;
    }

    /**
     * Flag corresponding to <code>RetentionPolicy.CLASS</code>.
     * @return <tt>true</tt> if the annotation is recorded by the compiler,
     *                       but not visible at runtime     *
      *        <tt>false</tt> otherwise
     */
    public boolean hasClassRetention() {
        return this.classRetention;
    }

    /** Sets the internal flag if the current annotation has
     * <code>RetentionPolicy.CLASS</code>.
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
    
    public static final String targetToName(int target) {
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
            default:
                return "unknown target";
        }
    }
}
