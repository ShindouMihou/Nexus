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
3. [Commnad Interceptors](#-command-interceptors)
4. [Basic Subcommand Router](#-basic-subcommand-router)
5. [Option Validation](#-option-validation)
6. [Command Synchronizations](#-command-synchronizations)

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
        event.respondNowWith("Hello ${server.name}!")
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

#### 💭 Command Synchronizations

Nexus brings together a simple and straightforward method of synchronization for commands. To synchronize commands, all you 
have to do is adding the following line:
```kotlin
Nexus.synchronizer.synchronize()
```

> **Warning**
> Due to the nature of commands being able to have multiple servers, Nexus uses custom handling for Futures and this 
> includes handling errors. To handle errors, please add `.addTaskErrorListener(...)` which is similar to `.exceptionally(...) 
> while `.addTaskCompletionListener(...)` is similar to `.thenAccept(...)` although is scoped towards a single task.
> 
> If you want to listen to the actual completion of all tasks, you have to use the `.addFinalTaskCompletionListener(...)` instead.

To learn more about how to use the synchronizer to do many things, you can check our example:
- [Command Synchronization](examples/synchronization)
