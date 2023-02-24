<div align="center">
    Build with simplicity.
</div>

### 

Nexus is a Javacord framework, originally written in Java, now in Kotlin-Java, designed to simplify development of Discord bots. It is the successor to 
the [Velen](https://github.com/ShindouMihou/Velen) framework and takes a more object-ful approach to designing commands, also follows a strict standard of 
one Discord bot per application.

#### Table of Contents
1. [Preparation](#-preparaton)
2. [Designing Commands](#-designing-commands)
3. [Command Interceptors](#-command-interceptors)
4. [Deferred Middleware Responses](#-deferred-middleware-responses)
5. [Basic Subcommand Router](#-basic-subcommand-router)
6. [Option Validation](#-option-validation)
7. [Auto-deferring Responses](#-auto-deferring-responses)
8. [Command Synchronizations](#-command-synchronizations)

#### 💭 Preparation

To install Nexus, head to [Jitpack](https://jitpack.io/#pw.mihou/Nexus) and select the release that you want to install, you may also 
select the branch, commit that you want to install and follow the instructions from there.

Nexus is pre-configured with most of the basic standards, but there are a few requirements that are in needed before proceeding and that is 
handling the shard connection and disconnections with the shard manager. The framework uses its own sharding manager to help it route different tasks 
to different shards without the need for you to bring along a shard parameter everytime, so we need a way for the framework to gather those shards.

To do so, you can do the following on where you spawn a shard:
```kotlin
val shard: DiscordApi = ...
Nexus.sharding.set(shard)
```

An example of how to do this would be:
```kotlin
// Sample One: Non-sharded
val shard = DiscordApiBuilder()
    .setToken(...)
    .addListener(Nexus)
    .login()
    .join()
Nexus.sharding.put(shard)

// Sample Two: Sharded
DiscordApiBuilder()
    .setToken(...)
    .addListener(Nexus)
    .setTotalShards(1)
    .loginAll()
    .forEach { future -> future.thenAccept { shard -> 
      Nexus.sharding.set(shard)
      // other essentials that you may do such as onShardLogin()  
    } }
```

You also have to remember to place the `addListener` line to tell Javacord to route events to Nexus:
```kotlin
DiscordApiBuilder()
    .addListener(Nexus)
```

> **Note!**
> When disconnecting with shards, you also have to indicate to Nexus that you want to remove the shard by adding 
> the following line before the actual disconnect:
> ```kotlin
> Nexus.sharding.remove(shard.currentShard)
> ```

#### 🍾 Designing Commands

> **Warning**
>
> Before we start, a fair warning, it is never recommended to use the `event.interaction.respondLater()` methods of Javacord when using 
> Nexus because we have special handling for middlewares  that requires coordination between the command and the middleware. It is better 
> to use `event.respondLater()` or `event.respondLaterAsEphemeral()` instead.
> 
> You can even use auto-deferring instead which handles these cases for you, read more at [Auto-deferring Responses](#-auto-deferring-responses)

Nexus offers a simple, and straightforward manner of designing commands, but before we can continue designing commands, let us first understand a few 
fundamental rules that Nexus enforces:
1. You cannot have two or more commands with the same name unless you modify one of the commands with the `@IdentifiableAs` annotation, for indexing reasons. ([Read more here]())
2. You have to implement `NexusHandler` otherwise the engine will reject the command.
3. You should have a name and a description field in the command itself, Discord requires these.

After understanding all of those, we can start designing a command by simply creating a simple class that implements the `NexusHandler` class:
```kotlin
object PingCommand: NexusHandler {
    override fun onEvent(event: NexusCommandEvent) {}
}
```

But, that is still not a command and Nexus won't recognize it as one because it is still missing a few requirements and that is the `name` and `description` field. 
It is here that we start to understand the object-ful nature of designing commands in Nexus, to add the following fields, all you need to do is add class fields:
```kotlin
object PingCommand: NexusHandler {
    val name: String = "ping"
    val description: String = "Ping, Pong!"

    override fun onEvent(event: NexusCommandEvent) {}
}
```

And that's pretty much how you can create a command, but how about we go deeper and add some functionality?
```kotlin
object PingCommand: NexusHandler {
    val name: String = "ping"
    val description: String = "Ping, Pong!"

    override fun onEvent(event: NexusCommandEvent) {
        val server = event.server.orElseThrow()
        // There are two ways to respond in Nexus, one is auto-deferring and the other is manual response.
        // the example shown here demonstrates auto-defer responses.
        event.autoDefer(ephemeral = false) {
            return@autoDefer NexusMessage.with {
                setContent("Hello ${server.name}")
            }
        }
        
        // The example below demonstrates manual response wherein it is up to you to manually respond or not.
        // event.respondNowWith("Hello ${server.name}!")
    }
}
```

And now, the command has a simple functionality that says "Hello {server}!", but there's a problem, we don't have a guarantee that it is executed on a server, but 
don't worry, Nexus has a solution and that is through **middlewares** and **afterwares** which is common for many web frameworks.

#### 💭 Command Interceptors

You can create a command interceptor, either a middleware or an afterware, through either two ways:
1. Creating a repository of interceptors.
2. Registering them directly onto the framework.

Let us first understand the second way first; Nexus stores all interceptors in a single, global container in the application that is accessible by a name. All of these 
are stored inside the `NexusCommandInterceptor` and we can add into it by simply doing the following:
```kotlin
// Sample One: Used to reduce code-duplication.
NexusCommandInterceptor.addMiddleware("nexus.auth.server") { event -> event.stopIf(event.server.isEmpty) }

// Sample Two: Used to create quick middlewares, not recommended.
// It uses UUID as its namespace instead of giving a user-defined one.
NexusCommandInterceptor.middleware { event -> event.stopIf(event.server.isEmpty) }
```

Middlewares implement the `NexusMiddleware` interface which uses the `NexusMiddlewareEvent` that contains a bit more methods than the traditional `NexusCommandEvent`:
- `next`: tells Nexus to skip to the next middleware, **does not require to be called response of a middleware.**
- `stop`: tells Nexus to stop the command from executing, this causes an `Interaction has failed` result in the Discord client of the user.
- `stop(NexusMesage)`: tells Nexus to stop the command from executing together with a message to be sent.
- `stopIf(boolean, NexusMessage?)`: tells Nexus to stop the command from executing if the boolean is true, and sends a message when provided.
- `stopIf(Predicate, NexusMessage?)`: same as above but just a predicate.

#### 💭 Deferred Middleware Responses

Nexus now supports two primary ways of deferring responses in middlewares, but neither of them will auto-defer for commands, therefore, it is 
still your responsibility to use deferred responses in the commands themselves. After understanding that, let us look into the two primary ways 
that one can defer responses in middlewares:

##### Manual Deferring

You can manually defer middlewares by using the `defer` or `deferEphemeral` followed by a response such as `stop(NexusMessage)`. An example of 
a middleware that defers is as follows:
```kotlin
NexusInterceptor.middleware { event -> 
    event.deferEphemeral().join()
    event.stop(NexusMessage.from("I have deferred the response!"))
}
```

##### Automatic Defers

Automatic defers is a newer feature of Nexus wherein deferring of middlewares is automated. To use this, you have to first enable it on the 
configuration by using:
```kotlin
Nexus.configuration.interceptors.autoDeferMiddlewareResponses = true
```

Once enabled, all middleware responses should automatically defer if the execution time has surpassed 2.5 seconds, such as this:
```kotlin
NexusInterceptor.middleware { event -> 
    Thread.sleep(3000)
    event.stop(NexusMessage.from("I have deferred the response!"))
}
```

You can even configure more properties such as whether to make the deferred responses ephemeral or when to set an automatic 
defer by setting either of the properties:
```kotlin
// Default values
Nexus.configuration.global.autoDeferAfterMilliseconds = 2350
Nexus.configuration.interceptors.autoDeferAsEphemeral = true
```

> **Warning**
>
> As stated above, it is your responsibility to use deferred responses in the commands after enabling this. Nexus 
> will not defer your command responses automatically, you should use methods such as `event.respondLater()` or `event.respondLaterAsEphemeral()` 
> to handle these cases, otherwise, you can have Nexus auto-defer for you.
> 
> To learn more about auto-deferring responses, you can read [Auto-deferring Responses](#-auto-deferring-responses).

#### 💭 Interceptor Repositories

Another way of creating interceptors is through a repository, and you can do that by creating a class that extends the `NexusInterceptorRepository` which 
contains default methods for creating interceptors in a faster way. We recommend creating interceptors in a manner such as this:
```kotlin
object SampleInterceptorRepository: NexusInterceptorRepository() {
    val SERVER_ONLY = "nexus.auth.server"
    
    override fun define() {
        middleware(SERVER_ONLY) { event -> event.stopIf(event.server.isEmpty) }
    }
}
```

You can then tell Nexus to register all the middlewares by doing the following:
```kotlin
NexusCommandInterceptor.addRepository(SampleInterceptorRepository)
```

One of the reasons that the above is recommended to build repositories is because we can immediately use the variable to include the middleware, for example:
```kotlin
object PingCommand: NexusHandler {
    val name: String = "ping"
    val description: String = "Ping, Pong!"
    val middlewares = NexusCommand.createMiddlewares(SampleInterceptorRepository.SERVER_ONLY)

    override fun onEvent(event: NexusCommandEvent) {
        val server = event.server.orElseThrow()
        event.respondNowWith("Hello ${server.name}!")
    }
}
```

And now, the command is guaranteed to be executed only when the server is present. But, did you know that you can do more with interceptors. You can add custom properties 
to commands that interceptors can access using the `@Share` annotation, which looks like this:
```kotlin
object PingCommand: NexusHandler {
    val name: String = "ping"
    val description: String = "Ping, Pong!"
    val middlewares = NexusCommand.createMiddlewares(SampleInterceptorRepository.SERVER_ONLY, SampleInterceptorRepository.DEVELOPER_LOCK)
    @Share val DEVELOPER_ONLY = false

    override fun onEvent(event: NexusCommandEvent) {
        val server = event.server.orElseThrow()
        event.respondNowWith("Hello ${server.name}!")
    }
}

object SampleInterceptorRepository: NexusInterceptorRepository() {
    val SERVER_ONLY = "nexus.auth.server"
    val DEVELOPER_LOCK = "nexus.auth.developer"

    override fun define() {
        middleware(SERVER_ONLY) { event -> event.stopIf(event.server.isEmpty) }
        middleware(DEVELOPER_LOCK) { event -> 
            val locked = event.command.get("DEVELOPER_ONLY", Boolean::class.java).orElse(false)
            event.stopIf(locked)
        }
    }
}
```

#### 💭 Basic Subcommand Router

Nexus offers a simple, basic subcommand router that is minimal and doesn't add much if not any overhead or complexity to 
your code. It is intended for the very basic needs and can be extended to your liking and serves as a base example for 
creating subcommand routers in Nexus.

To learn more about how to use the basic router, you can check our examples:
- [Basic Subcommand Router](examples/router)

To learn about how we built the subcommand router, you can check the source code at:
- [Subcommand Router](src/main/java/pw/mihou/nexus/features/command/router)

#### 💭 Option Validation

Nexus offers a near-barebones way of validating options without doing much code-duplication. It isn't the most flexible, 
but it suits most developers' needs and prevents a lot of potential code duplication. 

To learn more about how to use the option validation, you can check our example:
- [Option Validators](examples/option_validators)

#### 💭 Auto-deferring Responses

Nexus supports auto-deferring of responses in both middlewares and commands, but before that, we have to understand a 
thing with slash commands and Nexus, and that is the three-second response requirement before defer. In Nexus, there are two core 
features that can haggle up that three-second response requirement and that are:
1. Middlewares
2. Your actual command itself

And to solve an issue where the developer does not know which feature exactly causes an auto-defer, Nexus introduces auto-deferring, but 
it requires you to enable the feature on both middlewares and the command itself. To enable auto-defer in middlewares, you can check 
the [Deferred Middleware Responses](#-deferred-middleware-responses) section.

To enable auto-deferring in commands themselves, you have to use the `event.autoDefer(ephemeral, function)` method instead of the 
other related methods. It is recommended to actually use this especially when you have long-running middlewares because this will 
also take care of handling when a middleware actually requests for deferred response.

An example of how this looks is:
```kotlin
override fun onEvent(event: NexusCommandEvent) {
    event.autoDefer(ephemeral = true) { 
        // ... imagine something long running task
        return@autoDefer NexusMessage.with { 
            setContent("Hello!")
        }
    }
}
```

If you want to receive the response from Discord, it is possible by actually handling the response of the `autoDefer` method:
```kotlin
override fun onEvent(event: NexusCommandEvent) {
    event.autoDefer(ephemeral = true) { 
        // ... imagine something long running task
        return@autoDefer NexusMessage.with { 
            setContent("Hello!")
        }
    }.thenAccept { response -> 
        // Updater is only available if the message went through deferred response.
        val updater = response.updater
        // Using `getOrRequestMessage` actually calls `InteractionOriginalResponseUpdater.update()` if the interaction 
        // answered non-deferred since Javacord or Discord does not offer getting the actual message immediately from 
        // the response.
        val message = response.getOrRequestMessage()
    }
}
```

To configure when to defer, you can configure the following setting:
```kotlin
// Default values
Nexus.configuration.global.autoDeferAfterMilliseconds = 2350
```

#### 💭 Command Synchronizations

Nexus brings together a simple and straightforward method of synchronization for commands. To synchronize commands, all you 
have to do is adding the following line:
```kotlin
Nexus.synchronizer.synchronize()
```

> **Warning**
> Due to the nature of commands being able to have multiple servers, Nexus uses custom handling for Futures and this 
> includes handling errors. To handle errors, please add `.addTaskErrorListener(...)` which is similar to `.exceptionally(...)`
> while `.addTaskCompletionListener(...)` is similar to `.thenAccept(...)` although is scoped towards a single task.
> 
> If you want to listen to the actual completion of all tasks, you have to use the `.addFinalTaskCompletionListener(...)` instead.

To learn more about how to use the synchronizer to do many things, you can check our example:
- [Command Synchronization](examples/synchronization)
