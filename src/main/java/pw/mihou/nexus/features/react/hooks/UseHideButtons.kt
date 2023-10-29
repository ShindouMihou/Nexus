package pw.mihou.nexus.features.react.hooks

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.Cancellable
import pw.mihou.nexus.features.react.React
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * [useHideButtons] is a hook that will give you a little [React.Writable] that will
 * turn into [false] after the given [after] timestamp.
 *
 * This is useful for cases where you want to remove the buttons of a response after
 * a given set of time.
 *
 * @param after the amount of time before hiding the buttons, defaults to 10 minutes.
 * @return a [React.Writable] that will change to [false] after the given time.
 */
fun React.useHideButtons(after: Duration = 10.minutes): React.Writable<Boolean> {
    val showButtons = writable(true)
    var removeButtons: Cancellable? = null

    onRender {
        removeButtons?.cancel(true)
        removeButtons = Nexus.launch.scheduler.launch(after.inWholeMilliseconds) {
            showButtons.set(false)
        }
    }

    return showButtons
}