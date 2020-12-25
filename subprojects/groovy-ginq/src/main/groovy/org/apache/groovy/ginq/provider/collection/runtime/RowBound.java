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
package org.apache.groovy.ginq.provider.collection.runtime;

/**
 * Represents row bounds of window frame
 *
 * @since 4.0.0
 */
public class RowBound extends AbstractBound<Long, Long> {
    private static final long serialVersionUID = 5774234311203836615L;
    public static final RowBound DEFAULT = new RowBound(Long.MIN_VALUE, Long.MAX_VALUE);

    /**
     * Construct a new RowBound instance with lower and upper frame bounds
     *
     * @param lower the lower frame bound
     * @param upper the upper frame bound
     * @since 4.0.0
     */
    public RowBound(Long lower, Long upper) {
        super(lower, upper);
    }
}
