/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

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
    
    private ClassNode classNode;
    private Map members = new HashMap();
    private boolean runtimeRetention= false;
    private boolean sourceRetention= false;
    private int allowedTarges = 0;
    private boolean valid;

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
        return this.sourceRetention;
    }

    /** Sets the internal flag if the current annotation has 
     * <code>RetentionPolicy.SOURCE</code>.
     */ 
    public void setSourceRetention(boolean flag) {
        this.sourceRetention = flag;
    }

    public void setAllowedTargets(int bitmap) {
        this.allowedTarges = bitmap;
    }
    
    public boolean isTargetAllowed(int target) {
        return (this.allowedTarges & target) == target;
    }
    
    /**
     * Set if the current annotation is verified and passed all
     * validations
     * @param flag
     */
    public void setValid(boolean flag) {
        this.valid = flag;
    }
    
    /**
     * Returns the state of this annotation (verified and all verification passed).
     */
    public boolean isValid() {
        return this.valid;
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
            default:
                return "unknown target";
        }
    }
}
