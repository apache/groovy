/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.util;

import groovy.lang.GroovyObject;
import org.codehaus.groovy.classgen.TestSupport;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * Tests using the GroovyObject API from Java to access MBeans via
 * the normal properties API (to simulate normal Groovy property access)
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MBeanTest extends TestSupport {

    public void testGetProperty() throws Exception {
        MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();
        ObjectName name = new ObjectName("groovy.test:role=TestMBean,type=Dummy");
        mbeanServer.registerMBean(new Dummy(), name);

        assertEquals("JMX value of Name", "James", mbeanServer.getAttribute(name, "Name"));

        GroovyObject object = new GroovyMBean(mbeanServer, name);

        Object value = object.getProperty("Name");
        assertEquals("Name property", "James", value);

        object.setProperty("Name", "Bob");
        assertEquals("Name property", "Bob", object.getProperty("Name"));

        // now let's look up the name via JMX to check
        assertEquals("JMX value of Name", "Bob", mbeanServer.getAttribute(name, "Name"));
    }
}
