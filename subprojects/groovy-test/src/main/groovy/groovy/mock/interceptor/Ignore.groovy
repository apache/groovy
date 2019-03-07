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
package groovy.mock.interceptor

/**
 * Helper object to support convenience "ignore.methodName" notation similar to demand notation.
 * Can't be used with methods within <code>java.lang.Object</code>.
 */
class Ignore {

    def parent // point back to original MockFor/StubFor

    Object invokeMethod(String methodName, Object args) {
        if (args && args.size() > 1) {
            throw new IllegalArgumentException("Ranges/repetitions not supported for ignored method '$methodName'.")
        }
        if (args && args.size() == 1) {
            if (args[0] instanceof Closure)
                parent.ignore(methodName, (Closure) args[0])
            else
                throw new IllegalArgumentException(
                        "Optional parameter to ignored method '$methodName' must be a Closure but instead found a ${args[0].getClass().simpleName}.")
        } else {
            parent.ignore(methodName, null)
        }
    }

}
