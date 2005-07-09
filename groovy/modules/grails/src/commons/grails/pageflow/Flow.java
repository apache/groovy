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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;

/**
 * <p>Simple container class for a page flow.
 * 
 * @author Steven Devijver
 * @since Jul 9, 2005
 */
public class Flow {

	private List states = null;
	
	public Flow() {
		super();
		this.states = new ArrayList();
	}

	public void addState(State state) {
		Assert.notNull(state);
		for (Iterator iter = this.states.iterator(); iter.hasNext();) {
			State tmpState = (State)iter.next();
			if (tmpState.getId().equals(state.getId())) {
				throw new IllegalArgumentException("Flow already has a state with id [" + tmpState.getId() + "]!");
			}
		}
		this.states.add(state);
	}
	
	public List getStates() {
		return this.states;
	}
	
	public String toString() {
		return new ToStringCreator(this).append(this.states).toString();
	}
}
