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
import groovy.xml.DOMBuilder

/**
* Tests for DomBuilder. The tests directly in this file are specific
* to DomBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class DomBuilderTest  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::dombuilder_nullobject1[]
    DOMBuilder dombuilder;
    
    // dombuilder should be null when not initialized
    assert dombuilder == null;
// end::dombuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::dombuilder_nullobject2[]
    DOMBuilder dombuilder = null;
    
    // dombuilder should be null when initialized to null
    assert dombuilder == null;
// end::dombuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::dombuilder_object_does_not_exists1[]
    shouldFail
    {
        // Could not find which method <init>() to invoke from this list as Document or DocumentBuilder
        // required in constructor - empty parm not allowed 
        DOMBuilder dombuilder = new DOMBuilder();
    }
// end::dombuilder_object_does_not__exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::dombuilder_object_exists2[]
     // Could not find which method <init>() to invoke from this list:
     // public groovy.xml.DOMBuilder#<init>(javax.xml.parsers.DocumentBuilder)
     // public groovy.xml.DOMBuilder#<init>(org.w3c.dom.Document)
    shouldFail
    {
        DOMBuilder dombuilder = new DOMBuilder();
    }
// end::dombuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::dombuilder_object_exists3[]
    // dombuilder does not throw an exception when null parm used in constructor instead of Document
    DOMBuilder dombuilder = new DOMBuilder(null);
// end::dombuilder_object_exists3[]
    } // end of method

} // end of DomBuilder class