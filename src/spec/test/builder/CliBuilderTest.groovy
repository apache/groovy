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

/**
* Tests for CliBuilder. The tests directly in this file are specific
* to CliBuilder. Functionality in common with other Builders
* is tested in the parent class.
*
* @author Groovy Documentation Community
*/
class CliBuilderTest  extends GroovyTestCase {

    void testObjectNotDefined() {
// tag::clibuilder_nullobject1[]
    CliBuilder clibuilder;
    
    // clibuilder should be null when not initialized
    assert clibuilder == null;
// end::clibuilder_nullobject1[]
    } // end of method


    void testObjectDefinedNull() {
// tag::clibuilder_nullobject2[]
    CliBuilder clibuilder = null;
    
    // clibuilder should be null when initialized to null
    assert clibuilder == null;
// end::clibuilder_nullobject2[]
    } // end of method


    void testObjectDefinedDefaultConstructor() {
// tag::clibuilder_object_exists1[]
    CliBuilder clibuilder = new CliBuilder();
    
    // clibuilder should not be null after construction
    assert clibuilder != null;
// end::clibuilder_object_exists1[]
    } // end of method


    void testObjectDefinedAsInstanceOf() {
// tag::clibuilder_object_exists2[]
    CliBuilder clibuilder = new CliBuilder();
    
    // clibuilder should be an instance of correct CliBuilder class
    assert clibuilder instanceof CliBuilder, 'default CliBuilder constructor did not build a version of CliBuilder'
// end::clibuilder_object_exists2[]
    } // end of method


    void testObjectDefinedConstructorNullParm() {
// tag::clibuilder_object_exists3[]
    shouldFail
    {
    // clibuilder should throw an exception when null parm used in constructor
        CliBuilder clibuilder = new CliBuilder(null);
    }    
// end::clibuilder_object_exists3[]
    } // end of method

} // end of CliBuilder class