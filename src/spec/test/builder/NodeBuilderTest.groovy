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
import groovy.util.NodeBuilder

/**
* Tests for NodeBuilder. The tests directly in this file are specific
* to NodeBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class NodeBuilderTest  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::nodebuilder_nullobject1[]
    NodeBuilder nodebuilder;
    
    // nodebuilder should be null when not initialized
    assert nodebuilder == null;
// end::nodebuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::nodebuilder_nullobject2[]
    NodeBuilder nodebuilder = null;
    
    // nodebuilder should be null when initialized to null
    assert nodebuilder == null;
// end::nodebuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::nodebuilder_object_exists1[]
    NodeBuilder nodebuilder = new NodeBuilder();
    
    // nodebuilder should not be null after construction
    assert nodebuilder != null;
// end::nodebuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::nodebuilder_object_exists2[]
    NodeBuilder nodebuilder = new NodeBuilder();
    
    // nodebuilder should be an instance of correct NodeBuilder class
    assert nodebuilder instanceof NodeBuilder, 'default NodeBuilder constructor did not build a version of NodeBuilder'
// end::nodebuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::nodebuilder_object_exists3[]
    shouldFail 
    {
        NodeBuilder nodebuilder = new NodeBuilder(null);
    }
    
// end::nodebuilder_object_exists3[]
    } // end of method

} // end of NodeBuilder class