/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.tools.shell.util;

import groovy.lang.GroovyObjectSupport;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Message source backed up by one or more {@link java.util.ResourceBundle}
 * instances for simple i18n support.
 */
public class MessageSource
    extends GroovyObjectSupport
{
    private final String[] bundleNames;
    
    private ResourceBundle[] cachedBundles;
    
    public MessageSource(final String[] names) {
        assert names != null;
        assert names.length != 0;
        
        this.bundleNames = names;
    }
    
    public MessageSource(final String name) {
        this(new String[] { name });
    }
    
    private static String[] classNames(final Class[] types) {
        assert types != null;
        assert types.length != 0;
        
        String[] names = new String[types.length];
        
        for (int i=0; i<types.length; i++) {
            assert types[i] != null;
            
            names[i] = types[i].getName();
        }
        
        return names;
    }
    
    public MessageSource(final Class[] types) {
        this(classNames(types));
    }
    
    public MessageSource(final Class type) {
        this(new String[] { type.getName() });
    }
    
    private ResourceBundle[] createBundles() {
        ResourceBundle[] bundles = new ResourceBundle[bundleNames.length];
        
        for (int i=0; i<bundleNames.length; i++) {
            assert bundleNames[i] != null;
            
            bundles[i] = ResourceBundle.getBundle(bundleNames[i]);
        }
        
        return bundles;
    }
    
    private ResourceBundle[] getBundles() {
        if (cachedBundles == null) {
            cachedBundles = createBundles();
        }
        return cachedBundles;
    }
    
    /**
     * Get a raw message from the resource bundles using the given code.
     */
    public String getMessage(final String code) {
        assert code != null;
        
        MissingResourceException error = null;
        
        ResourceBundle[] bundles = getBundles();

        for (ResourceBundle bundle : bundles) {
            try {
                return bundle.getString(code);
            } catch (MissingResourceException e) {
                //
                // FIXME: For now just save the first error, should really roll a new message with all of the details
                //

                if (error != null) {
                    error = e;
                }
            }
        }
        
        assert error != null;
        
        throw error;
    }
    
    /**
     * Format a message (based on {@link MessageFormat} using the message
     * from the resource bundles using the given code as a pattern and the
     * given objects as arguments.
     */
    public String format(final String code, final Object[] args) {
        assert args != null;
        
        String pattern = getMessage(code);
        
        return MessageFormat.format(pattern, args);
    }
    
    /**
     * @see #getMessage(String)
     */
    public Object getProperty(final String name) {
        return getMessage(name);
    }
}
