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
package org.codehaus.groovy.grails.web.taglib.jsp;

/**
 * A JSP facade that delegates to the Grails taglib link tag
 *
 * @author Graeme Rocher
 * @since Jan 3, 2006
 */
public class JspLinkTag extends JspInvokeGrailsTagLibTag {
    private static final String TAG_NAME = "link";

    private String controller;
    private String action;

    public JspLinkTag() {
        super.setName(TAG_NAME);
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
