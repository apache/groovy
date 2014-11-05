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
import groovy.xml.SAXBuilder

/**
* Tests for SaxBuilder. The tests directly in this file are specific
* to SaxBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class SaxBuilderTest  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::saxbuilder_nullobject1[]
    SAXBuilder saxBuilder;
    
    // SAXBuilder should be null when not initialized
    assert saxBuilder == null;
// end::SAXBuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::SAXBuilder_nullobject2[]
    SAXBuilder saxBuilder = null;
    
    // SAXBuilder should be null when initialized to null
    assert saxBuilder == null;
// end::SAXBuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::SAXBuilder_object_exists1[]
    SAXBuilder saxBuilder = new SAXBuilder();
    
    // SAXBuilder should not be null after construction
    assert saxBuilder != null;
// end::SAXBuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::SAXBuilder_object_exists2[]
    SAXBuilder saxBuilder = new SAXBuilder();
    
    // SAXBuilder should be an instance of correct SAXBuilder class
    assert saxBuilder instanceof SAXBuilder, 'default SAXBuilder constructor did not build a version of SAXBuilder'
// end::SAXBuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::SAXBuilder_object_exists3[]
        SAXBuilder saxBuilder = new SAXBuilder(null);
        // SAXBuilder should not be null after construction
        assert saxBuilder != null;
    
// end::SAXBuilder_object_exists3[]
    } // end of method

} // end of SAXBuilder class