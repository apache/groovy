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
package org.codehaus.groovy.grails.commons.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.commons.GrailsDataSource;
import org.codehaus.groovy.grails.commons.GrailsPageFlowClass;
import org.codehaus.groovy.grails.orm.hibernate.ConfigurableLocalsSessionFactoryBean;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainConfiguration;
import org.codehaus.groovy.grails.orm.hibernate.support.HibernateDialectDetectorFactoryBean;
import org.codehaus.groovy.grails.web.pageflow.GrailsFlowBuilder;
import org.codehaus.groovy.grails.web.pageflow.execution.servlet.GrailsServletFlowExecutionManager;
import org.codehaus.groovy.grails.web.servlet.mvc.SimpleGrailsController;
import org.hibernate.dialect.HSQLDialect;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.Assert;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.webflow.config.FlowFactoryBean;
import org.springframework.webflow.mvc.FlowController;
import org.springmodules.beans.factory.config.MapToPropertiesFactoryBean;
import org.springmodules.beans.factory.drivers.Bean;
import org.springmodules.db.hsqldb.ServerBean;

/**
 * <p>Creates beans and bean references for a Grails application.
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class SpringConfig {

	private GrailsApplication application = null;
	
	public SpringConfig(GrailsApplication application) {
		super();
		this.application = application;
	}

	public Collection getBeanReferences() {
		Collection beanReferences = new ArrayList();
		Map urlMappings = new HashMap();
		
		Assert.notNull(application);
		
		GrailsPageFlowClass[] pageFlows = application.getPageFlows();
		for (int i = 0; i < pageFlows.length; i++) {
			GrailsPageFlowClass pageFlow = pageFlows[i];
			if (!pageFlow.getAvailable()) {
				continue;
			}
			Bean pageFlowClass = SpringConfigUtils.createSingletonBean(MethodInvokingFactoryBean.class);
			pageFlowClass.setProperty("targetObject", SpringConfigUtils.createBeanReference("grailsApplication"));
			pageFlowClass.setProperty("targetMethod", SpringConfigUtils.createLiteralValue("getPageFlow"));
			pageFlowClass.setProperty("arguments", SpringConfigUtils.createLiteralValue(pageFlow.getFullName()));
			beanReferences.add(SpringConfigUtils.createBeanReference(pageFlow.getFullName() + "Class", pageFlowClass));
			
			Bean pageFlowInstance = SpringConfigUtils.createSingletonBean();
			pageFlowInstance.setFactoryBean(SpringConfigUtils.createBeanReference(pageFlow.getFullName() + "Class"));
			pageFlowInstance.setFactoryMethod("newInstance");
			if (pageFlow.byType()) {
				pageFlowInstance.setAutowire("byType");
			} else if (pageFlow.byName()) {
				pageFlowInstance.setAutowire("byName");
			}
			beanReferences.add(SpringConfigUtils.createBeanReference(pageFlow.getFullName(), pageFlowInstance));
			
			Bean flowBuilder = SpringConfigUtils.createSingletonBean(GrailsFlowBuilder.class);
			flowBuilder.setProperty("pageFlowClass", SpringConfigUtils.createBeanReference(pageFlow.getFullName() + "Class"));
			
			Bean flowFactoryBean = SpringConfigUtils.createSingletonBean(FlowFactoryBean.class);
			flowFactoryBean.setProperty("flowBuilder", flowBuilder);
			beanReferences.add(SpringConfigUtils.createBeanReference(pageFlow.getFlowId(), flowFactoryBean));

			if (pageFlow.getAccessible()) {
				Bean flowExecutionManager = SpringConfigUtils.createSingletonBean(GrailsServletFlowExecutionManager.class);
				flowExecutionManager.setProperty("flow", SpringConfigUtils.createBeanReference(pageFlow.getFlowId()));
				
				Bean flowController = SpringConfigUtils.createSingletonBean(FlowController.class);
				flowController.setProperty("flowExecutionManager", flowExecutionManager);
				beanReferences.add(SpringConfigUtils.createBeanReference(pageFlow.getFullName() + "Controller", flowController));
				
				urlMappings.put(pageFlow.getUri(), pageFlow.getFullName() + "Controller");
			}
			
		}
		
		Bean simpleGrailsController = SpringConfigUtils.createSingletonBean(SimpleGrailsController.class);
		simpleGrailsController.setAutowire("byType");
		beanReferences.add(SpringConfigUtils.createBeanReference("simpleGrailsController", simpleGrailsController));
		
		Bean internalResourceViewResolver = SpringConfigUtils.createSingletonBean(InternalResourceViewResolver.class);
		internalResourceViewResolver.setProperty("prefix", SpringConfigUtils.createLiteralValue("/WEB-INF/jsp/"));
		internalResourceViewResolver.setProperty("suffix", SpringConfigUtils.createLiteralValue(".jsp"));
		beanReferences.add(SpringConfigUtils.createBeanReference("jspViewResolver", internalResourceViewResolver));
		
		Bean simpleUrlHandlerMapping = null;
		if (application.getControllers().length > 0 || application.getPageFlows().length > 0) {
			simpleUrlHandlerMapping = SpringConfigUtils.createSingletonBean(SimpleUrlHandlerMapping.class);
			beanReferences.add(SpringConfigUtils.createBeanReference("handlerMapping", simpleUrlHandlerMapping));
		}
		
		GrailsControllerClass[] simpleControllers = application.getControllers();
		for (int i = 0; i < simpleControllers.length; i++) {
			GrailsControllerClass simpleController = simpleControllers[i];
			if (!simpleController.getAvailable()) {
				continue;
			}
			Bean controllerClass = SpringConfigUtils.createSingletonBean(MethodInvokingFactoryBean.class);
			controllerClass.setProperty("targetObject", SpringConfigUtils.createBeanReference("grailsApplication"));
			controllerClass.setProperty("targetMethod", SpringConfigUtils.createLiteralValue("getController"));
			controllerClass.setProperty("arguments", SpringConfigUtils.createLiteralValue(simpleController.getFullName()));
			beanReferences.add(SpringConfigUtils.createBeanReference(simpleController.getFullName() + "Class", controllerClass));
			
			Bean controller = SpringConfigUtils.createSingletonBean();
			controller.setFactoryBean(SpringConfigUtils.createBeanReference(simpleController.getFullName() + "Class"));
			controller.setFactoryMethod("newInstance");
			if (simpleController.byType()) {
				controller.setAutowire("byType");
			} else if (simpleController.byName()) {
				controller.setAutowire("byName");
			}
			beanReferences.add(SpringConfigUtils.createBeanReference(simpleController.getFullName(), controller));
			for (int x = 0; x < simpleController.getURIs().length; x++) {
				urlMappings.put(simpleController.getURIs()[x], "simpleGrailsController");
			}
		}

		if (simpleUrlHandlerMapping != null) {
			simpleUrlHandlerMapping.setProperty("mappings", SpringConfigUtils.createProperties(urlMappings));
		}

		boolean dependsOnHsqldbServer = false;
		if (application.getGrailsDataSource() != null) {
			GrailsDataSource grailsDataSource = application.getGrailsDataSource();
			Bean dataSource = null;
			if (grailsDataSource.isPooled()) {
				dataSource = SpringConfigUtils.createSingletonBean(BasicDataSource.class);
				dataSource.setDestroyMethod("close");
			} else {
				dataSource = SpringConfigUtils.createSingletonBean(DriverManagerDataSource.class);
			}
			dataSource.setProperty("driverClassName", SpringConfigUtils.createLiteralValue(grailsDataSource.getDriverClassName()));
			dataSource.setProperty("url", SpringConfigUtils.createLiteralValue(grailsDataSource.getUrl()));
			dataSource.setProperty("username", SpringConfigUtils.createLiteralValue(grailsDataSource.getUsername()));
			dataSource.setProperty("password", SpringConfigUtils.createLiteralValue(grailsDataSource.getPassword()));	
			beanReferences.add(SpringConfigUtils.createBeanReference("dataSource", dataSource));
		} else {
			Bean dataSource = SpringConfigUtils.createSingletonBean(BasicDataSource.class);
			dataSource.setDestroyMethod("close");
			dataSource.setProperty("driverClassName", SpringConfigUtils.createLiteralValue("org.hsqldb.jdbcDriver"));
			dataSource.setProperty("url", SpringConfigUtils.createLiteralValue("jdbc:hsqldb:hsql://localhost:9101/"));
			dataSource.setProperty("username", SpringConfigUtils.createLiteralValue("sa"));
			dataSource.setProperty("password", SpringConfigUtils.createLiteralValue(""));
			beanReferences.add(SpringConfigUtils.createBeanReference("dataSource", dataSource));
			
			Bean hsqldbServer = SpringConfigUtils.createSingletonBean(ServerBean.class);
			hsqldbServer.setProperty("dataSource", SpringConfigUtils.createBeanReference("dataSource"));
			Map hsqldbProperties = new HashMap();
			hsqldbProperties.put("server.port", "9101");
			hsqldbProperties.put("server.database.0", "mem:temp");
			hsqldbServer.setProperty("serverProperties", SpringConfigUtils.createProperties(hsqldbProperties));
			beanReferences.add(SpringConfigUtils.createBeanReference("hsqldbServer", hsqldbServer));
			dependsOnHsqldbServer = true;
		}
		
		Map vendorNameDialectMappings = new HashMap();
		vendorNameDialectMappings.put("HSQL Database Engine", HSQLDialect.class.getName());
			
		Bean dialectDetector = SpringConfigUtils.createSingletonBean(HibernateDialectDetectorFactoryBean.class);
		dialectDetector.setProperty("dataSource", SpringConfigUtils.createBeanReference("dataSource"));
		dialectDetector.setProperty("vendorNameDialectMappings", SpringConfigUtils.createProperties(vendorNameDialectMappings));
		if (dependsOnHsqldbServer) {
			Collection dependsOn = new ArrayList();
			dependsOn.add(SpringConfigUtils.createBeanReference("hsqldbServer"));
			dialectDetector.setDependsOn(dependsOn);
		}
		
		Map hibernatePropertiesMap = new HashMap();
		hibernatePropertiesMap.put(SpringConfigUtils.createLiteralValue("hibernate.dialect"), dialectDetector);
		hibernatePropertiesMap.put(SpringConfigUtils.createLiteralValue("hibernate.hbm2ddl.auto"), SpringConfigUtils.createLiteralValue("create-drop"));
		Bean hibernateProperties = SpringConfigUtils.createSingletonBean(MapToPropertiesFactoryBean.class);
		hibernateProperties.setProperty("map", SpringConfigUtils.createMap(hibernatePropertiesMap));
		
		Bean grailsHibernateConfiguration = SpringConfigUtils.createSingletonBean(GrailsDomainConfiguration.class);
		grailsHibernateConfiguration.setProperty("grailsApplication", SpringConfigUtils.createBeanReference("grailsApplication"));
		
		Bean localSessionFactoryBean = SpringConfigUtils.createSingletonBean(ConfigurableLocalsSessionFactoryBean.class);
		localSessionFactoryBean.setProperty("dataSource", SpringConfigUtils.createBeanReference("dataSource"));
		localSessionFactoryBean.setProperty("hibernateProperties", hibernateProperties);
		localSessionFactoryBean.setProperty("configuration", grailsHibernateConfiguration);
		beanReferences.add(SpringConfigUtils.createBeanReference("sessionFactory", localSessionFactoryBean));
		
		return beanReferences;
	}
}
