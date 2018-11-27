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

/**
 * Represents a list of 3 typed Objects.
 *
 * @since 2.5.0
 */
public final class Tuple3<T1, T2, T3> extends Tuple {
    private static final long serialVersionUID = 8469774237154310687L;
    private final T1 v1;
    private final T2 v2;
    private final T3 v3;

    public Tuple3(T1 v1, T2 v2, T3 v3) {
        super(v1, v2, v3);

        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public Tuple3(Tuple3<T1, T2, T3> tuple) {
        this(tuple.v1, tuple.v2, tuple.v3);
    }

    @Deprecated
    public T1 getFirst() {
        return v1;
    }

    @Deprecated
    public T2 getSecond() {
        return v2;
    }

    @Deprecated
    public T3 getThird() {
        return v3;
    }

    public T1 getV1() {
        return v1;
    }

    public T2 getV2() {
        return v2;
    }

    public T3 getV3() {
        return v3;
    }

}
