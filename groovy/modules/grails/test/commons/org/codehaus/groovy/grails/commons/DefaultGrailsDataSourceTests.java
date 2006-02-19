package org.codehaus.groovy.grails.commons;

import junit.framework.TestCase;
import groovy.lang.GroovyClassLoader;

/**
 * User: Graeme
 * Date: 19-Feb-2006
 */
public class DefaultGrailsDataSourceTests extends TestCase {

    public void testGrailsDataSource() throws Exception {
        GroovyClassLoader gcl = new GroovyClassLoader();

        Class dsClass = gcl.parseClass("class TestDataSource {" +
                "@Property String driverClassName = 'test.driverClass'\n" +
                "@Property String url = 'jdbc://testurl'\n" +
                "@Property String username ='sa'\n" +
                "@Property String password = 'pass'\n" +
                "@Property configClass = org.codehaus.groovy.grails.orm.hibernate.cfg.DefaultGrailsDomainConfiguration.class" +
                "}");

        GrailsDataSource ds = new DefaultGrailsDataSource(dsClass);
        assertNotNull(ds.getConfigurationClass());

    }
}
