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
package groovy.bugs

//  The order of the classes is crucial, the first must be the GroovyTestCase.  Its name doesn't
//  matter it just has to be first.

/**
 * Test class and support to realize the GROOVY-662 test.  There is a difference between
 * improper uses of properties between Groovy defined classes and Java defined classes.  There
 * is no difference between correct uses so this is not a problem just an anti-regression test.
 */
class Groovy662 extends GroovyTestCase {
    private String expected = "Hello"

    private usePropertyCorrectly(def object) { return object.@myProperty }

    private usePropertyIncorrectly(def object) { return object.myProperty }

    private useMethod(def object) { return object.getMyProperty() }

    private void doAssertions(def object) {
        assertTrue(useMethod(object) == expected)
        assertTrue(usePropertyCorrectly(object) == expected)
    }

    private String theTestScriptDefinitions = """
        String expected = "Hello"
        def usePropertyCorrectly ( def object ) { return object.@myProperty }
        def usePropertyIncorrectly ( def object ) { return object.myProperty }
        def useMethod ( def object ) { return object.getMyProperty ( ) }
    """

    private String theTestScriptAssertions = """
        assert useMethod ( object ) == expected
        assert usePropertyCorrectly ( object ) == expected
    """

    public void testJavaClass() {
        def object = new groovy.bugs.Groovy662_JavaClass()
        doAssertions(object)
        assertTrue(usePropertyIncorrectly(object) == null)
    }

    public void testGroovyClass() {
        def object = new Groovy662_GroovyClass()
        doAssertions(object)
        assertTrue(usePropertyIncorrectly(object) == null)
    }

    public void testJavaClassAsScript() {
        assertScript(theTestScriptDefinitions + """
            def object = new groovy.bugs.Groovy662_JavaClass ( )
        """ + theTestScriptAssertions + """
            assert usePropertyIncorrectly ( object ) == null
        """)
    }

    public void testGroovyClassAsScript() {
        assertScript(theTestScriptDefinitions + """
            class Groovy662_GroovyClass extends HashMap {
                String myProperty = "Hello"
                public String getMyProperty ( ) { return myProperty }
            }
            def object = new Groovy662_GroovyClass ( )
        """ + theTestScriptAssertions + """
            assert usePropertyIncorrectly ( object ) == null
        """)
    }
}

class Groovy662_GroovyClass extends HashMap {
    String myProperty = "Hello"

    public String getMyProperty() {
        return myProperty
    }
}
