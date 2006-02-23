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
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.codehaus.groovy.grails.metaclass.DomainClassMethods;
import org.codehaus.groovy.grails.orm.hibernate.GrailsHibernateDomainClass;
import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility methods used in configuring the Grails Hibernate integration
 *
 * @author Graeme Rocher
 * @since 18-Feb-2006
 */
public class GrailsDomainConfigurationUtil {
    private static final Log LOG = LogFactory.getLog(GrailsDomainConfigurationUtil.class);

    /**
     * Configures the relationships between domain classes after they have been all loaded.
     * 
     * @param domainClasses
     * @param domainMap
     */
    public static void configureDomainClassRelationships(GrailsDomainClass[] domainClasses, Map domainMap) {

        for (int i = 0; i < domainClasses.length; i++) {
            GrailsDomainClassProperty[] props = domainClasses[i].getPersistantProperties();

            for (int j = 0; j < props.length; j++) {
                if(props[j].isAssociation()) {
                    GrailsDomainClassProperty prop = props[j];
                    GrailsDomainClass referencedGrailsDomainClass = (GrailsDomainClass)domainMap.get( props[j].getReferencedPropertyType().getName() );
                    prop.setReferencedDomainClass(referencedGrailsDomainClass);

                }
            }

        }

        for (int i = 0; i < domainClasses.length; i++) {
            GrailsDomainClassProperty[] props = domainClasses[i].getPersistantProperties();

            for (int j = 0; j < props.length; j++) {
                if(props[j].isAssociation()) {
                    GrailsDomainClassProperty prop = props[j];
                    GrailsDomainClassProperty[] referencedProperties =  prop.getReferencedDomainClass().getPersistantProperties();
                    for (int k = 0; k < referencedProperties.length; k++) {
                        if(referencedProperties[k].getReferencedPropertyType().equals( domainClasses[i].getClazz())) {
                            prop.setOtherSide(referencedProperties[k]);
                            break;
                        }
                    }
                }
            }

        }
    }
    /**
     * Configures dynamic methods on all Hibernate mapped domain classes
     *
     * @param sf The session factory instance
     * @param application The grails application instance
     */
    public static void configureDynamicMethods(SessionFactory sf, GrailsApplication application) {
        Collection classMetaData = sf.getAllClassMetadata().values();
        for (Iterator i = classMetaData.iterator(); i.hasNext();) {
            ClassMetadata cmd = (ClassMetadata) i.next();

            Class persistentClass = cmd.getMappedClass(EntityMode.POJO);

            // if its not a grails domain class and one written in java then add it
            // to grails
            if(application.getGrailsDomainClass(persistentClass.getName()) == null) {
                    application.addDomainClass(new GrailsHibernateDomainClass(persistentClass, sf,cmd));
            }
            LOG.info("[GrailsDomainConfiguration] Registering dynamic methods on class ["+persistentClass+"]");
            try {
                new DomainClassMethods(application,persistentClass,sf,application.getClassLoader());
            } catch (IntrospectionException e) {
                LOG.warn("[GrailsDomainConfiguration] Introspection exception registering dynamic methods for ["+persistentClass+"]:" + e.getMessage(), e);
            }
        }
    }
}
