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
package org.codehaus.groovy.grails.orm.hibernate.cfg;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.hibernate.MappingException;
import org.hibernate.cfg.HbmBinder;
import org.hibernate.cfg.Mappings;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.hibernate.util.StringHelper;



/**
 * Handles the binding Grails domain classes and properties to the Hibernate runtime meta model.
 * 
 * @author Graeme Rocher
 * @since 06-Jul-2005
 */
public final class GrailsDomainBinder {
	
	private static final Log LOG = LogFactory.getLog( HbmBinder.class );

	/**
	 * Binds a Grails domain class to the Hibernate runtime meta model
	 * @param domainClass The domain class to bind
	 * @param mappings The existing mappings
	 * @throws MappingException Thrown if the domain class uses inheritance which is not supported
	 */
	public static void bindClass(GrailsDomainClass domainClass, Mappings mappings)
		throws MappingException {		
		if(domainClass.newInstance().getClass().getSuperclass() == java.lang.Object.class) {
			bindRoot(domainClass, mappings);
		}
		else {
			throw new MappingException("Grails domain classes do not support inheritance");				
		}		
	}

	/**
	 * Binds the specified persistant class to the runtime model based on the 
	 * properties defined in the domain class
	 * @param domainClass The Grails domain class
	 * @param persistentClass The persistant class
	 * @param mappings Existing mappings
	 */
	private static void bindClass(GrailsDomainClass domainClass, PersistentClass persistentClass, Mappings mappings) {
		
		// set lazy loading for now
		persistentClass.setLazy(true);
		persistentClass.setEntityName(domainClass.getFullName());
		persistentClass.setProxyInterfaceName( domainClass.getFullName() );
		persistentClass.setClassName(domainClass.getFullName());
		// set dynamic insert to false
		persistentClass.setDynamicInsert(false);
		// set dynamic update to false
		persistentClass.setDynamicUpdate(false);
		// set select before update to false
		persistentClass.setSelectBeforeUpdate(false);
		
		// add import to mappings
		if ( mappings.isAutoImport() && persistentClass.getEntityName().indexOf( '.' ) > 0 ) {
			mappings.addImport( persistentClass.getEntityName(), StringHelper.unqualify( persistentClass
				.getEntityName() ) );
		}		
	}

	
	/**
	 * Binds a root class (one with no super classes) to the runtime meta model
	 * based on the supplied Grails domain class
	 * 
	 * @param domainClass The Grails domain class 
	 * @param mappings The Hibernate Mappings object
	 */
	public static void bindRoot(GrailsDomainClass domainClass, Mappings mappings) {
		
		RootClass root = new RootClass();
		bindClass(domainClass, root, mappings);		
		bindRootPersistentClassCommonValues(domainClass, root, mappings);
		
		mappings.addClass(root);
	}


	/**
	 * @param domainClass
	 * @param root
	 * @param mappings
	 */
	private static void bindRootPersistentClassCommonValues(GrailsDomainClass domainClass, RootClass root, Mappings mappings) {
		
		// get the schema and catalog names from the configuration
		String schema = mappings.getSchemaName();
		String catalog = mappings.getCatalogName();
		
		// create the table 
		Table table = mappings.addTable(
				schema,
				catalog,
				domainClass.getTableName(),
				null,
				false
		);
		root.setTable(table);
		
		LOG.info( "Mapping Grails domain class: " + domainClass.getFullName() + " -> " + root.getTable().getName() );
		
		bindSimpleId( domainClass.getIdentifier(), root, mappings );
		bindVersion( domainClass.getVersion(), root, mappings );
		
		root.createPrimaryKey();
		
		createClassProperties(domainClass,root,mappings);
	}

	/**
	 * Creates and binds the properties for the specified Grails domain class and PersistantClass
	 * and binds them to the Hibernate runtime meta model
	 * 
	 * @param domainClass The Grails domain class
	 * @param persistentClass The Hibernate PersistentClass instance
	 * @param mappings The Hibernate Mappings instance
	 */
	protected static void createClassProperties(GrailsDomainClass domainClass, PersistentClass persistentClass, Mappings mappings) {
		
		GrailsDomainClassProperty[] persistantProperties = domainClass.getPersistantProperties();
		String entityName = persistentClass.getEntityName();
		Table table = persistentClass.getTable();
		
		for(int i = 0; i < persistantProperties.length;i++) {
			
			GrailsDomainClassProperty currentGrailsProp = persistantProperties[i];
			
			Value value = null;
			
			// work out what type of relationship it is and bind value
			if ( currentGrailsProp.isManyToOne() ) {
				value = new ManyToOne( table );
				bindManyToOne( currentGrailsProp, (ManyToOne) value, mappings );
			}
			else if ( currentGrailsProp.isOneToOne() ) {
				value = new OneToOne( table, persistentClass );
				bindOneToOne( currentGrailsProp, (OneToOne)value, mappings );
			}
			else if ( currentGrailsProp.isOneToMany() ) {
				value = new OneToMany( persistentClass );
				bindOneToMany( currentGrailsProp, (OneToMany)value,  mappings );
			}			
			else {
				value = new SimpleValue( table );
				bindSimpleValue( persistantProperties[i], (SimpleValue) value, mappings );
			}

			Property property = createProperty( value, persistentClass, persistantProperties[i], mappings );			
			persistentClass.addProperty( property );						
		}		
	}

