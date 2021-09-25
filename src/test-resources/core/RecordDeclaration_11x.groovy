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
package core

@groovy.transform.CompileStatic
record Point(int x, int y, String color) {
    public Point {
        x = -x;
        Objects.requireNonNull(color);
        color = color.toUpperCase();
    }

    public Point(int x, int y) {
        this(x, y, "Blue");
    }
}

def p1 = new Point(5, 10, "Green")
assert -5 == p1.x()
assert 10 == p1.y()
assert 'GREEN' == p1.color()

def p2 = new Point(0, 20)
assert 0 == p2.x()
assert 20 == p2.y()
assert 'BLUE' == p2.color()
