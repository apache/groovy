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
package org.apache.groovy.gradle

import groovy.transform.CompileStatic
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

@CompileStatic
abstract class ConcurrentExecutionControlBuildService implements BuildService<Params> {
    interface Params extends BuildServiceParameters {

    }

    static Provider<ConcurrentExecutionControlBuildService> restrict(Class<?> clazz, Gradle gradle, int maxConcurrentTasks = 1) {
        gradle.sharedServices.registerIfAbsent("maxConcurrent${clazz.name}", ConcurrentExecutionControlBuildService) {
            it.maxParallelUsages.set(maxConcurrentTasks)
        }
    }
}
