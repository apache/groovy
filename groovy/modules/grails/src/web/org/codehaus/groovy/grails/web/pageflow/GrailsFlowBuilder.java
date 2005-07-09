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

import java.lang.String;
import java.lang.UnsupportedOperationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.grails.commons.GrailsPageFlowClass;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.TransitionCriteriaFactory;
import org.springframework.web.flow.action.FormAction;
import org.springframework.web.flow.config.AbstractFlowBuilder;
import org.springframework.web.flow.config.FlowBuilderException;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 9, 2005
 */
public class GrailsFlowBuilder extends AbstractFlowBuilder implements ApplicationContextAware {

	private static final String FLOW = "flow";
	
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
		
		for (Iterator iter = flow.getStates().iterator(); iter.hasNext();) {
			State state = (State)iter.next();
			
			if (state.isActionState()) {
				Action action = null;
				
				if (state.getAction() != null) {
					action = state.getAction();
				} else if (state.getActionClass() != null) {
					if (Action.class.isAssignableFrom(state.getActionClass())) {
						action = (Action)BeanUtils.instantiateClass(state.getActionClass());
					} else {
						throw new UnsupportedOperationException("None " + Action.class.getName() + " action classes are not yet supported!");
					}
					if (state.getActionProperties() != null) {
						BeanWrapper beanWrapper = new BeanWrapperImpl(action);
						for (Iterator iter2 = state.getActionProperties().entrySet().iterator(); iter2.hasNext();) {
							Map.Entry entry = (Map.Entry)iter2.next();
							beanWrapper.setPropertyValue((String)entry.getKey(), entry.getValue());
						}
					}
				} else if (state.getActionClosure() != null) {
					action = new ClosureAction(this.pageFlowClass.getFlowId(), state.getId(), state.getActionClosure());
				} else if (state.getActionFormDetails() != null) {
					action = new FormAction();
					BeanWrapper beanWrapper = new BeanWrapperImpl(action);
					for (Iterator iter2 = state.getActionFormDetails().entrySet().iterator(); iter2.hasNext();) {
						Map.Entry entry = (Map.Entry)iter2.next();
						beanWrapper.setPropertyValue((String)entry.getKey(), entry.getValue());
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
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}

	private org.springframework.web.flow.Transition[] getTransitions(List transitions) {
		org.springframework.web.flow.Transition[] transitionArray = new org.springframework.web.flow.Transition[transitions.size()];
		int i = 0;
		for (Iterator iter2 = transitions.iterator(); iter2.hasNext(); i++) {
			Transition transition = (Transition)iter2.next();
			TransitionCriteria transitionCriteria = TransitionCriteriaFactory.eventId(transition.getName());
			transitionArray[i] = new org.springframework.web.flow.Transition(transitionCriteria, transition.getTargetStateId());
		}
		return transitionArray;
	}
}
