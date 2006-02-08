/* Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.servlet;

import junit.framework.TestCase;

/**
 * @author Graeme Rocher
 * @since 07-Feb-2006
 */
public class GrailsFlashScopeTests extends TestCase {

    public void testNextState() {
        FlashScope fs = new GrailsFlashScope();
        fs.put("test","value");
        fs.put("fred","flintstone");

        assertFalse(fs.isEmpty());
        assertEquals("flintstone",fs.get("fred"));
        assertEquals(2, fs.size());
        assertTrue(fs.containsKey("test"));
        assertTrue(fs.containsValue("value"));
        assertFalse(fs.containsKey("wilma"));

        // the state immediately following this one the map should still contain the previous
        // entries
        fs.next();

        assertFalse(fs.isEmpty());
        assertEquals("flintstone",fs.get("fred"));
        assertEquals(2, fs.size());
        assertTrue(fs.containsKey("test"));
        assertTrue(fs.containsValue("value"));
        assertFalse(fs.containsKey("wilma"));

        // the next state it should be empty
        fs.next();

        assertTrue(fs.isEmpty());
        assertEquals(0,fs.size());

    }
}
