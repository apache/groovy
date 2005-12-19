/*
 * Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.orm.hibernate.support;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.orm.hibernate.exceptions.CouldNotDetermineHibernateDialectException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.DialectFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Aug 6, 2005
 */
public class HibernateDialectDetectorFactoryBean implements FactoryBean,
		InitializingBean {

	private DataSource dataSource = null;
	private Properties vendorNameDialectMappings = null;
	private String hibernateDialectClassName = null;
	private Dialect hibernateDialect;
	
	public HibernateDialectDetectorFactoryBean() {
		super();
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setVendorNameDialectMappings(Properties mappings) {
		this.vendorNameDialectMappings = mappings;
	}
	
	public Object getObject() throws Exception {
		return this.hibernateDialectClassName;
	}

	public Class getObjectType() {
		return String.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.dataSource == null) {
			throw new IllegalStateException("Data source is not set!");
		}
		if (this.vendorNameDialectMappings == null) {
			throw new IllegalStateException("Vendor name/dialect mappings are not set!");
		}
		
		String dbName = (String)JdbcUtils.extractDatabaseMetaData(this.dataSource, "getDatabaseProductName");
		Integer majorVersion = (Integer)JdbcUtils.extractDatabaseMetaData(this.dataSource, "getDatabaseMajorVersion");
		
		this.hibernateDialect = DialectFactory.determineDialect(dbName,majorVersion.intValue());
		this.hibernateDialectClassName = this.hibernateDialect.getClass().getName();
		//this.hibernateDialectClassName = this.vendorNameDialectMappings.getProperty(dbName);
		
		if (StringUtils.isBlank(this.hibernateDialectClassName)) {
			throw new CouldNotDetermineHibernateDialectException("Could not determine Hibernate dialect for database name [" + dbName + "]!");
		}
	}

}
