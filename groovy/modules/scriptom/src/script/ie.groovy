
import org.codehaus.groovy.scriptom.ActiveXProxy

// instanciate Internet Explorer
explorer = new ActiveXProxy("InternetExplorer.Application")

// set its properties
explorer.Visible = true
explorer.AddressBar = true

// navigate to a site
explorer.Navigate("http://glaforge.free.fr/weblog")
Thread.sleep(1000)
explorer.StatusText = "Guillaume Laforge's weblog"
Thread.sleep(2000)

// quit Internet Explorer
explorer.Quit()