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
 * Represents a list of 4 typed Objects.
 *
 * @since 2.5.0
 */
public class Tuple4<T1, T2, T3, T4> extends Tuple {
    private static final long serialVersionUID = -7788878731471377207L;
    private final T1 first;
    private final T2 second;
    private final T3 third;
    private final T4 fourth;

    public Tuple4(T1 first, T2 second, T3 third, T4 fourth) {
        super(first, second, third, fourth);

        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    @Override
    public Object get(int index) {
        switch (index) {
            case 0:
                return first;
            case 1:
                return second;
            case 2:
                return third;
            case 3:
                return fourth;
            default:
                throw new IndexOutOfBoundsException("index: " + index);
        }
    }

    @Override
    public int size() {
        return 4;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    public T3 getThird() {
        return third;
    }

    public T4 getFourth() {
        return fourth;
    }
}
