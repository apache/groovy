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
package groovy.grape;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Contract for Grape dependency resolution engines.
 */
public interface GrapeEngine {

    /**
     * Argument key for the caller depth.
     */
    String CALLEE_DEPTH = "calleeDepth";

    /**
     * Default caller depth used by the static facade.
     */
    int DEFAULT_CALLEE_DEPTH = 3 ;

    /**
     * Grabs a dependency expressed using the endorsed module shorthand.
     *
     * @param endorsedModule the endorsed module notation
     * @return an engine-specific result
     */
    Object grab(String endorsedModule);

    /**
     * Grabs a single dependency.
     *
     * @param args the dependency descriptor
     * @return an engine-specific result
     */
    Object grab(Map args);

    /**
     * Grabs one or more dependencies using the supplied arguments.
     *
     * @param args grab arguments
     * @param dependencies dependency descriptors
     * @return an engine-specific result
     */
    Object grab(Map args, Map... dependencies);

    /**
     * Enumerates locally available grapes.
     *
     * @return grapes grouped by organization and module
     */
    Map<String, Map<String, List<String>>> enumerateGrapes();

    /**
     * Resolves dependency coordinates to artifact URIs.
     *
     * @param args resolve arguments
     * @param dependencies dependency descriptors
     * @return the resolved artifact URIs
     */
    URI[] resolve(Map args, Map... dependencies);

    /**
     * Resolves dependency coordinates to artifact URIs while optionally collecting dependency information.
     *
     * @param args resolve arguments
     * @param depsInfo optional dependency metadata sink
     * @param dependencies dependency descriptors
     * @return the resolved artifact URIs
     */
    URI[] resolve(Map args, List depsInfo, Map... dependencies);

    /**
     * Lists dependencies associated with the supplied class loader.
     *
     * @param classLoader the class loader to inspect
     * @return the dependency descriptors
     */
    Map[] listDependencies(ClassLoader classLoader);

    /**
     * Adds a resolver to this engine.
     *
     * @param args the resolver descriptor
     */
    void addResolver(Map<String, Object> args);

    /**
     * Sets the logging level for the grape engine.
     * @param level the logging level (0=quiet/errors only, 1=warn, 2=info, 3=verbose, 4=debug)
     */
    default void setLoggingLevel(int level) { }
}

