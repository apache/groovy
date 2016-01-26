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
package groovy.bugs

class Groovy3868Bug extends GroovyTestCase {
    void testAsTypeCallWithPrimitiveType() {
        callAndcheckResults(Long)
        callAndcheckResults(Integer)
        callAndcheckResults(Short)
        callAndcheckResults(Byte)
        callAndcheckResults(Character)
        callAndcheckResults(Double)
        callAndcheckResults(Float)
    }
    def callAndcheckResults(klazz) {
        def num = "1"
        def result = num.asType(klazz.TYPE) // get the primitive type of this class
        
        if(klazz == Character) num = num as char // Character.valueOf(String) is not there
        
        assert result == klazz.valueOf(num)
        assert result.class == klazz 
    }
}
