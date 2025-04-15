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

import groovy.test.GroovyTestCase

/**
  * Fixes GROOVY-558 and GROOVY-616.
  * A fully qualified class name ending with .class or not were not recognized properly.
  */
class Groovy558_616_Bug extends GroovyTestCase {

    void testListClass() {
        assert java.util.ArrayList.class == ArrayList.class
        assert java.util.ArrayList.class == ArrayList
        assert ArrayList != Class
        def list = new ArrayList()
        assert list.class == ArrayList
    }

    void testStringClass() {
        assert java.lang.String.class == String.class
        assert java.lang.String.class == String
        assert String != Class
        def st = ""
        assert st.class == String
    }

    void testExpandoClass() {
        assert groovy.util.Expando.class == Expando.class
        assert groovy.util.Expando.class == Expando
        assert Expando != Class
        def dum = new Expando()
        assert dum.class == Expando
    }

    void testFooClass() {
        assert bugs.Groovy558_616_Bug.class == Groovy558_616_Bug
        assert Groovy558_616_Bug != Class
        def f = new Groovy558_616_Bug()
        assert f.class == Groovy558_616_Bug
    }
}

