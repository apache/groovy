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



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.hibernate.FetchMode;
import org.hibernate.MappingException;
import org.hibernate.cfg.GrailsSecondPass;
import org.hibernate.cfg.Mappings;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.mapping.Backref;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToMany;
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
 * Based on the HbmBinder code in Hibernate core and influenced by AnnotationsBinder.
 * 
 * @author Graeme Rocher
 * @since 06-Jul-2005
 */
public final class GrailsDomainBinder {
	
	private static final String FOREIGN_KEY_SUFFIX = "_ID";
	private static final Log LOG = LogFactory.getLog( GrailsDomainBinder.class );
	
	/**
	 * A Collection type, for the moment only Set is supported
	 * 
	 * @author Graeme
	 *
	 */
	abstract static class CollectionType {
		
		private Class clazz;

		public abstract Collection create(GrailsDomainClassProperty property, PersistentClass owner,
				Mappings mappings) throws MappingException;

		CollectionType(Class clazz) {
			this.clazz = clazz;
		}

		public String toString() {
			return clazz.getName();
		}
		
		private static CollectionType SET = new CollectionType(Set.class) {

			public Collection create(GrailsDomainClassProperty property, PersistentClass owner, Mappings mappings) throws MappingException {
				org.hibernate.mapping.Set coll = new org.hibernate.mapping.Set(owner);
				coll.setCollectionTable(owner.getTable());
				bindCollection( property, coll, owner, mappings );
				return coll;
			}

			
		};
		
		private static final Map INSTANCES = new HashMap();
		
		static {
			INSTANCES.put( SET.toString(), SET );			
		}
		public static CollectionType collectionTypeForClass(Class clazz) {
			return (CollectionType)INSTANCES.get( clazz.getName() );
		}
	}

	
	/**
	 * Second pass class for grails relationships. This is required as all
	 * persistent classes need to be loaded in the first pass and then relationships
	 * established in the second pass compile
	 * 
	 * @author Graeme
	 *
	 */
	static class GrailsCollectionSecondPass extends GrailsSecondPass {

		private static final long serialVersionUID = -5540526942092611348L;
		private GrailsDomainClassProperty property;
		private Mappings mappings;
		private Collection collection;

		public GrailsCollectionSecondPass(GrailsDomainClassProperty property, Mappings mappings, Collection coll) {			
			super(mappings, coll);
			this.property = property;
			this.mappings = mappings;
			this.collection = coll;
		}

		public void secondPass(Map persistentClasses, Map inheritedMetas) throws MappingException {
			bindCollectionSecondPass( this.property, mappings, persistentClasses, collection,inheritedMetas );			
		}

	}

