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

import java.lang.String;

import org.springframework.core.ToStringCreator;

/**
 * <p>Simple container class for a transition a in page flow.
 * 
 * @author Steven Devijver
 * @since Jul 9, 2005
 */
public class Transition {

	private State state = null;
	private String targetStateId = null;
	private String name = null;
	
	public Transition(State state, String name, String targetStateId) {
		super();
		if (state == null) {
			throw new IllegalArgumentException("State argument should not be null!");
		}
		if (name == null) {
			throw new IllegalArgumentException("Name argument should not be null!");
		}
 		if (targetStateId == null) {
			throw new IllegalArgumentException("Target state id argument should not be null!");
		}
		this.state = state;
		this.targetStateId = targetStateId;
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getTargetStateId() {
		return this.targetStateId;
	}

	public String toString() {
		return new ToStringCreator(this).toString();
	}

}
