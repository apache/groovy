/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Oct 19, 2007
 * Time: 1:03:22 PM
 * To change this template use File | Settings | File Templates.
 */
package groovy.swing.factory

import javax.swing.BorderFactory
import java.awt.Color

/**
 * accepts no value
 * accepts attributes:<br />
 * none <br />
 * highlight: java.awt.Color, shadow: java.awt.Color<br />
 * highlightOuter: java.awt.Color, highlightInner: java.awt.Color, shadowOuter: java.awt.Color, shadowInner: java.awt.Color<br />
 *
 */
class BevelBorderFactory extends SwingBorderFactory {

    final int type;

    public BevelBorderFactory(int newType) {
        type = newType;
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        builder.context.applyBorderToParent = attributes.remove('parent')

        // no if-else-if chain so that we can have one attribute failure block
        if (attributes.containsKey("highlight")) {
            Color highlight = attributes.remove("highlight")
            Color shadow = attributes.remove("shadow")
            if (highlight && shadow && !attributes) {
                return BorderFactory.createBevelBorder(type, highlight, shadow);
            }
        }
        if (attributes.containsKey("highlightOuter")) {
            Color highlightOuter = attributes.remove("highlightOuter")
            Color highlightInner = attributes.remove("highlightInner")
            Color shadowOuter = attributes.remove("shadowOuter")
            Color shadowInner = attributes.remove("shadowInner")
            if (highlightOuter && highlightInner && shadowOuter && shadowInner && !attributes) {
                return BorderFactory.createBevelBorder(type, highlightOuter, highlightInner, shadowOuter, shadowInner);
            }
        }
        if (attributes) {
            throw new RuntimeException("$name only accepts no attributes, or highlight: and shadow: attributes, or highlightOuter: and highlightInner: and shadowOuter: and shadowInner: attributes")
        }
        return BorderFactory.createBevelBorder(type);
    }
}