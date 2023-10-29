package pw.mihou.nexus.features.react.hooks

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.configuration.modules.Cancellable
import pw.mihou.nexus.features.react.React
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

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