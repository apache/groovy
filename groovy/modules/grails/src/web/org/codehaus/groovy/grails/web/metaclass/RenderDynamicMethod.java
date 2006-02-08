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

import grails.util.OpenRicoBuilder;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;
import groovy.lang.Writable;
import groovy.text.Template;
import groovy.xml.StreamingMarkupBuilder;
import org.apache.commons.collections.BeanMap;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine;
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletResponse;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.ControllerExecutionException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
    private static final String ARGUMENT_TEMPLATE = "template";
    private static final String ARGUMENT_BEAN = "bean";
    private static final String ARGUMENT_COLLECTION = "collection";
    private static final String ARGUMENT_BUILDER = "builder";
    private static final String DEFAULT_ARGUMENT = "it";
    private static final String BUILDER_TYPE_RICO = "rico";

    private GrailsControllerHelper helper;
    protected GrailsHttpServletResponse response;


    public RenderDynamicMethod(GrailsControllerHelper helper, HttpServletRequest request, HttpServletResponse response) {
        super(METHOD_SIGNATURE, request, response);
        this.helper = helper;
        this.response = new GrailsHttpServletResponse(response);
    }

    public Object invoke(Object target, Object[] arguments) {
        if(arguments.length == 0)
            throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);

        boolean renderView = true;
        GroovyObject controller = (GroovyObject)target;
        if(arguments[0] instanceof String) {
            try {
                response.getWriter().write((String)arguments[0]);
                renderView = false;
            } catch (IOException e) {
                throw new ControllerExecutionException(e.getMessage(),e);
            }
        }
        else if(arguments[0] instanceof Closure) {
            StreamingMarkupBuilder b = new StreamingMarkupBuilder();
            Writable markup = (Writable)b.bind(arguments[arguments.length - 1]);
            try {
                markup.writeTo(response.getWriter());
            } catch (IOException e) {
                throw new ControllerExecutionException("I/O error executing render method for arguments ["+arguments[0]+"]: " + e.getMessage(),e);
            }
            renderView = false;
        }
        else if(arguments[0] instanceof Map) {
            Map argMap = (Map)arguments[0];
           PrintWriter out;
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

            if(arguments[arguments.length - 1] instanceof Closure) {
                if(BUILDER_TYPE_RICO.equals(argMap.get(ARGUMENT_BUILDER))) {
                    OpenRicoBuilder orb = null;
                    try {
                        orb = new OpenRicoBuilder(response);
                        renderView = false;
                    } catch (IOException e) {
                        throw new ControllerExecutionException("I/O error executing render method for arguments ["+argMap+"]: " + e.getMessage(),e);
                    }
                    orb.invokeMethod("ajax", new Object[]{ arguments[arguments.length - 1] });
                }
                else {
                    StreamingMarkupBuilder b = new StreamingMarkupBuilder();
                    Writable markup = (Writable)b.bind(arguments[arguments.length - 1]);
                    try {
                        markup.writeTo(out);
                    } catch (IOException e) {
                        throw new ControllerExecutionException("I/O error executing render method for arguments ["+argMap+"]: " + e.getMessage(),e);
                    }
                    renderView = false;
                }
            }
            else if(arguments[arguments.length - 1] instanceof String) {
               out.write((String)arguments[arguments.length - 1]);
               renderView = false;
            }
            else if(argMap.containsKey(ARGUMENT_TEXT)) {
               String text = (String)argMap.get(ARGUMENT_TEXT);
               out.write(text);
               renderView = false;
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
                controller.setProperty( ControllerDynamicMethods.MODEL_AND_VIEW_PROPERTY, new ModelAndView(viewUri,model) );
            }
            else if(argMap.containsKey(ARGUMENT_TEMPLATE)) {
                String templateName = (String)argMap.get(ARGUMENT_TEMPLATE);

                // get the template uri
                GrailsApplicationAttributes attrs = (GrailsApplicationAttributes)controller.getProperty(ControllerDynamicMethods.GRAILS_ATTRIBUTES);
                String templateUri = attrs.getTemplateUri(templateName,request);
                // retrieve gsp engine
                GroovyPagesTemplateEngine engine = attrs.getPagesTemplateEngine();
                try {
                    Template t = engine.createTemplate(templateUri,attrs.getServletContext(),request,response);
                    Map binding = new HashMap();

                    if(argMap.containsKey(ARGUMENT_BEAN)) {
                        binding.put(DEFAULT_ARGUMENT, argMap.get(ARGUMENT_BEAN));
                        Writable w = t.make(binding);
                        w.writeTo(out);
                    }
                    else if(argMap.containsKey(ARGUMENT_COLLECTION)) {
                        Object colObject = argMap.get(ARGUMENT_COLLECTION);
                        if(colObject instanceof Collection) {
                             Collection c = (Collection) colObject;
                            for (Iterator i = c.iterator(); i.hasNext();) {
                                Object o = i.next();
                                binding.put(DEFAULT_ARGUMENT, o);
                                Writable w = t.make(binding);
                                w.writeTo(out);
                            }
                        }
                        else {
                            binding.put(DEFAULT_ARGUMENT, colObject);
                            Writable w = t.make(binding);
                            w.writeTo(out);
                        }
                    }
                    else if(argMap.containsKey(ARGUMENT_MODEL)) {
                        Object modelObject = argMap.get(ARGUMENT_MODEL);
                        if(modelObject instanceof Map) {
                            Writable w = t.make((Map)argMap.get(ARGUMENT_MODEL));
                            w.writeTo(out);
                        }
                        else {
                            Writable w = t.make(new BeanMap(target));
                            w.writeTo(out);
                        }
                    }
                    else {
                            Writable w = t.make(new BeanMap(target));
                            w.writeTo(out);
                    }
                    renderView = false;
                }
                catch(IOException ioex) {
                    throw new ControllerExecutionException("I/O error executing render method for arguments ["+argMap+"]: " + ioex.getMessage(),ioex);
                } catch (ServletException e) {
                    throw new ControllerExecutionException("Servlet exception executing render method for arguments ["+argMap+"]: " + e.getMessage(),e);
                }
            }
            else {
                throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);
            }
        }
        else {
            throw new MissingMethodException(METHOD_SIGNATURE,target.getClass(),arguments);
        }
        controller.setProperty(ControllerDynamicMethods.RENDER_VIEW_PROPERTY,Boolean.valueOf(renderView));
        return null;
    }
}
