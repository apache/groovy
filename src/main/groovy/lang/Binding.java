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
package groovy.lang;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the variable bindings of a script which can be altered
 * from outside the script object or created outside of a script and passed
 * into it.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class Binding extends GroovyObjectSupport {
    private Map variables;
    
    public Binding() {
        variables = new HashMap();
    }
    
    public Binding(Map variables) {
        this.variables = variables;
    }
    
    /**
     * A helper constructor used in main(String[]) method calls
     * 
     * @param args are the command line arguments from a main()
     */
    public Binding(String[] args) {
        this();
        setVariable("args", args);
    }
    
    /**
     * @param name the name of the variable to lookup
     * @return the variable value
     */
    public Object getVariable(String name) {
        Object result = variables.get(name);
        
        if (result == null && !variables.containsKey(name)) {
        		throw new MissingPropertyException(name, Binding.class);
        }
        
        return result;
    }
    
    /**
     * Sets the value of the given variable
     * @param name the name of the variable to set
     * @param value the new value for the given variable
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }
    
    public Map getVariables() {
        return variables;
    }

    /**
     * Overloaded to make variables appear as bean properties or via the subscript operator
     */
    public Object getProperty(String property) {
        /** @todo we should check if we have the property with the metaClass instead of try/catch  */
        try {
            return super.getProperty(property);
        }
        catch (MissingPropertyException e) {
            return getVariable(property);
        }
    }

    /**
     * Overloaded to make variables appear as bean properties or via the subscript operator
     */
    public void setProperty(String property, Object newValue) {
        /** @todo we should check if we have the property with the metaClass instead of try/catch  */
        try {
            super.setProperty(property, newValue);
        }
        catch (MissingPropertyException e) {
            setVariable(property, newValue);
        }
    }

}
