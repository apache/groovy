/*
 * Created on Feb 15, 2004
 *
 */
package groovy.swt.factory;

import groovy.lang.MissingPropertyException;
import groovy.swt.InvalidParentException;

import java.io.File;
import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

/**
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 * @version $Revision$
 */
public class ImageFactory extends AbstractSwtFactory implements SwtFactory {

    /*
     * @see groovy.swt.impl.Factory#newInstance(java.util.Map,
     *      java.lang.Object)
     */
    public Object newInstance(Map properties, Object parent) throws GroovyException {
        String src = (String) properties.remove("src");
        if (src == null) { throw new MissingPropertyException("src", Image.class); }

        if (parent == null) { throw new InvalidParentException("Widget or a Window"); }

        Image image = null;
        File imageFile = new File(src);
        if (imageFile.exists()) {
            image = new Image(Display.getCurrent(), src);
        } else {
            image = new Image(Display.getCurrent(), ImageFactory.class.getClassLoader()
                    .getResourceAsStream(src));
        }

        if (parent instanceof Window) {
            setWindowImage((Window) parent, image);
        } else if (parent instanceof Widget) {
            setWidgetImage((Widget) parent, image);
        }

        return image;
    }

    /**
     * Set default image Window
     * 
     * @param window
     * @param image
     */
    private void setWindowImage(Window window, Image image) {
        window.getShell().setImage(image);
    }

    /**
     * Add image to a widget
     * 
     * @param parent
     * @param image
     * @throws JellyTagException
     */
    protected void setWidgetImage(Widget parent, Image image) throws GroovyException {
        if (parent instanceof Label) {
            Label label = (Label) parent;
            label.setImage(image);

        } else if (parent instanceof Button) {
            Button button = (Button) parent;
            button.setImage(image);

        } else if (parent instanceof Item) {
            Item item = (Item) parent;
            item.setImage(image);

        } else if (parent instanceof Decorations) {
            Decorations item = (Decorations) parent;
            item.setImage(image);

        } else {
            throw new GroovyException(
                    "This tag must be nested inside a <label>, <button> or <item> tag");
        }
    }

}
