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

class ArrayAutoboxingTest extends GroovyTestCase {
    
    void testUnwantedAutoboxingWhenInvokingMethods() {
      def cl
      cl = blah2(new int[2*2])
      assert cl == "[I"
      cl = blah2(new long[2*2])
      assert cl == "[J"
      cl = blah2(new short[2*2])
      assert cl == "[S"
      cl = blah2(new boolean[2*2])
      assert cl == "[Z"
      cl = blah2(new char[2*2])
      assert cl == "[C"
      cl = blah2(new double[2*2])
      assert cl == "[D"
      cl = blah2(new float[2*2])
      assert cl == "[F"
    }
    
    def blah2(Object o) {
       return o.class.name
    }
        
} 