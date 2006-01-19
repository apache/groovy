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
package org.codehaus.groovy.grails.web.servlet.mvc;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.ProxyMetaClass;
import groovy.util.Proxy;
import org.apache.commons.collections.BeanMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.commons.GrailsTagLibClass;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import org.codehaus.groovy.grails.scaffolding.GrailsScaffolder;
import org.codehaus.groovy.grails.web.metaclass.ChainDynamicMethod;
import org.codehaus.groovy.grails.web.metaclass.ControllerDynamicMethods;
import org.codehaus.groovy.grails.web.metaclass.GetParamsDynamicProperty;
import org.codehaus.groovy.grails.web.metaclass.TagLibDynamicMethods;
import org.codehaus.groovy.grails.web.servlet.DefaultGrailsRequestAttributes;
import org.codehaus.groovy.grails.web.servlet.GrailsRequestAttributes;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.ControllerExecutionException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.NoClosurePropertyForURIException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.NoViewNameDefinedException;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.UnknownControllerException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleGrailsControllerHelper implements GrailsControllerHelper {

    private static final String SCAFFOLDER = "Scaffolder";

    private GrailsApplication application;
    private ApplicationContext applicationContext;
    private Map chainModel = Collections.EMPTY_MAP;
    private ControllerDynamicMethods interceptor;
    private GrailsScaffolder scaffolder;
    private ServletContext servletContext;
    private GrailsRequestAttributes grailsAttributes;
    private Pattern uriPattern = Pattern.compile("/(\\w+)/?(\\w*)/?(\\w*)/?(.*)");
    private static final Log LOG = LogFactory.getLog(SimpleGrailsControllerHelper.class);
    private TagLibDynamicMethods tagLibInterceptor;

    public SimpleGrailsControllerHelper(GrailsApplication application, ApplicationContext context, ServletContext servletContext) {
        super();
        this.application = application;
        this.applicationContext = context;
        this.servletContext = servletContext;
        this.grailsAttributes = new DefaultGrailsRequestAttributes(this.servletContext);
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    /* (non-Javadoc)
      * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#getControllerClassByName(java.lang.String)
      */
    public GrailsControllerClass getControllerClassByName(String name) {
        return this.application.getController(name);
    }

    public GrailsScaffolder getScaffolderForController(String controllerName) {
        GrailsControllerClass controllerClass = getControllerClassByName(controllerName);
        return (GrailsScaffolder)applicationContext.getBean( controllerClass.getFullName() + SCAFFOLDER );
    }

    /* (non-Javadoc)
      * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#getControllerClassByURI(java.lang.String)
      */
    public GrailsControllerClass getControllerClassByURI(String uri) {
        return this.application.getControllerByURI(uri);
    }

    /* (non-Javadoc)
      * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#getControllerInstance(org.codehaus.groovy.grails.commons.GrailsControllerClass)
      */
    public GroovyObject getControllerInstance(GrailsControllerClass controllerClass) {
        return (GroovyObject)this.applicationContext.getBean(controllerClass.getFullName());
    }

    /* (non-Javadoc)
      * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#handleURI(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
    public ModelAndView handleURI(String uri, HttpServletRequest request, HttpServletResponse response) {
        return handleURI(uri,request,response,Collections.EMPTY_MAP);
    }



    /**
     * If in Proxy's are used in the Groovy context, unproxy (is that a word?) them by setting
     * the adaptee as the value in the map so that they can be used in non-groovy view technologies
     *
     * @param model The model as a map
     */
    private void removeProxiesFromModelObjects(Map model) {

        for (Iterator keyIter = model.keySet().iterator(); keyIter.hasNext();) {
            Object current = keyIter.next();
            Object modelObject = model.get(current);
            if(modelObject instanceof Proxy) {
                model.put( current, ((Proxy)modelObject).getAdaptee() );
            }
        }
    }

    /* (non-Javadoc)
      * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#handleURI(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Map)
      */
    public ModelAndView handleURI(String uri, HttpServletRequest request, HttpServletResponse response, Map params) {
        if(uri == null)
            throw new IllegalArgumentException("Controller URI [" + uri + "] cannot be null!");

        // step 1: process the uri
        if (uri.indexOf("?") > -1) {
            uri = uri.substring(0, uri.indexOf("?"));
        }
        if(uri.indexOf('\\') > -1) {
            uri = uri.replaceAll("\\\\", "/");
        }
        if(!uri.startsWith("/"))
            uri = '/' + uri;
        if(uri.endsWith("/"))
            uri = uri.substring(0,uri.length() - 1);

        String id = null;
        Map extraParams = Collections.EMPTY_MAP;
        Matcher m = uriPattern.matcher(uri);
        if(m.find()) {
            uri = '/' + m.group(1) + '/' + m.group(2);
            id = m.group(3);
            String extraParamsString = m.group(4);
            if(extraParamsString != null && extraParamsString.indexOf('/') > - 1) {
                String[] tokens = extraParamsString.split("/");
                extraParams = new HashMap();
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i];
                    if(i == 0 || ((i % 2) == 0)) {
                        if((i + 1) < tokens.length) {
                            extraParams.put(token, tokens[i + 1]);
                        }
                    }
                    else {
                        continue;
                    }
                }

            }
        }

        // Step 2: lookup the controller in the application.
        GrailsControllerClass controllerClass = getControllerClassByURI(uri);

        // parse the uri in its individual tokens
        String controllerName = WordUtils.uncapitalize(controllerClass.getName());;



        GrailsTagLibClass tagLibClass = this.application.getTagLibClassForController(controllerClass.getFullName());

        if (controllerClass == null) {
            throw new UnknownControllerException("No controller found for URI [" + uri + "]!");
        }


        // Step 3: load controller from application context.
        GroovyObject controller = getControllerInstance(controllerClass);

        // create the tag lib for the controller if there is one
        GroovyObject tagLib = null;
        if(tagLibClass != null) {
            tagLib = (GroovyObject)this.applicationContext.getBean(tagLibClass.getFullName());
        }
        request.setAttribute( GrailsRequestAttributes.TAG_LIB, tagLib);
        request.setAttribute( GrailsRequestAttributes.CONTROLLER, controller );

        // Step 3a: Configure a proxy interceptor for controller dynamic methods for this request
        if(this.interceptor == null) {
            try {
                interceptor = new ControllerDynamicMethods(controller,this,request,response);
            }
            catch(IntrospectionException ie) {
                throw new ControllerExecutionException("Error creating dynamic controller methods for controller ["+controller.getClass()+"]: " + ie.getMessage(), ie);
            }
        }
        if(this.tagLibInterceptor == null && tagLib != null) {
            try {
                tagLibInterceptor =  new TagLibDynamicMethods(tagLib,controller,request,response);
            }
            catch(IntrospectionException ie) {
                throw new ControllerExecutionException("Error creating dynamic controller methods for controller ["+controller.getClass()+"]: " + ie.getMessage(), ie);
            }                                      
        }
        // Step 3b: if scaffolding retrieve scaffolder
        if(controllerClass.isScaffolding())  {
            this.scaffolder = (GrailsScaffolder)applicationContext.getBean( controllerClass.getFullName() + SCAFFOLDER );
            if(this.scaffolder == null)
                throw new IllegalStateException("Scaffolding set to true for controller ["+controllerClass.getFullName()+"] but no scaffolder available!");
        }

        // Step 4: get closure property name for URI.
        String actionPropertyName = controllerClass.getClosurePropertyName(uri);
        if (actionPropertyName == null) {
            // Step 4a: Check if scaffolding
            if( controllerClass.isScaffolding() && !scaffolder.supportsAction(actionPropertyName))
                throw new NoClosurePropertyForURIException("Could not find closure property for URI [" + uri + "] for controller [" + controllerClass.getFullName() + "]!");
        }

        // Step 4a: Set dynamic properties on controller
        if(LOG.isDebugEnabled()) {
            LOG.debug("Processing request for controller ["+controllerName+"], action ["+actionPropertyName+"], and id ["+id+"]");
        }
        if(LOG.isTraceEnabled()) {
            LOG.trace("Extra params from uri ["+extraParams+"] ");
        }
        controller.setProperty(ControllerDynamicMethods.CONTROLLER_URI_PROPERTY, '/' + controllerName);
        controller.setProperty(ControllerDynamicMethods.ACTION_URI_PROPERTY, '/' + controllerName + '/' + actionPropertyName);
        Map controllerParams = (Map)controller.getProperty(GetParamsDynamicProperty.PROPERTY_NAME);
        if(!StringUtils.isBlank(id)) {
            controllerParams.put(GrailsRequestAttributes.ID_PARAM, id);
        }
        if(!extraParams.isEmpty()) {
            for (Iterator i = extraParams.keySet().iterator(); i.hasNext();) {
                String name = (String) i.next();
                controllerParams.put(name,extraParams.get(name));
            }
        }

        // Step 5: get the view name for this URI.
        String viewName = controllerClass.getViewByURI(uri);

        // Step 6: get closure from closure property
        Closure action = (Closure)controller.getProperty(actionPropertyName);

        if(action == null)
            throw new IllegalStateException("Scaffolder supports action ["+actionPropertyName+"] for controller ["+controllerClass.getFullName()+"] but getAction returned null!");

        // Step 7: process the action
        Object returnValue = handleAction( controller,action,request,response,params );


        // Step 8: determine return value type and handle accordingly
        return handleActionResponse(controller,returnValue,actionPropertyName,viewName);
    }

    public void setChainModel(Map model) {
        this.chainModel  = model;
    }

    public GrailsRequestAttributes getGrailsAttributes() {
        return this.grailsAttributes;
    }

    public Object handleAction(GroovyObject controller,Closure action, HttpServletRequest request, HttpServletResponse response) {
        return handleAction(controller,action,request,response,Collections.EMPTY_MAP);
    }

    public Object handleAction(GroovyObject controller,Closure action, HttpServletRequest request, HttpServletResponse response, Map params) {
            if(interceptor == null) {
                ProxyMetaClass pmc = (ProxyMetaClass)controller.getMetaClass();
                interceptor = (ControllerDynamicMethods)pmc.getInterceptor();
            }
            // if there are additional params add them to the params dynamic property
            if(params != null && !params.isEmpty()) {
                GetParamsDynamicProperty paramsProp = (GetParamsDynamicProperty)interceptor.getDynamicProperty( GetParamsDynamicProperty.PROPERTY_NAME );
                paramsProp.addParams( params );
            }
            // check the chain model is not empty and add it
            if(!this.chainModel.isEmpty()) {
                // get the "chainModel" property
                GenericDynamicProperty chainProperty = (GenericDynamicProperty)interceptor.getDynamicProperty(ChainDynamicMethod.PROPERTY_CHAIN_MODEL);
                // if it doesn't exist create it
                if(chainProperty == null) {
                    interceptor.addDynamicProperty( new GenericDynamicProperty( ChainDynamicMethod.PROPERTY_CHAIN_MODEL,Map.class,this.chainModel,false ) );
                }
                else {
                    // otherwise add to it
                    Map chainPropertyModel = (Map)chainProperty.get(controller);
                    chainPropertyModel.putAll( this.chainModel );
                    this.chainModel = chainPropertyModel;
                }
            }



        // Step 7: determine argument count and execute.
        Object returnValue = action.call();

        // Step 8: add any errors to the request
        request.setAttribute( GrailsRequestAttributes.ERRORS, controller.getProperty(ControllerDynamicMethods.ERRORS_PROPERTY) );

        return returnValue;
    }

    /* (non-Javadoc)
      * @see org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper#handleActionResponse(org.codehaus.groovy.grails.commons.GrailsControllerClass, java.lang.Object, java.lang.String, java.lang.String)
      */
    public ModelAndView handleActionResponse( GroovyObject controller,Object returnValue,String closurePropertyName, String viewName) {
        boolean viewNameBlank = (viewName == null || viewName.length() == 0);
        // reset the metaclass
        ModelAndView explicityModelAndView = (ModelAndView)controller.getProperty(ControllerDynamicMethods.MODEL_AND_VIEW_PROPERTY);
        if(explicityModelAndView != null) {
            return explicityModelAndView;
        }
        else if (returnValue == null) {
            if (viewNameBlank) {
                return null;
            } else {
                return new ModelAndView(viewName, new BeanMap(controller));
            }
        } else if (returnValue instanceof Map) {
            // remove any Proxy wrappers and set the adaptee as the value
            removeProxiesFromModelObjects((Map)returnValue);
            if (viewNameBlank) {
                throw new NoViewNameDefinedException("Map instance returned by and no view name specified for closure on property [" + closurePropertyName + "] in controller [" + controller.getClass() + "]!");
            } else {
                Map returnMap = (Map)returnValue;
                if(!this.chainModel.isEmpty()) {
                    this.chainModel.putAll( returnMap );
                    return new ModelAndView(viewName, this.chainModel);
                }
                else {
                    return new ModelAndView(viewName, returnMap);
                }
            }
        } else if (returnValue instanceof ModelAndView) {
            ModelAndView modelAndView = (ModelAndView)returnValue;

            // remove any Proxy wrappers and set the adaptee as the value
            Map modelMap = modelAndView.getModel();
            removeProxiesFromModelObjects(modelMap);

            if(!this.chainModel.isEmpty()) {
                this.chainModel.putAll(modelMap);
                modelAndView.addAllObjects(this.chainModel);
            }

            if (modelAndView.getView() == null && modelAndView.getViewName() == null) {
                if (viewNameBlank) {
                    throw new NoViewNameDefinedException("ModelAndView instance returned by and no view name defined by nor for closure on property [" + closurePropertyName + "] in controller [" + controller.getClass() + "]!");
                } else {
                    modelAndView.setViewName(viewName);
                }
            }
            return modelAndView;
        }
        else {
            Map modelMap = new BeanMap(controller);
            ModelAndView modelAndView = new ModelAndView(viewName, modelMap);
            if(!this.chainModel.isEmpty()) {
                this.chainModel.putAll(modelMap);
                modelAndView.addAllObjects(this.chainModel);
            }
            return modelAndView;
        }
    }

}
