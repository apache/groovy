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
package groovy.lang;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Allows frameworks that integrate with Groovy to determine how Groovy files are resolved.
 */
public interface GroovyResourceLoader {

    /**
     * Loads a Groovy source file given its name.
     *
     * @param filename name of the file
     * @return a URL
     * @throws java.net.MalformedURLException if the URL is invalid
     */
    URL loadGroovySource(String filename) throws MalformedURLException;
}
