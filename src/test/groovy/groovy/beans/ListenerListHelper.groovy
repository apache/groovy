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
package groovy.beans


/**
 * These test event is used as a sample event.
 */
class TestEvent {
    def source
    String message

    TestEvent(def source, String message) {
        this.source = source
        this.message = message
    }
}

class SomeOtherTestEvent {
    def source
    String message

    SomeOtherTestEvent(def source, String message) {
        this.source = source
        this.message = message
    }
}

/**
 * These interfaces are all used as variations on producing listener lists.
 */
interface TestListener {
    void eventOccurred(TestEvent event)
}

interface SomeOtherTestListener {
    void event2Occurred(SomeOtherTestEvent event)
}

interface TestObjectListener {
  void eventOccurred(Object event)
}

interface TestListListener {
  void eventOccurred(List<? extends Object> event)
}

interface TestMapListener {
  void eventOccurred(Map event)
}

interface TestTwoMethodListener {
  void eventOccurred1(TestEvent event)
  void eventOccurred2(TestEvent event)
}


