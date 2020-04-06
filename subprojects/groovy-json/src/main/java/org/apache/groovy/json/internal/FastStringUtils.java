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
package org.apache.groovy.json.internal;

import org.apache.groovy.json.DefaultFastStringService;
import org.apache.groovy.json.FastStringService;
import org.apache.groovy.json.FastStringServiceFactory;

import java.util.ServiceLoader;

/**
 * Internal class for fast processing of Strings during JSON parsing
 */
public class FastStringUtils {

    private static class ServiceHolder {
        static final FastStringService INSTANCE = loadService();

        private static FastStringService loadService() {
// left classloading very simple in light of potential changes needed for jdk9
// that means you might need @GrabConfig(systemClassLoader=true) if getting json via grab
//            ClassLoader rootLoader = DefaultGroovyMethods.getRootLoader(loader);
            ServiceLoader<FastStringServiceFactory> serviceLoader = ServiceLoader.load(FastStringServiceFactory.class);
            FastStringService found = null;
            for (FastStringServiceFactory factory : serviceLoader) {
                FastStringService service = factory.getService();
                if (service != null) {
                    found = service;
                    if (!(service instanceof DefaultFastStringService)) {
                        break;
                    }
                }
            }
            return found;
        }
    }

    private static FastStringService getService() {
        if (ServiceHolder.INSTANCE == null) {
            throw new RuntimeException("Unable to load FastStringService");
        }
        return ServiceHolder.INSTANCE;
    }

    /**
     * @param string string to grab array from.
     * @return char array from string
     */
    public static char[] toCharArray(final String string) {
        return getService().toCharArray(string);
    }

    /**
     * @param charSequence to grab array from.
     * @return char array from char sequence
     */
    public static char[] toCharArray(final CharSequence charSequence) {
        return toCharArray(charSequence.toString());
    }

    /**
     * @param chars to shove array into.
     * @return new string with chars copied into it
     */
    public static String noCopyStringFromChars(final char[] chars) {
        return getService().noCopyStringFromChars(chars);
    }
}
