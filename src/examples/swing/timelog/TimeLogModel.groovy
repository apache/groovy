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
package groovy.swing.timelog

import groovy.beans.Bindable

class TimeLogRow {
    String client
    long start
    long stop

    long getDuration() {
        return stop - start
    }                                          
}

class TimeLogModel {

    String currentClient
    long currentStart
    List<TimeLogRow> entries = []

    @Bindable boolean running
    @Bindable long elapsedTime

    public synchronized startRecording(String client) {
        if (running) throw new RuntimeException("Currently Running")
        currentClient = client
        currentStart = System.currentTimeMillis()
        setRunning(true)

        while (running) {
            setElapsedTime(System.currentTimeMillis() - currentStart)
            this.wait(1000)
        }
    }

    public synchronized stopRecording() {
        if (!running) throw new RuntimeException("Not Running")
        setRunning(false)
        this.notifyAll()
        entries.add(new TimeLogRow(client:currentClient, start:currentStart, stop:System.currentTimeMillis()))
    }


}