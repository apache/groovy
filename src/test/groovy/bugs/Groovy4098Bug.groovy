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

class Groovy4098Bug extends GroovyTestCase {
    public String propertyOne
    public String propertyTwo
    public String propertyThree
    public String propertyFour
    public final String propertyFive = "five normal"
    public final String propertySix = "six normal"

    void setPropertyTwo(String propertyTwo) {
        this.propertyTwo = propertyTwo
    }

    String getPropertyThree() {
        propertyThree
    }

    String getPropertyFive() {
        propertyFive
    }

    String getPropertyFour() {
        propertyFour
    }

    void setPropertyFour(String propertyFour) {
        this.propertyFour = propertyFour
    }

    void testOne() {
        propertyOne = "one normal"
        assert propertyOne == "one normal"

        def metaProperty = this.metaClass.getMetaProperty("propertyOne")
        metaProperty.setProperty(this, "one mop")
        assert metaProperty.getProperty(this) == "one mop"
    }

    void testTwo() {
        propertyTwo = "two normal"
        assert propertyTwo == "two normal"

        def metaProperty = this.metaClass.getMetaProperty("propertyTwo")
        metaProperty.setProperty(this, "two mop")
        assert metaProperty.getProperty(this) == "two mop"
    }

    void testThree() {
        propertyThree = "three normal"
        assert propertyThree == "three normal"

        def metaProperty = this.metaClass.getMetaProperty("propertyThree")
        assert metaProperty.getProperty(this) == "three normal"
        metaProperty.setProperty(this, "three mop")
        assert metaProperty.getProperty(this) == "three mop"
    }

    void testFour() {
        propertyOne = "four normal"
        assert propertyOne == "four normal"

        def metaProperty = this.metaClass.getMetaProperty("propertyFour")
        metaProperty.setProperty(this, "four mop")
        assert metaProperty.getProperty(this) == "four mop"
    }

    void testFive() {
        assert propertyFive == "five normal"

        def metaProperty = this.metaClass.getMetaProperty("propertyFive")
        assert metaProperty.getProperty(this) == "five normal"
        def msg = shouldFail {
            metaProperty.setProperty(this, "five mop")
        }
        assert msg == "Cannot set read-only property: propertyFive"
    }

    void testSix() {
        assert propertySix == "six normal"

        def metaProperty = this.metaClass.getMetaProperty("propertySix")
        assert metaProperty.getProperty(this) == "six normal"
        def msg = shouldFail {
            metaProperty.setProperty(this, "six mop")
        }
        assert msg == "Cannot set the property 'propertySix' because the backing field is final."
    }

    void testOneProtected() {
        def p = new Groovy4098Child()
        p.propertyOne = "one normal"
        assert p.propertyOne == "one normal"

        def metaProperty = p.metaClass.getMetaProperty("propertyOne")
        metaProperty.setProperty(p, "one mop")
        assert metaProperty.getProperty(p) == "one mop"
    }

    void testTwoProtected() {
        def p = new Groovy4098Child()
        p.propertyTwo = "two normal"
        assert p.propertyTwo == "two normal"

        def metaProperty = p.metaClass.getMetaProperty("propertyTwo")
        metaProperty.setProperty(p, "two mop")
        assert metaProperty.getProperty(p) == "two mop"
    }

    void testThreeProtected() {
        def p = new Groovy4098Child()
        p.propertyThree = "three normal"
        assert p.propertyThree == "three normal"

        def metaProperty = p.metaClass.getMetaProperty("propertyThree")
        assert metaProperty.getProperty(p) == "three normal"
        metaProperty.setProperty(p, "three mop")
        assert metaProperty.getProperty(p) == "three mop"
    }

    void testFourProtected() {
        def p = new Groovy4098Child()
        p.propertyOne = "four normal"
        assert p.propertyOne == "four normal"

        def metaProperty = p.metaClass.getMetaProperty("propertyFour")
        metaProperty.setProperty(p, "four mop")
        assert metaProperty.getProperty(p) == "four mop"
    }

}