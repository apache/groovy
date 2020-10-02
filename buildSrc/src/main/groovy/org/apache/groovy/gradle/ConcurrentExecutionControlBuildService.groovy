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
