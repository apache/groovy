/**
 * Echoes back whatever is thrown at it (with a <br> at end for browsers) ...
 * @author <a href="mailto:jeremy.rayner@gmail.com">Jeremy Rayner</a>
 * 
 * invoke using
 *    groovy -w 80 Reflections.groovy
 * 
 *       (where 80 is the port to listen for requests upon)
 */

// echo, echo, echo...
println "${line} <br>"

//assume no input means we've finished...
if (line.size() == 0) {
    return "success"
}
