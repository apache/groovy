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
package groovy

import groovy.test.GroovyTestCase

class OverloadInvokeMethodTest extends GroovyTestCase {
    
    void testBug() {
        def value = foo(123)
        assert value == 246
    }

    /**
     * Lets overload the invokeMethod() mechanism to provide an alias
     * to an existing method
     */
    def invokeMethod(String name, Object args) {
        try {
            return metaClass.invokeMethod(this, name, args)
        }
        catch (MissingMethodException e) {
            if (name == 'foo') {
                return metaClass.invokeMethod(this, 'bar', args)
            }
            else {
                throw e
            }
        }
    }
    
    def bar(param) {
        return param * 2
    }

}