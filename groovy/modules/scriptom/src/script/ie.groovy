
import org.codehaus.groovy.scriptom.ActiveXProxy

explorer = new ActiveXProxy("InternetExplorer.Application")
explorer.Visible = true
explorer.AddressBar = true
explorer.Navigate('http://glaforge.free.fr')
Thread.sleep(1000)
explorer.StatusText = 'Guillaume Laforge'
Thread.sleep(2000)
explorer.Quit()