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
package org.codehaus.groovy.grails.orm.hibernate.metaclass;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty;
import org.codehaus.groovy.grails.commons.metaclass.DelegatingMetaClass;
import org.codehaus.groovy.grails.metaclass.DomainClassMethods;
import org.codehaus.groovy.grails.orm.hibernate.validation.GrailsDomainClassValidator;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.hibernate.SessionFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.io.Serializable;

/**
 * 
 * 
 * @author Steven Devijver
 * @author Graeme Rocher
 * 
 * @since Aug 7, 2005
 */
public class SavePersistentMethod extends AbstractDynamicPersistentMethod {

    public static final String METHOD_SIGNATURE = "save";
    private GrailsApplication application;

    public SavePersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader, GrailsApplication application) {
        super(METHOD_SIGNATURE,sessionFactory, classLoader);

        if(application == null)
            throw new IllegalArgumentException("Constructor argument 'application' cannot be null");
        this.application = application;
    }

    protected Object doInvokeInternal(Object target, Object[] arguments) {

        Errors errors = new BindException(target, target.getClass().getName());
        GrailsDomainClass domainClass = application.getGrailsDomainClass( target.getClass().getName() );
        Validator validator = null;
        boolean doValidation = true;
        Boolean success = Boolean.TRUE;
        if(domainClass != null) {
            validator = domainClass.getValidator();
            doValidation = true;
            if(arguments.length > 0) {
                if(arguments[0] instanceof Boolean) {
                    doValidation = ((Boolean)arguments[0]).booleanValue();
                }
            }
        }
        if(doValidation) {
            if(validator != null) {
                if(validator instanceof GrailsDomainClassValidator) {
                     ((GrailsDomainClassValidator)validator).setHibernateTemplate(getHibernateTemplate());
                }
                validator.validate(target,errors);

                if(errors.hasErrors()) {
                    DelegatingMetaClass metaClass = (DelegatingMetaClass)InvokerHelper.getInstance().getMetaRegistry().getMetaClass(target.getClass());
                    metaClass.setProperty(target,DomainClassMethods.ERRORS_PROPERTY,errors);
                    return Boolean.valueOf(!errors.hasErrors());
                }
            }
        }
        HibernateTemplate t = getHibernateTemplate();
        t.setFlushMode(HibernateTemplate.FLUSH_COMMIT);

        // this piece of code will retrieve a persistent instant
        // of a domain class property is only the id is set thus
        // relieving this burden off the developer
        if(domainClass != null) {
            BeanWrapper bean = new BeanWrapperImpl(target);
            GrailsDomainClassProperty[] props = domainClass.getPersistantProperties();
            for (int i = 0; i < props.length; i++) {
                GrailsDomainClassProperty prop = props[i];
                if(prop.isManyToOne() || prop.isOneToOne()) {
                    Object propValue = bean.getPropertyValue(prop.getName());
                    if(propValue != null && !t.contains(propValue)) {
                        GrailsDomainClass otherSide = application.getGrailsDomainClass(prop.getType().getName());
                        if(otherSide != null) {
                            BeanWrapper propBean = new BeanWrapperImpl(propValue);

                            Serializable id = (Serializable)propBean.getPropertyValue(otherSide.getIdentifier().getName());
                            bean.setPropertyValue(prop.getName(),t.get(prop.getType(),id));
                        }
                    }
                }
            }
        }


        if(t.contains(target)) {
            t.update(target);
        }
        else {
            t.saveOrUpdate(target);
        }


        return success;
    }

}
