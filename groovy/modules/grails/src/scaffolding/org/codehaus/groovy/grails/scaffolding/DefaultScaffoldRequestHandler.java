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
package org.codehaus.groovy.grails.scaffolding;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.web.binding.GrailsDataBinder;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the ScaffoldRequestHandler interface
 * 
 * @author Graeme Rocher
 * @since 30 Nov 2005
 */
public class DefaultScaffoldRequestHandler implements ScaffoldRequestHandler {

    private static final Log LOG = LogFactory.getLog(DefaultScaffoldRequestHandler.class);

    private static final String PARAM_MAX = "max";
    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_ID = "id";
    private static final String PARAM_SORT = "sort";
    private static final String PARAM_ORDER = "order";

    private ScaffoldDomain domain;

    public void setScaffoldDomain(ScaffoldDomain domain) {
        this.domain = domain;
    }

    public ScaffoldDomain getScaffoldDomain() {
        return this.domain;
    }

    public Map handleList(HttpServletRequest request,
                          HttpServletResponse response) {
        int max = 10;
        int offset = 0;
        String maxParam = request.getParameter(PARAM_MAX);
        String offsetParam = request.getParameter(PARAM_OFFSET);

        if(!StringUtils.isBlank(maxParam)) {
            try {
                max = Integer.parseInt(maxParam);
            }
            catch(NumberFormatException nfe) {
                LOG.warn("[ScaffoldRequestHandler] Error parsing max parameter ["+maxParam+"] for request", nfe);
            }
        }
        if(!StringUtils.isBlank(offsetParam)) {
            try {
                offset = Integer.parseInt(offsetParam);
            }
            catch(NumberFormatException nfe) {
                LOG.warn("[ScaffoldRequestHandler] Error parsing offset parameter ["+offsetParam+"] for request", nfe);
            }
        }
        if(LOG.isTraceEnabled()) {
            LOG.trace("[ScaffoldRequestHandler] Executing [list] for max ["+max+"] and offset ["+offset+"]");
        }
        Map model = new HashMap();
        model.put( domain.getPluralName(), domain.list(max, offset, request.getParameter(PARAM_SORT), request.getParameter(PARAM_ORDER)) );

        if(LOG.isTraceEnabled()) {
            LOG.trace("[ScaffoldRequestHandler] Returned model ["+model+"] from domain method [list]");
        }

        return model;
    }

    public Map handleShow(HttpServletRequest request,
                          HttpServletResponse response, ScaffoldCallback callback) {

        String id = request.getParameter(PARAM_ID);
        if(StringUtils.isBlank(id)) {
            LOG.debug("[ScaffoldRequestHandler] No ID parameter ["+id+"] for request [show]");
            callback.setInvoked(false);
            return Collections.EMPTY_MAP;
        }


        Map model = new HashMap();
        Object domainObject = domain.get(id);
        model.put(domain.getSingularName(), domainObject);
        callback.setInvoked(true);

        return model;
    }

    public Map handleDelete(HttpServletRequest request,
                            HttpServletResponse response, ScaffoldCallback callback) {

        String id = request.getParameter(PARAM_ID);
        if(StringUtils.isBlank(id)) {
            LOG.debug("[ScaffoldRequestHandler] No ID parameter ["+id+"] for request [delete]");
            callback.setInvoked(false);
            return Collections.EMPTY_MAP;
        }


        Map model = new HashMap();
        Object domainObject = domain.get(id);
        model.put(domain.getSingularName(), domainObject);

        if(domainObject != null) {
            domain.delete(id);
            callback.setInvoked(true);
        }

        return model;
    }

    public Map handleSave(HttpServletRequest request,
                          HttpServletResponse reponse, ScaffoldCallback callback) {

        Object domainObject = domain.newInstance();
        ServletRequestDataBinder dataBinder = GrailsDataBinder.createBinder(domainObject, domain.getName(),request);
        dataBinder.bind(request);

        Map model = new HashMap();
        model.put( domain.getSingularName(), domainObject);

        if( this.domain.save(domainObject,callback) ) {
            BeanWrapper domainBean = new BeanWrapperImpl(domainObject);
            Object identity = domainBean.getPropertyValue(domain.getIdentityPropertyName());
            model.put( PARAM_ID, identity );
            callback.setInvoked(true);
        }
        return model;
    }


    public Map handleUpdate(HttpServletRequest request, HttpServletResponse reponse, ScaffoldCallback callback) {
        String id = request.getParameter(PARAM_ID);
        if(StringUtils.isBlank(id)) {
            LOG.debug("[ScaffoldRequestHandler] No ID parameter ["+id+"] for request [update]");
            callback.setInvoked(false);
            return Collections.EMPTY_MAP;
        }

        Object domainObject = this.domain.get(id);
        ServletRequestDataBinder dataBinder = GrailsDataBinder.createBinder(domainObject, domain.getName(),request);
        dataBinder.bind(request);

        Map model = new HashMap();
        if( this.domain.update(domainObject,callback) ) {
            model.put( PARAM_ID, id );
            model.put(	this.domain.getSingularName(), domainObject);
        }
        return model;
    }

    public Map handleFind(HttpServletRequest request,
                          HttpServletResponse reponse) {
        // TODO Auto-generated method stub
        return null;
    }

}
