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

public class ReferenceBundle{
    private final ReferenceManager manager;
    private final ReferenceType type;
    public ReferenceBundle(ReferenceManager manager, ReferenceType type){
        this.manager = manager;
        this.type = type;
    }
    public ReferenceType getType() {
        return type;
    }
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

    public static ReferenceBundle getSoftBundle() {
        return softReferences;
    }

    public static ReferenceBundle getWeakBundle() {
        return weakReferences;
    }

    public static ReferenceBundle getHardBundle() {
        return hardReferences;
    }

    public static ReferenceBundle getPhantomBundle() {
        return phantomReferences;
    }
}
