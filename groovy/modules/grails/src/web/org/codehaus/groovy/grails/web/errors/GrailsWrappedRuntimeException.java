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
package org.codehaus.groovy.grails.web.errors;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;

import groovy.lang.GroovyRuntimeException;
/**
 *  An exception that wraps a GroovyRuntimeException and attempts to extract more relevent diagnostic messages from the stack trace
 * 
 * @author Graeme Rocher
 * @since 22 Dec, 2005
 */
public class GrailsWrappedRuntimeException extends GroovyRuntimeException {

	private static final Log LOG  = LogFactory.getLog(GrailsWrappedRuntimeException.class);
	private GroovyRuntimeException gre;
	private String className;
	private int lineNumber = - 1;
	private String stackTrace;
	private String[] codeSnippet = new String[0];
	
	/**
	 * @param gre
	 */
	public GrailsWrappedRuntimeException(GroovyRuntimeException gre) {
		super(gre.getMessage(), gre);
		this.gre = gre;
		
		if(gre.getModule() == null) {
			StringWriter sw  = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			gre.printStackTrace(pw);
			this.stackTrace = sw.toString();
			Pattern extractDetails = Pattern.compile("\\((\\w+)\\.groovy:(\\d+)\\)");
			Matcher matcher = extractDetails.matcher(stackTrace);			
			if(matcher.find()) {
				this.className = matcher.group(1);
				LineNumberReader reader = null;
				try {
					this.lineNumber = Integer.parseInt(matcher.group(2));
					if(getLineNumber() > -1) {
						String fileName = this.className.replace('.', '/') + ".groovy";
						InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
						if(in != null) {
							reader = new LineNumberReader(new InputStreamReader( in ));
							String currentLine = reader.readLine();
							StringBuffer buf = new StringBuffer();
							while(currentLine != null) {

								int currentLineNumber = reader.getLineNumber();
								if(currentLineNumber == this.lineNumber) {
									buf.append(currentLineNumber);
									buf.append(": ");
									buf.append(currentLine);
									buf.append("\n");
								}	
								else if(currentLineNumber == this.lineNumber + 1) {
									buf.append(currentLineNumber);
									buf.append(": ");
									buf.append(currentLine);
									break;
								}								
								currentLine = reader.readLine();
							}
							this.codeSnippet = buf.toString().split("\n");
						}
					}					
				}
				catch(NumberFormatException nfex) {
					// ignore
				} 
				catch (IOException e) {
					LOG.warn("[GrailsWrappedRuntimeException] I/O error reading line diagnostics: " + e.getMessage(), e);
				}
				finally {
					if(reader != null)
						try {
							reader.close();
						} catch (IOException e) {
							// ignore
						}
				}

			}
		}
	}

	/**
	 * @return Returns the line.
	 */
	public String[] getCodeSnippet() {
		return this.codeSnippet;
	}

	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		if(getModule() != null) {
			return getModule().getDescription();
		}
		return className;
	}

	/**
	 * @return Returns the lineNumber.
	 */
	public int getLineNumber() {
		if(getNode() != null) {
			return getNode().getLineNumber();
		}
		return lineNumber;
	}

	/**
	 * @return Returns the stackTrace.
	 */
	public String getStackTraceText() {
		return stackTrace;
	}

	/* (non-Javadoc)
	 * @see groovy.lang.GroovyRuntimeException#getMessage()
	 */
	public String getMessage() {
		return gre.getMessage();
	}

	/* (non-Javadoc)
	 * @see groovy.lang.GroovyRuntimeException#getMessageWithoutLocationText()
	 */
	public String getMessageWithoutLocationText() {
		return gre.getMessageWithoutLocationText();
	}

	/* (non-Javadoc)
	 * @see groovy.lang.GroovyRuntimeException#getModule()
	 */
	public ModuleNode getModule() {
		return gre.getModule();
	}

	/* (non-Javadoc)
	 * @see groovy.lang.GroovyRuntimeException#getNode()
	 */
	public ASTNode getNode() {
		return gre.getNode();
	}

	/* (non-Javadoc)
	 * @see groovy.lang.GroovyRuntimeException#setModule(org.codehaus.groovy.ast.ModuleNode)
	 */
	public void setModule(ModuleNode module) {
		gre.setModule(module);
	}

}
