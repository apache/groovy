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

import org.codehaus.groovy.ast.expr.*;

/**
 * Represents a parameter on a constructor or method call. The type name is
 * optional - it should be defaulted to java.lang.Object if unknown.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Parameter implements Variable {

    public static final Parameter[] EMPTY_ARRAY = {
    };

    private ClassNode type;
    private String name;
    private boolean dynamicTyped;
    private Expression defaultValue;
    private boolean hasDefaultValue;
    private boolean inStaticContext;
    private boolean closureShare=false;

    public Parameter(ClassNode type, String name) {
        this.name = name;
        this.setType(type);
        this.hasDefaultValue = false;
    }
    
    public Parameter(ClassNode type, String name, Expression defaultValue) {
        this(type,name);
        this.defaultValue = defaultValue;
        this.hasDefaultValue = true;
    }

    public String toString() {
        return super.toString() + "[name:" + name + ((type == null) ? "" : " type: " + type.getName()) + ", hasDefaultValue: " + this.hasInitialExpression() + "]";
    }

    public String getName() {
        return name;
    }

    public ClassNode getType() {
        return type;
    }

    public void setType(ClassNode type) {
        this.type = type;
        dynamicTyped |= type==ClassHelper.DYNAMIC_TYPE;
    }
    
    public boolean hasInitialExpression() {
        return this.hasDefaultValue;
    }
    
    /**
     * @return the default value expression for this parameter or null if
     * no default value is specified
     */
    public Expression getInitialExpression() {
        return defaultValue;
    }
    
    public boolean isInStaticContext() {
        return inStaticContext;
    }
    
    public void setInStaticContext(boolean inStaticContext) {
        this.inStaticContext = inStaticContext;
    }

    public boolean isDynamicTyped() {
        return dynamicTyped;
    }

    public boolean isClosureSharedVariable() {
        return closureShare;
    }

    public void setClosureSharedVariable(boolean inClosure) {
        closureShare = inClosure;        
    }
}
