package groovy.swing

import javax.swing.JFrame
import javax.swing.JDialog

class SwingBuilderTest extends GroovyTestCase {
    // TODO: add more widgets in here
    void testNamedWidgetCreation() {
        def swing = new SwingBuilder()
        // invoke builder
        swing.frame(id:'myFrameId', title:'This is my Frame')
        swing.dialog(id:'myDialogId', title:'This is my Dialog')
        // extract named widget
        def myFrame = swing.myFrameId
        def myDialog = swing.myDialogId
        // test widget properties
        assert myFrame instanceof JFrame
        assert myFrame.title == 'This is my Frame'
        assert myDialog instanceof JDialog
        assert myDialog.title == 'This is my Dialog'
    }
}