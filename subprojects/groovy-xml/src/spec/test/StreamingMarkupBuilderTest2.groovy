/*
 * Copyright 2014 the original author or authors.
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
package groovy.xml
import groovy.util.GroovyTestCase
import org.junit.Test
import groovy.xml.StreamingMarkupBuilder

/**
* Tests for StreamingMarkupBuilder. The tests directly in this file are specific
* to StreamingMarkupBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class StreamingMarkupBuilderTest2  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::streamingmarkupbuilder_nullobject1[]
    StreamingMarkupBuilder streamingmarkupbuilder;
    
    // streamingmarkupbuilder should be null when not initialized
    assert streamingmarkupbuilder == null;
// end::streamingmarkupbuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::streamingmarkupbuilder_nullobject2[]
    StreamingMarkupBuilder streamingmarkupbuilder = null;
    
    // streamingmarkupbuilder should be null when initialized to null
    assert streamingmarkupbuilder == null;
// end::streamingmarkupbuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::streamingmarkupbuilder_object_exists1[]
    StreamingMarkupBuilder streamingmarkupbuilder = new StreamingMarkupBuilder();
    
    // streamingmarkupbuilder should not be null after construction
    assert streamingmarkupbuilder != null;
// end::streamingmarkupbuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::streamingmarkupbuilder_object_exists2[]
    StreamingMarkupBuilder streamingmarkupbuilder = new StreamingMarkupBuilder();
    
    // streamingmarkupbuilder should be an instance of correct StreamingMarkupBuilder class
    assert streamingmarkupbuilder instanceof StreamingMarkupBuilder, 'default StreamingMarkupBuilder constructor did not build a version of StreamingMarkupBuilder'
// end::streamingmarkupbuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::streamingmarkupbuilder_object_exists3[]
    shouldFail 
    {
        // streamingmarkupbuilder should throw an exception when null parm used in constructor
        StreamingMarkupBuilder streamingmarkupbuilder = new StreamingMarkupBuilder(null);
    }    
// end::streamingmarkupbuilder_object_exists3[]
    } // end of method

} // end of StreamingMarkupBuilder class