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
        if (variables == null)
            throw new MissingPropertyException(name, Binding.class);

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
        if (variables == null)
          variables = new HashMap();
        variables.put(name, value);
    }
    
    public Map getVariables() {
        if (variables == null)
          variables = new HashMap();
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
