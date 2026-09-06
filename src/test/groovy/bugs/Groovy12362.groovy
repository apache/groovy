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

import com.oracle.svm.core.methodhandles.FakeGraalFrame
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * Frames of GraalVM's native-image dispatch machinery ({@code com.oracle.svm.*})
 * are runtime frames, not callers, and must be skipped by Groovy's caller
 * lookup. {@code FakeGraalFrame} only borrows the package name. This test
 * lives outside the MOP packages that the lookup ignores, so its own frames
 * are observable.
 */
final class Groovy12362 {

    @Test
    void testGetCallingClassIgnoresGraalVMDispatchFrames() {
        // chain: getCallingClass <- FakeGraalFrame (ignored) <- Relay <- this test;
        // the immediate caller ignoring runtime frames is Relay's caller: this class
        assertEquals(Groovy12362, Relay.callerSeenThroughGraalFrame())
    }

    static class Relay {
        static Class callerSeenThroughGraalFrame() {
            FakeGraalFrame.callerOf()
        }
    }
}
