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
package groovy.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.codehaus.groovy.runtime.InvokerHelper;

public class SpoofTask extends Task {
    private int foo;

    public SpoofTask() {
        super();
        SpoofTaskContainer.spoof("SpoofTask ctor");
    }

    public void setFoo(final int i) {
        foo = i;
    }


    public void execute() throws BuildException {
        SpoofTaskContainer.spoof("begin SpoofTask execute");
        SpoofTaskContainer.spoof("tag name from wrapper: " + getWrapper().getElementTag());
        // don't rely on Map.toString(), behaviour is not documented
        SpoofTaskContainer.spoof("attributes map from wrapper: "
                + InvokerHelper.toMapString(getWrapper().getAttributeMap()));
        SpoofTaskContainer.spoof("param foo: " + foo);

        SpoofTaskContainer.spoof("end SpoofTask execute");
    }

}
