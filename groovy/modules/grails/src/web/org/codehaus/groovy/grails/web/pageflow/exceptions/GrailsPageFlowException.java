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
package org.codehaus.groovy.grails.web.pageflow.exceptions;

import java.lang.String;
import java.lang.Throwable;

import org.codehaus.groovy.grails.exceptions.GrailsException;

/**
 * <p>Abstract base class for exceptions related to page flows.
 * 
 * @author Steven Devijver
 * @since Jul 10, 2005
 */
public abstract class GrailsPageFlowException extends GrailsException {

	public GrailsPageFlowException() {
		super();
	}

	public GrailsPageFlowException(String arg0) {
		super(arg0);
	}

	public GrailsPageFlowException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public GrailsPageFlowException(Throwable arg0) {
		super(arg0);
	}

}
