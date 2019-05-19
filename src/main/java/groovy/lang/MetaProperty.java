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

import java.lang.reflect.Modifier;

import static org.apache.groovy.util.BeanUtils.capitalize;

/**
 * Represents a property on a bean which may have a getter and/or a setter
 */
public abstract class MetaProperty {

    protected final String name;
    protected Class type;
    public static final String PROPERTY_SET_PREFIX = "set";

    /**
     * Constructor that sets the property name and type (class)
     */
    public MetaProperty(String name, Class type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return the property of the given object
     * @throws Exception if the property could not be evaluated
     */
    public abstract Object getProperty(Object object);

    /**
     * Sets the property on the given object to the new value
     * 
     * @param object on which to set the property
     * @param newValue the new value of the property
     * @throws RuntimeException if the property could not be set
     */
    public abstract void setProperty(Object object, Object newValue);

    /**
     * Return the name of the property
     *
     * @return the name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type of the property
     */
    public Class getType() {
        return type;
    }
    
    /**
     * Returns the access modifier.
     * @return Modifier.PUBLIC
     */
    public int getModifiers() {
        return Modifier.PUBLIC;
    }

    /**
     * Gets the name for the getter for this property
     *
     * @return The name of the property. The name is "get"+ the capitalized propertyName
     *         or, in the case of boolean values, "is" + the capitalized propertyName
     */
    public static String getGetterName(String propertyName, Class type) {
        String prefix = type == boolean.class || type == Boolean.class ? "is" : "get";
        return prefix + capitalize(propertyName);
    }

    /**
     * Gets the setter for the getter for this property.
     *
     * @return The name of the property. The name is "set"+ the capitalized propertyName.
     */
    public static String getSetterName(String propertyName) {
        return PROPERTY_SET_PREFIX + capitalize(propertyName);
    }
}
