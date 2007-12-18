package groovy.ui.view

import javax.swing.JComponent
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import org.codehaus.groovy.runtime.InvokerHelper

build(Defaults)

// change fonts for vista
if (System.properties['os.version'] =~ /6\./) {
    // Vista/Server 2008 or later
    styles.regular[StyleConstants.FontFamily] = 'Consolas'
    styles[StyleContext.DEFAULT_STYLE][StyleConstants.FontFamily] = 'Consolas'

    // in JDK 1.5 we need to turn on anti-aliasing so consolas looks better
    if (System.properties['java.version'] =~ /^1\.5/) {
        key = InvokerHelper.getProperty('com.sun.java.swing.SwingUtilities2' as Class,
            'AA_TEXT_PROPERTY_KEY')
        addAttributeDelegate {builder, node, attributes ->
            if (node instanceof JComponent) {
                node.putClientProperty(key, new Boolean(true));
            }
        }
    }
}
