
import org.codehaus.groovy.scriptom.ActiveXProxy

// showing the current directory
cmd = new ActiveXProxy("Scripting.FileSystemObject")
println cmd.GetAbsolutePathName(".").value

sh = new ActiveXProxy("Shell.Application")

// minimizing all opened windows
sh.MinimizeAll()

// opens an Explorer at the current location
sh.Explore(cmd.GetAbsolutePathName(".").value)

// choosing a folder from a native windows directory chooser
folder = sh.BrowseForFolder(0, "Choose a folder", 0)
println folder.Items().Item().Path.value

wshell = new ActiveXProxy("WScript.Shell")
// create a popup
wshell.popup("Groovy popup")

// show some key from the registry
println wshell.RegRead("HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\\User Agent").value

net = new ActiveXProxy("WScript.Network")
// prints the computer name
println net.ComputerName.value
