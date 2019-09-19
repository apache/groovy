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
package org.codehaus.groovy.reflection.utils

import groovy.test.GroovyTestCase
import org.codehaus.groovy.reflection.ReflectionUtils

// note, this must be in a package other than org.codehause.groovy.reflection or else
// the tests will incorrectly miss the target

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Jun 24, 2008
 * Time: 9:45:15 PM
 */
class ReflectionUtilsTest extends GroovyTestCase {

    private Class privateCaller() {
        return ReflectionUtils.getCallingClass()
    }

    public Class publicCaller() {
        return ReflectionUtils.getCallingClass()
    }

    public void testMethodCallingClass() {
        if (ReflectionUtils.isCallingClassReflectionAvailable()) {
            assert privateCaller() == ReflectionUtilsTest
            assert publicCaller() == ReflectionUtilsTest

            // make sure we don't reflect ourselves as the caller of testMethodCallingClass
            assert ReflectionUtils.getCallingClass() != ReflectionUtilsTest
        }
    }

    public void testPogoMethodCallingClass() {
        if (ReflectionUtils.isCallingClassReflectionAvailable()) {
            assert PogoCalleTestClass.staticClassCaller() == ReflectionUtilsTest
            assert new PogoCalleTestClass().instanceCaller() == ReflectionUtilsTest
        }
    }

    public void testPojoMethodCallingClass() {
        if (ReflectionUtils.isCallingClassReflectionAvailable()) {
            assert PojoCallerTestClass.staticClassCaller() == ReflectionUtilsTest
            assert new PojoCallerTestClass().instanceCaller() == ReflectionUtilsTest
        }
    }

    public void testMetaMethodCallingClass() {
        if (ReflectionUtils.isCallingClassReflectionAvailable()) {
            MetaClassRegistry mcr = GroovySystem.getMetaClassRegistry()
            mcr.removeMetaClass PojoCallerTestClass
            mcr.removeMetaClass PogoCalleTestClass

            Closure testClosure = {-> return ReflectionUtils.getCallingClass()}
            PojoCallerTestClass.metaClass.metaInstanceCaller = testClosure
            PogoCalleTestClass.metaClass.metaInstanceCaller = testClosure

            PojoCallerTestClass.metaClass.'static'.metaStaticCaller = testClosure
            PogoCalleTestClass.metaClass.'static'.metaStaticCaller = testClosure

            assert PojoCallerTestClass.metaStaticCaller() == ReflectionUtilsTest
            assert new PojoCallerTestClass().metaInstanceCaller() == ReflectionUtilsTest

            assert PogoCalleTestClass.metaStaticCaller() == ReflectionUtilsTest
            assert new PogoCalleTestClass().metaInstanceCaller() == ReflectionUtilsTest
        }
    }

}

class PogoCalleTestClass {
    public static Class staticClassCaller() {
        return ReflectionUtils.getCallingClass()
    }

    public Class instanceCaller() {
        return ReflectionUtils.getCallingClass()
    }
}