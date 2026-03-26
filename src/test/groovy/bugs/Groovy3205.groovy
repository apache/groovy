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
package bugs

import org.junit.jupiter.api.Test

final class Groovy3205 {

    @Test
    void testOverrideToStringInMapOfClosures() {
        def proxyImpl = [
                control: { 'new control' },
                toString: { 'new toString' }
        ] as IGroovy3205

        assert proxyImpl.control() == 'new control'
        assert proxyImpl.toString() == 'new toString'
    }

    static class IGroovy3205 {
        String control() { 'original control' }
        String toString() { 'original toString' }
    }
}
