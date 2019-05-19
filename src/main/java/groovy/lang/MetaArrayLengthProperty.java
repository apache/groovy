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


/**
 * Represents the length property of an array
 */
public class MetaArrayLengthProperty extends MetaProperty {

    /**
     * Sole constructor setting name to "length" and type to int
     */
    public MetaArrayLengthProperty() {
        super("length", int.class);
    }

    /**
     * Get this property from the given object.
     * @param object an array
     * @return the length of the array object
     * @throws IllegalArgumentException if object is not an array
     */
    public Object getProperty(Object object) {
        return java.lang.reflect.Array.getLength(object);
    }

    /**
     * Sets the property on the given object to the new value
     *
     * @param object   on which to set the property
     * @param newValue the new value of the property
     * @throws RuntimeException if the property could not be set
     */
    public void setProperty(Object object, Object newValue) {
        throw new ReadOnlyPropertyException("length", object.getClass());
    }
}
