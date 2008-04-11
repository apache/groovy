package swing.timelog

import groovy.swing.SwingBuilder

SwingBuilder swing = new SwingBuilder();
swing.lookAndFeel('system')

swing.model = new TimeLogModel()

swing.actions() {
    action(name:'Start', id:'startAction') {
        stopButton.requestFocusInWindow()
        doOutside {
            model.startRecording(tfClient.text)
        }
    }
    action(name:'Stop', id:'stopAction') {
        doOutside {
            model.stopRecording();
            clientsTable.revalidate()
        }
    }
}

frame = swing.build(TimeLogView)
frame.pack()
frame.show()

