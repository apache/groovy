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

import grails.pageflow.Flow;
import grails.pageflow.State;
import grails.pageflow.Transition;
import groovy.lang.GroovyObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.grails.commons.GrailsPageFlowClass;
import org.codehaus.groovy.grails.web.pageflow.action.GrailsFormAction;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionCriteriaFactory;
import org.springframework.webflow.config.AbstractFlowBuilder;
import org.springframework.webflow.config.FlowBuilderException;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 9, 2005
 */
public class GrailsFlowBuilder extends AbstractFlowBuilder implements ApplicationContextAware {

	private static final String FLOW = "flow";
	private static final String METHOD = "method";
	private static final String BIND_AND_VALIDATE = "bindAndValidate";
	
	private ApplicationContext applicationContext = null;
	private GrailsPageFlowClass pageFlowClass = null;
	
	public GrailsFlowBuilder() {
		super();
	}

	public void setPageFlowClass(GrailsPageFlowClass pageFlowClass) {
		this.pageFlowClass = pageFlowClass;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	protected String flowId() {
		return this.pageFlowClass.getFlowId();
	}

	public void buildStates() throws FlowBuilderException {
		GroovyObject pageFlow = (GroovyObject)this.applicationContext.getBean(pageFlowClass.getFullName());
		Assert.notNull(pageFlow);
		Flow flow = (Flow)pageFlow.getProperty(FLOW);
		Assert.notNull(flow);
		String firstState = null;
		
		for (Iterator iter = flow.getStates().iterator(); iter.hasNext();) {
			State state = (State)iter.next();
			
			if (firstState == null) {
				firstState = state.getId();
			}
			
			if (state.isActionState()) {
				AnnotatedAction action = null;
				
				if (state.getAction() != null) {
					action = new AnnotatedAction(state.getAction());
				} else if (state.getActionClass() != null) {
					Action tmpAction = null;
					if (Action.class.isAssignableFrom(state.getActionClass())) {
						tmpAction = (Action)BeanUtils.instantiateClass(state.getActionClass());
					} else {
						throw new UnsupportedOperationException("None " + Action.class.getName() + " action classes are not yet supported!");
					}
					if (state.getActionProperties() != null) {
						BeanWrapper beanWrapper = new BeanWrapperImpl(tmpAction);
						for (Iterator iter2 = state.getActionProperties().entrySet().iterator(); iter2.hasNext();) {
							Map.Entry entry = (Map.Entry)iter2.next();
							beanWrapper.setPropertyValue((String)entry.getKey(), entry.getValue());
						}
					}
					action = new AnnotatedAction(tmpAction);
				} else if (state.getActionClosure() != null) {
					action = new AnnotatedAction(new ClosureAction(this.pageFlowClass.getFlowId(), state.getId(), state.getActionClosure()));
				} else if (state.getActionFormDetails() != null) {
					GrailsFormAction formAction = new GrailsFormAction();
					BeanWrapper beanWrapper = new BeanWrapperImpl(formAction);
					for (Iterator iter2 = state.getActionFormDetails().entrySet().iterator(); iter2.hasNext();) {
						Map.Entry entry = (Map.Entry)iter2.next();
						beanWrapper.setPropertyValue((String)entry.getKey(), entry.getValue());
					}
					formAction.afterPropertiesSet();
					action = new AnnotatedAction(formAction);
					if (state.getActionMethod() != null) {
						action.setProperty(METHOD, state.getActionMethod());
					} else {
						action.setProperty(METHOD, BIND_AND_VALIDATE);
					}
				} else {
					throw new UnsupportedOperationException();
				}
						
				addActionState(state.getId(), action, getTransitions(state.getTransitions()));
			} else if (state.isViewState()) {
				if (state.getViewName() != null) {
					addViewState(state.getId(), new ModelViewDescriptorCreator(state.getViewName(), state.getViewModel()), getTransitions(state.getTransitions()), new HashMap());
				} else if (state.getViewClosure() != null) {
					throw new UnsupportedOperationException("View closures are not yet supported!");
				}
			} else if (state.isDecisionState()) {
				throw new UnsupportedOperationException("Decision states are not yet supported!");
			} else if (state.isSubflowState()) {
				throw new UnsupportedOperationException("Subflow states are not yet supported!");
			} else if (state.isEndState()) {
				if (state.getViewName() != null) {
					addEndState(state.getId(), new ModelViewDescriptorCreator(state.getViewName(), state.getViewModel()), new HashMap());
				} else {
					addEndState(state.getId());
				}
			} else {
				throw new UnsupportedOperationException();
			}
		}
		if (firstState != null) {
			getFlow().setStartState(firstState);
		}
	}

	private org.springframework.webflow.Transition[] getTransitions(List transitions) {
		org.springframework.webflow.Transition[] transitionArray = new org.springframework.webflow.Transition[transitions.size()];
		int i = 0;
		for (Iterator iter2 = transitions.iterator(); iter2.hasNext(); i++) {
			Transition transition = (Transition)iter2.next();
			TransitionCriteria transitionCriteria = TransitionCriteriaFactory.eventId(transition.getName());
			transitionArray[i] = new org.springframework.webflow.Transition(transitionCriteria, transition.getTargetStateId());
		}
		return transitionArray;
	}
}
