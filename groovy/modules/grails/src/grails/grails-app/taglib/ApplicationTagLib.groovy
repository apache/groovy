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
 *  The global application tag library for Grails many of which take inspiration from Rails helpers (thanks guys! :)
 *
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
import org.springframework.validation.Errors;
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU;

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
     * Checks if the request has errors either for a field or global errors
     */
    @Property hasErrors = { attrs, body ->
        def model = attrs['model']
        def checkList = []
        if(model) {
            checkList = model.findAll { k,v ->
                GCU.isDomainClass(v.class)
            }
        }
        if(attrs['bean']) {
            checkList << attrs['bean']
        }
        else {
            request.attributeNames.each {
                def ra = request[it]
                if(ra instanceof Errors)
                    checkList << ra
                else if(GCU.isDomainClass(ra.class))
                    checkList << ra
            }
        }

        for(i in checkList) {
            def errors = null
            if(GCU.isDomainClass(i.class)) {
                if(i.hasErrors())
                    errors = i.errors
            }
            else if(i instanceof Errors) {
               errors = i
            }
            if(errors) {
                if(attrs['field']) {
                    if(errors.hasFieldErrors(attrs['field'])) {
                        body()
                    }
                }
                else {
                    body()
                }
            }
        }
    }

    /**
     * Loops through each error for either field or global errors
     */
    @Property eachError = { attrs, body ->
        def model = attrs['model']
        def errorList = []
        if(model) {
            errorList = model.findAll { k,v ->
                GCU.isDomainClass(v.class)
            }
        }
        if(attrs['bean']) {
            errorList << attrs['bean']
        }
        else {
            request.attributeNames.each {
                def ra = request[it]
                if(ra instanceof Errors)
                    errorList << ra
                else if(GCU.isDomainClass(ra.class))
                    errorList << ra
            }
        }

        for(i in errorList) {
            def errors = null
            if(GCU.isDomainClass(i.class)) {
                if(i.hasErrors())
                    errors = i.errors
            }
            else if(i instanceof Errors) {
               errors = i
            }
            if(errors && errors.hasErrors()) {
                if(attrs['field']) {
                    if(errors.hasFieldErrors(attrs['field'])) {
                        errors.getFieldErrors( attrs["field"] ).each {
                            body(it)
                        }
                    }
                }
                else {
                    errors.allErrors.each {
                        body( it )
                    }
                }
            }
        }
    }

    /**
     *  allows rendering of templates inside views for collections, models and beans. Examples:
     *
     *  <gr:render template="atemplate" collection="${users}" />
     *  <gr:render template="atemplate" model="[user:user,company:company]" />
     *  <gr:render template="atemplate" bean="${user}" />
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
     * Loops through each error and renders it using one of the supported mechanisms (defaults to "list" if unsupported)
     */
    @Property renderErrors = { attrs, body ->
        def renderAs = attrs.remove('as')
        if(!renderAs) renderAs = 'list'

        if(renderAs == 'list') {
            out << "<ul>"
            eachError(attrs, {
                out << "<li>"
                body(it)
                out << "</li>"
              }
            )
            out << "</ul>"
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
        def uri = "/WEB-INF/grails-app/views/scaffolding/${type.name}.gsp";
        def url = servletContext.getResource(uri)

        if(url != null) {
            return uri
        }
        else {
            return findUriForType(type.superClass)
        }
    }

    // Maps out how Grails contraints map to Apache commons validators
    static CONSTRAINT_TYPE_MAP = [ email : 'email',
                                             creditCard : 'creditCard',
                                             match : 'mask',
                                             blank: 'required',
                                             nullable: 'required',
                                             maxSize: 'maxLength',
                                             minSize: 'minLength',
                                             range: 'intRange',
                                             size: 'intRange',
                                             length: 'maxLength,minLength' ]
    /**
     * Validates a form using Apache commons validator javascript against constraints defined in a Grails
     * domain class
     */
    @Property validate = { attrs, body ->
        def form = attrs["form"]
        def againstClass = attrs["against"]
        if(!form)
            throwTagError("Tag [validate] is missing required attribute [form]")

        if(!againstClass) {
            againstClass = form.substring(0,1).toUpperCase() + form.substring(1)
        }

        def app = grailsAttributes.getGrailsApplication()
        def dc = app.getGrailsDomainClass(againstClass)

        if(!dc)
            throwTagError("Tag [validate] could not find a domain class to validate against for name [${againstClass}]")

        def constrainedProperties = dc.constrainedProperties.collect { k,v -> return v }
        def appliedConstraints = []

        constrainedProperties.each {
           appliedConstraints += it.collect{ it.appliedConstraints }
        }

        appliedConstraints = appliedConstraints.flatten()
        def fieldValidations = [:]
        appliedConstraints.each {
            def validateType = CONSTRAINT_TYPE_MAP[it.name]
            if(validateType) {
                if(fieldValidations[validateType]) {
                    fieldValidations[validateType] << it
                }
                else {
                     fieldValidations[validateType] =  [it]
                }
            }
        }

        out << '<script type="text/javascript">\n'
        fieldValidations.each { k,v ->
           def validateType = k

           if(validateType) {

                def validateTypes = [validateType]

                if(validateType.contains(",")) {
                    validateTypes = validateType.split(",")
                }


                validateTypes.each { vt ->
                    // import required script
                    def scriptName = "org/apache/commons/validator/javascript/validate" + vt.substring(0,1).toUpperCase() + vt.substring(1) + ".js"
                    def inStream = getClass().classLoader.getResourceAsStream(scriptName)
                    if(inStream) {
                        out << inStream.text
                    }

                    v.each { constraint ->
                           out << "this.${constraint.propertyName} = new Array("
                           out << "document.forms['${form}'].elements['${constraint.propertyName}']," // the field
                           out << '"Test message"' // the message
                           switch(vt) {
                                case 'mask': out << ",function() { return '${constraint.regex}'; }";break;
                                case 'intRange': out << ",function() { if(arguments[0]=='min') return ${constraint.range.from}; else return ${constraint.range.to} }";break;
                                case 'floatRange': out << ",function() { if(arguments[0]=='min') return ${constraint.range.from}; else return ${constraint.range.to} }";break;
                                case 'maxLength': out << ",function() { return ${constraint.maxSize};  }";break;
                                case 'minLength': out << ",function() { return ${constraint.minSize};  }";break;
                           }
                           out << ');\n'
                    }
                    out << "}\n"
                }
            }
        }
        out << 'function validateForm(form) {\n'
         fieldValidations.each { k,v ->
               def validateType = k.substring(0,1).toUpperCase() + k.substring(1)
               out << "if(!validate${validateType}(form)) return false;\n"
         }
        out << 'return true;\n';
        out << '}\n'
      //  out << "document.forms['${attrs['form']}'].onsubmit = function(e) {return validateForm(this)}\n"
        out << '</script>'
    }
}