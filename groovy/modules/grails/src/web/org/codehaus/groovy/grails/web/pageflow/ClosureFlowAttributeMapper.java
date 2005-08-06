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

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.grails.web.pageflow.exceptions.InvalidClosureReturnValueException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.RequestContext;

/**
 * <p>Delegates subflow input and output operations to closures.
 * 
 * @author Steven Devijver
 * @since Aug 6, 2005
 */
public class ClosureFlowAttributeMapper implements FlowAttributeMapper {

	String flowId = null;
	String stateId = null;
	Closure subflowInputClosure = null;
	Closure subflowOutputClosure = null;
	
	public ClosureFlowAttributeMapper(String flowId, String stateId, Closure subflowInputClosure, Closure subflowOutputClosure) {
		super();
		this.flowId = flowId;
		this.stateId = stateId;
		this.subflowInputClosure = subflowInputClosure;
		this.subflowOutputClosure = subflowOutputClosure;
	}
	
	public Map createSubflowInput(RequestContext context) {
		if (this.subflowInputClosure != null) {
			Object returnValue = this.subflowInputClosure.call(context);
			if (returnValue == null) {
				return new HashMap();
			} else if (returnValue instanceof Map) {
				return (Map)returnValue;
			} else {
				throw new InvalidClosureReturnValueException("Return value of type [" + returnValue.getClass().getName() + "] of subflow input closure for flow attribute mapper of state [" + this.stateId + "] of page flow [" + this.flowId + "] is not supported!");
			}
		} else {
			return new HashMap();
		}
	}
	
	public void mapSubflowOutput(RequestContext context) {
		if (this.subflowOutputClosure != null) {
			Object returnValue = this.subflowOutputClosure.call(context);
			if (returnValue == null) {
				return;
			} else if (returnValue instanceof Map) {
				context.getFlowExecutionContext().getActiveSession().getParent().getScope().setAttributes((Map)returnValue);
			} else {
				throw new InvalidClosureReturnValueException("Return value of type [" + returnValue.getClass().getName() + "] of subflow output closure for flow attribute mapper of state [" + this.stateId + "] of page flow [" + this.flowId + "] is not supported!");
			}
		}
	}
}
