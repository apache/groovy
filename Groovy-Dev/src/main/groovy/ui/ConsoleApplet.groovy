package groovy.ui

import javax.swing.JApplet

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Jun 20, 2008
 * Time: 7:12:09 PM
 */
public class ConsoleApplet extends JApplet {

    Console console

    public void start() {
        console = new Console()
        console.run this
    }

    public void stop() {
        console.exit()
    }
}
