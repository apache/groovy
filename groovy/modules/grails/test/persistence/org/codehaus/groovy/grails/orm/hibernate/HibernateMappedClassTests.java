package org.codehaus.groovy.grails.orm.hibernate;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**

 */
public class HibernateMappedClassTests extends
		AbstractDependencyInjectionSpringContextTests {

	/* (non-Javadoc)
	 * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
	 */
	protected String[] getConfigLocations() {
		return new String[] { "org/codehaus/groovy/grails/orm/hibernate/hibernate-mapped-class-tests.xml" };
	}

	public void testDynamicMethods() {
		HibernateMappedClass hmc = new HibernateMappedClass();
		hmc.setMyProp("somevalue");
		InvokerHelper.invokeMethod(hmc, "save", new Object[0]);
		String className = hmc.getClass().getName();
		hmc = null;
		
		hmc = (HibernateMappedClass)InvokerHelper.invokeStaticMethod(className, "get", new Object[] { new Integer(1) });
		assertEquals("somevalue", hmc.getMyProp());
	}
}
