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
package groovy.xml

import groovy.xml.StreamingMarkupBuilder

/**
* Tests for the Groovy Xml user guide related to StreamingMarkupBuilderTest.
*/
class UserGuideStreamingMarkupBuilderTest  extends GroovyTestCase {

    void testSimpleExample() {
        // tag::testSimpleExample[]
        def xml = new StreamingMarkupBuilder().bind { // <1>
            records {
                car(name:'HSV Maloo', make:'Holden', year:2006) { // <2>
                    country('Australia')
                    record(type:'speed', 'Production Pickup Truck with speed of 271kph')
                }
                car(name:'P50', make:'Peel', year:1962) {
                    country('Isle of Man')
                    record(type:'size', 'Smallest Street-Legal Car at 99cm wide and 59 kg in weight')
                }
                car(name:'Royale', make:'Bugatti', year:1931) {
                    country('France')
                    record(type:'price', 'Most Valuable Car at $15 million')
                }
            }
        }

        def records = new XmlSlurper().parseText(xml.toString()) // <3>

        assert records.car.size() == 3
        assert records.car.find { it.@name == 'P50' }.country.text() == 'Isle of Man'
        // end::testSimpleExample[]
    }

    void testMkp() {
        // tag::testMkp[]
        def xml = new StreamingMarkupBuilder().bind {
            records {
                car(name: mkp.yield('3 < 5')) // <1>
                car(name: mkp.yieldUnescaped('1 < 3')) // <2>
            }
        }

        assert xml.toString().contains('3 &lt; 5')
        assert xml.toString().contains('1 < 3')
        // end::testMkp[]
    }

}
