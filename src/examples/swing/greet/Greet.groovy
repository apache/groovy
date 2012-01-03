/*
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Apr 26, 2008
 * Time: 8:32:03 AM
 */
package groovy.swing.greet

import groovy.beans.Bindable
import groovy.swing.SwingBuilder
import javax.swing.JOptionPane

class Greet {

    TwitterAPI api
    Binding view

    @Bindable boolean allowLogin = true
    @Bindable boolean allowSelection = true
    @Bindable boolean allowTweet = true
    @Bindable def focusedUser = ""
    @Bindable def friends  = []
    @Bindable def tweets   = []
    @Bindable def timeline = []
    @Bindable def statuses = []

    void startUp() {
        setAllowSelection(false)
        setAllowTweet(false)
        view.greetFrame.show()
        view.loginDialog.show()
    }

    void login(evt) {
        setAllowLogin(false)
        view.doOutside {
            try {
                if (api.login(view.twitterNameField.text, view.twitterPasswordField.password)) {
                    setFriends(api.getFriends(api.authenticatedUser))
                    friends.each {it.status.user = [screen_name:it.screen_name, profile_image_url:it.profile_image_url] }
                    setStatuses(friends.collect {it.status})
                    selectUser(api.authenticatedUser)
                    view.greetFrame.show()
                    view.loginDialog.dispose()
                } else {
                    JOptionPane.showMessageDialog(view.loginDialog, "Login failed")
                }
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                view.edt {
                    setAllowLogin(true)
                    setAllowSelection(true)
                    setAllowTweet(true)
                }
            }
        }
    }

    void filterTweets(evt = null) {
        setAllowSelection(false)
        setAllowTweet(false)
        view.doOutside {
            try {
                setStatuses(
                    friends.collect {it.status}.findAll {it.text =~ view.searchField.text}
                )
                setTimeline(
                    api.getFriendsTimeline(focusedUser).findAll {it.text =~ view.searchField.text}
                )
                setTweets(
                    api.getTweets(focusedUser).findAll {it.text =~ view.searchField.text}
                )
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                view.edt {
                    setAllowSelection(true)
                    setAllowTweet(true)
                }
            }
        }
    }

    def userSelected(evt) {
        view.doOutside {
            selectUser(view.users.selectedItem)
        }
    }

    def selectUser(user) {
        setAllowSelection(false)
        setAllowTweet(false)
        try {
            setFocusedUser(api.getUser(user.screen_name as String))
            setTweets(api.getTweets(focusedUser).findAll {it.text =~ view.searchField.text})
            setTimeline(api.getFriendsTimeline(focusedUser).findAll {it.text =~ view.searchField.text})
        } finally {
            view.edt {
                setAllowSelection(true)
                setAllowTweet(true)
            }
        }
    }

    def tweet(evt = null) {
        setAllowTweet(false)
        view.doOutside {
            try {
                api.tweet(view.tweetBox.text)
                // true story: it froze w/o the EDT call here
                view.edt {tweetBox.text = ""}
                filterTweets()
            } finally {
                setAllowTweet(true)
            }
        }
    }

    public static void main(String[] args) {
        def model = new TwitterAPI()
        def controller = new Greet()
        def view = new SwingBuilder()

        controller.api = model
        controller.view = view

        view.controller = controller

        view.build(View)
        view.view = view

        controller.startUp()
    }
}