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
package groovy.util;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaExpandoProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Represents a dynamically expandable bean.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Expando extends GroovyObjectSupport {

    private Map expandoProperties;

    public Expando() {
    }

    public Expando(Map expandoProperties) {
        this.expandoProperties = expandoProperties;
    }

    /**
     * @return the dynamically expanded properties
     */
    public Map getExpandoProperties() {
        if (expandoProperties == null) {
            expandoProperties = createMap();
        }
        return expandoProperties;
    }
	
	public List getProperties() {
		// run through all our current properties and create MetaProperty objects
		List ret = new ArrayList();
		Iterator itr = getExpandoProperties().entrySet().iterator();
		while(itr.hasNext()) {
			Entry entry = (Entry) itr.next();
			ret.add(new MetaExpandoProperty(entry));
		}
		
		return ret;
	}

    public Object getProperty(String property) {
        try {
            return super.getProperty(property);
        }
        catch (GroovyRuntimeException e) {
            return getExpandoProperties().get(property);
        }
    }

    public void setProperty(String property, Object newValue) {
        try {
            super.setProperty(property, newValue);
        }
        catch (GroovyRuntimeException e) {
            getExpandoProperties().put(property, newValue);
        }
    }

    public Object invokeMethod(String name, Object args) {
        try {
            return super.invokeMethod(name, args);
        }
        catch (GroovyRuntimeException e) {
        	// br should get a "native" property match first. getProperty includes such fall-back logic
            Object value = this.getProperty(name);
            if (value instanceof Closure) {
                Closure closure = (Closure) value;
                closure.setDelegate(this);
                return closure.call(args);
            }
            else {
                throw e;
            }
        }
        
    }
    
    /**
     * Factory method to create a new Map used to store the expando properties map
     * @return a newly created Map implementation
     */
    protected Map createMap() {
        return new HashMap();
    }

}
