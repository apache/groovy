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
package gls.ch06.s05;

import gls.ch06.s05.testClasses.Tt1cgi;
import gls.ch06.s05.testClasses.Tt1cgo;
import gls.ch06.s05.testClasses.Tt1gi;
import gls.ch06.s05.testClasses.Tt1go;
import groovy.lang.Closure;
import junit.framework.TestCase;

public class JName1Test extends TestCase {
    public void testObjectSupportNameHandling() {
        final Tt1go obj = new Tt1go();  // Test subclass of GroovyObjectSupport
        final String newX = "new x";
        final String newX1 = "new x1";
        final String newX2 = "new x2";
        final String newX3 = "new x3";

        assertTrue(obj.getProperty("x") == obj.getX());
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == obj.x);
        assertTrue(obj.invokeMethod("x", new Object[]{}) == obj.x());

        obj.setProperty("x", newX);
        obj.getMetaClass().setAttribute(obj, "x", newX1);

        assertTrue(obj.getProperty("x") == newX);
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == newX1);

        obj.setX(newX2);
        obj.x = newX3;

        assertTrue(obj.getProperty("x") == newX2);
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == newX3);
    }

    public void testObjectSupportNameHandling1() {
        final Tt1go obj = new Tt1go() {
        }; // repeat test with subclass
        final String newX = "new x";
        final String newX1 = "new x1";
        final String newX2 = "new x2";
        final String newX3 = "new x3";

        assertTrue(obj.getProperty("x") == obj.getX());
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == obj.x);
        assertTrue(obj.invokeMethod("x", new Object[]{}) == obj.x());

        obj.setProperty("x", newX);
        obj.getMetaClass().setAttribute(obj, "x", newX1);

        assertTrue(obj.getProperty("x") == newX);
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == newX1);

        obj.setX(newX2);
        obj.x = newX3;

        assertTrue(obj.getProperty("x") == newX2);
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == newX3);
    }

    public void testObjectSupportNameHandlingWitnClosureValues() {
        final Tt1cgo obj = new Tt1cgo();  // Test subclass of GroovyObjectSupport
        final Closure newX = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x";
            }
        };
        final Closure newX1 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x1";
            }
        };
        final Closure newX2 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x2";
            }
        };
        final Closure newX3 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x3";
            }
        };

        assertTrue(((Closure) obj.getProperty("x")).call() == obj.getX().call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == obj.x.call());
        assertTrue(obj.invokeMethod("x", new Object[]{}) == obj.x());

        obj.setProperty("x", newX);
        obj.getMetaClass().setAttribute(obj, "x", newX1);

        assertTrue(((Closure) obj.getProperty("x")).call() == newX.call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == newX1.call());

        obj.setX(newX2);
        obj.x = newX3;

        assertTrue(((Closure) obj.getProperty("x")).call() == newX2.call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == newX3.call());
    }

    public void testObjectSupportNameHandlingWitnClosureValuesi() {
        final Tt1cgo obj = new Tt1cgo() {
        };  // repeat test with subclass
        final Closure newX = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x";
            }
        };
        final Closure newX1 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x1";
            }
        };
        final Closure newX2 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x2";
            }
        };
        final Closure newX3 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x3";
            }
        };

        assertTrue(((Closure) obj.getProperty("x")).call() == obj.getX().call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == obj.x.call());
        assertTrue(obj.invokeMethod("x", new Object[]{}) == obj.x());

        obj.setProperty("x", newX);
        obj.getMetaClass().setAttribute(obj, "x", newX1);

        assertTrue(((Closure) obj.getProperty("x")).call() == newX.call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == newX1.call());

        obj.setX(newX2);
        obj.x = newX3;

        assertTrue(((Closure) obj.getProperty("x")).call() == newX2.call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == newX3.call());
    }

    public void testMetaClassNameHandling() {
        final Tt1gi obj = new Tt1gi();  // Test class implementing GroovyObject
        final String newX = "new x";
        final String newX1 = "new x1";
        final String newX2 = "new x2";
        final String newX3 = "new x3";

        assertTrue("dynamic property".equals(obj.getProperty("x")));
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == obj.x);
        assertTrue("dynamic method".equals(obj.invokeMethod("x", new Object[]{})));

        obj.setProperty("x", newX);
        obj.getMetaClass().setAttribute(obj, "x", newX1);

        assertTrue("dynamic property".equals(obj.getProperty("x")));
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == newX1);

        obj.setX(newX2);
        obj.x = newX3;

        assertTrue("dynamic property".equals(obj.getProperty("x")));
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == newX3);
    }

    public void testMetaClassNameHandling1() {
        final Tt1gi obj = new Tt1gi() {
        }; // repeat test with subclass
        final String newX = "new x";
        final String newX1 = "new x1";
        final String newX2 = "new x2";
        final String newX3 = "new x3";

        assertTrue("dynamic property".equals(obj.getProperty("x")));
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == obj.x);
        assertTrue("dynamic method".equals(obj.invokeMethod("x", new Object[]{})));

        obj.setProperty("x", newX);
        obj.getMetaClass().setAttribute(obj, "x", newX1);

        assertTrue("dynamic property".equals(obj.getProperty("x")));
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == newX1);

        obj.setX(newX2);
        obj.x = newX3;

        assertTrue("dynamic property".equals(obj.getProperty("x")));
        assertTrue(obj.getMetaClass().getAttribute(obj, "x") == newX3);
    }

    public void testMetaClassNameHandlingWithClosures() {
        final Tt1cgi obj = new Tt1cgi();  // Test class implementing GroovyObject
        final Closure newX = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x";
            }
        };
        final Closure newX1 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x1";
            }
        };
        final Closure newX2 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x2";
            }
        };
        final Closure newX3 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x3";
            }
        };

        assertTrue(((Closure) obj.getProperty("x")).call() == obj.getX().call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == obj.x.call());
        assertTrue(obj.invokeMethod("x", new Object[]{}) == obj.x());

        obj.setProperty("x", newX);
        obj.getMetaClass().setAttribute(obj, "x", newX1);

        assertTrue(((Closure) obj.getProperty("x")).call() == newX.call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == newX1.call());

        obj.setX(newX2);
        obj.x = newX3;

        assertTrue(((Closure) obj.getProperty("x")).call() == newX2.call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == newX3.call());
    }

    public void testMetaClassNameHandlingWithClosures1() {
        final Tt1cgi obj = new Tt1cgi() {
        };  // repeat test with subclass
        final Closure newX = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x";
            }
        };
        final Closure newX1 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x1";
            }
        };
        final Closure newX2 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x2";
            }
        };
        final Closure newX3 = new Closure(null) {
            public Object doCall(final Object params) {
                return "new x3";
            }
        };

        assertTrue(((Closure) obj.getProperty("x")).call() == obj.getX().call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == obj.x.call());
        assertTrue(obj.invokeMethod("x", new Object[]{}) == obj.x());

        obj.setProperty("x", newX);
        obj.getMetaClass().setAttribute(obj, "x", newX1);

        assertTrue(((Closure) obj.getProperty("x")).call() == newX.call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == newX1.call());

        obj.setX(newX2);
        obj.x = newX3;

        assertTrue(((Closure) obj.getProperty("x")).call() == newX2.call());
        assertTrue(((Closure) obj.getMetaClass().getAttribute(obj, "x")).call() == newX3.call());
    }
}
