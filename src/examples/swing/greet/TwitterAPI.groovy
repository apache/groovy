/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Apr 25, 2008
 * Time: 9:47:20 PM
 */
package swing.greet

import groovy.beans.Bindable

class TwitterAPI {

    @Bindable String status = "\u00a0"
    def authenticatedUser
    XmlSlurper slurper = new XmlSlurper()
    def imageMap = [:]

    boolean login(name, password) {
        try {
            setStatus("Logging in")
            Authenticator.setDefault(
                [getPasswordAuthentication : {
                    return new PasswordAuthentication(name, password) }
            ] as Authenticator)
            authenticatedUser = getUser(name)
            setStatus("\u00a0")
            return true
        } catch (Exception e) {
            setStatus("Login Failed")
            authenticatedUser = null
            return false
        }
    }

    def getFriends() {
        getFriends(authenticatedUser)
    }

    def getFriends(String user) {
        return getFriends(getUser(user))
    }

    def getFriends(user) {
        setStatus("Loading Friends")
        def friends = [user]
        def page = 1
        def list = slurper.parse(new URL("http://twitter.com/statuses/friends/${user.screen_name}.xml").openStream())
        while (list.length) {
            list.user.collect(friends) {it}
            page++
            try {
              list = slurper.parse("http://twitter.com/statuses/friends/${user.screen_name}.xml&page=$page")
            } catch (Exception e) { break }
        }
        setStatus("Loading Friends Images")
        friends.each {
            loadImage(it.profile_image_url as String)
        }
        setStatus("\u00a0")
        return friends
    }

    def getFriendsTimeline() {
        getFriendsTimeline(user)
    }

    def getFriendsTimeline(String friend) {
        getFriendsTimeline(getUser(authenticatedUser))
    }

    def getFriendsTimeline(user) {
        setStatus("Loading Timeline")
        def timeline =  slurper.parse(
                new URL("http://twitter.com/statuses/friends_timeline/${user.screen_name}.xml").openStream()
            ).status.collect{it}
        setStatus("Loading Timeline Images")
        timeline.each {
            loadImage(it.user.profile_image_url as String)
        }
        setStatus("\u00a0")
        return timeline
    }

    def getTweets() {
      return getTweets(user)
    }

    def getTweets(String friend) {
    }

    def getTweets(friend) {
        setStatus("Loading Tweets")
        def tweets = slurper.parse(
                new URL("http://twitter.com/statuses/user_timeline/${friend.screen_name}.xml").openStream()
            ).status.collect{it}
        setStatus("Loading Tweet Images")
        tweets.each {
            loadImage(it.user.profile_image_url as String)
        }
        setStatus("\u00a0")
        return tweets
    }

    def getUser(String screen_name) {
        return slurper.parse(
                new URL("http://twitter.com/users/show/${screen_name}.xml").openStream()
            )
    }

    def tweet(message) {
        def urlConnection = new URL("http://twitter.com/statuses/update.xml").openConnection()
        urlConnection.doOutput = true
        urlConnection.outputStream << "status=${URLEncoder.encode(message, 'UTF-8')}"
        return slurper.parse(urlConnection.inputStream)
    }

    // no need to read these, swing seems to cache these so the EDT won't stall
    def loadImage(image) {
        if (!imageMap[image]) {
            Thread.start {imageMap[image] = new javax.swing.ImageIcon(new URL(image))}
        }
    }

}