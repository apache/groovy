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
package org.codehaus.groovy.grails.web.sitemesh;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.module.sitemesh.Config;
import com.opensymphony.module.sitemesh.Decorator;
import com.opensymphony.module.sitemesh.DecoratorMapper;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.module.sitemesh.mapper.AbstractDecoratorMapper;
import com.opensymphony.module.sitemesh.mapper.DefaultDecorator;
/**
 * Implements the SiteMesh decorator mapper interface and allows grails views to map to grails layouts
 *  
 * @author Graeme Rocher
 * @since Oct 10, 2005
 */
public class GrailsLayoutDecoratorMapper extends AbstractDecoratorMapper implements DecoratorMapper {

	private static final String DEFAULT_DECORATOR_PATH = "/WEB-INF/grails-app/views/layouts";
	private static final String DEFAULT_VIEW_TYPE = ".jsp";
	
	private static final Log LOG = LogFactory.getLog( GrailsLayoutDecoratorMapper.class );
	
	
	private Map decoratorMap = new HashMap();
	
	public void init(Config config, Properties properties, DecoratorMapper parent) throws InstantiationException {
		super.init(config,properties,parent);
	}

	public Decorator getDecorator(HttpServletRequest request, Page page) {
		String layoutName = page.getProperty("meta.layout");
		if(StringUtils.isBlank(layoutName))
			return super.getDecorator(request, page);
		
		return getNamedDecorator(request, layoutName);
	}

	public Decorator getNamedDecorator(HttpServletRequest request, String name) {
		
		if(decoratorMap.containsKey(name)) {
			return (Decorator)decoratorMap.get(name);
		}
		else {
			String decoratorName = name;
			if(!name.matches("(.+)(\\.)(\\w{2}|\\w{3})")) {
				name += DEFAULT_VIEW_TYPE;
			}
			String decoratorPage = DEFAULT_DECORATOR_PATH + '/' + name;
			
			if(LOG.isInfoEnabled()) 
				LOG.info("Using decorator " + decoratorPage);
			
			Decorator d = new DefaultDecorator(decoratorName,decoratorPage, Collections.EMPTY_MAP);
			decoratorMap.put(decoratorName,d);
			return d;
		}	
	}

}
