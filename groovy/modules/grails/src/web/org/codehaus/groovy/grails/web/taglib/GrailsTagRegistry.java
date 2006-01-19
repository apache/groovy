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
package org.codehaus.groovy.grails.web.taglib;

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A registry for holding all Grails tag implementations
 *
 * @author Graeme Rocher
 * @since 11-Jan-2006
 */
public class GrailsTagRegistry {
    private static GrailsTagRegistry instance;

    private static Map tagRegistry = Collections.synchronizedMap(new HashMap());

    static {
        GrailsTagRegistry tagRegistry = getInstance();
        tagRegistry.registerTag(LinkTag.TAG_NAME, LinkTag.class);
        tagRegistry.registerTag(RenderInputTag.TAG_NAME, RenderInputTag.class);
        tagRegistry.registerTag(GroovyEachTag.TAG_NAME, GroovyEachTag.class);        
        tagRegistry.registerTag(GroovyIfTag.TAG_NAME, GroovyIfTag.class);
        tagRegistry.registerTag(GroovyElseTag.TAG_NAME, GroovyElseTag.class);
    }

    private GrailsTagRegistry() {
    }

    public static GrailsTagRegistry getInstance() {
        if(instance == null)
            instance = new GrailsTagRegistry();

        return instance;
    }

    public void registerTag(String tagName, Class tag) {
        tagRegistry.put(tagName,tag);
    }

    public boolean tagSupported(String tagName) {
        return tagRegistry.containsKey(tagName);
    }
    
    public boolean isSyntaxTag(String tagName) {
        if(tagRegistry.containsKey(tagName)) {
            Class tagClass = (Class)tagRegistry.get(tagName);
            return GroovySyntaxTag.class.isAssignableFrom(tagClass);
        }
        return false;
    }

    public GrailsTag newTag(String tagName) {
        if(tagRegistry.containsKey(tagName)) {
            Class tagClass = (Class)tagRegistry.get(tagName);

            GrailsTag tag;
            try {
                tag = (GrailsTag)tagClass.newInstance();
            } catch (InstantiationException e) {
                throw new GrailsTagException("Instantiation error loading tag ["+tagName+"]: " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new GrailsTagException("Illegal access error loading tag ["+tagName+"]: " + e.getMessage(), e);
            }
            return tag;
        }
        else {
            throw new GrailsTagException("Tag ["+tagName+"] is not a a valid grails tag");
        }
    }

    public boolean isRequestContextTag(String tagName) {
        if(tagRegistry.containsKey(tagName)) {
            Class tagClass = (Class)tagRegistry.get(tagName);
            return RequestContextTag.class.isAssignableFrom(tagClass);
        }
        return false;
    }
}
