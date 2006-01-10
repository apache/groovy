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
package org.codehaus.groovy.grails.web.metaclass;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.lang.GroovyObject;
import groovy.xml.MarkupBuilder;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.ControllerExecutionException;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.apache.commons.collections.BeanMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Allows rendering of text, views, and templates to the response
 *
 * @author Graeme Rocher
 * @since Oct 27, 2005
 */
public class RenderDynamicMethod extends AbstractDynamicControllerMethod {
    private static final String METHOD_SIGNATURE = "render";
    private static final String ARGUMENT_TEXT = "text";
    private static final String ARGUMENT_CONTENT_TYPE = "contentType";
    private static final String ARGUMENT_ENCODING = "encoding";
    private static final String ARGUMENT_VIEW = "view";
    private static final String ARGUMENT_MODEL = "model";
    private GrailsControllerHelper helper;

    public RenderDynamicMethod(GrailsControllerHelper helper, HttpServletRequest request, HttpServletResponse response) {
        super(METHOD_SIGNATURE, request, response);
        this.helper = helper;
    }

    public Object invoke(Object target, Object[] arguments) {
        if(arguments.length == 0)
            throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);

        if(arguments[0] instanceof String) {
            try {
                response.getWriter().write((String)arguments[0]);
            } catch (IOException e) {
                throw new ControllerExecutionException(e.getMessage(),e);
            }
        }
        else if(arguments[0] instanceof Map) {
            Map argMap = (Map)arguments[0];
           PrintWriter out = null;
           try {
               if(argMap.containsKey(ARGUMENT_CONTENT_TYPE) && argMap.containsKey(ARGUMENT_ENCODING)) {
                   out = response.getWriter((String)argMap.get(ARGUMENT_CONTENT_TYPE),
                                            (String)argMap.get(ARGUMENT_ENCODING));
               }
               else if(argMap.containsKey(ARGUMENT_CONTENT_TYPE)) {
                   out = response.getWriter((String)argMap.get(ARGUMENT_CONTENT_TYPE));
               }
               else {
                   out = response.getWriter();
               }
           }
           catch(IOException ioe) {
                throw new ControllerExecutionException("I/O creating write in method [render] on class ["+target.getClass()+"]: " + ioe.getMessage(),ioe);
           }
            if(argMap.containsKey(ARGUMENT_TEXT)) {
               String text = (String)argMap.get(ARGUMENT_TEXT);
               out.write(text);
            }
            else if(argMap.containsKey(ARGUMENT_VIEW)) {
               String viewName = (String)argMap.get(ARGUMENT_VIEW);
               GrailsControllerClass controllerClass = helper.getControllerClassByName(target.getClass().getName());
               String viewUri = controllerClass.getViewByName(viewName);

               Map model;
               Object modelObject = argMap.get(ARGUMENT_MODEL);
                if(modelObject instanceof Map) {
                    model = (Map)modelObject;
                }
                else {
                    model = new BeanMap(target);
                }
                GroovyObject controller = (GroovyObject)target;
                controller.setProperty( ControllerDynamicMethods.MODEL_AND_VIEW_PROPERTY, new ModelAndView(viewUri,model) );
            }
            else {
                throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);
            }
        }
        else if(arguments[0] instanceof Closure) {
            try {
                MarkupBuilder mkp = new MarkupBuilder(response.getWriter());
                mkp.invokeMethod("doCall", new Object[]{ arguments[0] });
           }
           catch(IOException ioe) {
                throw new ControllerExecutionException("I/O error rendering text markup from closure to response: " + ioe.getMessage(),ioe);
           }
        }
        else {
            throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);
        }
        return null;
    }
}
