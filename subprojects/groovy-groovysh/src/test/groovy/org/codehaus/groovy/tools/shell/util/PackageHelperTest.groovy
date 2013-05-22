/*
 * Copyright 2003-2007 the original author or authors.
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

package org.codehaus.groovy.tools.shell.util

/**
 * Unit tests for the {@link MessageSource} class.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class PackageHelperTest
    extends GroovyTestCase
{

    void testLoadAndGetPackagesEmpty() {
        PackageHelper helper = new PackageHelper(null)
        Set<String> rootPackages = helper.getContents("")
        assertNotNull(rootPackages)
        assertTrue(rootPackages.toString(), rootPackages.contains("java"))
        assertTrue(rootPackages.toString(), rootPackages.contains("javax"))
        assertTrue(rootPackages.toString(), rootPackages.contains("groovy"))
    }

    void testLoadAndGetPackagesJava() {
        PackageHelper helper = new PackageHelper(null)
        Set<String> names = helper.getContents("java")
        assertNotNull(names)
        assertTrue(names.toString(), names.contains('io'))
    }

    void testLoadAndGetPackagesJavaUtil() {
        PackageHelper helper = new PackageHelper(null)
        Set<String> names = helper.getContents("java.util")
        assertNotNull(names)
        assertTrue(names.toString(), names.contains('zip'))
        assertTrue(names.toString(), names.contains('Set'))
    }

    void testLoadAndGetPackagesInvalid() {
        PackageHelper helper = new PackageHelper(null)
        assertEquals(null, helper.getContents("invalid:name"))
    }

    void testLoadAndGetPackagesUnknown() {
        PackageHelper helper = new PackageHelper(null)
        assertEquals(null, helper.getContents("java.util.regex.tools"))
    }
}