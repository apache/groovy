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
package org.codehaus.groovy.grails.scaffolding;

import org.codehaus.groovy.grails.metaclass.DomainClassMethods;
import org.codehaus.groovy.grails.orm.hibernate.metaclass.DeletePersistentMethod;
import org.codehaus.groovy.grails.orm.hibernate.metaclass.SavePersistentMethod;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.hibernate.SessionFactory;
import org.springframework.validation.Errors;

import java.io.Serializable;

/**
 * Extends the default domain to delegate to Grails specific dynamic methods
 *
 * @author Graeme Rocher
 * @since 23-Feb-2006
 */
public class GrailsScaffoldDomain extends DefaultScaffoldDomain implements ScaffoldDomain{
    public GrailsScaffoldDomain(Class persistentClass, SessionFactory sessionFactory) {
        super(persistentClass, sessionFactory);
    }

    public boolean save(Object domainObject, ScaffoldCallback callback) {
        Boolean b = (Boolean)InvokerHelper.invokeMethod(domainObject,SavePersistentMethod.METHOD_SIGNATURE, new Object[0]);
        boolean success = b.booleanValue();
        if(!success) {
            callback.setErrors((Errors)InvokerHelper.getProperty(domainObject, DomainClassMethods.ERRORS_PROPERTY));
        }

        return success;
    }

    public boolean update(final Object domainObject, final ScaffoldCallback callback) {
        Boolean b = (Boolean)InvokerHelper.invokeMethod(domainObject,SavePersistentMethod.METHOD_SIGNATURE, new Object[0]);
        boolean success = b.booleanValue();
        if(!success) {
            callback.setErrors((Errors)InvokerHelper.getProperty(domainObject, DomainClassMethods.ERRORS_PROPERTY));
        }

        return success;
    }

    public Object delete(Serializable id) {
        Object domainObject = get(id);

        if(domainObject != null) {
            InvokerHelper.invokeMethod(domainObject, DeletePersistentMethod.METHOD_SIGNATURE, new Object[0]);
        }
        return domainObject;
    }
}
