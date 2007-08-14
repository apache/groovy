/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util;

import groovy.lang.GroovyObjectSupport;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

//
// FIXME: This class really isn't all that useful.  It would be *much* better if there
//        was a simple log API in groovy to dynamically switch to the logging facade that
//        is actually installed.
//

/**
 * Represents an arbitrary logging service. By default this outputs to
 * System.out though derivations of this class could log to Jakarta Commons Logging
 * or log4j or JDK 1.5 logging etc
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GroovyLog extends GroovyObjectSupport {

    String prefix;

    /** 
     * Factory method to create new instances 
     */
    public static GroovyLog newInstance(Class aClass) {
        return new GroovyLog(aClass);
    }
    
    public GroovyLog() {
        this("");
    }

    public GroovyLog(Class type) {
        this(type.getName());
    }

    public GroovyLog(Object obj) {
        this(obj.getClass());
    }

    public GroovyLog(String prefix) {
        //
        // FIXME: This kinda sucks as a default... shouldn't tack on any [ or : muck
        //

        this.prefix = (prefix != null && prefix.length() > 0) ? "[" + prefix + ":" : "[";
    }

    public Object invokeMethod(String name, Object args) {
        if (args != null && args.getClass().isArray()) {
            args = DefaultGroovyMethods.join((Object[])args, ",");    
        }

        //
        // FIXME: This kinda sucks as an output format, should probably ucase name and then
        //        warp prefix in [] and then output the args.  Basically what the SimpleLog
        //        does in JCL.
        //
        
        System.out.println(prefix + name + "] " + args);

        return null;
    }
}
