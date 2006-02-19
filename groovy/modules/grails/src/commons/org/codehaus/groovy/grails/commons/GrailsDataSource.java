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
package org.codehaus.groovy.grails.commons;

import java.util.Properties;

/**
 * <p>Represents a data source in Grails.
 * 
 * @author Steven Devijver
 * @since Aug 6, 2005
 */
public interface GrailsDataSource extends InjectableGrailsClass {

	/**
	 * <p>True is connection pooling is enabled.
	 * 
	 * @return connection pooling enabled
	 */
	public boolean isPooled();
	
	/**
	 * <p>The driver class name for the data source.
	 * 
	 * @return driver class name
	 */
	public String getDriverClassName();
	
	/**
	 * <p>The URL for the data source.
	 * 
	 * @return URL
	 */
	public String getUrl();
	
	/**
	 * <p>The username for the data source.
	 * 
	 * @return username
	 */
	public String getUsername();
	
	/**
	 * <p>The password for the data source.
	 * 
	 * @return password
	 */
	public String getPassword();
	
	/**
	 * <p>Other properties for this data source.
	 * 
	 * @return other properties
	 */
	public Properties getOtherProperties();
	
	/**
	 * Whether to generate the database with HBM 2 DDL, values can be "create", "create-drop" or "update"
	 * @return The dbCreate method to use
	 */
	public String getDbCreate();

    /**
     *
     * @return The configuration class to use when setting up the database
     */
    public Class getConfigurationClass();
}
