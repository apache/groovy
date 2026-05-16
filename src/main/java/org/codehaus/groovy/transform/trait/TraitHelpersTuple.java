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
package org.codehaus.groovy.transform.trait;

import org.codehaus.groovy.ast.ClassNode;

/**
 * A class meant to hold reference to the helper and field helper of a trait.
 *
 * @since 2.3.0
 */
class TraitHelpersTuple {
    private final ClassNode helper;
    private final ClassNode fieldHelper;
    private final ClassNode staticFieldHelper;

    /**
     * Creates a tuple without a static field helper.
     *
     * @param helper the generated trait helper class
     * @param fieldHelper the generated instance field helper class
     */
    public TraitHelpersTuple(final ClassNode helper, final ClassNode fieldHelper) {
        this(helper, fieldHelper, null);
    }

    /**
     * Creates a tuple of helper classes associated with a trait.
     *
     * @param helper the generated trait helper class
     * @param fieldHelper the generated instance field helper class
     * @param staticFieldHelper the generated static field helper class
     */
    public TraitHelpersTuple(final ClassNode helper, final ClassNode fieldHelper, final ClassNode staticFieldHelper) {
        this.helper = helper;
        this.fieldHelper = fieldHelper;
        this.staticFieldHelper = staticFieldHelper;
    }

    /**
     * Returns the generated trait helper class.
     *
     * @return the helper class node
     */
    public ClassNode getHelper() {
        return helper;
    }

    /**
     * Returns the generated instance field helper class.
     *
     * @return the field helper class node
     */
    public ClassNode getFieldHelper() {
        return fieldHelper;
    }

    /**
     * Returns the generated static field helper class.
     *
     * @return the static field helper class node, or {@code null} when none is needed
     *
     * @since 2.5.1
     */
    public ClassNode getStaticFieldHelper() {
        return staticFieldHelper;
    }
}
