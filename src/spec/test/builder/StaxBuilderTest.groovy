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
package builder
import groovy.util.GroovyTestCase
import org.junit.Test
import groovy.xml.StaxBuilder

/**
* Tests for StaxBuilder. The tests directly in this file are specific
* to StaxBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class StaxBuilderTest  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::staxbuilder_nullobject1[]
    StaxBuilder staxbuilder;
    
    // staxbuilder should be null when not initialized
    assert staxbuilder == null;
// end::staxbuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::staxbuilder_nullobject2[]
    StaxBuilder staxbuilder = null;
    
    // staxbuilder should be null when initialized to null
    assert staxbuilder == null;
// end::staxbuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::staxbuilder_object_exists1[]
    shouldFail
    {
        StaxBuilder staxbuilder = new StaxBuilder();
    }
// end::staxbuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::staxbuilder_object_exists2[]
    shouldFail
    {
        StaxBuilder staxbuilder = new StaxBuilder();
    }
// end::staxbuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::staxbuilder_object_exists3[]
    shouldFail 
    {
        // Cannot invoke method writeStartDocument() on null object
        StaxBuilder staxbuilder = new StaxBuilder(null);
    }
    
// end::staxbuilder_object_exists3[]
    } // end of method

} // end of StaxBuilder class