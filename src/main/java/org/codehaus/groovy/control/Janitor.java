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

import java.util.HashSet;
import java.util.Set;

/**
 * An agent that can be used to defer cleanup operations to
 * a later time.  Users much implement the HasCleanup interface.
 */
public class Janitor implements HasCleanup {
    private final Set pending = new HashSet();   // All objects pending cleanup

    public void register(HasCleanup object) {
        pending.add(object);
    }

    public void cleanup() {
        for (Object o : pending) {
            HasCleanup object = (HasCleanup) o;

            try {
                object.cleanup();
            } catch (Exception e) {
                // Ignore
            }
        }

        pending.clear();
    }
}
