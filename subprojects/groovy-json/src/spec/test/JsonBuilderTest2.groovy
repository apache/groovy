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
//import groovy.json.JsonBuilder

/**
* Tests for JsonBuilder. The tests directly in this file are specific
* to JsonBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class JsonBuilderTest2  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::jsonbuilder_nullobject1[]
    JsonBuilder jsonbuilder;
    
    // jsonbuilder should be null when not initialized
    assert jsonbuilder == null;
// end::jsonbuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::jsonbuilder_nullobject2[]
    JsonBuilder jsonbuilder = null;
    
    // jsonbuilder should be null when initialized to null
    assert jsonbuilder == null;
// end::jsonbuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::jsonbuilder_object_exists1[]
    JsonBuilder jsonbuilder = new JsonBuilder();
    
    // jsonbuilder should not be null after construction
    assert jsonbuilder != null;
// end::jsonbuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::jsonbuilder_object_exists2[]
    JsonBuilder jsonbuilder = new JsonBuilder();
    
    // jsonbuilder should be an instance of correct JsonBuilder class
    assert jsonbuilder instanceof JsonBuilder, 'default JsonBuilder constructor did not build a version of JsonBuilder'
// end::jsonbuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::jsonbuilder_object_exists3[]
        // jsonbuilder does not throw an exception when null parm used in constructor
        JsonBuilder jsonbuilder = new JsonBuilder(null);
// end::jsonbuilder_object_exists3[]
    } // end of method

} // end of JsonBuilder class