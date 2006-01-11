/* Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.web.servlet.view;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.ServletContext;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;

/**
 * A Grails view resolver which evaluates the existance of a view for different extensions choosing which
 * one to delegate to.
 *
 * @author Graeme Rocher
 * @since 11-Jan-2006
 */
public class GrailsViewResolver extends InternalResourceViewResolver {
    private String localSuffix;
    private String localPrefix;
    private static final String GSP_SUFFIX = ".gsp";
    private Map resolvedCache = new HashMap();

    public void setPrefix(String prefix) {
        super.setPrefix(prefix);
        this.localPrefix = prefix;
    }

    public void setSuffix(String suffix) {
        super.setSuffix(suffix);
        this.localSuffix = suffix;
    }

    protected View loadView(String viewName, Locale locale) throws Exception {
        AbstractUrlBasedView view = buildView(viewName);

        ServletContext context = getServletContext();
        if(this.resolvedCache.containsKey(viewName)) {
            view.setUrl((String)this.resolvedCache.get(viewName));
        }
        else {
            URL res = context.getResource(view.getUrl());
            // try GSP if res is null
            if(res == null) {
                String gspView = localPrefix + viewName + GSP_SUFFIX;
                res = context.getResource(gspView);
                if(res != null) {
                    view.setUrl(gspView);
                    this.resolvedCache.put(viewName,gspView);
                }
            }
            else {
                this.resolvedCache.put(viewName,view.getUrl());
            }
        }

        view.setApplicationContext(getApplicationContext());
        view.afterPropertiesSet();
        return view;
    }

    private String localUrlForView(String viewName) {
        return null;
    }
}
