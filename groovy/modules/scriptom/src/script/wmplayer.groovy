
import org.codehaus.groovy.scriptom.ActiveXProxy

def folderName = "."
println "Playing files from: ${folderName}"

// create a Windows Media Player (from its Class ID)
def player = new ActiveXProxy("clsid:{6BF52A52-394A-11D3-B153-00C04F79FAA6}")

// react upon a state change
player.events.PlayStateChange = { 
    // if the media ended change the status flag
    // so that the main thread notices the change
    if (it[0].toInt() == 1) synchronized(player) { player.notify() }
}
// start listening to the events we're subsribed to
player.events.listen()

// loop over the available files and play them
new File(folderName).eachFileMatch(~/.*(wav|au|wma|mp3)/) { song ->
    println "Listening to: $song"
    player.URL = song.absolutePath
    control = player.controls.play()
    synchronized(player) { player.wait() }
}

// close the player
player.close()
