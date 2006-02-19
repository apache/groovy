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
package org.codehaus.groovy.grails.orm.hibernate;

import org.codehaus.groovy.grails.commons.AbstractGrailsClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.codehaus.groovy.grails.commons.ExternalGrailsDomainClass;
import org.codehaus.groovy.grails.orm.hibernate.validation.GrailsDomainClassValidator;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.springframework.beans.BeanWrapper;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * An implementation of the GrailsDomainClass interface that allows Classes mapped in
 * Hibernate to integrate with Grails' validation, dynamic methods etc. seamlessly
 *
 * @author Graeme Rocher
 * @since 18-Feb-2006
 */
public class GrailsHibernateDomainClass extends AbstractGrailsClass implements ExternalGrailsDomainClass {
    private static final String HIBERNATE = "hibernate";
    private GrailsHibernateDomainClassProperty identifier;
    private GrailsDomainClassProperty[] properties;
    private Map propertyMap = new HashMap();
    private Validator validator;

    /**
     * <p>Contructor to be used by all child classes to create a
     * new instance and get the name right.
     *
     * @param clazz        the Grails class
     */
    public GrailsHibernateDomainClass(Class clazz,ClassMetadata metaData) {
        super(clazz, "");

        BeanWrapper bean = getReference();
        // configure identity property
        String ident = metaData.getIdentifierPropertyName();
        Class identType = bean.getPropertyType(ident);
        this.identifier = new GrailsHibernateDomainClassProperty(this,ident);
        this.identifier.setIdentity(true);
        this.identifier.setType(identType);

        propertyMap.put(ident,identifier);

        // configure remaining properties
        String[] propertyNames = metaData.getPropertyNames();
        for (int i = 0; i < propertyNames.length; i++) {
            String propertyName = propertyNames[i];
            if(!propertyName.equals(ident)) {
                GrailsHibernateDomainClassProperty prop = new GrailsHibernateDomainClassProperty(this,propertyName);
                prop.setType(bean.getPropertyType(propertyName));
                Type hibernateType = metaData.getPropertyType(propertyName);
                if(hibernateType.isAssociationType()) {
                    prop.setAssociation(true);
                    if(hibernateType.isCollectionType()) {
                        prop.setOneToMany(true);
                    }
                    else if(hibernateType.isEntityType()) {
                        prop.setManyToOne(true);
                        // might not really be true, but for our purposes this is ok
                        prop.setOneToOne(true);
                    }
                }
                propertyMap.put(propertyName,prop);
            }
        }

        this.properties = (GrailsDomainClassProperty[])propertyMap.values().toArray(new GrailsDomainClassProperty[propertyMap.size()]);
    }

    public boolean isOwningClass(Class domainClass) {
        throw new UnsupportedOperationException("Method 'isOwningClass' is not supported by implementation");
    }

    public GrailsDomainClassProperty[] getProperties() {
        return this.properties;
    }

    public GrailsDomainClassProperty[] getPersistantProperties() {
        return this.properties;
    }

    public GrailsDomainClassProperty getIdentifier() {
        return this.identifier;
    }

    public GrailsDomainClassProperty getVersion() {
        throw new UnsupportedOperationException("Method 'getVersion' is not supported by implementation");
    }

    public GrailsDomainClassProperty getPropertyByName(String name) {
        return (GrailsDomainClassProperty)propertyMap.get(name);
    }

    public String getFieldName(String propertyName) {
        throw new UnsupportedOperationException("Method 'getFieldName' is not supported by implementation");
    }

    public String getTableName() {
        throw new UnsupportedOperationException("Method 'getTableName' is not supported by implementation");
    }

    public boolean isOneToMany(String propertyName) {
        GrailsDomainClassProperty prop = getPropertyByName(propertyName);
        if(prop == null)
            return false;
        else {
            return prop.isOneToMany();
        }
    }

    public boolean isManyToOne(String propertyName) {
        GrailsDomainClassProperty prop = getPropertyByName(propertyName);
        if(prop == null)
            return false;
        else {
            return prop.isManyToOne();
        }
    }

    public boolean isBidirectional(String propertyName) {
        throw new UnsupportedOperationException("Method 'isBidirectional' is not supported by implementation");
    }

    public Class getRelatedClassType(String propertyName) {
        throw new UnsupportedOperationException("Method 'getRelatedClassType' is not supported by implementation");
    }

    public Map getConstrainedProperties() {
        if(getReference().isReadableProperty(GrailsDomainClassProperty.CONSTRAINTS)) {
            return (Map)getReference().getPropertyValue(GrailsDomainClassProperty.CONSTRAINTS);
        }
        return Collections.EMPTY_MAP;
    }

    public Validator getValidator() {
        if(this.validator == null) {
            GrailsDomainClassValidator gdcv = new GrailsDomainClassValidator();
            gdcv.setDomainClass(this);
            this.validator = gdcv;
        }
        return this.validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public String getMappedBy() {
        return HIBERNATE;
    }
}
