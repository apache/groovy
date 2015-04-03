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
println """
<html>
    <head>
        <title>Groovy Servlet Example - hello</title>
    </head>
    <body>
    <a href="../"><img src="../images/return.gif" width="24" height="24" border="0"></a><a href="../">Return</a>
    <p>
"""

session = request.getSession(true);

if (session.counter == null) {
  session.counter = 1
}


println """Hello, ${request.remoteHost}! ${new java.util.Date()}"""

println """
<dl>
 <dt><b>requestURI</b></dt><dd>${request.requestURI}</dd>
 <dt><b>servletPath</b></dt><dd>${request.servletPath}</dd>
 <dt><b>session.counter</b></dt><dd>${session.counter}</dd>
</dl>
"""

println """
    </body>
</html>
"""

session.counter = session.counter + 1
