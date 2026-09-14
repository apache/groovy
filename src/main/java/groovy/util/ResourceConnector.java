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

import java.net.URI;
import java.net.URLConnection;

/**
 * Base interface for customizing where resources can be found for the <code>GroovyScriptEngine</code>.
 */
public interface ResourceConnector {

    /**
     * Retrieve a URLConnection to a script referenced by name.
     * <p>
     * Two shapes of name are passed. Most lookups use a resource name relative to
     * whatever roots the connector searches, with package separators written as
     * {@code '/'} on every platform, for example {@code com/example/Foo.groovy}.
     * Staleness checks and dependency resolution during recompilation instead pass
     * back a name the connector itself resolved earlier, in the external form of
     * that resource's URL, for example {@code file:/srv/scripts/com/example/Foo.groovy}.
     * An implementation that resolves names against a base should therefore cope
     * with an already-absolute name; {@link URI#resolve(String)}, which
     * {@link GroovyScriptEngine} itself uses, has that behaviour.
     * <p>
     * The caller may close the returned connection once it has what it needs,
     * which for some call sites is only {@link URLConnection#getURL()}.
     *
     * @param name the resource to resolve, as described above
     * @return an open connection to the resource, never {@code null}
     * @throws ResourceException if the resource cannot be found or opened
     */
    URLConnection getResourceConnection(String name) throws ResourceException;
}
