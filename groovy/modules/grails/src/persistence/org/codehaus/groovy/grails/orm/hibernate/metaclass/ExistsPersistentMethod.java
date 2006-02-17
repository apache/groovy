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
package org.codehaus.groovy.grails.orm.hibernate.metaclass;

import groovy.lang.MissingMethodException;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A static method that checks whether an entity exists in the database for the specified id
 *
 * eg. Account.exsits(1)
 * 
 * @author Graeme Rocher
 * @since 17-Feb-2006
 */
public class ExistsPersistentMethod extends AbstractStaticPersistentMethod {

    private static final Pattern METHOD_PATTERN = Pattern.compile("^exists$");
    private static final String METHOD_SIGNATURE = "exists";

    private GrailsApplication application;
    private Map context = Ognl.createDefaultContext(this);
    private DefaultTypeConverter typeConverter = new DefaultTypeConverter();

    public ExistsPersistentMethod(GrailsApplication application, SessionFactory sessionFactory, ClassLoader classLoader) {
        super(sessionFactory, classLoader,METHOD_PATTERN);
        this.application = application;
    }

    protected Object doInvokeInternal(Class clazz, String methodName, Object[] arguments) {
        // if no arguments passed throw exception
        if(arguments.length == 0)
            throw new MissingMethodException(METHOD_SIGNATURE, clazz,arguments);

        // if its not a map throw exception
        Object arg = arguments[0];

        if(arg == null)
            return null;

        GrailsDomainClass domainClass = this.application.getGrailsDomainClass(clazz.getName());
        if(domainClass != null) {
            Class identityType = domainClass.getIdentifier().getType();
            if(!identityType.isAssignableFrom(arg.getClass())) {
                arg = typeConverter.convertValue(context,arg, identityType);
            }
        }

        return Boolean.valueOf(super.getHibernateTemplate().get( clazz, (Serializable)arg ) != null);
    }
}
