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

import junit.framework.TestCase;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass;

import java.io.File;

/**
 * @author Graeme Rocher
 * @since 09-Feb-2006
 */
public class GrailsTemplateGeneratorsTests extends TestCase {

    public void testGenerateController() throws Exception {
        GrailsTemplateGenerator generator;

        GroovyClassLoader gcl = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());

        generator = (GrailsTemplateGenerator)gcl.parseClass(gcl.getResourceAsStream("org/codehaus/groovy/grails/scaffolding/DefaultGrailsTemplateGenerator.groovy"))
                                                    .newInstance();


        Class dc = gcl.parseClass("class Test { \n@Property Long id;\n @Property Long version;  }");
        GrailsDomainClass domainClass = new DefaultGrailsDomainClass(dc);

        generator.generateController(domainClass,"test");

        File generatedFile = new File("test/TestController.groovy");
        assertTrue(generatedFile.exists());

        String text = (String)new GroovyShell().evaluate("new File('test/TestController.groovy').text");

        assertTrue(text.indexOf("class TestController") > -1);
        assertTrue(text.indexOf("@Property list") > -1);
        assertTrue(text.indexOf("@Property update") > -1);
        assertTrue(text.indexOf("@Property create") > -1);
        assertTrue(text.indexOf("@Property show") > -1);
        assertTrue(text.indexOf("@Property edit") > -1);
        assertTrue(text.indexOf("@Property delete") > -1);
    }

    public void testGenerateViews() throws Exception {
        GrailsTemplateGenerator generator;

        GroovyClassLoader gcl = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());

        generator = (GrailsTemplateGenerator)gcl.parseClass(gcl.getResourceAsStream("org/codehaus/groovy/grails/scaffolding/DefaultGrailsTemplateGenerator.groovy"))
                                                    .newInstance();


        Class dc = gcl.parseClass("class Test { " +
                                        "\n@Property Long id;" +
                                        "\n@Property Long version;" +
                                        "\n@Property String name;" +
                                        "\n@Property TimeZone tz;" +
                                        "\n@Property Locale locale;" +
                                        "\n@Property Currency currency;" +
                                        "\n@Property Boolean active;" +
                                        "\n@Property Date age  }");
        GrailsDomainClass domainClass = new DefaultGrailsDomainClass(dc);

        generator.generateViews(domainClass,"test");

        File showFile = new File("test/grails-app/views/test/show.gsp");
        assertTrue(showFile.exists());
        File listFile = new File("test/grails-app/views/test/list.gsp");
        assertTrue(listFile.exists());
        File editFile = new File("test/grails-app/views/test/edit.gsp");
        assertTrue(editFile.exists());        
    }
}
