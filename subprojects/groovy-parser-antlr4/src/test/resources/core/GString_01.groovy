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
"abc${'123'}def"
"${'123'}def"
"${'123'}"
"a:$a"
"a:$a.b.c"
"$a.b.c"
"$a.b.c,d"
"${12}a${}c${34}"
"${->12}"
"${12}${->12}${'12'}"
"""abc${'123'}def"""
"""${'123'}def"""
"""${'123'}"""
"""a:$a"""
"""a:$a.b.c"""
"""$a.b.c"""
"""$a.b.c,d"""
"""${12}a${}c${34}"""
"""${->12}"""
"""${12}${->12}${'12'}"""
"""${12}${->12}${'12'}${a=1;return a;}"""


"${}"
"${;}"
"${;;}"
"${;;;}"

"${a b}"
"${obj.a b}"