	/**
	 * Creates a persistant class property based on the GrailDomainClassProperty instance
	 * 
	 * @param value
	 * @param persistentClass
	 * @param property
	 * @param mappings
	 * @return
	 */
	private static Property createProperty(Value value, PersistentClass persistentClass, GrailsDomainClassProperty grailsProperty, Mappings mappings) {
		// set type
		value.setTypeUsingReflection( persistentClass.getClassName(), grailsProperty.getName() );
		
		// if it is a ManyToOne or OneToOne relationship
		if ( value instanceof ToOne ) {
			ToOne toOne = (ToOne) value;
			String propertyRef = toOne.getReferencedPropertyName();
			if ( propertyRef != null ) {
				// TODO: Hmm this method has package visibility. Why?
				
				//mappings.addUniquePropertyReference( toOne.getReferencedEntityName(), propertyRef );
			}
		}		
		
		value.createForeignKey();
		Property prop = new Property();
		prop.setValue( value );
		bindProperty( grailsProperty, prop, mappings );
		return prop;
	}

	/**
	 * @param currentGrailsProp
	 * @param one
	 * @param mappings
	 */
	private static void bindOneToOne(GrailsDomainClassProperty currentGrailsProp, OneToOne one, Mappings mappings) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param currentGrailsProp
	 * @param one
	 * @param mappings
	 */
	private static void bindOneToMany(GrailsDomainClassProperty currentGrailsProp, OneToMany one, Mappings mappings) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Binds a many-to-one relationship to the 
	 * @param currentGrailsProp
	 * @param one
	 * @param mappings
	 */
	private static void bindManyToOne(GrailsDomainClassProperty currentGrailsProp, ManyToOne one, Mappings mappings) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param version
	 * @param root
	 * @param mappings
	 */
	private static void bindVersion(GrailsDomainClassProperty version, RootClass entity, Mappings mappings) {
		
		SimpleValue val = new SimpleValue( entity.getTable() );
		bindSimpleValue( version, val, mappings);
		
		if ( !val.isTypeSpecified() ) {
			val.setTypeName( "version".equals( version.getName() ) ? "integer" : "timestamp" );
		}
		Property prop = new Property();
		prop.setValue( val );
		
		bindProperty( version, prop, mappings );
		val.setNullValue( "undefined" );
		entity.setVersion( prop );
		entity.addProperty( prop );		
	}

	/**
	 * @param identifier
	 * @param root
	 * @param mappings
	 */
	private static void bindSimpleId(GrailsDomainClassProperty identifier, RootClass entity, Mappings mappings) {
		
		// create the id value
		SimpleValue id = new SimpleValue(entity.getTable());
		// set identifier on entity
		entity.setIdentifier( id );
		
		// bind value
		bindSimpleValue(identifier, id, mappings );

		// create property
		Property prop = new Property();
		prop.setValue(id);
		
		// bind property
		bindProperty( identifier, prop, mappings );
		// set identifier property
		entity.setIdentifierProperty( prop );
		
		id.getTable().setIdentifierValue( id );

	}

	/**
	 * @param identifier
	 * @param prop
	 * @param mappings
	 */
	private static void bindProperty(GrailsDomainClassProperty grailsProperty, Property prop, Mappings mappings) {
		// set the property name
		prop.setName( grailsProperty.getName() );
		prop.setInsertable(true);
		prop.setUpdateable(true);
		prop.setPropertyAccessorName( mappings.getDefaultAccess() );
		prop.setOptional( grailsProperty.isOptional() );
		// lazy to true
		prop.setLazy(true);
		
	}

	/**
	 * @param grailsProp
	 * @param id
	 * @param b
	 * @param name
	 * @param mappings
	 */
	private static void bindSimpleValue(GrailsDomainClassProperty grailsProp, SimpleValue simpleValue,Mappings mappings) {
		// set type
		simpleValue.setTypeName(grailsProp.getType().getName());
		
		Column column = new Column();
		column.setValue(simpleValue);
		bindColumn(grailsProp, column);
		
		simpleValue.addColumn(column);
		simpleValue.getTable().addColumn(column);
	}

	/**
	 * @param grailsProp
	 * @param column
	 */
	private static void bindColumn(GrailsDomainClassProperty grailsProp, Column column) {
		column.setNullable(grailsProp.isOptional());
		column.setName(grailsProp.getFieldName());
	}


}