	private static void bindCollectionSecondPass(GrailsDomainClassProperty property, Mappings mappings, Map persistentClasses, Collection collection, Map inheritedMetas) {

		PersistentClass associatedClass = null;
		// Configure one-to-many
		if(collection.isOneToMany()) {
			OneToMany oneToMany = (OneToMany)collection.getElement();
			String associatedClassName = oneToMany.getReferencedEntityName();
			
			associatedClass = (PersistentClass)persistentClasses.get(associatedClassName);
			// if there is no persistent class for the association throw
			// exception
			if(associatedClass == null) {
				throw new MappingException( "Association references unmapped class: " + oneToMany.getReferencedEntityName() );
			}
			
			oneToMany.setAssociatedClass( associatedClass );
			collection.setCollectionTable( associatedClass.getTable() );
			collection.setLazy(true);
			
			LOG.info( "Mapping collection: "
					+ collection.getRole()
					+ " -> "
					+ collection.getCollectionTable().getName() );
			
		}
		
		// setup the primary key references
		KeyValue keyValue;
		
		String propertyRef = collection.getReferencedPropertyName();
		// this is to support mapping by a property
		if(propertyRef == null) {
			keyValue = collection.getOwner().getIdentifier();
		}
		else {
			keyValue = (KeyValue)collection.getOwner().getProperty( propertyRef ).getValue();
		}
		
		DependantValue key = new DependantValue(collection.getCollectionTable(), keyValue);		
		key.setTypeName(null);
		
//		
		if(property.isBidirectional()) {
			GrailsDomainClassProperty otherSide = property.getOtherSide();
			if(otherSide.isManyToOne()) {
				collection.setInverse(true);
				Iterator mappedByColumns = associatedClass.getProperty( otherSide.getName() ).getValue().getColumnIterator();
				while(mappedByColumns.hasNext()) {
					Column column = (Column)mappedByColumns.next();
					linkValueUsingAColumnCopy(otherSide,column,key);
				}
			}			
		}
		else {
			bindSimpleValue( property,key,mappings );
		}				
		collection.setKey( key );
		// make required and non-updateable
		key.setNullable(false);
		key.setUpdateable(false);
		
		// if we have a many-to-many
		if(property.isManyToMany()) {
			ManyToOne element = new ManyToOne( collection.getCollectionTable() );
			collection.setElement(element);
			bindManyToOne(property,element, mappings);
		}
		else if ( property.isOneToMany() && !property.isBidirectional() ) {
				// for non-inverse one-to-many, with a not-null fk, add a backref!
			OneToMany oneToMany = (OneToMany) collection.getElement();
				String entityName = ( oneToMany ).getReferencedEntityName();
				PersistentClass referenced = mappings.getClass( entityName );
				Backref prop = new Backref();
				prop.setName( '_' + property.getName() + "Backref" );
				prop.setUpdateable( true );
				prop.setInsertable( true );
				prop.setCollectionRole( collection.getRole() );
				prop.setValue( collection.getKey() );
				prop.setOptional( property.isOptional() );
				referenced.addProperty( prop );
		}		
	}		
	
	private static void linkValueUsingAColumnCopy(GrailsDomainClassProperty prop, Column column, DependantValue key) {
		Column mappingColumn = new Column();
		mappingColumn.setName(column.getName());
		mappingColumn.setLength(column.getLength());
		mappingColumn.setNullable(!prop.isOptional());
		mappingColumn.setSqlType(column.getSqlType());		
		
		mappingColumn.setValue(key);
		key.addColumn( mappingColumn );
		key.getTable().addColumn( mappingColumn );		
	}

