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
package groovy.operator

import java.awt.Color

class MyColor {
    Color delegate

    MyColor(int r, int g, int b) {
        delegate = new Color(r, g, b)
    }

    MyColor negative() {
        return new MyColor(delegate.red.intdiv(2), delegate.green.intdiv(2), delegate.blue.intdiv(2))
    }

    MyColor positive() {
        return new MyColor(2 * delegate.red - 1, 2 * delegate.green - 1, 2 * delegate.blue - 1)
    }

    String toString() {
        return delegate.toString()
    }
}
