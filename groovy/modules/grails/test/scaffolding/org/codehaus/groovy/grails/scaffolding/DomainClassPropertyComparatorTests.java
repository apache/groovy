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
package org.codehaus.groovy.grails.scaffolding;

import groovy.lang.GroovyClassLoader;
import junit.framework.TestCase;
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;

/**
 * @author Graeme Rocher
 * @since 10-Feb-2006
 */
public class DomainClassPropertyComparatorTests extends TestCase {

    public void testPropertyComparator() throws Exception {
        GroovyClassLoader gcl = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());

        Class dc = gcl.parseClass("class Test { " +
                                        "\n@Property Long id;" +
                                        "\n@Property Long version;" +
                                        "\n@Property String name;" +
                                        "\n@Property Date age" +
                                        "\n@Property String zip" +
                                        "\n@Property String dob" +
                                        "\n@Property constraints = {" +
                                        "\n  name(length:5..15)" +
                                        "\n  age()" +
                                        "}  }");
        GrailsDomainClass domainClass = new DefaultGrailsDomainClass(dc);

        DomainClassPropertyComparator comp = new DomainClassPropertyComparator(domainClass);

        GrailsDomainClassProperty[] props =domainClass.getProperties();
        Arrays.sort(props,comp);
        System.out.println(ArrayUtils.toString(props));
        assertEquals("id",props[0].getName());
        assertEquals("name",props[1].getName());
        assertEquals("age",props[2].getName());
        assertEquals("dob",props[3].getName());
        assertEquals("version",props[4].getName());
        assertEquals("zip",props[5].getName());
    }
}
