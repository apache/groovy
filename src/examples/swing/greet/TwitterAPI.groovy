/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Apr 25, 2008
 * Time: 9:47:20 PM
 */
package groovy.swing.greet

import groovy.beans.Bindable

class TwitterAPI {

    @Bindable String status = "\u00a0"
    def authenticatedUser
    XmlSlurper slurper = new XmlSlurper()
    def imageMap = [:]

    def withStatus(status, c) {
        setStatus(status)
        try {
            def o = c()
            setStatus("\u00a0")
            return o
        } catch (Throwable t) {
            setStatus("Error $status : ${t.message =~ '400'?'Rate Limit Reached':t}")
            throw t
        }
    }


    boolean login(def name, def password) {
        withStatus("Logging in") {
            Authenticator.setDefault(
                [getPasswordAuthentication : {
                    return new PasswordAuthentication(name, password) }
                ] as Authenticator)
            authenticatedUser = getUser(name)
            return true
        }
    }

    def getFriends() {
        getFriends(authenticatedUser)
    }

    def getFriends(String user) {
        return getFriends(getUser(user))
    }

    def getFriends(user) {
        def friends = [user]
        withStatus("Loading Friends") {
            def page = 1
            def list = slurper.parse(new URL("http://twitter.com/statuses/friends/${user.screen_name}.xml").openStream())
            while (list.length) {
                list.user.collect(friends) {it}
                page++
                try {
                  list = slurper.parse("http://twitter.com/statuses/friends/${user.screen_name}.xml&page=$page")
                } catch (Exception e) { break }
            }
        }
        withStatus("Loading Friends Images") {
            return friends.each {
                loadImage(it.profile_image_url as String)
            }
        }
    }

    def getFriendsTimeline() {
        getFriendsTimeline(user)
    }

    def getFriendsTimeline(String friend) {
        getFriendsTimeline(getUser(friend))
    }

    def getFriendsTimeline(user) {
        def timeline = []
        withStatus("Loading Timeline") {
            timeline =  slurper.parse(
                    new URL("http://twitter.com/statuses/friends_timeline/${user.screen_name}.xml").openStream()
                ).status.collect{it}
        }
        withStatus("Loading Timeline Images") {
            return timeline.each {
                loadImage(it.user.profile_image_url as String)
            }
        }
    }

    def getTweets() {
        return getTweets(user)
    }

    def getTweets(String friend) {
        return getTweets(getUser(frield))
    }

    def getTweets(friend) {
        def tweets = []
        withStatus("Loading Tweets") {
            tweets = slurper.parse(
                    new URL("http://twitter.com/statuses/user_timeline/${friend.screen_name}.xml").openStream()
                ).status.collect{it}
        }
        withStatus("Loading Tweet Images") {
            return tweets.each {
                loadImage(it.user.profile_image_url as String)
            }
        }
    }

    def getUser(String screen_name) {
        withStatus("Loading User $screen_name") {
            if (screen_name.contains('@')) {
                return slurper.parse(
                        new URL("http://twitter.com/users/show.xml?email=${screen_name}").openStream()
                    )
            } else {
                return slurper.parse(
                        new URL("http://twitter.com/users/show/${screen_name}.xml").openStream()
                    )
            }
        }
    }

    def tweet(message) {
        withStatus("Tweeting") {
            def urlConnection = new URL("http://twitter.com/statuses/update.xml").openConnection()
            urlConnection.doOutput = true
            urlConnection.outputStream << "status=${URLEncoder.encode(message, 'UTF-8')}"
            return slurper.parse(urlConnection.inputStream)
        }
    }

    // no need to read these, swing seems to cache these so the EDT won't stall
    def loadImage(image) {
        if (!imageMap[image]) {
            Thread.start {imageMap[image] = new javax.swing.ImageIcon(new URL(image))}
        }
    }

}