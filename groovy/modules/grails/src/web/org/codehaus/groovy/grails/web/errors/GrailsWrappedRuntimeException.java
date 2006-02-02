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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.exceptions.GrailsException;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  An exception that wraps a Grails RuntimeException and attempts to extract more relevent diagnostic messages from the stack trace
 * 
 * @author Graeme Rocher
 * @since 22 Dec, 2005
 */
public class GrailsWrappedRuntimeException extends GrailsException {

    private static final Pattern PARSE_DETAILS_STEP1 = Pattern.compile("\\((\\w+)\\.groovy:(\\d+)\\)");
    private static final Pattern PARSE_DETAILS_STEP2 = Pattern.compile("at\\s{1}(\\w+)\\$_closure\\d+\\.doCall\\(\\1:(\\d+)\\)");
    public static final String URL_PREFIX = "/WEB-INF/grails-app/";
    private static final Log LOG  = LogFactory.getLog(GrailsWrappedRuntimeException.class);
    private Throwable t;
    private String className = "Unknown";
    private int lineNumber = - 1;
    private String stackTrace;
    private String[] codeSnippet = new String[0];


    /**
     * @param servletContext
     * @param t
     */
    public GrailsWrappedRuntimeException(ServletContext servletContext, Throwable t) {
        super(t.getMessage(), t);
        this.t = t;

        StringWriter sw  = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        this.t.printStackTrace(pw);
        this.stackTrace = sw.toString();

        Matcher m1 = PARSE_DETAILS_STEP1.matcher(stackTrace);
        Matcher m2 = PARSE_DETAILS_STEP2.matcher(stackTrace);
        try {
            if(m1.find()) {
                this.className = m1.group(1);
                this.lineNumber = Integer.parseInt(m1.group(2));
            }
            else if(m2.find()) {
                this.className = m2.group(1);
                this.lineNumber = Integer.parseInt(m2.group(2));
            }
        }
        catch(NumberFormatException nfex) {
            // ignore
        }

        LineNumberReader reader = null;
        try {
            if(getLineNumber() > -1) {
                String fileName = this.className.replace('.', '/') + ".groovy";
                String urlPrefix = URL_PREFIX;
                if(GrailsClassUtils.isControllerClass(className) || GrailsClassUtils.isPageFlowClass(className)) {
                    urlPrefix += "/controllers/";
                }
                else if(GrailsClassUtils.isTagLibClass(className)) {
                    urlPrefix += "/taglib/";
                }
                else if(GrailsClassUtils.isService(className)) {
                   urlPrefix += "/services/";
                }
                InputStream in = servletContext.getResourceAsStream(urlPrefix + fileName);
                if(in != null) {
                    reader = new LineNumberReader(new InputStreamReader( in ));
                    String currentLine = reader.readLine();
                    StringBuffer buf = new StringBuffer();
                    while(currentLine != null) {

                        int currentLineNumber = reader.getLineNumber();
                        if(currentLineNumber == this.lineNumber) {
                            buf.append(currentLineNumber)
                               .append(": ")
                               .append(currentLine)
                               .append("\n");
                        }
                        else if(currentLineNumber == this.lineNumber + 1) {
                            buf.append(currentLineNumber)
                               .append(": ")
                               .append(currentLine);
                            break;
                        }
                        currentLine = reader.readLine();
                    }
                    this.codeSnippet = buf.toString().split("\n");
                }
            }
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
        return className;
    }

    /**
     * @return Returns the lineNumber.
     */
    public int getLineNumber() {
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
        return t.getMessage();
    }


}
