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
'123'
'abc'
'a1b2c3'
'a\tb\tc'
'a\nb\r\nc'
'$a$b$c'
'$1$2$3'
'$1$2\$3'
'\$1\$2\$3\
  hello world\
'
"\$1\$2\$3\
  hello world\
"
' nested "double quotes" '
" nested 'quotes' "
' \6 1 digit is escaped'
' \665 2 digits are escaped, \'5\' is a character.'
' \3666 3 digits are escaped'
' \166 '
" \166 "
' \u1234 '

'''abc'''
'''123'''
'''
            ''hello world''
            'hello'
            ''world'
            'hi''
            \
            \t\r\n
            $\$
            \u1234
            \123
'''

"""
            ''hello world''
            'hello'
            ''world'
            'hi''
            \
            \t\r\n
            \$
            \u1234
            \123
"""
