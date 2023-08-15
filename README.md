#  Nexus

*Slash Commands for Javacord, simplified.*

Nexus is a Javacord framework, written in Kotlin-Java, designed to enable developers to add slash commands to their Discord bots with simplicity. It is the successor of the [Velen](https://github.com/ShindouMihou/velen) framework and takes an "object-based" approach to designing commands.

## Example

```kotlin
object PingCommand: NexusHandler {
    val name: String = "ping"
    val description: String = "Ping, Pong!"

    override fun onEvent(event: NexusCommandEvent) {
        val server = event.server.orElseThrow()
        event.respondNowWith("Hello ${server.name}!")
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

If you want to add your bot to the list, feel free to add it by creating a pull request!

## License

Nexus is distributed under the Apache 2.0 license, the same one used by [Javacord](https://github.com/Javacord/Javacord). See [**LICENSE**](LICENSE) for more information.
