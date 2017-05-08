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
List list = new ArrayList();
List list2 = new java.util.ArrayList();
List<String> list3 = new ArrayList<String>();
List<String> list4 = new java.util.ArrayList<String>();
List<String> list5 = new ArrayList<>();
//List<String> list6 = new java.util.ArrayList<>(); // the old parser can not parse "new java.util.ArrayList<>()"
def x = new A<EE, TT>();
int[] a = new int[10];
int[][] b = new int[length()][2 * 8];
ArrayList[] c = new ArrayList[10];
ArrayList[][] cc = new ArrayList[10][size()];
java.util.ArrayList[] d = new java.util.ArrayList[10];
ArrayList[] e = new ArrayList<String>[10];
java.util.ArrayList[] f = new java.util.ArrayList<String>[10];
java.util.ArrayList[] g = new java.util.ArrayList<String>[size()];

int[][] h = new int[10][];
int[][][] i = new int[10][][];
ArrayList[][] j = new ArrayList[10][];
ArrayList[][] k = new ArrayList<String>[10][];

def bb = new A.B();
def bb2 = new A.B[0];

new
    A
        ('x', 'y');


new a();
new $a();
new as.def.in.trait.a();
