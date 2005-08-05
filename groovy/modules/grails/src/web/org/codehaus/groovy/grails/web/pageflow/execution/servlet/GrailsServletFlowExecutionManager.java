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
package org.codehaus.groovy.grails.web.pageflow.execution.servlet;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.FlowSession;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.ViewDescriptor;
import org.springframework.webflow.config.FlowLocator;
import org.springframework.webflow.execution.EnterStateVetoException;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.servlet.ServletEvent;
import org.springframework.webflow.execution.servlet.ServletFlowExecutionManager;

/**
 * <p>This specialisation of {@link org.springframework.webflow.execution.servlet.ServletFlowExecutionManager}
 * stores and retrieves the flow execution id from a cookie if no flow execution id parameter is available.
 * 
 * <p>Users must use the regular Spring Web Flow mechanism of passing the flow execution id as a parameter if
 * more than one page flow is configured for a given URI.
 * 
 * @author Steven Devijver
 * @since Jul 19, 2005
 */
public class GrailsServletFlowExecutionManager extends
		ServletFlowExecutionManager {

	public static final String FLOW_EXECUTION_ID_COOKIE_NAME = GrailsServletFlowExecutionManager.class.getName() + "$FlowExecutionIdCookie";
	private static final String TRUE = "true";
	private static final String RESET_PARAMETER = "_reset";
	
	public GrailsServletFlowExecutionManager() {
		super();
	}

	public GrailsServletFlowExecutionManager(Flow flow) {
		super(flow);
	}

	public GrailsServletFlowExecutionManager(FlowLocator flowLocator) {
		super(flowLocator);
	}

	protected Event createEvent(HttpServletRequest request,
			HttpServletResponse response) {
		return super.createEvent(request, response);
	}
	
	protected String getFlowExecutionId(Event event) {
		if (event instanceof ServletEvent) {			
			String flowExecutionId = super.getFlowExecutionId(event);
			if (StringUtils.isBlank(flowExecutionId)) {
				String resetValue = (String)event.getParameter(RESET_PARAMETER);
				if (StringUtils.isNotBlank(resetValue) && TRUE.equals(resetValue)) {
					((ServletEvent)event).getRequest().setAttribute(RESET_PARAMETER, TRUE);
					return null;
				}
				Cookie[] cookies = ((ServletEvent)event).getRequest().getCookies();
				for (int i = 0; cookies != null && i < cookies.length; i++) {
					if (FLOW_EXECUTION_ID_COOKIE_NAME.equals(cookies[i].getName())) {
						if (StringUtils.isNotBlank(cookies[i].getValue())) {
							return cookies[i].getValue();
						} else {
							return null;
						}
					}
				}
				return null;
			} else {
				return flowExecutionId;
			}
		} else {
			return super.getFlowExecutionId(event);
		}
	}
	
	public ViewDescriptor handle(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		final boolean[] created = new boolean[1];
		created[0] = false;
		ViewDescriptor viewDescriptor = super.handle(request, response, new FlowExecutionListener() {
			public void created(FlowExecutionContext context) {
				created[0] = true;
			}
			public void sessionStarted(RequestContext context) {
			}
			public void resumed(RequestContext context) {
			}
			public void sessionEnded(RequestContext context, FlowSession endedSession) {
			}
			public void stateEntered(RequestContext context, State previousState, State state) {
			}
			public void eventSignaled(RequestContext context) {
			}
			public void requestProcessed(RequestContext context) {
			}
			public void loaded(FlowExecutionContext context, Serializable id) {
			}
			public void paused(RequestContext context) {
			}
			public void removed(FlowExecutionContext context, Serializable id) {
			}
			public void requestSubmitted(RequestContext context) {
			}
			public void saved(FlowExecutionContext context, Serializable id) {
			}
			public void sessionStarting(RequestContext context, State startState, Map input) throws EnterStateVetoException {
			}
			public void stateEntering(RequestContext context, State nextState) throws EnterStateVetoException {
			}
		});
		
		Cookie flowExecutionIdCookie = null;
		if (request.getAttribute(RESET_PARAMETER) != null && request.getAttribute(RESET_PARAMETER).equals(TRUE)) {
			flowExecutionIdCookie = new Cookie(FLOW_EXECUTION_ID_COOKIE_NAME, "");
		} else if (created[0]) {
			flowExecutionIdCookie = new Cookie(FLOW_EXECUTION_ID_COOKIE_NAME, (String)viewDescriptor.getAttribute(FLOW_EXECUTION_ID_ATTRIBUTE));
		}
		if (flowExecutionIdCookie != null) {
			response.addCookie(flowExecutionIdCookie);
		}

		return viewDescriptor;
	}
}
