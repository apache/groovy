package org.codehaus.groovy.runtime;

class JdkDynamicProxyServiceBeanImpl1 implements JdkDynamicProxyServiceBean {

	JdkDynamicProxyServiceBean jdkDynamicProxyServiceBean;

	String doService () {
		if (jdkDynamicProxyServiceBean != null) {
			return jdkDynamicProxyServiceBean.doService ();
		} else {
			return "SERVICE";
		}
	}
}
