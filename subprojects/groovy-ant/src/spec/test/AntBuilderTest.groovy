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
package groovy.ant
import groovy.util.GroovyTestCase
import org.junit.Test

/**
* Tests for AntBuilder. The tests directly in this file are specific
* to AntBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class AntBuilderTest  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::antbuilder_nullobject1[]
    AntBuilder antbuilder;
    
    // antbuilder should be null when not initialized
    assert antbuilder == null;
// end::antbuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::antbuilder_nullobject2[]
    AntBuilder antbuilder = null;
    
    // antbuilder should be null when initialized to null
    assert antbuilder == null;
// end::antbuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::antbuilder_object_exists1[]
    AntBuilder antbuilder = new AntBuilder();
    
    // antbuilder should not be null after construction
    assert antbuilder != null;
// end::antbuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::antbuilder_object_exists2[]
    AntBuilder antbuilder = new AntBuilder();
    
    // antbuilder should be an instance of correct AntBuilder class
    assert antbuilder instanceof AntBuilder, 'default AntBuilder constructor did not build a version of AntBuilder'
// end::antbuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::antbuilder_object_exists3[]
    shouldFail 
    {
        // antbuilder should throw an exception when null parm used in constructor
        AntBuilder antbuilder = new AntBuilder(null);
    }    
// end::antbuilder_object_exists3[]
    } // end of method
    
        
    void testObjectEcho() {
// tag::antbuilder_echo4[]        
    def ant = new AntBuilder()
    ant.echo(message:"message via attribute!")         
    ant.echo("Hello World!")
// end::antbuilder_echo4[]
    } // end of method
    
    void testObjectNullEcho() {
// tag::antbuilder_echo5[]        
    def ant = new AntBuilder()
    ant.echo(message:null)         
// end::antbuilder_echo5[]
    } // end of method
    
    void testObjectEmptyEcho() {
// tag::antbuilder_echo6[]        
    shouldFail
    {
        def ant = new AntBuilder()
        ant.echo(null)
    }
// end::antbuilder_echo6[]
    } // end of method

} // end of AntBuilder class