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
package groovy.lang;

import java.io.Serial;
import java.util.List;

/**
 * Represents a sequence of one or more objects of a given type.
 * The type can be omitted in which case any type of object can be added.
 */
public class NonEmptySequence extends Sequence {

    @Serial private static final long serialVersionUID = 1614604919062836998L;

    /**
     * Creates a non-empty sequence with no type restriction.
     */
    public NonEmptySequence() {
        super(null);
    }

    /**
     * Creates a non-empty sequence constrained to the supplied type.
     *
     * @param type the allowed element type
     */
    public NonEmptySequence(Class type) {
        super(type);
    }

    /**
     * Creates a non-empty sequence initialized with the supplied content.
     *
     * @param type the allowed element type
     * @param content the initial content
     */
    public NonEmptySequence(Class type, List content) {
        super(type, content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int minimumSize() {
        return 1;
    }
}