	/**
	 * First pass to bind collection to Hibernate metamodel, sets up second pass
	 * 
	 * @param property The GrailsDomainClassProperty instance
	 * @param collection The collection
	 * @param owner The owning persistent class
	 * @param mappings The Hibernate mappings instance
	 */
	private static void bindCollection(GrailsDomainClassProperty property, Collection collection, PersistentClass owner, Mappings mappings) {
		
		// set role
		collection.setRole( StringHelper.qualify( property.getDomainClass().getFullName() , property.getName() ) );
		
		// TODO: add code to configure optimistic locking
		
		// TODO: configure fetch strategy
		collection.setFetchMode( FetchMode.DEFAULT );
		
		// if its a one-to-many mapping
		if(property.isOneToMany()) {
			
			OneToMany oneToMany = new OneToMany( collection.getOwner() );
			collection.setElement( oneToMany );
			
			bindOneToMany( property, oneToMany, mappings );			
		}
		else {
			String tableName = property.getReferencedDomainClass().getName();
			
			Table table = mappings.addTable(
								mappings.getSchemaName(),
								mappings.getCatalogName(),
								tableName,
								null,
								false
							);
			collection.setCollectionTable(table);
		}
		
		// setup second pass
		mappings.addSecondPass( new GrailsCollectionSecondPass(property, mappings, collection) );
		
	}
	/**
	 * Binds a Grails domain class to the Hibernate runtime meta model
	 * @param domainClass The domain class to bind
	 * @param mappings The existing mappings
	 * @throws MappingException Thrown if the domain class uses inheritance which is not supported
	 */
	public static void bindClass(GrailsDomainClass domainClass, Mappings mappings)
		throws MappingException {		
		if(domainClass.getClazz().getSuperclass() == java.lang.Object.class) {
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
	 * Binds a persistent classes to the table representation and binds the class properties
	 * 
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
		
		LOG.info( "[GrailsDomainBinder] Mapping Grails domain class: " + domainClass.getFullName() + " -> " + root.getTable().getName() );
		
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
		Table table = persistentClass.getTable();
		
		for(int i = 0; i < persistantProperties.length;i++) {
			
			GrailsDomainClassProperty currentGrailsProp = persistantProperties[i];
			// TODO: Implement support for many from many relationships
			if(currentGrailsProp.isManyToMany())
				continue;
/*			if(currentGrailsProp.isManyToOne() && currentGrailsProp.isBidirectional() ) {
				GrailsDomainClassProperty otherSide = currentGrailsProp.getOtherSide();
				if(otherSide.isOneToMany())
					table = null;
			}*/
			
			if(LOG.isTraceEnabled()) 
				LOG.trace("[GrailsDomainBinder] Binding persistent property [" + currentGrailsProp.getName() + "]");
			
			Value value = null;
			
			// see if its a collection type
			CollectionType collectionType = CollectionType.collectionTypeForClass( currentGrailsProp.getType() );
			if(collectionType != null) {
				// create collection
				Collection collection = collectionType.create( 
						currentGrailsProp,
						persistentClass,
						mappings
				);
				mappings.addCollection(collection);
				value = collection;
			}
			// work out what type of relationship it is and bind value
			else if ( currentGrailsProp.isManyToOne() ) {
				value = new ManyToOne( table );
				bindManyToOne( currentGrailsProp, (ManyToOne) value, mappings );
			}
			else if ( currentGrailsProp.isOneToOne()) {				
				//value = new OneToOne( table, persistentClass );
				//bindOneToOne( currentGrailsProp, (OneToOne)value, mappings );
				value = new ManyToOne( table );
				bindManyToOne( currentGrailsProp, (ManyToOne) value, mappings );
			}	
			else {
				value = new SimpleValue( table );
				bindSimpleValue( persistantProperties[i], (SimpleValue) value, mappings );
			}

			if(value != null) {
				Property property = createProperty( value, persistentClass, persistantProperties[i], mappings );			
				persistentClass.addProperty( property );						
			}
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
		else if( value instanceof Collection ) {
			//Collection collection = (Collection)value;
			//String propertyRef = collection.getReferencedPropertyName();
		}
		
		if(value.getTable() != null)
			value.createForeignKey();
		
		Property prop = new Property();
		prop.setValue( value );
		
		bindProperty( grailsProperty, prop, mappings );
		return prop;
	}

	/**
	 * @param property
	 * @param oneToOne
	 * @param mappings
	 */
/*	private static void bindOneToOne(GrailsDomainClassProperty property, OneToOne oneToOne, Mappings mappings) {
		
		// bind value
		bindSimpleValue(property, oneToOne, mappings );
		// set foreign key type
		oneToOne.setForeignKeyType( ForeignKeyDirection.FOREIGN_KEY_TO_PARENT );
		
		oneToOne.setForeignKeyName( property.getFieldName() + FOREIGN_KEY_SUFFIX );
		
		// TODO configure fetch settings
		oneToOne.setFetchMode( FetchMode.DEFAULT );
		// TODO configure lazy loading
		oneToOne.setLazy(true);
				
		oneToOne.setPropertyName( property.getName() );
		oneToOne.setReferencedEntityName( property.getType().getName() );
		
		
	}*/

	/**
	 * @param currentGrailsProp
	 * @param one
	 * @param mappings
	 */
	private static void bindOneToMany(GrailsDomainClassProperty currentGrailsProp, OneToMany one, Mappings mappings) {
		one.setReferencedEntityName( currentGrailsProp.getReferencedPropertyType().getName() );		
	}

	/**
	 * Binds a many-to-one relationship to the 
	 * @param property
	 * @param manyToOne
	 * @param mappings
	 */
	private static void bindManyToOne(GrailsDomainClassProperty property, ManyToOne manyToOne, Mappings mappings) {
		
		// TODO configure fetching
		manyToOne.setFetchMode(FetchMode.DEFAULT);
		// TODO configure lazy loading
		manyToOne.setLazy(true);
		
		// bind column
		bindSimpleValue(property,manyToOne,mappings);

		// set referenced entity
		manyToOne.setReferencedEntityName( property.getReferencedPropertyType().getName() );
		manyToOne.setIgnoreNotFound(true);
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
	 * @param entity
	 * @param mappings
	 */
	private static void bindSimpleId(GrailsDomainClassProperty identifier, RootClass entity, Mappings mappings) {
		
		// create the id value
		SimpleValue id = new SimpleValue(entity.getTable());
		// set identifier on entity
		entity.setIdentifier( id );
		// configure generator strategy
		id.setIdentifierGeneratorStrategy( "native" );
		
		Properties params = new Properties();

		if ( mappings.getSchemaName() != null ) {
			params.setProperty( PersistentIdentifierGenerator.SCHEMA, mappings.getSchemaName() );
		}
		if ( mappings.getCatalogName() != null ) {
			params.setProperty( PersistentIdentifierGenerator.CATALOG, mappings.getCatalogName() );
		}
		id.setIdentifierGeneratorProperties(params);
		
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
	 * @param grailsProperty
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
		// set to cascade all for the moment
		if(grailsProperty.isAssociation()) {
			prop.setCascade("all");
			if(LOG.isTraceEnabled()) 
				LOG.trace( "[GrailsDomainBinder] Set cascading strategy on property ["+grailsProperty.getName()+"] to [all]" );
		}
		// lazy to true
		prop.setLazy(true);
		
	}

	/**
	 * Binds a simple value to the Hibernate metamodel. A simple value is 
	 * any type within the Hibernate type system
	 * 
	 * @param grailsProp The grails domain class property
	 * @param simpleValue The simple value to bind
	 * @param mappings The Hibernate mappings instance
	 */
	private static void bindSimpleValue(GrailsDomainClassProperty grailsProp, SimpleValue simpleValue,Mappings mappings) {
		// set type
		simpleValue.setTypeName(grailsProp.getType().getName());
		Table table = simpleValue.getTable();
		Column column = new Column();
		if(grailsProp.isManyToOne())
			column.setNullable(false);
		
		column.setValue(simpleValue);
		bindColumn(grailsProp, column);
								
		if(table != null) table.addColumn(column);
		
		simpleValue.addColumn(column);		
	}
	

	/**
	 * Binds a Column instance to the Hibernate meta model
	 * @param grailsProp The Grails domain class property
	 * @param column The column to bind
	 */
	private static void bindColumn(GrailsDomainClassProperty grailsProp, Column column) {						
		if(grailsProp.isAssociation()) {
			if(grailsProp.isOneToMany()) {							
				column.setName( grailsProp.getDomainClass().getTableName() + FOREIGN_KEY_SUFFIX );
			}
			else if(grailsProp.isManyToOne()) {							
				column.setName( grailsProp.getReferencedDomainClass().getTableName() + FOREIGN_KEY_SUFFIX );
			}			
			else {
				column.setName( grailsProp.getFieldName() + FOREIGN_KEY_SUFFIX );
			}			
			column.setNullable(true);
			
		} else {
			column.setNullable(grailsProp.isOptional());
			column.setName(grailsProp.getFieldName());
		}
		if(LOG.isTraceEnabled()) 
			LOG.trace("[GrailsDomainBinder] bound property [" + grailsProp + "] to column name ["+column.getName()+"]");		
	}


}
