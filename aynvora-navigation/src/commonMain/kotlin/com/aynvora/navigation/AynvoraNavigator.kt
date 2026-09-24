package com.aynvora.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest

interface AynvoraRoute : NavKey

sealed class AynvoraNavCommand {
    data class NavigateRoute(val route: AynvoraRoute) : AynvoraNavCommand()
    data class NavigatePopBackStack(val route: AynvoraRoute, val inclusive: Boolean) :
        AynvoraNavCommand()

    data class NavigateWithPopUp(
        val route: AynvoraRoute,
        val popUpToRoute: AynvoraRoute,
        val inclusive: Boolean = false
    ) : AynvoraNavCommand()

    data class NavigateSingleTop(val route: AynvoraRoute) : AynvoraNavCommand()
    data class NavigateBottomBar(val route: AynvoraRoute) : AynvoraNavCommand()
    data object NavigateBack : AynvoraNavCommand()
    data class NavigateBackWithResult(val result: Any) : AynvoraNavCommand()
}

class AynvoraNavigator {
    private val _navEvents = MutableSharedFlow<AynvoraNavCommand>(extraBufferCapacity = 64)
    val navEvents = _navEvents.asSharedFlow()

    fun navigate(route: AynvoraRoute) {
        _navEvents.tryEmit(AynvoraNavCommand.NavigateRoute(route))
    }

    fun navigate(route: AynvoraRoute, popUpTo: AynvoraRoute, inclusive: Boolean = false) {
        _navEvents.tryEmit(AynvoraNavCommand.NavigateWithPopUp(route, popUpTo, inclusive))
    }

    fun navigateSingleTop(route: AynvoraRoute) {
        _navEvents.tryEmit(AynvoraNavCommand.NavigateSingleTop(route))
    }

    fun navigateBottomBar(route: AynvoraRoute) {
        _navEvents.tryEmit(AynvoraNavCommand.NavigateBottomBar(route))
    }

    fun pop() {
        _navEvents.tryEmit(AynvoraNavCommand.NavigateBack)
    }

    fun popTo(route: AynvoraRoute, inclusive: Boolean = false) {
        _navEvents.tryEmit(AynvoraNavCommand.NavigatePopBackStack(route, inclusive))
    }

    fun navigateBackWithResult(result: Any) {
        _navEvents.tryEmit(AynvoraNavCommand.NavigateBackWithResult(result))
    }
}

val LocalAynvoraNavigator = staticCompositionLocalOf<AynvoraNavigator> {
    error("No Aynvora-Navigator provided")
}

suspend fun processNavigationEvents(
    backStack: NavBackStack<NavKey>,
    navigator: AynvoraNavigator
) {
    navigator.navEvents.collectLatest { command ->
        when (command) {
            is AynvoraNavCommand.NavigateRoute -> {
                backStack.add(command.route)
            }

            is AynvoraNavCommand.NavigatePopBackStack -> {
                val targetIndex = backStack.indexOf(command.route)
                if (targetIndex != -1) {
                    val removeFromIndex = if (command.inclusive) targetIndex else targetIndex + 1
                    if (removeFromIndex < backStack.size) {
                        backStack.subList(removeFromIndex, backStack.size).clear()
                    }
                }
            }

            is AynvoraNavCommand.NavigateWithPopUp -> {
                val popUpToIndex = backStack.indexOf(command.popUpToRoute)
                if (popUpToIndex != -1) {
                    val removeFromIndex = if (command.inclusive) popUpToIndex else popUpToIndex + 1
                    if (removeFromIndex < backStack.size) {
                        backStack.subList(removeFromIndex, backStack.size).clear()
                    }
                }
                backStack.add(command.route)
            }

            is AynvoraNavCommand.NavigateSingleTop -> {
                val existingIndex = backStack.indexOf(command.route)
                if (existingIndex != -1) {
                    if (existingIndex + 1 < backStack.size) {
                        backStack.subList(existingIndex + 1, backStack.size).clear()
                    }
                } else {
                    if (backStack.lastOrNull() != command.route) {
                        backStack.add(command.route)
                    }
                }
            }

            is AynvoraNavCommand.NavigateBottomBar -> {
                if (backStack.size > 1) {
                    backStack.subList(1, backStack.size).clear()
                }
                if (backStack.lastOrNull() != command.route) {
                    backStack.add(command.route)
                }
            }

            is AynvoraNavCommand.NavigateBack -> {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.lastIndex)
                }
            }

            is AynvoraNavCommand.NavigateBackWithResult -> {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.lastIndex)
                }
            }
        }
    }
}
