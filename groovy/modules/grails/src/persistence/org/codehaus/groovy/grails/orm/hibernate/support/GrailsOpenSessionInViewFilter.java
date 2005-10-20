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

import javax.servlet.http.HttpServletRequest;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.spring.SpringConfig;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springmodules.beans.factory.drivers.xml.XmlApplicationContextDriver;
/**
 * <p>Uses the GrailsApplication sesssionFactory to apply the filter
 * 
 * @author Graeme Rocher
 * @since Oct 20, 2005
 */
public class GrailsOpenSessionInViewFilter extends OpenSessionInViewFilter {

    private static final String GRAILS_APPLICATION_ID = "grailsApplication";
    private static final String GRAILS_APPLICATION_CONTEXT = "grailsApplicationContext";

	protected SessionFactory lookupSessionFactory() {
		WebApplicationContext parent =
			WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		
        // construct the SpringConfig for the container managed application
        GrailsApplication application = (GrailsApplication) parent.getBean(GRAILS_APPLICATION_ID, GrailsApplication.class);
        SpringConfig springConfig = new SpringConfig(application);
        // return a context that obeys grails' settings
        
        ApplicationContext context = new XmlApplicationContextDriver().getApplicationContext(
        			springConfig.getBeanReferences(),
        			parent
        		); 
                
       getServletContext().setAttribute( GRAILS_APPLICATION_CONTEXT, context );
       
       return (SessionFactory) context.getBean(getSessionFactoryBeanName(), SessionFactory.class);
	}

	protected Session getSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
		Session session = SessionFactoryUtils.getSession(sessionFactory, true);
		session.setFlushMode(FlushMode.AUTO);
		return session;
	}
	
	
}
