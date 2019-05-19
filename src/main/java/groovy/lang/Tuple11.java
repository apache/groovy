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
 * Represents a list of 10 typed Objects.
 *
 * @since 3.0.0
 */
public final class Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> extends Tuple {
    private static final long serialVersionUID = -8882524414681687282L;
    private final T1 v1;
    private final T2 v2;
    private final T3 v3;
    private final T4 v4;
    private final T5 v5;
    private final T6 v6;
    private final T7 v7;
    private final T8 v8;
    private final T9 v9;
    private final T10 v10;
    private final T11 v11;

    public Tuple11(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11) {
        super(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11);

        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.v5 = v5;
        this.v6 = v6;
        this.v7 = v7;
        this.v8 = v8;
        this.v9 = v9;
        this.v10 = v10;
        this.v11 = v11;
    }

    public Tuple11(Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> tuple) {
        this(tuple.v1, tuple.v2, tuple.v3, tuple.v4, tuple.v5, tuple.v6, tuple.v7, tuple.v8, tuple.v9, tuple.v10, tuple.v11);
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

    public T4 getV4() {
        return v4;
    }

    public T5 getV5() {
        return v5;
    }

    public T6 getV6() {
        return v6;
    }

    public T7 getV7() {
        return v7;
    }

    public T8 getV8() {
        return v8;
    }

    public T9 getV9() {
        return v9;
    }

    public T10 getV10() {
        return v10;
    }

    public T11 getV11() {
        return v11;
    }

    @Override
    public Tuple11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> clone() {
        return new Tuple11<>(this);
    }
}
