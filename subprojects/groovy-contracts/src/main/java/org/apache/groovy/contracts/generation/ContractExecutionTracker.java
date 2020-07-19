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
package org.apache.groovy.contracts.generation;

import java.util.HashSet;
import java.util.Objects;

/**
 * Keeps track of contract executions to avoid cyclic contract checks.
 */
public class ContractExecutionTracker {

    public static final class ContractExecution {
        final String className;
        final String methodIdentifier;
        final String assertionType;
        final boolean isStatic;

        public ContractExecution(String className, String methodIdentifier, String assertionType, boolean isStatic) {
            this.className = className;
            this.methodIdentifier = methodIdentifier;
            this.assertionType = assertionType;
            this.isStatic = isStatic;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ContractExecution that = (ContractExecution) o;

            if (isStatic != that.isStatic)
                return false;
            if (!Objects.equals(assertionType, that.assertionType))
                return false;
            if (!Objects.equals(className, that.className))
                return false;
            if (!Objects.equals(methodIdentifier, that.methodIdentifier))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = className != null ? className.hashCode() : 0;
            result = 31 * result + (methodIdentifier != null ? methodIdentifier.hashCode() : 0);
            result = 31 * result + (assertionType != null ? assertionType.hashCode() : 0);
            result = 31 * result + (isStatic ? 1 : 0);
            return result;
        }
    }


    static class ContractExecutionThreadLocal extends ThreadLocal<HashSet<ContractExecution>> {

        @Override
        protected HashSet<ContractExecution> initialValue() {
            return new HashSet<ContractExecution>();
        }
    }

    private static ThreadLocal<HashSet<ContractExecution>> executions = new ContractExecutionThreadLocal();

    public static boolean track(String className, String methodIdentifier, String assertionType, boolean isStatic) {
        final ContractExecution ce = new ContractExecution(className, methodIdentifier, assertionType, isStatic);
        final HashSet<ContractExecution> contractExecutions = executions.get();

        if (!contractExecutions.contains(ce)) {
            contractExecutions.add(ce);
            return true;
        }

        return false;
    }

    public static void clear(String className, String methodIdentifier, String assertionType, boolean isStatic) {
        final HashSet<ContractExecution> contractExecutions = executions.get();

        contractExecutions.remove(new ContractExecution(className, methodIdentifier, assertionType, isStatic));
    }
}
