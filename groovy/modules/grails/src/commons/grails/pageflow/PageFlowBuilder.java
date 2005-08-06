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
package grails.pageflow;

import groovy.util.BuilderSupport;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * <p>Builder to create a page flow configuration.
 * 
 * @author Steven Devijver
 * @since Jul 9, 2005
 */
public class PageFlowBuilder extends BuilderSupport {

	private static final String FLOW = "flow";
	
	public PageFlowBuilder() {
		super();
	}

	protected void setParent(Object parent, Object child) {
		if (parent == null) {
			return;
		}
		if (parent instanceof Flow && child instanceof State) {
			((Flow)parent).addState((State)child);
		} else if (parent instanceof State && child instanceof Transition) {
			((State)parent).addTransition((Transition)child);
		} else {
			throw new IllegalArgumentException("No parent/child relationship between classes [" + parent.getClass().getName() + "] (parent) and [" + child.getClass().getName() + "] (child)!");
		}
	}

	protected Object createNode(Object name) {
		if (FLOW.equals(name) && getCurrent() == null) {
			return new Flow();
		} else if (getCurrent() instanceof Flow) {
			return new State((String)name, new HashMap());
		} else {
			throw new IllegalArgumentException("call to [" + name + "] without attributes is not supported!");
		}
	}

	protected Object createNode(Object name, Object value) {
		if (getCurrent() == null) {
			throw new IllegalArgumentException("call to [" + name + "] with value [" + value + "] is not supported!");
		} else if (getCurrent() instanceof Flow) {
			throw new IllegalArgumentException("State [" + name + "] requires one of these attributes: [action, view, decision, subflow]!");
		} else if (getCurrent() instanceof State && !((State)getCurrent()).isEndState()) {
			if (value == null) {
				throw new IllegalArgumentException("Target state id is required as value for transition [" + name + "], state [" + ((State)getCurrent()).getId() + "]!");
			} else if (!(value instanceof String)) {
				throw new IllegalArgumentException("Target state id must be a string value for transition [" + name + "], state [" + ((State)getCurrent()).getId() + "]!");
			} else {
				return new Transition((State)getCurrent(), name.toString(), value.toString());
			}
		} else if (getCurrent() instanceof State && ((State)getCurrent()).isEndState()) {
			throw new IllegalArgumentException("No transitions allowed for end state [" + ((State)getCurrent()).getId() + "]!");
		} else {
			throw new IllegalArgumentException("[" + name +"] not allowed as child!");
		}
	}

	protected Object createNode(Object name, Map attributes) {
		if (getCurrent() == null) {
			throw new IllegalArgumentException("Call to [" + name + "] with attributes [" + attributes + "] is not supported!");
		} else if (getCurrent() instanceof Flow) {
			if (StringUtils.isBlank((String)name)) {
				throw new IllegalArgumentException("State id is required!");
			}
			return new State((String)name, attributes);
		} else if (getCurrent() instanceof State) {
			throw new IllegalArgumentException("Call to [" + name + "] with attributes is not allowed!");
		} else {
			throw new IllegalArgumentException("[" + name + "] not allowed a child!");
		}
	}

	protected Object createNode(Object name, Map attributes, Object value) {
		throw new IllegalArgumentException("Call to [" + name + "] with attributes and value is not allowed!");
	}

}
