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

/**
 * Represents a list of 2 typed Objects.
 */
public final class Tuple2<T1, T2> extends Tuple {
    @Serial private static final long serialVersionUID = 9006144674906325597L;
    private final T1 v1;
    private final T2 v2;

    /**
     * Creates a tuple containing the supplied elements.
     *
     * @param v1 the first element
     * @param v2 the second element
     */
    public Tuple2(T1 v1, T2 v2) {
        super(v1, v2);

        this.v1 = v1;
        this.v2 = v2;
    }

    /**
     * Creates a tuple from the supplied tuple.
     *
     * @param tuple the source tuple
     */
    public Tuple2(Tuple2<T1, T2> tuple) {
        this(tuple.v1, tuple.v2);
    }

    /**
     * Returns the first element.
     *
     * @return the first element
     */
    @Deprecated
    public T1 getFirst() {
        return v1;
    }

    /**
     * Returns the second element.
     *
     * @return the second element
     */
    @Deprecated
    public T2 getSecond() {
        return v2;
    }

    /**
     * Returns the first element.
     *
     * @return the first element
     */
    public T1 getV1() {
        return v1;
    }

    /**
     * Returns the second element.
     *
     * @return the second element
     */
    public T2 getV2() {
        return v2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tuple2<T1, T2> clone() {
        return new Tuple2<>(this);
    }
}
