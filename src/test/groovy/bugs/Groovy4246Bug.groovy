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

class Groovy4246Bug extends GroovyTestCase {
    void testPostFixExpEvaluations() {
        assertScript """
            class Bug4246 {
                int randCallCount = 0
                static void main(args) {
                    new Bug4246()    
                }
            
                Bug4246() {
                    def num = 10
                    def arr = [0, 0, 0]
                    for (def i = 0; i < num; i++) {
                        arr[rand()]++
                    }
                    assert (arr[0] + arr[1] + arr[2] == num)
                    assert randCallCount == 10
                }
            
                int rand() {
                    randCallCount++
                    return new Random().nextInt(3)    
                }
            }
        """
    }
}
