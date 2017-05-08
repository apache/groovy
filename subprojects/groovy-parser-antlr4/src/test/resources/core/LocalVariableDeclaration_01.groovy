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
import groovy.transform.Field

int a;
int b = 1;
int c =
        1;
final d = 1;
@Test2 e = 1;
@Test2 final f = 1;
final
@Test2 g = 1;

int h, i = 1;
int j,
        k =
                1;
int l =
        2,
    m =
            1;
int n =
        1
int o =
        2,
    p =
            1

List list = [1, 2 + 6, [1, 2 + 3]]
List list2 =
        [
                1,
         2 +
                6,
                [1,
                 2 +
                         3]
        ]

def (int x, int y) = [1, 2]

@Test2
def
        (int q,
                int r) =
                        [1, 2]

def (int s, int t) = otherTuple

def (int w, z) = [1, 2]
def (a2, int b2) = [1, 2]

def (u, v) = [1, 2]

def (int c2, String d2, java.lang.Double e2) = [1, '2', 3.3D]

def cc = {
        String bb = 'Test'
        return bb;
}

int xx = b c d e

@Field static List list = [1, 2, 3]

if (false)
        def a = 5

if(false)
        def a, b = 10

if(false)
        def a = 9, b = 10

if (false)
        def a = 5
else
        def b = 2

if(false)
        def a, b = 10
else
        def a, b = 8

if(false)
        def a = 9, b = 10
else
        def a = 6, b = 8

while(false)
        def a = 5

while(false)
        def a, b = 10

while(false)
        def a = 9, b = 10

for(;;)
        def a = 5

for(;;)
        def a, b = 10

for(;;)
        def a = 9, b = 10



Class<String>[] c
Class<?>[] c2