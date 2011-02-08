/*
 * Copyright 2003-2011 the original author or authors.
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
package org.codehaus.groovy.runtime;

import groovy.util.GroovyTestCase;

import java.util.Iterator;
import java.util.Map;


/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class TupleListTest extends GroovyTestCase {

    public void testIterateOverTuple() throws Exception {
        StringBuffer buffer = new StringBuffer();
        for (Iterator iter = InvokerHelper.asIterator(InvokerHelper.createTuple(new Object[]{"a", "b", "c"}));
             iter.hasNext();
                ) {
            Object i = iter.next();
            buffer.append(i);
        }

        assertEquals("buffer", "abc", buffer.toString());
    }

    public void testIterateOverList() throws Exception {
        StringBuffer buffer = new StringBuffer();
        for (Iterator iter = InvokerHelper.asIterator(InvokerHelper.createList(new Object[]{"a", "b", "c"}));
             iter.hasNext();
                ) {
            Object i = iter.next();
            buffer.append(i);
        }

        assertEquals("buffer", "abc", buffer.toString());
    }

    public void testCreateMap() throws Exception {
        Map map = InvokerHelper.createMap(new Object[]{"a", "x", "b", "y"});

        assertNotNull("map", map);
        assertEquals("size", 2, map.size());
        assertEquals("value of a", "x", map.get("a"));
        assertEquals("value of b", "y", map.get("b"));
    }
}
