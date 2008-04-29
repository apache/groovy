/*
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Apr 26, 2008
 * Time: 8:32:03 AM
 */
package swing.greet

import groovy.beans.Bindable
import groovy.swing.SwingBuilder
import javax.swing.JOptionPane

class Greet {

    TwitterAPI api
    Binding view

    @Bindable boolean allowLogin = true
    @Bindable boolean allowSelection = true
    @Bindable def focusedUser = ""
    @Bindable def friends  = []
    @Bindable def tweets   = []
    @Bindable def timeline = []

    void startUp() {
        setAllowSelection(false)
        view.greetFrame.show()
        view.loginDialog.show()
    }

    void login(evt) {
        setAllowLogin(false)
        view.doOutside {
            try {
                if (api.login(view.twitterNameField.text, view.twitterPasswordField.password)) {
                    setFriends(api.getFriends(api.user))
                    selectUser(api.user)
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
                }
            }
        }
    }

    void filterTweets(evt) {
        setAllowSelection(false)
        view.doOutside {
            try {
                setTimeline(
                    api.getFriendsTimeline(focusedUser).findAll {it.text =~ view.searchField.text}
                )
                setTweets(
                    api.getTweets(focusedUser).findAll {it.text =~ view.searchField.text}
                )
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                view.edt {setAllowSelection(true)}
            }
        }
    }

    def userSelected(evt) {
        view.doOutside {
            selectUser(view.users.selectedItem.screen_name)
        }
    }

    def selectUser(user) {
        setAllowSelection(false)
        try {
            setFocusedUser(user)
            setTweets(api.getTweets(focusedUser).findAll {it.text =~ view.searchField.text})
            setTimeline(api.getFriendsTimeline(focusedUser).findAll {it.text =~ view.searchField.text})
        } finally {
            view.edt {setAllowSelection(true)}
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