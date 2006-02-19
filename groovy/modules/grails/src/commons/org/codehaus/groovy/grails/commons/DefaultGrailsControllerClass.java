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
package org.codehaus.groovy.grails.commons;

import groovy.lang.Closure;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.codehaus.groovy.grails.scaffolding.DefaultGrailsScaffolder;
import org.codehaus.groovy.grails.scaffolding.GrailsScaffolder;

import java.beans.PropertyDescriptor;
import java.util.*;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class DefaultGrailsControllerClass extends AbstractInjectableGrailsClass
		implements GrailsControllerClass {

    public static final String CONTROLLER = "Controller";

    private static final String SLASH = "/";
    private static final String VIEW = "View";
    private static final String TYPED_VIEWS = "TypedViews";
    private static final String DEFAULT_CLOSURE_PROPERTY = "defaultAction";
	private static final String SCAFFOLDING_PROPERTY = "scaffold";

	

	private Map uri2viewMap = null;
	private Map uri2closureMap = null;
	private Map viewNames = null;
	private String[] uris = null;
    private String uri;

    private boolean scaffolding;
    private Class scaffoldedClass;


    public DefaultGrailsControllerClass(Class clazz) {
        super(clazz, CONTROLLER);
        this.uri = SLASH + (StringUtils.isNotBlank(getPackageName()) ? getPackageName().replace('.', '/')  + SLASH : "" ) + WordUtils.uncapitalize(getName());
        String defaultActionName = (String)getPropertyValue(DEFAULT_CLOSURE_PROPERTY, String.class);
        if(defaultActionName == null) {
            defaultActionName = INDEX_ACTION;
        }
        Boolean tmp = (Boolean)getPropertyValue(SCAFFOLDING_PROPERTY, Boolean.class);
        if(tmp != null) {
            this.scaffolding = tmp.booleanValue();
        }
        this.scaffoldedClass = (Class)getPropertyValue(SCAFFOLDING_PROPERTY, Class.class);
        if(this.scaffoldedClass != null) {
            this.scaffolding = true;
        }

        Collection closureNames = new ArrayList();
        this.uri2viewMap = new HashMap();
        this.uri2closureMap = new HashMap();
        this.viewNames = new HashMap();

        if(this.scaffolding) {
            for(int i = 0; i < DefaultGrailsScaffolder.ACTION_NAMES.length;i++) {
                closureNames.add(DefaultGrailsScaffolder.ACTION_NAMES[i]);
                String viewName = (String)getPropertyValue(DefaultGrailsScaffolder.ACTION_NAMES[i] + VIEW, String.class);
                // if no explicity view name is specified just use action name
                if(viewName == null) {
                    viewName =DefaultGrailsScaffolder.ACTION_NAMES[i];
                }
                String tmpUri = uri + SLASH + DefaultGrailsScaffolder.ACTION_NAMES[i];
                String viewUri = uri + SLASH + viewName;
                if (StringUtils.isNotBlank(viewName)) {
                    this.uri2viewMap.put(tmpUri, viewUri);
                    this.viewNames.put( DefaultGrailsScaffolder.ACTION_NAMES[i], viewUri );
                }
                this.uri2closureMap.put(tmpUri, DefaultGrailsScaffolder.ACTION_NAMES[i]);
                this.uri2closureMap.put(tmpUri + "/**", DefaultGrailsScaffolder.ACTION_NAMES[i]);
            }
        }

        PropertyDescriptor[] propertyDescriptors = getReference().getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
            Closure closure = (Closure)getPropertyValue(propertyDescriptor.getName(), Closure.class);
            if (closure != null) {
                closureNames.add(propertyDescriptor.getName());
                String closureName = propertyDescriptor.getName();
                String viewName = (String)getPropertyValue(propertyDescriptor.getName() + VIEW, String.class);
                // if no explicity view name is specified just use property name
                if(viewName == null) {
                    viewName = propertyDescriptor.getName();
                }
                Map typedViews = (Map)getPropertyValue(propertyDescriptor.getName() + TYPED_VIEWS, Map.class);
                String tmpUri = uri + SLASH + propertyDescriptor.getName();
                String viewUri = uri + SLASH + viewName;
                if (StringUtils.isNotBlank(viewName)) {
                    this.uri2viewMap.put(tmpUri, viewUri);
                    this.viewNames.put( closureName, viewUri );
                }
                closure = (Closure)getPropertyValue(propertyDescriptor.getName(), Closure.class);
                if (closure != null) {
                    this.uri2closureMap.put(tmpUri, propertyDescriptor.getName());
                    this.uri2closureMap.put(tmpUri + "/**", propertyDescriptor.getName());
                    // TODO: This code is likely broken and needs re-thinking as there may be a better way to
                    // handle typed views
                    if (typedViews != null) {
                        for (Iterator iter = typedViews.keySet().iterator(); iter.hasNext();) {
                            String viewType = (String)iter.next();
                            String typedViewName = (String)typedViews.get(viewType);
                            String typedUri = tmpUri + SLASH + viewType;
                            this.uri2viewMap.put(typedUri, typedViewName);
                            this.uri2closureMap.put(typedUri, propertyDescriptor.getName());
                            if (defaultActionName != null && defaultActionName.equals(propertyDescriptor.getName())) {
                                this.uri2closureMap.put(uri + SLASH + viewType, propertyDescriptor.getName());
                                this.uri2viewMap.put(uri + SLASH + viewType, typedViewName);
                            }
                        }
                    }
                }
            }
        }

        if (defaultActionName != null) {
            this.uri2closureMap.put(uri, defaultActionName);
            this.uri2closureMap.put(uri + SLASH, defaultActionName);
            this.uri2viewMap.put(uri + SLASH, uri + SLASH + defaultActionName);
            this.uri2viewMap.put(uri,uri + SLASH +  defaultActionName);
            this.viewNames.put( defaultActionName, uri + SLASH + defaultActionName );
        }

        if (closureNames.size() == 1) {
            String closureName = ((String)closureNames.iterator().next());
            this.uri2closureMap.put(uri, this.uri2closureMap.values().iterator().next());
            if (!this.uri2viewMap.isEmpty()) {
                this.uri2viewMap.put(uri, this.uri2viewMap.values().iterator().next());
            }
            Map typedViews = (Map)getPropertyValue(closureName + TYPED_VIEWS, Map.class);
            if (typedViews != null) {
                for (Iterator iter = typedViews.keySet().iterator(); iter.hasNext();) {
                    String viewType = (String)iter.next();
                    String typedViewName = (String)typedViews.get(viewType);
                    this.uri2closureMap.put(uri + SLASH + viewType, this.uri2closureMap.values().iterator().next());
                    this.uri2viewMap.put(uri + SLASH + viewType, typedViewName);
                }
            }
        }
        this.uris  = (String[])this.uri2closureMap.keySet().toArray(new String[this.uri2closureMap.keySet().size()]);
    }

	public String[] getURIs() {
		return this.uris;
	}

	public boolean mapsToURI(String uri) {
		for (int i = 0; i < uris.length; i++) {
			if (uris[i].equals(uri)) {
				return true;
			}
		}
		return false;
	}
	
	public String getViewByURI(String uri) {
		return (String)this.uri2viewMap.get(uri);
	}
	
	public String getClosurePropertyName(String uri) {
		return (String)this.uri2closureMap.get(uri);
	}

	public String getViewByName(String viewName) {
        if(this.viewNames.containsKey(viewName)) {
            return (String)this.viewNames.get(viewName);
        }
        else {
             return this.uri + SLASH + viewName;
        }
    }

	public boolean isScaffolding() {
		return this.scaffolding;
	}

    public Class getScaffoldedClass() {
        return this.scaffoldedClass;
    }

    /**
     * @param scaffolding The scaffolding to set.
     */
    public void setScaffolding(boolean scaffolding) {
        this.scaffolding = scaffolding;
    }

}
