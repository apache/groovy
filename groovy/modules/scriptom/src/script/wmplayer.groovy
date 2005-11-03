
import org.codehaus.groovy.scriptom.ActiveXProxy

def folderName = "D:\\projets\\HalloweenSounds\\sounds\\animals"
println "Playing files from: ${folderName}"

def songFiles = []
new File(folderName).eachFileMatch(~/.*(wav|mp3)/){ songFiles << it }

// create a Windows Media Player (from its Class ID)
def player = new ActiveXProxy("clsid:{6BF52A52-394A-11D3-B153-00C04F79FAA6}")

// a flag indicating the player is ready to play
def ready = true

// react upon a state change
player.events.PlayStateChange = { 
    // if the media ended change the status flag
    // so that the main thread notices the change
    if (it[0].toInt() == 1) ready = true
}
// start listening to the events we're subsribed to
player.events.listen()

// loop over the available files and play them
songFiles.each{ song ->
    while (!ready) sleep(1000)
    ready = false
    println "Listening to: $song"
    player.URL = song.absolutePath
    control = player.controls.play()
}



// close the player
player.close()