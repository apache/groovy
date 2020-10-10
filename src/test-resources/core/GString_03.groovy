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
def a = """hello${a}
world
"""

a = """
hello
${a}
world
"""

a =~ $/(${123}hello) \/
            ${a}world\1
 \u9fa5 \r
/$
a =~ $/\
    x  $
    $$
    $/
/$
a =~ $/\
    $x  $
    $$
    $/
/$
a = $/
            Hello name,
            today we're date.

            $ dollar sign
            $$ escaped dollar sign
            \ backslash
            / forward slash
            $/ escaped forward slash
            $/$ escaped dollar slashy string delimiter
        /$

a = $/
            Hello $name,
            today we're ${date}.

            $ dollar sign
            $$ escaped dollar sign
            \ backslash
            / forward slash
            $/ escaped forward slash
            $/$ escaped dollar slashy string delimiter
        /$
a = $/$$VAR/$
a = $/$$ $VAR/$
