/**
 * Simple web server
 * @author <a href="mailto:jeremy.rayner@gmail.com">Jeremy Rayner</a>
 * 
 * invoke using
 *    groovy -w 80 SimpleWebServer.groovy
 * 
 *       (where 80 is the port to listen for requests upon)
 */
import java.io.File

if (init) { 
    headers = [:] 
}

// parse the request
if (line.toLowerCase().startsWith("get")) {
    content = line.tokenize()[1]
} else {
    h = line.tokenize(":")
    headers[h[0]] = h[1]
}

// all done, now process request
if (line.size() == 0) {
    processRequest()
    return "success"
}

// ------------------------

def processRequest() {
    if (content.indexOf("..") < 0) { //simplistic security
        // simple file browser rooted from current dir
        f = new File("." + content)
        if (f.isDirectory()) {
            // directory listing
            for (i in f.list()) {
                if ("/" == content) { content = "" } // special case for root document
                println "<a href='${content}/${i}'>${i}</a><br>"
            }
        } else {
            println(f.text)
        }
    }
}
