package org.codehaus.groovy.runtime;

import groovy.lang.GroovyClassLoader;
import groovy.util.GroovyTestCase;

public class JdkDynamicProxyTest extends GroovyTestCase {

	public void testJdkDynamicProxySameLoader() throws Exception {

		// Instantiate all beans.
        final GroovyClassLoader loader = new GroovyClassLoader();
        JdkDynamicProxyServiceBean sb1 = (JdkDynamicProxyServiceBean) JdkDynamicProxyInvocationHandler.getProxiedObject (loader.loadClass ("org.codehaus.groovy.runtime.JdkDynamicProxyServiceBeanImpl1").newInstance () );
		JdkDynamicProxyServiceBean sb2 = (JdkDynamicProxyServiceBean) JdkDynamicProxyInvocationHandler.getProxiedObject (loader.loadClass ("org.codehaus.groovy.runtime.JdkDynamicProxyServiceBeanImpl2").newInstance () );

		// Manually wire beans together.
		sb1.setJdkDynamicProxyServiceBean (sb2);
		assertEquals ("SERVICE", sb1.doService () );
	}

    public void testJdkDynamicProxyDifferentLoaders() throws Exception {

        // Instantiate all beans.
        JdkDynamicProxyServiceBean sb1 = (JdkDynamicProxyServiceBean) JdkDynamicProxyInvocationHandler.getProxiedObject (new GroovyClassLoader().loadClass ("org.codehaus.groovy.runtime.JdkDynamicProxyServiceBeanImpl1").newInstance () );
        JdkDynamicProxyServiceBean sb2 = (JdkDynamicProxyServiceBean) JdkDynamicProxyInvocationHandler.getProxiedObject (new GroovyClassLoader().loadClass ("org.codehaus.groovy.runtime.JdkDynamicProxyServiceBeanImpl2").newInstance () );

        // Manually wire beans together.
        sb1.setJdkDynamicProxyServiceBean (sb2);
        assertEquals ("SERVICE", sb1.doService () );
    }

}
