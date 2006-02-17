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
 * WITHOUT c;pWARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 /**
 * The base application tag library for Grails many of which take inspiration from Rails helpers (thanks guys! :)
 * This tag library tends to get extended by others as tags within here can be re-used in said libraries
 *
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
import org.springframework.validation.Errors;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU;

class ApplicationTagLib {
    /**
     * Creates a link to a resource, generally used as a method rather than a tag.
     *
     * eg. <link type="text/css" href="${createLinkTo(dir:'css',file:'main.css')}" />
     */
    @Property createLinkTo = { attrs, body ->
         out << grailsAttributes.getApplicationUri(request)
         if(attrs['dir']) {
            out << "/${attrs['dir']}"
         }
         if(attrs['file']) {
            out << "/${attrs['file']}"
         }
    }

    /**
     *  General linking to controllers, actions etc. Examples:
     *
     *  <g:link action="myaction">link 1</gr:link>
     *  <g:link controller="myctrl" action="myaction">link 2</gr:link>
     */
    @Property link = { attrs, body ->
        out << "<a href=\""
        // create the link
        createLink(attrs)

        out << '\" '
        // process remaining attributes
        attrs.each { k,v ->
            out << k << "=\"" << v << "\" "
        }
        out << ">"
        // output the body
        body()

        // close tag
        out << "</a>"
    }


    /**
     * Creates a grails application link from a set of attributes. This
     * link can then be included in links, ajax calls etc. Generally used as a method call
     * rather than a tag eg.
     *
     *  <a href="${createLink(action:'list')}">List</a>
     */
    @Property createLink = { attrs ->
        out << grailsAttributes.getApplicationUri(request)
        // prefer a URL attribute
        if(attrs['url']) {
             attrs = attrs.remove('url')
        }
        // if the current attribute null set the controller uri to the current controller
        if(attrs["controller"]) {
            out << '/' << attrs.remove("controller")
        }
        else {
           out << grailsAttributes.getControllerUri(request)
        }
        if(attrs["action"]) {
            out << '/' << attrs.remove("action")
        }
        if(attrs["id"]) {
            out << '/' << attrs.remove("id")
        }
    }



    /**
     *  allows rendering of templates inside views for collections, models and beans. Examples:
     *
     *  <g:render template="atemplate" collection="${users}" />
     *  <g:render template="atemplate" model="[user:user,company:company]" />
     *  <g:render template="atemplate" bean="${user}" />
     */
    @Property render = { attrs, body ->
        if(!attrs['template'])
            throwTagError("Tag [render] is missing required attribute [template]")

        def engine = grailsAttributes.getPagesTemplateEngine()
        def uri = '/WEB-INF/grails-app/views' << grailsAttributes.getControllerUri(request)
        uri << "/_${attrs['template']}.gsp"
        uri = uri.toString()

        def url = servletContext.getResource(uri)
        if(!url)
            throwTagError("No template found for name [${attrs['template']}] in tag [render]")

        def t = engine.createTemplate(  uri,
                                        servletContext,
                                        request,
                                        response)

        if(attrs['model'] instanceof Map) {
            t.make( attrs['model'] ).writeTo(out)
        }
        else if(attrs['collection']) {
            attrs['collection'].each {
                t.make( ['it': it] ).writeTo(out)
            }
        }
        else if(attrs['bean']) {
            t.make( [ 'it' : attrs['bean'] ] ).writeTo(out)
        }
    }

    /**
     * Attempts to render input for a property value based by attempting to choose a rendering component
     * to use based on the property type
     */
    @Property renderInput = { attrs, body ->
        def bean = attrs['bean']
        if(!bean) {
            throwTagError("Tag [renderInput] is missing required attribute [bean]")
        }
        if(!attrs['property']) {
            throwTagError("Tag [renderInput] is missing required attribute [property]")
        }

       def app = grailsAttributes.getGrailsApplication()
       def dc = app.getGrailsDomainClass(bean.class.name)
       def pv = bean.metaPropertyValues.find {
            it.name == attrs['property']
       }
       if(!pv) {
          throwTagError("Property [${attrs['property']}] does not exist in tag [renderInput] for bean [${bean}]")
       }
       def engine = grailsAttributes.getPagesTemplateEngine()
       def uri = findUriForType(pv.type)

       if(!uri)
            throwTagError("Type [${pv.type}] is unsupported by tag [renderInput]. No template found.")

       def t = engine.createTemplate(   uri,
                                        servletContext,
                                        request,
                                        response)
       if(!t) {
            throwTagError("Type [${pv.type}] is unsupported by tag [renderInput]. No template found.")
       }

       def binding = [ name:pv.name,value:pv.value]
       binding['constraints'] = (dc ? dc.constrainedProperties : null)

       t.make(binding).writeTo(out)
    }

    private String findUriForType(type) {
        if(type == Object.class)
            return null;
        def uri = "/WEB-INF/internal/render/${type.name}.gsp";
        def url = servletContext.getResource(uri)

        if(url != null) {
            return uri
        }
        else {
            return findUriForType(type.superClass)
        }
    }
}