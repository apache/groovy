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