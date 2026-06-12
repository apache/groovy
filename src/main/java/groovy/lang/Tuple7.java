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
 * Represents a list of 7 typed Objects.
 *
 * @since 2.5.0
 */
public final class Tuple7<T1, T2, T3, T4, T5, T6, T7> extends Tuple {
    @Serial private static final long serialVersionUID = 4226144828786865766L;
    private final T1 v1;
    private final T2 v2;
    private final T3 v3;
    private final T4 v4;
    private final T5 v5;
    private final T6 v6;
    private final T7 v7;

    /**
     * Creates a tuple containing the supplied elements.
     *
     * @param v1 the first element
     * @param v2 the second element
     * @param v3 the third element
     * @param v4 the fourth element
     * @param v5 the fifth element
     * @param v6 the sixth element
     * @param v7 the seventh element
     */
    public Tuple7(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7) {
        super(v1, v2, v3, v4, v5, v6, v7);

        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.v5 = v5;
        this.v6 = v6;
        this.v7 = v7;
    }

    /**
     * Creates a tuple from the supplied tuple.
     *
     * @param tuple the source tuple
     */
    public Tuple7(Tuple7<T1, T2, T3, T4, T5, T6, T7> tuple) {
        this(tuple.v1, tuple.v2, tuple.v3, tuple.v4, tuple.v5, tuple.v6, tuple.v7);
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
     * Returns the third element.
     *
     * @return the third element
     */
    @Deprecated
    public T3 getThird() {
        return v3;
    }

    /**
     * Returns the fourth element.
     *
     * @return the fourth element
     */
    @Deprecated
    public T4 getFourth() {
        return v4;
    }

    /**
     * Returns the fifth element.
     *
     * @return the fifth element
     */
    @Deprecated
    public T5 getFifth() {
        return v5;
    }

    /**
     * Returns the sixth element.
     *
     * @return the sixth element
     */
    @Deprecated
    public T6 getSixth() {
        return v6;
    }

    /**
     * Returns the seventh element.
     *
     * @return the seventh element
     */
    @Deprecated
    public T7 getSeventh() {
        return v7;
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
     * Returns the third element.
     *
     * @return the third element
     */
    public T3 getV3() {
        return v3;
    }

    /**
     * Returns the fourth element.
     *
     * @return the fourth element
     */
    public T4 getV4() {
        return v4;
    }

    /**
     * Returns the fifth element.
     *
     * @return the fifth element
     */
    public T5 getV5() {
        return v5;
    }

    /**
     * Returns the sixth element.
     *
     * @return the sixth element
     */
    public T6 getV6() {
        return v6;
    }

    /**
     * Returns the seventh element.
     *
     * @return the seventh element
     */
    public T7 getV7() {
        return v7;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tuple7<T1, T2, T3, T4, T5, T6, T7> clone() {
        return new Tuple7<>(this);
    }
}
