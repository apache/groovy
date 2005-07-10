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

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.commons.GrailsPageFlowClass;
import org.codehaus.groovy.grails.web.pageflow.GrailsFlowBuilder;
import org.codehaus.groovy.grails.web.servlet.mvc.SimpleGrailsController;
import org.codehaus.groovy.grails.web.servlet.view.GrailsView;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.util.Assert;
import org.springframework.web.flow.config.FlowFactoryBean;
import org.springframework.web.flow.mvc.FlowController;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springmodules.beans.factory.drivers.Bean;

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
		StringBuffer urlMappings = new StringBuffer();
		
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
			
			Bean flowController = SpringConfigUtils.createSingletonBean(FlowController.class);
			flowController.setProperty("flow", flowFactoryBean);
			beanReferences.add(SpringConfigUtils.createBeanReference(pageFlow.getFullName() + "Controller", flowController));
			
			urlMappings.append(pageFlow.getUri());
			urlMappings.append("=");
			urlMappings.append(pageFlow.getFullName());
			urlMappings.append("Controller\n");
		}
		
		Bean simpleGrailsController = SpringConfigUtils.createSingletonBean(SimpleGrailsController.class);
		simpleGrailsController.setAutowire("byType");
		beanReferences.add(SpringConfigUtils.createBeanReference("simpleGrailsController", simpleGrailsController));
		
		Bean internalResourceViewResolver = SpringConfigUtils.createSingletonBean(InternalResourceViewResolver.class);
		internalResourceViewResolver.setProperty("prefix", SpringConfigUtils.createLiteralValue("/WEB-INF/jsp/"));
		internalResourceViewResolver.setProperty("suffix", SpringConfigUtils.createLiteralValue(".jsp"));
//		internalResourceViewResolver.setProperty("viewClass", SpringConfigUtils.createLiteralValue(GrailsView.class.getName()));
		beanReferences.add(SpringConfigUtils.createBeanReference("jspViewResolver", internalResourceViewResolver));
		
		Bean simpleUrlHandlerMapping = null;
		if (application.getControllers().length > 0 || application.getPageFlows().length > 0) {
			simpleUrlHandlerMapping = SpringConfigUtils.createSingletonBean(SimpleUrlHandlerMapping.class);
	//		simpleUrlHandlerMapping.setProperty("mappings", SpringConfigUtils.createLiteralValue("/*=simpleGrailsController"));
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
				urlMappings.append(simpleController.getURIs()[x]);
				urlMappings.append("=simpleGrailsController\n");
			}
		}

		if (simpleUrlHandlerMapping != null) {
			System.out.println(urlMappings.toString());
			urlMappings.setLength(urlMappings.length() - "\n".length());
			simpleUrlHandlerMapping.setProperty("mappings", SpringConfigUtils.createLiteralValue(urlMappings.toString()));
		}
			
		return beanReferences;
	}
}
