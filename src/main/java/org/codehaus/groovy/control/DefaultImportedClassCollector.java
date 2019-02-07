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
package org.codehaus.groovy.control;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Collect the default imported classes
 *
 * @since 3.0.0
 */
class DefaultImportedClassCollector {
    private final Map<String, Set<String>> classNameToPackageMap;
    public static final String[] DEFAULT_IMPORTS = DefaultImportedClassCollectorHelper.DEFAULT_IMPORTS;
    public static final DefaultImportedClassCollector INSTANCE = new DefaultImportedClassCollector();

    private DefaultImportedClassCollector() {
        classNameToPackageMap = collect();
    }

    public Map<String, Set<String>> getClassNameToPackageMap() {
        return classNameToPackageMap;
    }

    private Map<String, Set<String>> collect() {
        try (ScanResult scanResult =
                     new ClassGraph()
                             .enableSystemJarsAndModules()
                             .whitelistLibOrExtJars()
                             .whitelistPackagesNonRecursive(Arrays.stream(DEFAULT_IMPORTS).map(e -> e.substring(0, e.length() - 1)).toArray(String[]::new))
                             .scan()) {

            ClassInfoList classInfoList = scanResult.getAllClasses();
            Map<String, Set<String>> classNameToPackageMap = new LinkedHashMap<>(classInfoList.size());

            classNameToPackageMap.put("Object", new HashSet<>(Collections.singletonList("java.lang.")));
            for (ClassInfo classInfo : classInfoList) {
                if (classInfo.isAnonymousInnerClass()) continue;

                String className = classInfo.getSimpleName();

                Set<String> packageNameSet = classNameToPackageMap.computeIfAbsent(className, e -> new HashSet<>(2));

                String packageName = classInfo.getPackageName() + ".";
                packageNameSet.add(packageName);
            }

            return Collections.unmodifiableMap(classNameToPackageMap);
        }
    }
}
