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
package org.codehaus.groovy.util;

import java.lang.ref.ReferenceQueue;

/**
 * Couples a {@link ReferenceType} with the {@link ReferenceManager} responsible for processing it.
 */
public class ReferenceBundle{
    private final ReferenceManager manager;
    private final ReferenceType type;

    /**
     * Creates a reference bundle for the supplied manager and reference type.
     *
     * @param manager the manager that processes collected references
     * @param type the reference kind to create
     */
    public ReferenceBundle(ReferenceManager manager, ReferenceType type){
        this.manager = manager;
        this.type = type;
    }

    /**
     * Returns the reference kind represented by this bundle.
     *
     * @return the configured reference type
     */
    public ReferenceType getType() {
        return type;
    }

    /**
     * Returns the manager associated with this bundle.
     *
     * @return the reference manager
     */
    public ReferenceManager getManager() {
        return manager;
    }      

    private static final ReferenceBundle softReferences, weakReferences, hardReferences, phantomReferences;
    static {
        ReferenceQueue queue = new ReferenceQueue();
        ReferenceManager callBack = ReferenceManager.createCallBackedManager(queue);
        ReferenceManager manager  = ReferenceManager.createThresholdedIdlingManager(queue, callBack, 500);
        softReferences = new ReferenceBundle(manager, ReferenceType.SOFT);
        weakReferences = new ReferenceBundle(manager, ReferenceType.WEAK);
        phantomReferences = new ReferenceBundle(manager, ReferenceType.PHANTOM);
        hardReferences = new ReferenceBundle(ReferenceManager.createIdlingManager(null), ReferenceType.HARD);
    }

    /**
     * Returns the shared soft-reference bundle.
     *
     * @return the shared soft-reference bundle
     */
    public static ReferenceBundle getSoftBundle() {
        return softReferences;
    }

    /**
     * Returns the shared weak-reference bundle.
     *
     * @return the shared weak-reference bundle
     */
    public static ReferenceBundle getWeakBundle() {
        return weakReferences;
    }

    /**
     * Returns the shared hard-reference bundle.
     *
     * @return the shared hard-reference bundle
     */
    public static ReferenceBundle getHardBundle() {
        return hardReferences;
    }

    /**
     * Returns the shared phantom-reference bundle.
     *
     * @return the shared phantom-reference bundle
     */
    public static ReferenceBundle getPhantomBundle() {
        return phantomReferences;
    }
}
