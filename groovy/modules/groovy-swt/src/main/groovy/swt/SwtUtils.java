package groovy.swt;

import groovy.jface.impl.ApplicationWindowImpl;

import java.lang.reflect.Field;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.GroovyException;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/** 
 * A helper class for working with SWT.
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @author <a href="mailto:ckl@dacelo.nl">Christiaan ten Klooster </a>
 *  
 * @version 1.1
 */
public class SwtUtils  {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(SwtUtils.class);
    

    /**
     * Parses the comma delimited String of style codes which are or'd
     * together. The given class describes the integer static constants
     *
     * @param constantClass is the type to look for static fields
     * @param text is a comma delimited text value such as "border, resize"
     * @return the int code
     */
    public static int parseStyle(Class constantClass, String text) throws GroovyException {
        return parseStyle(constantClass, text, true);
    }

    /**
     * Parses the comma delimited String of style codes which are or'd
     * together. The given class describes the integer static constants
     *
     * @param constantClass is the type to look for static fields
     * @param text is a comma delimited text value such as "border, resize"
     * @param toUpperCase is whether the text should be converted to upper case
     * before its compared against the reflection fields
     * 
     * @return the int code
     */
    public static int parseStyle(Class constantClass, String text, boolean toUpperCase) throws GroovyException{
        int answer = 0;
        if (text != null) {
            if (toUpperCase) {
                text = text.toUpperCase();
            }
            StringTokenizer enum = new StringTokenizer(text, ",");
            while (enum.hasMoreTokens()) {
                String token = enum.nextToken().trim();
                answer |= getStyleCode(constantClass, token);
            }
        }
        return answer;
    }
    
    /**
     * @return the code for the given word or zero if the word doesn't match a
     * valid style
     */
    public static int getStyleCode(Class constantClass,String text) throws GroovyException {
        try {
            Field field = constantClass.getField(text);
            if (field == null) {
                log.warn( "Unknown style code: " + text +" will be ignored");
                return 0;
            }
            return field.getInt(null);
        } catch (NoSuchFieldException e) {
            throw new GroovyException("The value: " + text + " is not understood ");
        } catch (IllegalAccessException e) {
            throw new GroovyException("The value: " + text + " is not understood");
        }
    }
    
    /**
     * dispose all children
     * 
     * @param parent
     */
    public static void disposeChildren(Composite parent) {
        Control[] children = parent.getChildren();
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];
            control.dispose();
        }
        System.out.println();
    }

    /**
     * return the parent shell
     * 
     * @param parent
     * @return
     */
    public static Shell getParentShell(Object parent) {
        if (parent instanceof ApplicationWindow) {
            return ((ApplicationWindowImpl) parent).getShell();
        }
        else if (parent instanceof Shell) {
            return (Shell) parent;
        }
        else {
            return null;
        }
    }

    /**
     * return the parent widget
     * 
     * @param parent
     * @return
     */
    public static Object getParentWidget(Object parent) {
        if (parent instanceof ApplicationWindow) {
            return (Composite) ((ApplicationWindowImpl) parent).getContents();
        }
        else if (parent instanceof Form) {
            return ((Form) parent).getBody();
        }
        else if (parent instanceof ScrolledForm) {
            return ((ScrolledForm) parent).getBody();
        }
        else if (parent instanceof Section) {
            return ((Section) parent).getClient();
        }
        else if (parent instanceof CTabItem) {
            return ((CTabItem) parent).getParent();
        }
        else if (parent instanceof TabItem) {
            return ((TabItem) parent).getParent();
        }
        else if (parent instanceof Widget) {
            return (Widget) parent;
        }
        else {
            return parent;
        }
    }
    
    
}
