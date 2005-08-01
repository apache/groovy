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

import groovy.lang.Closure;

import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.webflow.Action;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.FormAction;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 9, 2005
 */
public class StateTests extends TestCase {

	public StateTests() {
		super();
	}

	public StateTests(String arg0) {
		super(arg0);
	}

	public void testSuccessOneDiscriminatorField() {
		State state = new State("test");
		state.setViewName("test");
		state.validate();
	}
	
	public void testFailTwoDiscriminatorFields() {
		State state = new State("test");
		state.setActionClass(Object.class);
		state.setViewName("test");
		try {
			state.validate();
            fail("validate should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			// expected
		}
	}
	
	public void testFailNoDiscriminatorFields() {
		State state = new State("test");
		try {
			state.validate();
            fail("validate should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			// expected
		}
	}
	
	public void testFailWrongActionClass() {
		State state = new State("test");
		state.setActionClass(Object.class);
		try {
			state.validate();
            fail("validate should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			// expected
		}
	}
	
	public void testFailAbstractActionClass() {
		State state = new State("test");
		state.setActionClass(Action.class);
		try {
			state.validate();
            fail("validate should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			// expected
		}
	}
	
	public void testSuccessGoodActionClass1() {
		State state = new State("test");
		state.setActionClass(FormAction.class);
		state.validate();
	}
	
	public void testSuccessGoodActionClass2() {
		State state = new State("test");
		state.setActionClass(new Object() {
			public String execute(RequestContext context) {
				return null;
			}
		}.getClass());
		state.validate();
	}
	
	public void testFailWrongAttributeMapperClass() {
		State state = new State("test");
		state.setAttributeMapperClass(Object.class);
		try {
			state.validate();
            fail("validate should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			// expected
		}
	}
	
	public void testFailAbstractAttributeMapperClass() {
		State state = new State("test");
		state.setAttributeMapperClass(FlowAttributeMapper.class);
		try {
			state.validate();
            fail("validate should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			// expected
		}
	}
	
	public void testSuccessGoodAttributeMapperClass() {
		State state = new State("test");
		state.setSubFlowId("test");
		state.setAttributeMapperClass(new FlowAttributeMapper() {
			public Map createSubflowInput(RequestContext arg0) {
				return null;
			}
			public void mapSubflowOutput(RequestContext arg0) {
			}
		}.getClass());
		state.validate();
	}
	
	public void testSuccessActionAttributes1() {
		Map attributes = new HashMap();
		attributes.put("action", new FormAction());
		attributes.put("properties", new HashMap());
		State state = new State("myAction", attributes);
		state.validate();
	}
	
	public void testSuccessActionAttributes2() {
		Map attributes = new HashMap();
		attributes.put("action", FormAction.class);
		attributes.put("properties", null);
		State state = new State("myAction", attributes);
		state.validate();
	}
	
	public void testSuccessActionAttributes3() {
		Map attributes = new HashMap();
		attributes.put("action", new Closure(new Object()) {});
		attributes.put("properties", null);
		State state = new State("myAction", attributes);
		state.validate();
	}	
	
	public void testFailActionAttributes1() {
		Map attributes = new HashMap();
		attributes.put("action", new Object());
		try {
			new State("myAction", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
	
	public void testFailActionAttributes2() {
		Map attributes = new HashMap();
		attributes.put("action", FormAction.class);
		attributes.put("properties", new ArrayList());
		try {
			new State("myAction", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
	
	public void testFailProperties() {
		Map attributes = new HashMap();
		attributes.put("properties", new HashMap());
		State state = new State("myAction", attributes);
		try {
			state.validate();
            fail("validate should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			// expected
		}
	}
	
	public void testSuccessViewAttributes1() {
		Map attributes = new HashMap();
		attributes.put("view", "someView");
		attributes.put("model", new HashMap());
		State state = new  State("myViewState", attributes);
		state.validate();
	}
	
	public void testSuccessViewAttributes2() {
		Map attributes = new HashMap();
		attributes.put("view", new Closure(new Object()) {});
		attributes.put("model", null);
		State state = new State("myViewState", attributes);
		state.validate();
	}
	
	public void testFailViewAttributes1() {
		Map attributes = new HashMap();
		attributes.put("view", new Object());
		try {
			new State("myViewState", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
	
	public void testFailViewAttributes2() {
		Map attributes = new HashMap();
		attributes.put("view", "someView");
		attributes.put("model", new ArrayList());
		try {
			new State("myViewState", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
	
	public void testSuccessDecisionAttributes1() {
		Map attributes = new HashMap();
		attributes.put("decision", "expression");
		attributes.put("trueState", "someState");
		attributes.put("falseState", "someOtherState");
		State state = new State("myDecisionState", attributes);
		state.validate();
	}

	public void testSuccessDecisionAttributes2() {
		Map attributes = new HashMap();
		attributes.put("decision", new Closure(new Object()) {});
		attributes.put("trueState", "someState");
		attributes.put("falseState", "someOtherState");
		State state = new State("myDecisionState", attributes);
		state.validate();
	}
	
	public void testFailDecisionAttributes1() {
		Map attributes = new HashMap();
		attributes.put("decision", "expression");
		attributes.put("falseState", "someOtherState");
		State state = new State("myDecisionState", attributes);
		try {
			state.validate();
            fail("validate should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
			// expected
		}
	}

	public void testFailDecisionAttributes2() {
		Map attributes = new HashMap();
		attributes.put("decision", "expression");
		attributes.put("trueState", "someState");
		State state = new State("myDecisionState", attributes);
		try {
			state.validate();
            fail("validate should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testFailDecisionAttributes3() {
		Map attributes = new HashMap();
		attributes.put("decision", new Object());
		try {
			new State("myDecisionState", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}
	
	public void testFailDecisionAttributes4() {
		Map attributes = new HashMap();
		attributes.put("decision", "expression");
		attributes.put("trueState", new Object());
		try {
			new State("myDecisionState", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testFailDecisionAttributes5() {
		Map attributes = new HashMap();
		attributes.put("decision", "expression");
		attributes.put("falseState", new Object());
		try {
			new State("myDecisionState", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testSuccessSubflowAttributes1() {
		Map attributes = new HashMap();
		attributes.put("subflow", "someFlow");
		State state = new State("mySubflowState", attributes);
		state.validate();
	}
	
	public void testSuccessSubflowAttributes2() {
		Map attributes = new HashMap();
		attributes.put("subflow", "someFlow");
		attributes.put("input", new Closure(new Object()) {});
		State state = new State("mySubflowState", attributes);
		state.validate();
	}

	public void testSuccessSubflowAttributes3() {
		Map attributes = new HashMap();
		attributes.put("subflow", "someFlow");
		attributes.put("output", new Closure(new Object()) {});
		State state = new State("mySubflowState", attributes);
		state.validate();
	}

	public void testSuccessSubflowAttributes4() {
		Map attributes = new HashMap();
		attributes.put("subflow", "someFlow");
		attributes.put("mapper", new FlowAttributeMapper() {
			public Map createSubflowInput(RequestContext arg0) {
				return null;
			}
			public void mapSubflowOutput(RequestContext arg0) {
			}
		}.getClass());
		attributes.put("properties", new HashMap());
		State state = new State("mySubflowState", attributes);
		state.validate();
	}

	public void testSuccessSubflowAttributes5() {
		Map attributes = new HashMap();
		attributes.put("subflow", "someFlow");
		attributes.put("mapper", new FlowAttributeMapper() {
			public Map createSubflowInput(RequestContext arg0) {
				return null;
			}
			public void mapSubflowOutput(RequestContext arg0) {
			}
		});
		attributes.put("properties", null);
		State state = new State("mySubflowState", attributes);
		state.validate();
	}

	public void testFailSubflowAttributes1() {
		Map attributes = new HashMap();
		attributes.put("subflow", new Object());
		try {
			new State("mySubflowState", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			//expected
		}
	}
	
	public void testFailSubflowAttributes2() {
		Map attributes = new HashMap();
		attributes.put("subflow", "expression");
		attributes.put("input", new Object());
		try {
			new State("mySubflowState", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			//expected
		}
	}

	public void testFailSubflowAttributes3() {
		Map attributes = new HashMap();
		attributes.put("subflow", "expression");
		attributes.put("output", new Object());
		try {
			new State("mySubflowState", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			//expected
		}
	}

	public void testFailSubflowAttributes4() {
		Map attributes = new HashMap();
		attributes.put("subflow", "expression");
		attributes.put("mapper", new Object());
		try {
			new State("mySubflowState", attributes);
            fail("State ctor should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			//expected
		}
	}

	public void testFailSubflowAttributes5() {
		Map attributes = new HashMap();
		attributes.put("subflow", "expression");
		attributes.put("mapper", Object.class);
		State state = new State("mySubflowState", attributes);
		try {
			state.validate();
            fail("State validate should have thrown IllegalStateException");
		} catch (IllegalStateException e) {
			//expected
		}
	}

}
