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


/**
 * Represents a type, either a dynamic type or statically defined type
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Type {

    public static final Type DYNAMIC_TYPE = new Type();
    
    private String name;
    private boolean dynamic;
    transient private String realName = ""; // holds the real type name if dynamic or reference

    public Type() {
        this.name = "java.lang.Object";
        this.dynamic = true;
    }

    public Type(String name) {
        this.name = name;
        this.dynamic = false;
    }

    public Type(String name, boolean isDynamic) {
        this.name = name;
        this.dynamic = isDynamic;
    }

    public String toString() {
        return super.toString() + "[name:" + name + " dynamic: " + dynamic + " real name: " + realName + "]";
    }

    public String getName() {
        return name;
    }

    public boolean isDynamic() {
        return dynamic;
    }
	/**
	 * @return Returns the realName.
	 */
	public String getRealName() {
		return realName;
	}
	/**
	 * @param realName The realName to set.
	 */
	public void setRealName(String realName) {
		this.realName = realName;
	}

    public void setDynamic(boolean b) {
        dynamic = b;
    }
}
