/*
 $Id$

 Copyright 2003 (C) The Codehaus. All Rights Reserved.

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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map;


/**
 * A helper class used by the runtime to allow Groovy classes to be extended at runtime
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ClassExtender {
    private Map variables;
    private Map methods;

    public synchronized Object get(String name) {
        if (variables != null) {
            return variables.get(name);
        }
        return null;
    }

    public synchronized void set(String name, Object value) {
        if (variables == null) {
            variables = createMap();
        }
        variables.put(name, value);
    }

    public synchronized void remove(String name) {
        if (variables != null) {
            variables.remove(name);
        }
    }

    public void call(String name, Object params) {
        Closure closure = null;
        synchronized (this) {
            if (methods != null) {
                closure = (Closure) methods.get(name);
            }
        }
        if (closure != null) {
            closure.call(params);
        }
        else {
            // throw DoesNotUnderstandException();
        }
    }

    public synchronized void addMethod(String name, Closure closure) {
        if (methods == null) {
            methods = createMap();
        }
        methods.put(name, methods);
    }

    public synchronized void removeMethod(String name) {
        if (methods != null) {
            methods.remove(name);
        }
    }

    protected Map createMap() {
        return new HashMap();
    }
}
