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
package org.codehaus.groovy.grails.domain;

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.codehaus.groovy.grails.exceptions.InvalidPropertyException;

import groovy.lang.GroovyClassLoader;
import junit.framework.TestCase;

/**
 * @author Graeme Rocher
 * @since 06-Jul-2005
 */
public class DefaultGrailsDomainClassTest extends TestCase {

	
	public void testDefaultGrailsDomainClass()
		throws Exception {
		GroovyClassLoader cl = new GroovyClassLoader();
		Class clazz = cl.parseClass("class UserTest { @Property int id; @Property int version; @Property List transients = [ \"age\" ]; @Property List optional  = [ \"lastName\" ]; @Property String firstName; @Property String lastName; @Property java.util.Date age; }");
				
		
		GrailsDomainClass domainClass = new DefaultGrailsDomainClass(clazz);
				
		assertEquals("UserTest",domainClass.getName());
		
		assertNotNull(domainClass.getIdentifier());
		assertNotNull(domainClass.getVersion());
		assertTrue(domainClass.getIdentifier().isIdentity());
		
		try {
			domainClass.getPropertyByName("rubbish");
			fail("should throw exception");
		}
		catch(InvalidPropertyException ipe) {
			// expected
		}
		
		GrailsDomainClassProperty age = domainClass.getPropertyByName( "age" );
		assertNotNull(age);
		assertFalse(age.isPersistant());
		
		GrailsDomainClassProperty lastName = domainClass.getPropertyByName( "lastName" );
		assertNotNull(lastName);
		assertTrue(lastName.isOptional());
		
		GrailsDomainClassProperty firstName = domainClass.getPropertyByName( "firstName" );
		assertNotNull(firstName);
		assertFalse(firstName.isOptional());
		assertTrue(firstName.isPersistant());
		

		GrailsDomainClassProperty[] persistantProperties = domainClass.getPersistantProperties();
		for(int i = 0; i < persistantProperties.length;i++) {
			assertTrue(persistantProperties[i].isPersistant());
		}
	}

	public void testOneToManyRelationship()
		throws Exception {
		
		GroovyClassLoader cl = new GroovyClassLoader();
		
		Thread.currentThread().setContextClassLoader(cl);
				
		Class c1 = cl.loadClass( "org.codehaus.groovy.grails.domain.OneToManyTest1" );
		Class c2 = cl.loadClass( "org.codehaus.groovy.grails.domain.OneToManyTest2" );
				
		c1.newInstance();
		
		//c2.newInstance();
		//Class c1 = cl.parseClass(new File("test/persistence/org/codehaus/groovy/grails/domain/OneToManyTest1.groovy"));
						
		//GrailsDomainClass c1dc = new DefaultGrailsDomainClass(c1);
		//GrailsDomainClass c2dc = new DefaultGrailsDomainClass(c2);
		
		//assertTrue( c1dc.getPropertyByName( "accounts" ).isOneToMany() );
		//assertTrue( c2dc.getPropertyByName( "holder" ).isManyToOne() );
		
	}

}
