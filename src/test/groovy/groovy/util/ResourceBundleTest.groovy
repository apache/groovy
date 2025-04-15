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
package groovy.util

import groovy.test.GroovyTestCase;

public class ResourceBundleTest extends GroovyTestCase {
    public void testNoClassLoaderNoLocale() {
        def results = []
        // run test twice, call site optimizations result in call stack differences
        2.times {
            ResourceBundle rb = ResourceBundle.getBundle("groovy.util.i18n")
            results << rb
            assert rb.getString('upvote') == '+1'
        }
        assert results.size() == 2
    }

    public void testWithLocale() {
        def results = []
        // run test twice, call site optimizations result in call stack differences
        2.times {
            ResourceBundle rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.ENGLISH)
            results << rb
            println "en"
            println "'${rb.getString('yes')}'"
            println "'${rb.getString('upvote')}'"
            assert rb.getString('yes') == 'yes'
            assert rb.getString('upvote') == '+1'
            rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.FRENCH)
            results << rb
            println "fr"
            println "'${rb.getString('yes')}'"
            println "'${rb.getString('upvote')}'"
            assert rb.getString('yes') == 'oui'
            assert rb.getString('upvote') == '+1'
        }
        assert results.size() == 4
    }

    public void testWithClassLoader() {
        def results = []
        ClassLoader cl = this.class.classLoader
        // run test twice, call site optimizations result in call stack differences
        2.times {
            ResourceBundle rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.ENGLISH, cl)
            results << rb
            println "en"
            println "'${rb.getString('yes')}'"
            println "'${rb.getString('upvote')}'"
            assert rb.getString('yes') == 'yes'
            assert rb.getString('upvote') == '+1'
            rb = ResourceBundle.getBundle("groovy.util.i18n", Locale.FRENCH, cl)
            results << rb
            println "fr"
            println "'${rb.getString('yes')}'"
            println "'${rb.getString('upvote')}'"
            assert rb.getString('yes') == 'oui'
            assert rb.getString('upvote') == '+1'
        }
        assert results.size() == 4
    }
}
