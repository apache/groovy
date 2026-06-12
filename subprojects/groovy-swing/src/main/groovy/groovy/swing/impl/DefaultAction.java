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
package groovy.swing.impl;

import groovy.lang.Closure;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An {@link Action} implementation that delegates execution to a Groovy closure.
 */
public class DefaultAction extends AbstractAction {

    private Closure closure;

    /**
     * Invokes the configured closure in response to the action event.
     *
     * @param event the Swing action event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (closure == null) {
            throw new NullPointerException("No closure has been configured for this Action");
        }
        closure.call(event);
    }

    /**
     * Returns the closure invoked when the action fires.
     *
     * @return the action closure, or {@code null}
     */
    public Closure getClosure() {
        return closure;
    }

    /**
     * Sets the closure invoked when the action fires.
     *
     * @param closure the action closure
     */
    public void setClosure(Closure closure) {
        this.closure = closure;
    }

}
