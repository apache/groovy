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
import org.springframework.validation.Errors;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU;

 /**
 *  A  tag lib that provides tags for developing javascript and ajax applications
 *
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
class JavascriptTagLib extends ApplicationTagLib  {

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
        if(attrs['url']) {
            createLink(attrs['url'])
        }
        else {
            createLink(attrs)
        }

        out << "',"

        // process options
        out << getAjaxOptions(attrs)
        // close
        out << ');'

        // after remote function
        if(after)
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

            if(options['parameters']) {
                ajaxOptions << "parameters:${options.remove('parameters')}"
            }
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

    /**
     * A link to a remote uri that used the prototype library to invoke the link via ajax
     */
    @Property remoteLink = { attrs, body ->
       out << "<a href=\"#\" onclick=\""
        // create remote function
        remoteFunction(attrs)
        out << 'return false;" '
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
     * A form which used prototype to serialize its parameters and submit via an asynchronous ajax call
     */
    @Property formRemote = { attrs, body ->
       attrs['parameters'] = "Form.serialize(this)"
       out << '<form onsubmit="' << remoteFunction(attrs) << ';return false;" '
       out << 'method="' <<  (attrs['method'] ? attrs['method'] : 'post') << '" '
       out << 'action="' <<  (attrs['action'] ? attrs['action'] : createLink(attrs['url'])) << '">'

        // output body
           body()
        // close tag
       out << '</form>'
    }

    /**
     *  Creates a form submit button that submits the current form to a remote ajax call
     */
    @Property submitToRemote = { attrs, body ->
        attrs['parameters'] = "Form.serialize(this.form)"
        out << "<input type='button' name='${attrs.remove('name')}' value='${attrs.remove('value')}' "
        out << 'onclick="'
        remoteFunction(attrs)
        out << ';return false;" />'
    }

}