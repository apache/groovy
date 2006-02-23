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

import org.codehaus.groovy.grails.commons.GrailsClassUtils;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;

/**
 * An implementation of the GrailsDomainClassProperty interface that allows Classes mapped in
 * Hibernate to integrate with Grails' validation, dynamic methods etc. seamlessly
 *
 * @author Graeme Rocher
 * @since 18-Feb-2006
 */
public class GrailsHibernateDomainClassProperty implements GrailsDomainClassProperty {
    private GrailsHibernateDomainClass domainClass;

    private String name;
    private String naturalName;
    private Class type;
    private boolean identity;
    private boolean oneToOne;
    private boolean manyToOne;
    private boolean association;
    private boolean oneToMany;
    private boolean manyToMany;
    private boolean bidirectional;
    private boolean optional;
    private Class relatedClassType;
    private GrailsDomainClass referencedDomainClass;
    private GrailsDomainClassProperty otherSide;


    public GrailsHibernateDomainClassProperty(GrailsHibernateDomainClass domainClass, String propertyName) {
        this.domainClass = domainClass;
        this.name = propertyName;
        this.naturalName = GrailsClassUtils.getNaturalName(propertyName);
    }

    public String getName() {
        return this.name;
    }

    public Class getType() {
        return this.type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getTypePropertyName() {
        return GrailsClassUtils.getPropertyNameRepresentation(this.type);
    }

    public GrailsDomainClass getDomainClass() {
        return this.domainClass;
    }

    public boolean isPersistant() {
        return true;
    }

    public String getNaturalName() {
        return this.naturalName;
    }

    public void setReferencedDomainClass(GrailsDomainClass referencedGrailsDomainClass) {
        this.referencedDomainClass =   referencedGrailsDomainClass;
    }

    public void setOtherSide(GrailsDomainClassProperty referencedProperty) {
        this.otherSide = referencedProperty;
    }

    public GrailsDomainClassProperty getOtherSide() {
        return this.otherSide;
    }

    public Class getReferencedPropertyType() {
        return this.relatedClassType;
    }

    public boolean isIdentity() {
        return identity;
    }

    public void setIdentity(boolean identity) {
        this.identity = identity;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isOneToOne() {
        return oneToOne;
    }

    public void setOneToOne(boolean oneToOne) {
        this.oneToOne = oneToOne;
    }

    public boolean isManyToOne() {
        return manyToOne;
    }

    public void setManyToOne(boolean manyToOne) {
        this.manyToOne = manyToOne;
    }

    public boolean isAssociation() {
        return association;
    }

    public void setAssociation(boolean association) {
        this.association = association;
    }

    public boolean isOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(boolean oneToMany) {
        this.oneToMany = oneToMany;
    }

    public boolean isManyToMany() {
        return manyToMany;
    }

    public void setManyToMany(boolean manyToMany) {
        this.manyToMany = manyToMany;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public String getFieldName() {
        throw new UnsupportedOperationException("Method 'getFieldName' is not supported by implementation");
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public GrailsDomainClass getReferencedDomainClass() {
        return this.referencedDomainClass;
    }


    public void setRelatedClassType(Class relatedType) {
        this.relatedClassType = relatedType;
    }
}
