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
	GroovyClassLoader cl = new GroovyClassLoader();
	private Class relClass;
	private Class manyToManyClass;
	private Class oneToManyClass;
	private Class oneToOneClass;
	
	

	
	protected void setUp() throws Exception {
		Thread.currentThread().setContextClassLoader(cl);
		
		relClass = cl.loadClass( "org.codehaus.groovy.grails.domain.RelationshipsTest" );
		
		Class[] loadedClasses = cl.getLoadedClasses();
		
		for (int i = 0; i < loadedClasses.length; i++) {
			if(loadedClasses[i].getName().equals("org.codehaus.groovy.grails.domain.ManyToManyTest") ) {
				manyToManyClass = loadedClasses[i];
			}
			else if(loadedClasses[i].getName().equals("org.codehaus.groovy.grails.domain.OneToManyTest2") ) {
				oneToManyClass = loadedClasses[i];
			}
			else if(loadedClasses[i].getName().equals("org.codehaus.groovy.grails.domain.OneToOneTest") ) {
				oneToOneClass = loadedClasses[i];
			}			
		}
		
		super.setUp();
	}

	public void testDefaultGrailsDomainClass()
		throws Exception {
	
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

	public void testOneToManyRelationships()
		throws Exception {		
						
										
		GrailsDomainClass c1dc = new DefaultGrailsDomainClass(relClass);
		GrailsDomainClass c2dc = new DefaultGrailsDomainClass(oneToManyClass);
		
		// test relationship property
		assertTrue( c1dc.getPropertyByName( "ones" ).isOneToMany() );
		assertFalse( c1dc.getPropertyByName( "ones" ).isManyToMany() );
		assertFalse( c1dc.getPropertyByName( "ones" ).isManyToOne() );
		assertFalse( c1dc.getPropertyByName( "ones" ).isOneToOne() );		
		
		assertTrue( c2dc.getPropertyByName( "other" ).isManyToOne() );	
		assertFalse( c2dc.getPropertyByName( "other" ).isManyToMany() );
		assertFalse( c2dc.getPropertyByName( "other" ).isOneToOne() );
		assertFalse( c2dc.getPropertyByName( "other" ).isOneToMany() );				
	}
	
	public void testManyToManyRelationships()
		throws Exception {
		

		
		GrailsDomainClass c1dc = new DefaultGrailsDomainClass(relClass);
		GrailsDomainClass c2dc = new DefaultGrailsDomainClass(manyToManyClass);
		
		// test relationships
		assertTrue( c1dc.getPropertyByName( "manys" ).isManyToMany() );		
		assertFalse( c1dc.getPropertyByName( "manys" ).isOneToMany() );
		assertFalse( c1dc.getPropertyByName( "manys" ).isManyToOne() );
		assertFalse( c1dc.getPropertyByName( "manys" ).isOneToOne() );			
		
		assertTrue( c2dc.getPropertyByName( "manys" ).isManyToMany() );
		assertFalse( c2dc.getPropertyByName( "manys" ).isManyToOne() );
		assertFalse( c2dc.getPropertyByName( "manys" ).isOneToOne() );
		assertFalse( c2dc.getPropertyByName( "manys" ).isOneToMany() );		
	}
	
	public void testOneToOneRelationships() 
		throws Exception {
		GrailsDomainClass c1dc = new DefaultGrailsDomainClass(relClass);
		GrailsDomainClass c2dc = new DefaultGrailsDomainClass(oneToOneClass);		
		
		// test relationships
		assertTrue( c1dc.getPropertyByName( "one" ).isOneToOne() );	
		assertFalse( c1dc.getPropertyByName( "one" ).isManyToMany() );
		assertFalse( c1dc.getPropertyByName( "one" ).isManyToOne() );
		assertFalse( c1dc.getPropertyByName( "one" ).isOneToMany() );
		
		assertTrue( c2dc.getPropertyByName( "other" ).isOneToOne() );
		assertFalse( c2dc.getPropertyByName( "other" ).isManyToMany() );
		assertFalse( c2dc.getPropertyByName( "other" ).isManyToOne() );
		assertFalse( c2dc.getPropertyByName( "other" ).isOneToMany() );		
	}

}
