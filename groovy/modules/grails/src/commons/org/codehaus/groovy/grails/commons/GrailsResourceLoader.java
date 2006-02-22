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
package org.codehaus.groovy.grails.commons;

import groovy.lang.GroovyResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A GroovyResourceLoader that loads groovy files using Spring's IO abstraction
 * 
 * @author Graeme Rocher
 * @since 22-Feb-2006
 */
public class GrailsResourceLoader implements GroovyResourceLoader {
    private Resource[] resources;
    private List loadedResources = new ArrayList();

    public GrailsResourceLoader(Resource[] resources) {
         this.resources = resources;
    }

    public List getLoadedResources() {
        return loadedResources;
    }

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    public URL loadGroovySource(String resource) throws MalformedURLException {
        String groovyFile = resource.replace('.', '/') + ".groovy";
        Resource foundResource = null;
        for (int i = 0; resources != null && i < resources.length; i++) {
            if (resources[i].getFilename().endsWith(groovyFile)) {
                if (foundResource == null) {
                    foundResource = resources[i];
                } else {
                    throw new IllegalArgumentException("Resources [" + resources[i].getFilename() + "] and [" + foundResource.getFilename() + "] end with [" + groovyFile + "]. Cannot load because of duplicate match!");
                }
            }
        }
        try {
            if (foundResource != null) {
                loadedResources.add(foundResource);
                return foundResource.getURL();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
