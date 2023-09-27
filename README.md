![Splashscreen](https://github.com/ShindouMihou/Nexus/assets/69381903/e2e2118b-07c4-4c49-9322-0507dc1ebf5c)

#

<div align="center"><i>Discord Bot Framework for Javacord, simplified.</i></div>
<br/>

Nexus is a Javacord framework, written in Kotlin-Java, designed to enable developers to add application commands to their Discord bots with simplicity. It is the successor of the [Velen](https://github.com/ShindouMihou/velen) framework and takes an "object-based" approach to designing application commands (slash commands, context menus, etc).

## Example

```kotlin
object PingCommand: NexusHandler {
    val name: String = "ping"
    val description: String = "Ping, Pong!"

    override fun onEvent(event: NexusCommandEvent) {
        // Auto-deferred response (automatically handles deferring of responses when the framework
        // detects potentially late response).
        event.autoDefer(ephemeral = true) {
           return@autodefer NexusMessage.from("Hello ${event.user.name}")
        }

        // Manual response (you are in control of deferring, etc.)
        // event.respondNowWith("Hello ${event.user.name}!")
    }
}
```
```kotlin
object ReportUserContextMenu: NexusUserContextMenu() {
    val name = "test"

    override fun onEvent(event: NexusContextMenuEvent<UserContextMenuCommandEvent, UserContextMenuInteraction>) {
        val target = event.interaction.target
        event.respondNowEphemerallyWith("${target.discriminatedName} has been reported to our servers!")
    }
}
```

## Getting Started

To get started with Nexus, we recommend reading the wiki in its chronological order:
- [`GitHub Wiki`](https://github.com/ShindouMihou/Nexus/wiki)

You can also read the examples that we have:
- [`Examples`](examples)

If you want to install Nexus as a dependency, you can head to Jitpack, select a version and follow the instructions there:
- [`Jitpack`](https://jitpack.io/#pw.mihou/Nexus)

## Bots using Nexus
Nexus is used in production by Discord bots, such as:
- [Beemo](https://beemo.gg): An anti-raid Discord bot that prevents raids on many large servers.
- [Amelia-chan](https://github.com/Amelia-chan/Amelia): A simple RSS Discord bot for the novel site, ScribbleHub.
- [Threadscore](https://threadscore.mihou.pw): Gamifying Q&A for Discord.

If you want to add your bot to the list, feel free to add it by creating a pull request!

## Features
Nexus was created from the ground up to power Discord bots with a simplistic yet flexible developer experience without compromising 
on performance, allowing developers to build their Discord bots fast and clean.
- [x] **Object-based commands**
- [x] **Object-based context menus**
- [x] **Middlewares, Afterwares**
- [x] **Supports auto-deferring of responses**
- [x] **Flexible command synchronization system**
- [x] **Supports optional validation and subcommand routers**
- [x] **Clean, developer-oriented API**
- [x] **Supports persistent slash command indexes, and many more**
- [x] **Supports different pagination systems**

We recommend reading the [`GitHub Wiki`](https://github.com/ShindouMihou/Nexus/wiki) to learn more about the different features of Nexus.

## License

Nexus is distributed under the Apache 2.0 license, the same one used by [Javacord](https://github.com/Javacord/Javacord). See [**LICENSE**](LICENSE) for more information.
