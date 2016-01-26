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
package org.codehaus.groovy.tools

class LoaderConfigurationTest extends GroovyTestCase {

    void testComment() {
        def txt = "# I am a comment"

        def config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 0
    }

    void testNormalPath() {
        // generate a load instruction with a valid path
        def file = new File(".")
        def txt = "load $file"

        def config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 1
        assert config.classPathUrls[0].sameFile(file.toURI().toURL())
    }

    void testNonexistingPath() {
        // generate a load instruction with a non-existing path
        def file = getNonexistantFile(new File("."))

        def txt = "load $file"

        def config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 0
    }

    private File getNonexistantFile(File base) {
        def number = "0"
        while (base.exists()) {
            base = new File(base, number)
            number++
        }
        return base
    }

    void testExistingProperty() {
        def txt = 'load ${java.home}'

        def config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 1
        def url1 = config.classPathUrls[0]
        def url2 = new File(System.getProperty("java.home")).toURI().toURL()
        assert url1.sameFile(url2)
    }

    void testPropertyDefn() {
        System.setProperty('myprop', 'baz')
        def txt = 'property foo1=bar\nproperty foo2=${myprop}\nproperty foo3=!{myprop}'

        def config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))
        assert System.getProperty('foo1') == 'bar'
        assert System.getProperty('foo2') == 'baz'
        assert System.getProperty('foo3') == 'baz'
    }

    void testNonexistingProperty() {
        String name = getNonexistingPropertyName("foo")

        def txt = 'load !{' + name + '}'

        def config = new LoaderConfiguration()
        config.requireMain = false
        shouldFail {
            config.configure(new StringBufferInputStream(txt))
        }

        txt = 'load ${' + name + '}'

        config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 0
    }

    private getNonexistingPropertyName(String base) {
        while (System.getProperty(base) != null) {
            base += "x"
        }
        return base
    }
    
    void testSlashCorrection() {
        def prop = getNonexistingPropertyName("nope")
        System.setProperty("prop",'/')
        
        def txt = 'load ${prop}/'

        def config = new LoaderConfiguration()
        config.requireMain = false
        config.configure(new StringBufferInputStream(txt))

        assert config.classPathUrls.length == 1
        def url = config.classPathUrls[0]
        assert !url.path.endsWith("//")
        System.setProperty("prop","")
    }
}