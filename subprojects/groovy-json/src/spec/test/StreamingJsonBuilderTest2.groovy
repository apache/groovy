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
package groovy.json
import groovy.util.GroovyTestCase
import org.junit.Test
//import groovy.json.StreamingJsonBuilder

/**
* Tests for StreamingJsonBuilder. The tests directly in this file are specific
* to StreamingJsonBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class StreamingJsonBuilderTest2  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::streamingjsonbuilder_nullobject1[]
    StreamingJsonBuilder streamingjsonbuilder;
    
    // streamingjsonbuilder should be null when not initialized
    assert streamingjsonbuilder == null;
// end::streamingjsonbuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::streamingjsonbuilder_nullobject2[]
    StreamingJsonBuilder streamingjsonbuilder = null;
    
    // streamingjsonbuilder should be null when initialized to null
    assert streamingjsonbuilder == null;
// end::streamingjsonbuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::streamingjsonbuilder_object_exists1[]
    shouldFail 
    {
        // streamingjsonbuilder should not be null after construction
        StreamingJsonBuilder streamingjsonbuilder = new StreamingJsonBuilder();
    }
// end::streamingjsonbuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::streamingjsonbuilder_object_exists2[]
    shouldFail 
    {
        // streamingjsonbuilder should be an instance of correct StreamingJsonBuilder class
        StreamingJsonBuilder streamingjsonbuilder = new StreamingJsonBuilder();
    }
// end::streamingjsonbuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::streamingjsonbuilder_object_exists3[]
    // streamingjsonbuilder should throw an exception when null parm used in constructor
        StreamingJsonBuilder streamingjsonbuilder = new StreamingJsonBuilder(null);
// end::streamingjsonbuilder_object_exists3[]
    } // end of method

} // end of StreamingJsonBuilder class