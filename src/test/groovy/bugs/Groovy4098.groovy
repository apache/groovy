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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail
import static java.lang.reflect.Modifier.isFinal

final class Groovy4098 {

    public String propertyOne
    public String propertyTwo
    public String propertyThree
    public String propertyFour
    public final String propertyFive = 'five normal'
    public final String propertySix = 'six normal'

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

    //--------------------------------------------------------------------------

    @Test
    void test1() {
        propertyOne = 'one normal'
        assert propertyOne == 'one normal'

        def metaProperty = getMetaClass().getMetaProperty('propertyOne')
        metaProperty.setProperty(this, 'one mop')
        assert metaProperty.getProperty(this) == 'one mop'
    }

    @Test
    void test2() {
        propertyTwo = 'two normal'
        assert propertyTwo == 'two normal'

        def metaProperty = getMetaClass().getMetaProperty('propertyTwo')
        metaProperty.setProperty(this, 'two mop')
        assert metaProperty.getProperty(this) == 'two mop'
    }

    @Test
    void test3() {
        propertyThree = 'three normal'
        assert propertyThree == 'three normal'

        def metaProperty = getMetaClass().getMetaProperty('propertyThree')
        assert metaProperty.getProperty(this) == 'three normal'
        metaProperty.setProperty(this, 'three mop')
        assert metaProperty.getProperty(this) == 'three mop'
    }

    @Test
    void test4() {
        propertyOne = 'four normal'
        assert propertyOne == 'four normal'

        def metaProperty = getMetaClass().getMetaProperty('propertyFour')
        metaProperty.setProperty(this, 'four mop')
        assert metaProperty.getProperty(this) == 'four mop'
    }

    @Test
    void test5() {
        assert propertyFive == 'five normal'

        def metaProperty = getMetaClass().getMetaProperty('propertyFive')
        assert isFinal(metaProperty.getModifiers()) // GROOVY-11562
        assert metaProperty.getProperty(this) == 'five normal'
        def err = shouldFail {
            metaProperty.setProperty(this, 'five mop')
        }
        assert err.message =~ /Cannot set read-only property: propertyFive/
    }

    @Test
    void test6() {
        assert propertySix == 'six normal'

        def metaProperty = getMetaClass().getMetaProperty('propertySix')
        assert metaProperty.getProperty(this) == 'six normal'
        def err = shouldFail {
            metaProperty.setProperty(this, 'six mop')
        }
        assert err.message =~ /Cannot set the property 'propertySix' because the backing field is final./
    }

    //

    @Test
    void testProtected1() {
        def p = new Groovy4098Child()
        p.propertyOne = 'one normal'
        assert p.propertyOne == 'one normal'

        def metaProperty = p.getMetaClass().getMetaProperty('propertyOne')
        metaProperty.setProperty(p, 'one mop')
        assert metaProperty.getProperty(p) == 'one mop'
    }

    @Test
    void testProtected2() {
        def p = new Groovy4098Child()
        p.propertyTwo = 'two normal'
        assert p.propertyTwo == 'two normal'

        def metaProperty = p.getMetaClass().getMetaProperty('propertyTwo')
        metaProperty.setProperty(p, 'two mop')
        assert metaProperty.getProperty(p) == 'two mop'
    }

    @Test
    void testProtected3() {
        def p = new Groovy4098Child()
        p.propertyThree = 'three normal'
        assert p.propertyThree == 'three normal'

        def metaProperty = p.getMetaClass().getMetaProperty('propertyThree')
        assert metaProperty.getProperty(p) == 'three normal'
        metaProperty.setProperty(p, 'three mop')
        assert metaProperty.getProperty(p) == 'three mop'
    }

    @Test
    void testProtected4() {
        def p = new Groovy4098Child()
        p.propertyOne = 'four normal'
        assert p.propertyOne == 'four normal'

        def metaProperty = p.getMetaClass().getMetaProperty('propertyFour')
        metaProperty.setProperty(p, 'four mop')
        assert metaProperty.getProperty(p) == 'four mop'
    }
}
