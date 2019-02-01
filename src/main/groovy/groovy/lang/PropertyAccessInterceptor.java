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
 * <p>An interface that adds the ability to intercept
 * property getters/setters
 *
 * @since Oct 24, 2005
 */
public interface PropertyAccessInterceptor extends Interceptor {

    /**
     * Intercepts a getXXX call and returns a result. The result is replaced by the
     * real value if doGet() return false
     *
     * @param object   The target object
     * @param property The property to get
     * @return A value supplied by the interceptor
     */
    Object beforeGet(Object object, String property);

    /**
     * Intercepts a setXXX call
     *
     * @param object   The target object
     * @param property The property to set
     * @param newValue The new value
     */
    void beforeSet(Object object, String property, Object newValue);

}
