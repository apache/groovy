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
 *  The global application tag library for Grails
 *
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
class ApplicationTagLib {

    @Property link = { attrs, body ->

    }

    @Property linkToRemote = { attrs, body ->
    
    }

    /**
     * Checks if the request has errors either for a field or global errors
     */
    @Property hasErrors = { attrs, body ->
        def errors = this.grailsAttributes.getErrors( this.request )
        if(errors == null)
            return false
        // if there is a field attribute check errors for that field
        if(attrs["field"] != null) {
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
        def errors = this.grailsAttributes.getErrors( this.request )
        if(errors == null)
            return;
        // if there is a field attribute iterative only the field
        if(attrs["field"] != null) {
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
        def dcAttr = attrs["against"]
        // do some validation of attributes here
        def app = this.grailsAttributes.getGrailsApplication()
        def domainClass = app.getGrailsDomainClass(dcAttr)
        if(domainClass != null) {
            def template = """<script type='text/javascript'>\n
                                    document.getElementById('${form}').onsubmit = function(e) {
                                        if(!e)e = window.event;
		                                var src = e.srcElement;
				                        if(!src) src = this;
                                        return validateForm(src)
                                    }
                              </script>"""
        }
    }
}