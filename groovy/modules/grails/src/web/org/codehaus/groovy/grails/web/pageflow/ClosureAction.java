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

import java.lang.Exception;

import org.codehaus.groovy.grails.web.pageflow.exceptions.InvalidClosureReturnValueException;
import org.springframework.util.Assert;
import org.springframework.webflow.Action;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * <p>Wrapper class to execute a Groovy closure as a SWF actions.
 * 
 * @author Steven Devijver
 * @since Jul 10, 2005
 */
public class ClosureAction implements Action {

	private Closure closure = null;
	private String stateId = null;
	private String flowId = null;
	
	public ClosureAction(String flowId, String stateId, Closure closure) {
		super();
		Assert.notNull(flowId);
		Assert.notNull(stateId);
		Assert.notNull(closure);
		this.flowId = flowId;
		this.stateId = stateId;
		this.closure = closure;
	}

	public Event execute(RequestContext requestContext) throws Exception {
		Object result = this.closure.call(new Object[] { requestContext });
		
		if (result == null) {
			throw new InvalidClosureReturnValueException("Null value returned by action closure of state [" + stateId + "] in page flow [" + flowId + "]!");
		} else if (result instanceof Event) {
			return (Event)result;
		} else if (result instanceof String) {
			return new Event(closure, (String)result);
		} else {
			throw new InvalidClosureReturnValueException("Return value of type [" + result.getClass().getName() + "] returned by action closure of state [" + stateId + "] in page flow [" + flowId + "] is not supported!");
		}
	}

}
