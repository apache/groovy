
import org.codehaus.groovy.scriptom.ActiveXProxy
import java.io.File

// create a proxy for the Shell object
sh = new ActiveXProxy("Shell.Application")

// use a Windows standard folder chooser
folder = sh.BrowseForFolder(0, "Choose a folder with wav files", 0)

// get the folder chosen
folderName = folder.Items().Item().Path.value
println "Playing Wav files from: ${folderName}"

// create a Windows Media Player (from its Class ID)
player = new ActiveXProxy("clsid:{6BF52A52-394A-11D3-B153-00C04F79FAA6}")

// for each file in the folder
new File(folderName).eachFile{ file ->
    if (file.name.endsWith("wav")) {
        println file
        player.URL = file.absolutePath
        // play the wav for one second
        control = player.controls.play()
        Thread.sleep(1000)
    }
}

// close the player
player.close()
