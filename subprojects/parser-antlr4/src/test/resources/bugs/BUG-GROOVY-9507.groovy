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

package groovy.tmp.templates;def getTemplate() { return { _p, _s, _b, out -> int _i = 0;try {delegate = new Binding(_b);out<<_s[_i=0];           items.each { ;out<<_s[_i=1];out<<"""${it}""";out<<_s[_i=2];           } ;out<<_s[_i=3];} catch (Throwable e) { _p.error(_i, _s, e);}}.asWritable()}