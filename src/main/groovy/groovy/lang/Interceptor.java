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
 * Implementers of this interface can be registered in the ProxyMetaClass for
 * notifications about method calls for objects managed by the ProxyMetaClass.
 * See groovy/lang/InterceptorTest.groovy for details.
 */
public interface Interceptor {
    /**
     * This code is executed before the method is optionally called.
     * @param object        receiver object for the method call
     * @param methodName    name of the method to call
     * @param arguments     arguments to the method call
     * @return any arbitrary result that replaces the result of the
     * original method call only if doInvoke() returns false and afterInvoke()
     * relays this result.
     */
    Object beforeInvoke(Object object, String methodName, Object[] arguments);
    /**
     * This code is executed after the method is optionally called.
     * @param object        receiver object for the called method
     * @param methodName    name of the called method
     * @param arguments     arguments to the called method
     * @param result        result of the executed method call or result of beforeInvoke if method was not called
     * @return any arbitrary result that can replace the result of the
     * original method call. Typically, the result parameter is returned.
     */
    Object afterInvoke(Object object, String methodName, Object[] arguments, Object result);
    /**
     * @return whether the target method should be invoked at all.
     */
    boolean doInvoke();
}
