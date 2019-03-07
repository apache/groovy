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
package groovy.util;

import java.net.URLConnection;

/**
 * Base interface for customizing where resources can be found for the <code>GroovyScriptEngine</code>.
 */
public interface ResourceConnector {

    /**
     * Retrieve a URLConnection to a script referenced by name.
     *
     * @param name
     * @throws ResourceException
     */
    URLConnection getResourceConnection(String name) throws ResourceException;
}
