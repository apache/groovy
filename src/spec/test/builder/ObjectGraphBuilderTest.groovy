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
import groovy.util.*

/**
* Tests for ObjectGraphBuilder. The tests directly in this file are specific
* to ObjectGraphBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class ObjectGraphBuilderTest  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::objectgraphbuilder_nullobject1[]
    ObjectGraphBuilder objectgraphbuilder;
    
    // objectgraphbuilder should be null when not initialized
    assert objectgraphbuilder == null;
// end::objectgraphbuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::objectgraphbuilder_nullobject2[]
    ObjectGraphBuilder objectgraphbuilder = null;
    
    // objectgraphbuilder should be null when initialized to null
    assert objectgraphbuilder == null;
// end::objectgraphbuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::objectgraphbuilder_object_exists1[]
    ObjectGraphBuilder objectgraphbuilder = new ObjectGraphBuilder();
    
    // objectgraphbuilder should not be null after construction
    assert objectgraphbuilder != null;
// end::objectgraphbuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::objectgraphbuilder_object_exists2[]
    ObjectGraphBuilder objectgraphbuilder = new ObjectGraphBuilder();
    
    // objectgraphbuilder should be an instance of correct ObjectGraphBuilder class
    assert objectgraphbuilder instanceof ObjectGraphBuilder, 'default ObjectGraphBuilder constructor did not build a version of ObjectGraphBuilder'
// end::objectgraphbuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::objectgraphbuilder_object_exists3[]
    shouldFail 
    {
        ObjectGraphBuilder objectgraphbuilder = new ObjectGraphBuilder(null);
    }
    
// end::objectgraphbuilder_object_exists3[]
    } // end of method

} // end of ObjectGraphBuilder class