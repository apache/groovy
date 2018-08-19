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
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;

import java.util.ArrayList;
import java.util.List;

public class SpoofTaskContainer extends Task implements TaskContainer {
    private List<Task> tasks = new ArrayList<Task>();
    static StringBuffer spoof = new StringBuffer();

    public SpoofTaskContainer() {
        super();
        spoof("SpoofTaskContainer ctor");
    }

    static StringBuffer getSpoof() {
        return spoof;
    }

    static void resetSpoof() {
        spoof = new StringBuffer();
    }

    static void spoof(String message) {
        spoof.append(message);
        spoof.append("\n");
    }

    public void addTask(Task task) {
        // to work with ant 1.6
        spoof("in addTask");
        if (task instanceof UnknownElement) {
            spoof("configuring UnknownElement");
            task.maybeConfigure();
            task = ((UnknownElement) task).getTask();
        }
        tasks.add(task);
    }

    public void execute() throws BuildException {
        spoof("begin SpoofTaskContainer execute");
        for (Object task1 : tasks) {
            Task task = (Task) task1;
            task.perform();
        }
        spoof("end SpoofTaskContainer execute");
    }

}
