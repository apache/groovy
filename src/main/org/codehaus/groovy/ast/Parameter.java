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

import groovy.lang.Reference;

import org.codehaus.groovy.ast.expr.*;

/**
 * Represents a parameter on a constructor or method call. The type name is
 * optional - it should be defaulted to java.lang.Object if unknown.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Parameter {

    public static final Parameter[] EMPTY_ARRAY = {
    };

    private String type;
    private String name;
    private boolean dynamicType;
    private Expression defaultValue;
    private String realType;

    public Parameter(String name) {
        this(null, name);
    }

    public Parameter(String type, String name) {
        this(type, name, null);
    }

    public Parameter(String type, String name, Expression defaultValue) {
        this.name = MethodNode.ensureJavaTypeNameSyntax(name);
        this.type = type;
        this.defaultValue = defaultValue;
        if (type == null || type.length() == 0) {
            this.type = "java.lang.Object";
            this.dynamicType = true;
        }
    }

    public String toString() {
        return super.toString() + "[name:" + name + ((type == null) ? "" : " type: " + type) + "]";
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDynamicType() {
        return dynamicType;
    }
    
    /**
     * @return the default value expression for this parameter or null if
     * no default value is specified
     */
    public Expression getDefaultValue() {
        return defaultValue;
    }

    public void makeReference() {
        realType = type;
        type = Reference.class.getName();
    }
    
    /**
     * @return the real logical type if a dereference is being made 
     * (e.g. to share variables across closure scopes)
     */
    public String getRealType() {
        return realType;
    }

}
