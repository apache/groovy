package org.codehaus.groovy.grails.web.errors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;

import groovy.lang.GroovyRuntimeException;

public class GrailsWrappedRuntimeException extends GroovyRuntimeException {

	private GroovyRuntimeException gre;
	private String className;
	private int lineNumber = - 1;
	private String stackTrace;

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
				try {
					this.lineNumber = Integer.parseInt(matcher.group(2));
				}
				catch(NumberFormatException nfex) {
					// ignore
				}
			}
		}
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
