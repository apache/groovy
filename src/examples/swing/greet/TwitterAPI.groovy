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
    String user
    XmlSlurper slurper = new XmlSlurper()
    def imageMap = [:]

    boolean login(name, password) {
        try {
            setStatus("Logging in")
            user = name
            Authenticator.setDefault(
                [getPasswordAuthentication : {
                    return new PasswordAuthentication(name, password) }
            ] as Authenticator)
            new URL("http://twitter.com/account/verify_credentials.xml").openStream().close()
            setStatus("\u00a0")
            return true
        } catch (Exception e) {
            setStatus("Login Failed")
            user = "\u00a0"
            return false
        }
    }

    def getFriends() {
        getFriends(user)
    }

    def getFriends(user) {
        setStatus("Loading Friends")
        def list = slurper.parse(new URL("http://twitter.com/statuses/user_timeline/${user}.xml").openStream())
        def friends = [list.status[0].user]
        def page = 1
        list = slurper.parse(new URL("http://twitter.com/statuses/friends/${user}.xml").openStream())
        while (list.length) {
            list.user.collect(friends) {it}
            page++
            try {
              list = slurper.parse("http://twitter.com/statuses/friends/${user}.xml&page=$page")
            } catch (Exception e) {
                break
            }
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

    def getFriendsTimeline(friend) {
        setStatus("Loading Timeline")
        def timeline =  slurper.parse(
                new URL("http://twitter.com/statuses/friends_timeline/${friend}.xml").openStream()
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

    def getTweets(friend) {
        setStatus("Loading Tweets")
        def tweets = slurper.parse(
                new URL("http://twitter.com/statuses/user_timeline/${friend}.xml").openStream()
            ).status.collect{it}
        setStatus("Loading Tweet Images")
        tweets.each {
            loadImage(it.user.profile_image_url as String)
        }
        setStatus("\u00a0")
        return tweets
    }

    // no need to read these, swing seems to cache these so the EDT won't stall
    def loadImage(image) {
        if (!imageMap[image]) {
            Thread.start {imageMap[image] = new javax.swing.ImageIcon(new URL(image))}
        }
    }

}