
import org.codehaus.groovy.scriptom.ActiveXProxy

// showing the current directory
cmd = new ActiveXProxy("Scripting.FileSystemObject")
println cmd.GetAbsolutePathName(".").value

cmd = new ActiveXProxy("Shell.Application")

// minimizing all opened windows
//cmd.MinimizeAll()

// choosing a folder from a native windows directory chooser
folder = cmd.BrowseForFolder(0, "Choose a folder", 0)
println folder.Items().Item().Path.value



Thread.sleep(1000)