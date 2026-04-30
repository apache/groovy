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
package groovy.util;

import groovy.lang.Closure;

import java.util.Map;

/**
 * Base {@link Factory} implementation with default no-op lifecycle hooks.
 */
public abstract class AbstractFactory implements Factory {
    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHandlesNodeChildren() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void onFactoryRegistration(FactoryBuilderSupport builder, String registeredName, String group) {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder, Object node,
                                          Map attributes ) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeCompleted(FactoryBuilderSupport builder, Object parent, Object node ) {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(FactoryBuilderSupport builder, Object parent, Object child ) {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setChild(FactoryBuilderSupport builder, Object parent, Object child ) {
        // do nothing
    }

}
