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
import java.util.Set;

/**
 * Keeps track of contract executions to avoid cyclic contract checks.
 */
public class ContractExecutionTracker {

    /**
     * Identifies one contract execution so recursive re-entry can be suppressed.
     */
    public static final class ContractExecution {
        /**
         * Fully qualified name of the declaring class.
         */
        final String className;

        /**
         * Method descriptor or constructor descriptor identifying the executable.
         */
        final String methodIdentifier;

        /**
         * Logical contract kind such as precondition, postcondition, or invariant.
         */
        final String assertionType;

        /**
         * Whether the tracked executable is static.
         */
        final boolean isStatic;

        /**
         * Creates a contract execution identifier.
         *
         * @param className the declaring class name
         * @param methodIdentifier the executable identifier
         * @param assertionType the logical contract kind
         * @param isStatic whether the executable is static
         */
        public ContractExecution(String className, String methodIdentifier, String assertionType, boolean isStatic) {
            this.className = className;
            this.methodIdentifier = methodIdentifier;
            this.assertionType = assertionType;
            this.isStatic = isStatic;
        }

        /**
         * Compares two execution identifiers for logical equality.
         *
         * @param o the object to compare against
         * @return {@code true} if both identifiers describe the same execution
         */
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

        /**
         * Returns the hash code for this execution identifier.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            int result = className != null ? className.hashCode() : 0;
            result = 31 * result + (methodIdentifier != null ? methodIdentifier.hashCode() : 0);
            result = 31 * result + (assertionType != null ? assertionType.hashCode() : 0);
            result = 31 * result + (isStatic ? 1 : 0);
            return result;
        }
    }


    /**
     * Thread-local holder for the active contract executions on the current thread.
     */
    static class ContractExecutionThreadLocal extends ThreadLocal<Set<ContractExecution>> {

        /**
         * Creates the per-thread set used to track active contract executions.
         *
         * @return a fresh mutable tracking set
         */
        @Override
        protected Set<ContractExecution> initialValue() {
            return new HashSet<>();
        }
    }

    private static final ThreadLocal<Set<ContractExecution>> executions = new ContractExecutionThreadLocal();

    /**
     * Attempts to register a contract execution for the current thread.
     *
     * @param className the declaring class name
     * @param methodIdentifier the executable identifier
     * @param assertionType the logical contract kind
     * @param isStatic whether the executable is static
     * @return {@code true} if the execution was newly tracked, {@code false} if it was already active
     */
    public static boolean track(String className, String methodIdentifier, String assertionType, boolean isStatic) {
        final ContractExecution ce = new ContractExecution(className, methodIdentifier, assertionType, isStatic);
        final Set<ContractExecution> contractExecutions = executions.get();

        if (!contractExecutions.contains(ce)) {
            contractExecutions.add(ce);
            return true;
        }

        return false;
    }

    /**
     * Removes a previously tracked contract execution from the current thread.
     *
     * @param className the declaring class name
     * @param methodIdentifier the executable identifier
     * @param assertionType the logical contract kind
     * @param isStatic whether the executable is static
     */
    public static void clear(String className, String methodIdentifier, String assertionType, boolean isStatic) {
        final Set<ContractExecution> contractExecutions = executions.get();

        contractExecutions.remove(new ContractExecution(className, methodIdentifier, assertionType, isStatic));
    }
}
