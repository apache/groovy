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
package org.apache.groovy.osgi.provider;

import org.apache.groovy.json.DefaultFastStringService;
import org.apache.groovy.json.FastStringService;
import org.apache.groovy.json.FastStringServiceFactory;

/**
 * A {@link FastStringServiceFactory} that records use of its service.
 * <p>
 * Never loaded on the test classpath: the tests copy these class files into a
 * generated fragment of the {@code groovy} bundle, so the classes are defined by
 * the host bundle's class loader inside the framework.
 */
public class RecordingFastStringServiceFactory implements FastStringServiceFactory {

    /** Number of calls made to the recording service. */
    public static volatile int calls;

    @Override
    public FastStringService getService() {
        return new RecordingFastStringService();
    }

    public static class RecordingFastStringService implements FastStringService {
        private final DefaultFastStringService delegate = new DefaultFastStringService();

        @Override
        public char[] toCharArray(String string) {
            calls++;
            return delegate.toCharArray(string);
        }

        @Override
        public String noCopyStringFromChars(char[] chars) {
            calls++;
            return delegate.noCopyStringFromChars(chars);
        }
    }
}
