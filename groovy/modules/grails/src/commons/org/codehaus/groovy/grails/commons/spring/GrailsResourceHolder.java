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
package org.codehaus.groovy.grails.commons.spring;

import org.springframework.core.io.Resource;
import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.File;

/**
 * A class that holds references to all the Grails resource instances to support class reloading
 *
 * @author Graeme Rocher
 * @since 31-Jan-2006
 */
public class GrailsResourceHolder {

    public static Pattern GRAILS_RESOURCE_PATTERN = Pattern.compile(".+\\\\grails-app\\\\\\w+\\\\(.+)\\.groovy");
    static{
            if(File.separator.equals("/")){
                GRAILS_RESOURCE_PATTERN =
                    Pattern.compile(".+/grails-app/\\w+/(.+)\\.groovy");
            }
    }


    public static final String APPLICATION_CONTEXT_ID = "grailsResourceHolder";

    private Resource[] resources;

    public Resource[] getResources() {
        return resources;
    }

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    /**
     * @param resource
     * @return Retrieves the class name of the specified resource
     */
    public String getClassName(Resource resource) {
        Matcher m;
        try {
            m = GRAILS_RESOURCE_PATTERN.matcher(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            throw new GrailsConfigurationException("I/O error reading class name from resource ["+resource+"]: " + e.getMessage(),e );
        }
        if(m.find()) {
            return m.group(1);
        }
        return null;
    }
}
