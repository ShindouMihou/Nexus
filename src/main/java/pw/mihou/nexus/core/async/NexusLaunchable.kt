package pw.mihou.nexus.core.async

import pw.mihou.nexus.Nexus
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Launchables is an asynchronous task that can have multiple completion stages and exceptions handle-able contrary to the
 * one that [CompletableFuture] has. It's a more complex variant that is designed to work support multiple tasks inside a single asynchronous
 * task.
 *
 * A launchable has two stages of completion:
 * - non-final completion is when the task calls a `complete()` that can indicate that a task inside the task has completed with a result.
 * - final completion is when the task itself completes and is actually called by the launchable itself and returns the result of the task.
 *
 * Due to this, a task exception listener can also indicate that one of the tasks had an exception or the main task itself had an exception.
 *
 * A key part to remember when using the launchable is that secondary exceptions will not cause `.join()` to throw an exception, but primary
 * exceptions (one that is caught by the final task completion or uncaught exceptions) will throw.
 */
class NexusLaunchable<PrimaryResult, SecondaryResult>
    (private val task: NexusLaunchableStack.NexusLaunchableStackSignal<SecondaryResult>.() -> PrimaryResult) {

    private val stack: NexusLaunchableStack<SecondaryResult> = NexusLaunchableStack()
    private val finalCompletionStack: MutableList<NexusLaunchableCallback<PrimaryResult>> = CopyOnWriteArrayList()

    private val finalFuture = CompletableFuture<Void>()

    init {
        finalCompletionStack.add { finalFuture.complete(null) }
        Nexus.configuration.launch.launcher.launch {
            try {
                val result = task(stack.stackSingal)

                Nexus.configuration.launch.launcher.launch {
                    for (stack in finalCompletionStack) {
                        try {
                            stack.on(result)
                        } catch (exception: Exception) {
                            Nexus.logger.error("An uncaught exception was caught in a launchable.")
                            exception.printStackTrace()
                        }
                    }
                }
            } catch (exception: Exception) {
                finalFuture.completeExceptionally(exception)
                if (stack.taskErrorStack.isEmpty()) {
                    Nexus.logger.error("An uncaught exception was caught in a launchable.")
                    exception.printStackTrace()

                    return@launch
                }

                stack.stackSingal.error(exception)
            }
        }
    }

    /**
     * Adds one or more non-final task completion listeners to the launchable.
     * Not to be confused with [addFinalCompletionListeners] which adds one or more **final** task completion listeners
     * that are triggered at the very end of the task.
     *
     * **Task completion and final task completion are different** in that **task completion can be triggered multiple times
     * when the task itself calls the complete method** while **final task completion occurs at the end of the function itself**.
     *
     * @param launchables the tasks to execute.
     */
    fun addTaskCompletionListeners(launchables: List<NexusLaunchableCallback<SecondaryResult>>) {
        stack.taskCompletionStack.addAll(launchables)
    }

    /**
     * Adds one or more final task completion listeners to the launchable.
     * Not to be confused with [addTaskCompletionListener] which adds one or more **non-final** task completion listeners
     * that are triggered anytime the task itself calls a complete.
     *
     * **Task completion and final task completion are different** in that **task completion can be triggered multiple times
     * when the task itself calls the complete method** while **final task completion occurs at the end of the function itself**.
     *
     * @param launchables the tasks to execute.
     */
    fun addFinalCompletionListeners(launchables: List<NexusLaunchableCallback<PrimaryResult>>) {
        finalCompletionStack.addAll(launchables)
    }

    /**
     * Adds one or more task error listeners to the launchable, there can be more than one times when the launchables are
     * called. If an exception is uncaught and reaches the final task completion, it will call this otherwise console logs
     * the exception.
     *
     * @param launchables the tasks to execute.
     */
    fun addTaskErrorListeners(launchables: List<NexusLaunchableCallback<Exception>>) {
        stack.taskErrorStack.addAll(launchables)
    }

    /**
     * Adds one non-final task completion listeners to the launchable.
     * Not to be confused with [addFinalCompletionListener] which adds one **final** task completion listeners
     * that is triggered at the very end of the task.
     *
     * **Task completion and final task completion are different** in that **task completion can be triggered multiple times
     * when the task itself calls the complete method** while **final task completion occurs at the end of the function itself**.
     *
     * @param launchable the task to execute.
     */
    fun addTaskCompletionListener(launchable: NexusLaunchableCallback<SecondaryResult>) {
        stack.taskCompletionStack.add(launchable)
    }

    /**
     * Adds one or more final task completion listeners to the launchable.
     * Not to be confused with [addTaskCompletionListener] which adds one **non-final** task completion listeners
     * that is triggered anytime the task itself calls a complete.
     *
     * **Task completion and final task completion are different** in that **task completion can be triggered multiple times
     * when the task itself calls the complete method** while **final task completion occurs at the end of the function itself**.
     *
     * @param launchable the tasks to execute.
     */
    fun addFinalCompletionListener(launchable: NexusLaunchableCallback<PrimaryResult>) {
        finalCompletionStack.add(launchable)
    }

    /**
     * Adds one task error listeners to the launchable, there can be more than one times when the launchables are
     * called. If an exception is uncaught and reaches the final task completion, it will call this otherwise console logs
     * the exception.
     *
     * @param launchables the tasks to execute.
     */
    fun addTaskErrorListener(launchable: NexusLaunchableCallback<Exception>) {
        stack.taskErrorStack.add(launchable)
    }

    fun join() = finalFuture.join()
}

fun interface NexusLaunchableCallback<Result> {
    fun on(result: Result)
}

class NexusLaunchableStack<Result> internal constructor() {
    val taskCompletionStack: MutableList<NexusLaunchableCallback<Result>> = CopyOnWriteArrayList()
    val taskErrorStack: MutableList<NexusLaunchableCallback<Exception>> = CopyOnWriteArrayList()

    val stackSingal: NexusLaunchableStackSignal<Result> = NexusLaunchableStackSignal(this)

    class NexusLaunchableStackSignal<Result> internal constructor(private val stack: NexusLaunchableStack<Result>) {

        /**
         * Signals that a secondary task has been completed, this will not signal a final completion task.
         * Using this method will call all the secondary task completion listeners in a sequential manner inside another
         * asynchronous task.
         *
         * @param result the result to send to the secondary task completion.
         */
        fun complete(result: Result) {
            Nexus.configuration.launch.launcher.launch {
                for (taskCompletionStack in stack.taskCompletionStack) {
                    try {
                        taskCompletionStack.on(result)
                    } catch (exception: Exception) {
                        Nexus.logger.error("An uncaught exception was caught in a launchable.")
                        exception.printStackTrace()
                    }
                }
            }
        }

        /**
         * Signals that an exception occurred, this will not signal an exception on any that is listening onto `.join()`.
         * Using this method will call all the task error completion listeners in a sequential manner inside another
         * asynchronous task.
         *
         * @param exception the exception to be sent to the listeners.
         */
        fun error(exception: Exception) {
            Nexus.configuration.launch.launcher.launch {
                for (taskErrorStack in stack.taskErrorStack) {
                    try {
                        taskErrorStack.on(exception)
                    } catch (exception: Exception) {
                        Nexus.logger.error("An uncaught exception was caught in a launchable.")
                        exception.printStackTrace()
                    }
                }
            }
        }
    }
}