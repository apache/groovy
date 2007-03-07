package groovy.swing

import javax.swing.JFrame

class SwingBuilderTest extends GroovyTestCase {
    void testNamedWidgetCreation() {
        def swing = new SwingBuilder()
        // invoke builder
        swing.frame(id:'myFrameId', title:'This is my Frame')
        // extract named widget
        def myFrame = swing.myFrameId
        // test widget properties
        assert myFrame instanceof JFrame
        assert myFrame.title == 'This is my Frame'
    }
}