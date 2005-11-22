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
package org.codehaus.groovy.grails.orm.hibernate.exceptions;

import org.codehaus.groovy.grails.exceptions.GrailsException;

/**
 * <p>Base exception class for errors related to Domain class queries in Grails 
 * 
 * @author Graeme Rocher
 * @since Nov 22, 2005
 */
public class GrailsQueryException extends GrailsException {

	public GrailsQueryException() {
		super();
	}

	public GrailsQueryException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public GrailsQueryException(String arg0) {
		super(arg0);
	}

	public GrailsQueryException(Throwable arg0) {
		super(arg0);
	}

}
