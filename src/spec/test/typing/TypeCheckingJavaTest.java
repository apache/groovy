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
package typing;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TypeCheckingJavaTest {
    @Test
    public void testJavaMethodSelection() {
        // tag::java_method_selection_body[]
        // ...
        Object string = "Some string";          // <1>
        Object result = compute(string);        // <2>
        System.out.println(result);             // <3>
        // end::java_method_selection_body[]
        assertEquals("Nope", result);
    }

    // tag::java_method_selection_head[]
    public Integer compute(String str) {
        return str.length();
    }
    public String compute(Object o) {
        return "Nope";
    }
    // end::java_method_selection_head[]
}
