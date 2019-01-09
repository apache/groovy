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
package groovy.lang;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the variable bindings of a script which can be altered
 * from outside the script object or created outside of a script and passed
 * into it.
 * <p> Binding instances are not supposed to be used in a multi-threaded context.
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
            throw new MissingPropertyException(name, this.getClass());

        Object result = variables.get(name);

        if (result == null && !variables.containsKey(name)) {
            throw new MissingPropertyException(name, this.getClass());
        }

        return result;
    }

    /**
     * Sets the value of the given variable
     *
     * @param name  the name of the variable to set
     * @param value the new value for the given variable
     */
    public void setVariable(String name, Object value) {
        if (variables == null)
            variables = new LinkedHashMap();
        variables.put(name, value);
    }

    /**
     * remove the variable with the specified name
     *
     * @param name the name of the variable to remove
     */
    public void removeVariable(String name) {
        if (null == variables) {
            return;
        }

        variables.remove(name);
    }
    
    /**
     * Simple check for whether the binding contains a particular variable or not.
     * 
     * @param name the name of the variable to check for
     */
    public boolean hasVariable(String name) {
        return variables != null && variables.containsKey(name);
    }

    public Map getVariables() {
        if (variables == null)
            variables = new LinkedHashMap();
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
