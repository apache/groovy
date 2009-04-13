/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.lang

class CategoryAnnotationTest extends GroovyTestCase {
    void testTransformationOfPropertyInvokedOnThis() {
        //Test the fix for GROOVY-3367
        assertScript """
            @Category(Distance3367)
            class DistanceCategory3367 {
                Distance3367 plus(Distance3367 increment) {
                    new Distance3367(number: this.number + increment.number)
                }
            }
    
            class Distance3367 {
                def number
            }
    
            use(DistanceCategory3367) {
                def d1 = new Distance3367(number: 5)
                def d2 = new Distance3367(number: 10)
                def d3 = d1 + d2
                assert d3.number == 15
            }
        """
    }
    
}

