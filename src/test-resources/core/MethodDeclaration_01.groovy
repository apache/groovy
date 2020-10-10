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
int plus(int a, int b) {
        return a + b;
}

int plus2(int a,
          int b)
{
        return a + b;
}

int plus3(int a,
          int b)
throws
        Exception1,
        Exception2
{
        return a + b;
}

def <T> T someMethod() {}
def <T extends List> T someMethod2() {}
def <T extends A & B> T someMethod3() {}

static m(a) {}
static m2(a, b) {}
static m3(a, b, c) {}
static Object m4(a, b, c) {}

private String relativePath() { '' }
def foo() {}


