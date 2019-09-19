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

import groovy.test.GroovyTestCase

/**
 * Fix for https://issues.apache.org/jira/browse/GROOVY-3871
 */
class Groovy3871Bug extends GroovyTestCase {

    protected void setUp() {
        super.setUp()
        G3871Base.metaClass = null
        G3871Child.metaClass = null
    }

    protected void tearDown() {
        G3871Base.metaClass = null
        G3871Child.metaClass = null
        super.tearDown();
    }

    void testPropertyMissingInheritanceIssue() {
        // defining a propertyMissing on the base class
        G3871Base.metaClass.propertyMissing = { String name -> name }
        def baseInstance = new G3871Base()
        assert baseInstance.someProp == "someProp"

        // the child class inherits the propertyMissing
        def childInstance = new G3871Child()
        assert childInstance.otherProp == "otherProp"

        // when a propertyMissing is registered for the child
        // it should be used over the inherited one
        G3871Child.metaClass.propertyMissing = { String name -> name.reverse() }
        def otherChildInstance = new G3871Child()
        assert otherChildInstance.otherProp == "porPrehto"
    }
}

/** a dummy base class */
class G3871Base { }

/** a dummy child class */
class G3871Child extends G3871Base { }

