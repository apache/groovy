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

 /**
 *  The global application tag library for Grails many of which are based off of Rails
 *  helpers
 *
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
class ApplicationTagLib {

    /**
     *  General linking to controllers, actions etc. Examples:
     *
     *  <gr:link action="myaction">link 1</gr:link>
     *  <gr:link controller="myctrl" action="myaction">link 2</gr:link>
     */
    @Property link = { attrs, body ->
        out << "<a href=\""
        out << grailsAttributes.getApplicationUri(request)
        // if the current attribute null set the controller uri to the current controller
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
     * link can then be included in links, ajax calls etc.
     */
    @Property createLink = { attrs ->
        out << grailsAttributes.getApplicationUri(request)
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
     *  Creates a remote function call using the prototype library
     */
    @Property remoteFunction = { attrs  ->
        // before remote function
        def after = ''
        if(attrs["before"])
            out << "${attrs.remove('before')};"
        if(attrs["after"])
            after = "${attrs.remove('after')};"

        out << 'new Ajax.'
        if(attrs["update"]) {
            out << 'Updater('
            if(attrs["update"] instanceof Map) {
                out << "{"
                def update = []
                if(attrs["update"]["success"]) {
                    update << "success:'${attrs['update']['success']}'"
                }
                if(attrs["update"]["failure"]) {
                    update << "failure:'${attrs['update']['failure']}'"
                }
                out << update.join(',')
                out << "},"
            }
            else {
                out << "'" << attrs["update"] << "',"
            }
            attrs.remove("update")
        }
        else {
            out << "Request("
        }

        out << "'"
        createLink(attrs)
        out << "',"

        // process options
        out << getAjaxOptions(attrs)
        // close
        out << ');'

        // after remote function
        out <<  after
    }

    // helper function to build ajax options
    def getAjaxOptions(options) {
        def ajaxOptions = []

        if(options) {
            // process callbacks
            def callbacks = options.findAll { k,v ->
                k ==~ /on(\p{Upper}|\d){1}\w+/
            }
            callbacks.each { k,v ->
                ajaxOptions << "${k}:function(e){${v}}"
                options.remove(k)
            }

            // necessary defaults
            if(options['asynchronous'])
                ajaxOptions << "asynchronous:${options.remove('asynchronous')}"
            else
                ajaxOptions << "asynchronous:true"


            if(options['evalScripts'])
                ajaxOptions << "evalScripts:${options.remove('evalScripts')}"
            else
                ajaxOptions << "evalScripts:true"

            // remaining options
            options.each { k, v ->
                 switch(v) {
                    case 'true': ajaxOptions << "${k}:${v}"; break;
                    case 'false': ajaxOptions << "${k}:${v}"; break;
                    case ~/\s*function(\w*)\s*/: ajaxOptions << "${k}:${v}"; break;
                    default:ajaxOptions << "${k}:'${v}'"; break;
                 }
            }
        }
        // set defaults
        else {
             ajaxOptions << "asynchronous:true"
             ajaxOptions << "evalScripts:true"
        }

        return "{${ajaxOptions.join(',')}}"
    }

    @Property remoteLink = { attrs, body ->
       out << "<a href=\"#\" onclick=\""
        // create remote function
        remoteFunction(attrs)
        out << 'return false;\" '
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

    @Propert remoteForm = { attrs, body ->
           // TODO
    }

    /**
     * Checks if the request has errors either for a field or global errors
     */
    @Property hasErrors = { attrs, body ->
        def errors = grailsAttributes.getErrors( request )
        if(!errors)
            return false
        // if there is a field attribute check errors for that field
        if(attrs["field"]) {
            if(errors.hasFieldErrors( attrs["field"] )) {
                body()
            }
        }
        // otherwise check all errors
        else {
           if(errors.hasErrors()) {
                body()
           }
        }
    }

    /**
     * Loops through each error for either field or global errors
     */
    @Property eachError = { attrs, body ->
        def errors = grailsAttributes.getErrors( request )
        if(!errors)
            return;
        // if there is a field attribute iterative only the field
        if(attrs["field"]) {
            if(errors.hasFieldErrors( attrs["field"] )) {
                errors.getFieldErrors( attrs["field"] ).each {
                   body( it )
                }
            }
        }
        // otherwise check all errors
        else {
           if(errors.hasErrors()) {
                errors.allErrors.each {
                    body( it )
                }
           }
        }
    }

    @Property validate = { attrs, body ->
        def form = attrs["form"]
        def againstClass = attrs["against"]
        // TODO
    }


}