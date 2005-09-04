/*
 * Copyright 2004, 2005 John G. Wilson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.co.wilson.net.xmlrpc;

import org.xml.sax.SAXException;

public class XMLRPCFailException extends SAXException {
    private static final long serialVersionUID = 1L;
    
    private final String faultString;
	private final int faultCode;
	
	public XMLRPCFailException(final String faultString, final int faultCode) {
		super(faultString);
		
		this.faultString = faultString;
		this.faultCode = faultCode;
	}

	public int getFaultCode() {
		return faultCode;
	}
	
	public String getFaultString() {
		return faultString;
	}
}
