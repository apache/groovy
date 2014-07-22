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
package groovy.swing
import groovy.util.GroovyTestCase
import org.junit.Test
import groovy.swing.*

/**
* Tests for SwingBuilder. The tests directly in this file are specific
* to SwingBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class SwingBuilderTest2  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::swingbuilder_nullobject1[]
    SwingBuilder swingbuilder;
    
    // swingbuilder should be null when not initialized
    assert swingbuilder == null;
// end::swingbuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::swingbuilder_nullobject2[]
    SwingBuilder swingbuilder = null;
    
    // swingbuilder should be null when initialized to null
    assert swingbuilder == null;
// end::swingbuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::swingbuilder_object_exists1[]
    SwingBuilder swingbuilder = new SwingBuilder();
    
    // swingbuilder should not be null after construction
    assert swingbuilder != null;
// end::swingbuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::swingbuilder_object_exists2[]
    SwingBuilder swingbuilder = new SwingBuilder();
    
    // swingbuilder should be an instance of correct SwingBuilder class
    assert swingbuilder instanceof SwingBuilder, 'default SwingBuilder constructor did not build a version of SwingBuilder'
// end::swingbuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::swingbuilder_object_exists3[]
    shouldFail
    {
        SwingBuilder swingbuilder = new SwingBuilder(null);
    }
// end::swingbuilder_object_exists3[]
    } // end of method

} // end of SwingBuilder class