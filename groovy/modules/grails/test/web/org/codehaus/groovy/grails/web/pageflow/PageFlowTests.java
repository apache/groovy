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
package org.codehaus.groovy.grails.web.pageflow;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.spring.SpringConfig;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.webflow.mvc.FlowController;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 9, 2005
 */
public class PageFlowTests extends AbstractDependencyInjectionSpringContextTests {

	protected FlowController testPageFlowController = null;
	protected GrailsApplication grailsApplication = null;
	
	public PageFlowTests() {
		super();
		setPopulateProtectedVariables(true);
	}


	protected String[] getConfigLocations() {
		return new String[] { "org/codehaus/groovy/grails/web/pageflow/page-flow-tests.xml" };
	}
	
	public void testFlowControllerFirstStage() throws Exception {
		SpringConfig springConfig = new SpringConfig(grailsApplication);
		springConfig.getBeanReferences();
	}
}
