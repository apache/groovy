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

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.hibernate.SessionFactory;

/**
 * @author Graeme Rocher
 * @since 04-Aug-2005
 */

public interface GrailsDomainConfiguration {
    /**
     * Adds a domain class to the configuration
     * @param domainClass
     * @return this
     */
    public abstract GrailsDomainConfiguration addDomainClass(
            GrailsDomainClass domainClass);

    /**
     * Sets the grails application instance
     * @param application The grails application to use or null if none.
     */
    public abstract void setGrailsApplication(GrailsApplication application);

    /**
     * Configures Grails dynamic methods for the specified session factory
      * @param sf
     */
    public abstract void configureDynamicMethods(SessionFactory sf);

    /**
     * Whether the configuration should configure dynamic methods (defaults to true)
     * @param shouldConfigure
     */
    public abstract void setConfigureDynamicMethods(boolean shouldConfigure);

}
