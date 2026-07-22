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
package org.codehaus.groovy.runtime.dgmimpl;

import org.codehaus.groovy.runtime.typehandling.NumberMath;

public final class NumberNumberMinus extends NumberNumberMetaMethod {
    @Override
    public String getName() {
        return "minus";
    }

    @Override
    public Object invoke(Object object, Object[] arguments) {
        return NumberMath.subtract((Number) object, (Number) arguments[0]);
    }

    /**
     * Subtraction of two Numbers.
     *
     * @param left  a Number
     * @param right another Number to subtract to the first one
     * @return the subtraction
     */
    public static Number minus(Number left, Number right) {
        return NumberMath.subtract(left, right);
    }
}
