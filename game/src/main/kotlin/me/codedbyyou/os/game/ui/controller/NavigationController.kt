package com.software.project.ui.controller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.github.oshai.kotlinlogging.KLogger
import me.codedbyyou.os.game.data.NavigationHost
import me.codedbyyou.os.game.utils.Logger

/**
 * A controller to handle navigation between views
 * @see NavigationHost
 * @author Abdollah Kandrani
 * @since 0.0.1
 * EXAMPLE:
 * to add a navigation host:
 *  NavigationController.addNavigationHost("view/hostName", { sharedData -> CallContentOfTheView(sharedData) })
 * to navigate to a view:
 *  NavigationController.navigateTo("view/hostName")
 * to navigate back:
 *  NavigationController.goBack()
 * to get the current view:
 *  NavigationController.loadView()
 * to go to a view with back navigation:
 *  NavigationController.goTo("view/hostName")
 * to load the current view:
 * NavigationController.loadView()
 */
object NavigationController {

    private val _currentView: MutableState<NavigationHost?> = mutableStateOf(null)
    private val _navigationHosts: MutableState<MutableList<NavigationHost>> = mutableStateOf(mutableListOf())
    private val _navigationStack: MutableState<MutableList<NavigationHost>> = mutableStateOf(mutableListOf())
    private val _sharedData: MutableState<MutableMap<String, Any>> = mutableStateOf(mutableMapOf())
    private val logger: KLogger = Logger.navController
    val currentView: MutableState<NavigationHost?> = _currentView

    /**
     * Adds a navigation host to the controller
     * @param host the navigation host to add
     * @see NavigationHost
     */
    fun addNavigationHost(host: NavigationHost) {
        logger.info{"Adding navigation host: ${host.title}"}
        _navigationHosts.value.add(host)
        logger.info{"Added navigation host: ${host.title}"}
    }

    /**
     * Adds a navigation host to the controller
     * @param title the title of the navigation host
     * @param content the content of the navigation host
     * @see NavigationHost
     * @see addNavigationHost - wraps this function around it
     */
    fun addNavigationHost(title: String, content: @Composable (sharedData: Map<String, Any>) -> Unit = {}) {
       addNavigationHost(NavigationHost(title, content))
    }

    /**
     * Navigate back to the previous view
     * @param sharedData a map of data to share from the current view to the previous view
     * @return true if the navigation was successful, false otherwise
     * @see navigateTo
     * @see NavigationHost
     * @see currentView
     */
    fun goBack(sharedData: Map<String, Any> = emptyMap()): Boolean {
        if (_navigationStack.value.isEmpty()) {
            return false
        }
        sharedData.forEach { (key, value) -> _sharedData.value[key] = value }
        val toGo = _navigationStack.value.removeLast()
        logger.info{"Navigating back to: ${toGo.title} from ${_currentView.value?.title}"}
        _currentView.value = toGo
        return true
    }

    /**
     * Navigates to another view by its title, usually used for navigation from a dialog/overlay to a subview
     * uses the [_navigationStack] to keep track of the previous views
     * @param view the title of the view to navigate to
     * @param sharedData a map of data to share between views
     * @see NavigationHost
     */
    fun goTo(view: String, sharedData: Map<String, Any> = emptyMap()){
        val toGo = _navigationHosts.value.find { it.title.equals(view, true) }
        if(toGo == currentView.value)
            return
        if (toGo != null) {
            sharedData.forEach { (key, value) -> _sharedData.value[key] = value }
            logger.info{"Navigating to: ${toGo.title} from ${_currentView.value?.title}"}
            _navigationStack.value.add(_currentView.value!!)
            _currentView.value = toGo
        } else {
            logger.error{"Navigation failed: view not found"}
            _currentView.value = null
        }
    }

    /**
     * Navigate to a view by its title
     * @param view the title of the view to navigate to
     * @param sharedData a map of data to share between views
     * @see NavigationHost
     */
    fun navigateTo(view: String, sharedData: Map<String, Any> = emptyMap()) {
        val toGo = _navigationHosts.value.find { it.title.equals(view, true) }
        if(toGo == currentView.value)
            return
        if (toGo != null) {
            logger.info{"Navigating to: ${toGo.title} from ${_currentView.value?.title}"}
            _sharedData.value.clear()
            sharedData.forEach { (key, value) -> _sharedData.value[key] = value }
            _navigationStack.value.clear()
            _currentView.value = toGo
        } else {
            logger.error{"Navigation failed: view not found"}
            _currentView.value = null
        }
    }

    /**
     * Loads the current view
     * @see NavigationHost
     * @see currentView
     */
    @Composable
    fun loadView(){
        _currentView.value!!.content(_sharedData.value)
    }

    /**
     * get the navigation hosts count
     */
    fun getHostsCount(): Int {
        return _navigationHosts.value.size
    }
}