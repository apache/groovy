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
 * The interface implemented by all Groovy objects.
 * <p>
 * Especially handy for using Groovy objects when in the Java world.
 */
public interface GroovyObject {

    /**
     * Invokes the given method.
     *
     * @param name the name of the method to call
     * @param args the arguments to use for the method call
     * @return the result of invoking the method
     */
    Object invokeMethod(String name, Object args);

    /**
     * Retrieves a property value.
     *
     * @param propertyName the name of the property of interest
     * @return the given property
     */
    Object getProperty(String propertyName);

    /**
     * Sets the given property to the new value.
     *
     * @param propertyName the name of the property of interest
     * @param newValue     the new value for the property
     */
    void setProperty(String propertyName, Object newValue);

    /**
     * Returns the metaclass for a given class.
     *
     * @return the metaClass of this instance
     */
    MetaClass getMetaClass();

    /**
     * Allows the MetaClass to be replaced with a derived implementation.
     *
     * @param metaClass the new metaclass
     */
    void setMetaClass(MetaClass metaClass);
}
