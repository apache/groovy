import javax.swing.JButton
import javax.swing.JPanel

/**
 * @version $Revision$
 */
class PropertyBug extends GroovyTestCase {
     
    void testBug() {
        panel = new JPanel()
        bean = new JButton()
        
        panel.add(bean)
        
        value = bean.parent
        assert value != null
    }
}