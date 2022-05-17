# 🍰 Nexus Framework
The formal successor of [Velen](https://github.com/ShindouMihou/Velen) which takes a more OOP (Object Oriented Programming) approach to integrating slash commands onto your Discord bot without the need of builders. Nexus is slightly inspired by Laravel and is aimed to be more efficient than Velen at performing certain tasks.

## 🌉 Dependencies
Nexus doesn't enforce other dependencies other than the latest stable version of Javacord. Every development version of Javacord will have a branch of Nexus that is dedicated to compatiability changes (if the development version includes a breaking change), we recommend including any SLF4J-compatiable logging framework although Nexus supports adapters for custom logging. 
- [💻 Logging](https://github.com/ShindouMihou/Nexus/#-Logging)

## 📦 Installation
The framework doesn't have any plans of moving to Maven Central at this moment, as such, it is recommended to use [Jitpack.io](https://jitpack.io/#pw.mihou/Nexus) to install the framework onto your project. Please follow the instructions written there.
- [pw.mihou.Nexus](https://jitpack.io/#pw.mihou/Nexus)

# 🧑‍🎨 Artisan

## 🌸 Your Nexus instance
To start with using Nexus, one must create a global Nexus instance. It is recommended to place this instance as a **`public static`** field on your Main class or similar that can be accessed at any time without needing to recreate the instance.
```java
public class Main {

  public static Nexus NEXUS;

  public static void main(String[] args) {
     NEXUS = Nexus.builder().build();
  }

}
```

You can configure the message configuration of Nexus and related configuration (e.g. maximum lifespan of an cross-shard Nexus request) from the [`NexusBuilder`](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/core/builder/NexusBuilder.java). One can also set the creation of the `DiscordApi` instances from here which also allows the shard to be included in the [`NexusShardManager`](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/core/managers/NexusShardManager.java)

For developers that are not planning on using Nexus' DiscordApi creation methods then we recommend adding this line on every shard startup:
```java
Main.NEXUS.getShardManager().put(api);
```

And for every shard removal:
```java
Main.NEXUS.getShardManager().remove(shardNumber);
```

These methods allows Nexus to perform command synchronization and similar (**REQUIRED for command synchronization**). You can view an example of those methods in use from below. 
- [Synchronization Example](https://github.com/ShindouMihou/Nexus/blob/master/examples/synchronization/Main.java)

## 🫓 Fundamentals of creating commands
You can design commands in Nexus simply by creating a class that implements the [`NexusHandler`](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/facade/NexusHandler.java) interface before creating two required `String` fields named: `name` and `description` which are required to create slash commands.
```java
public class PingCommand implements NexusHandler {

   private final String name = "ping"
   private final String description = "Ping pong!"
   
   @Override
   public void onEvent(NexusCommandEvent event) {}

}
```
When creating class fields, one must ensure that it doesn't conflict with the names of the variables found in [NexusCommandCore](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/core/NexusCommandCore.java) that has the `@Required` or `@WithDefault` annotations as these are options that must be set or can be overriden by defining these fields with the proper types on your class.

For instance, we want to include the option of making our command ephemeral then all we need to do is define a `options` field with `List<SlashCommandOption>` before defining the value of the field with the ephemeral option.

```java
public class PingCommand implements NexusHandler {

   private final String name = "ping"
   private final String description = "Ping pong!"
   private final List<SlashCommandOption> options = List.of(
      SlashCommandOption.create(
           SlashCommandOptionType.BOOLEAN, "ephemeral", "Whether to make the command ephemeral or not.", false
      )
   );
   
   @Override
   public void onEvent(NexusCommandEvent event) {}

}
```

All fields with the exception of those defined in the [NexusCommandCore](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/core/NexusCommandCore.java) are not visible to interceptors but can be made visible by including the `@Share` annotation before the command which places the field into an immutable Map (whatever the field's value is upon generation will be the final value known to Nexus). You can see an example of this on the Authentication middlewares example:
- [Authentication Examples](https://github.com/ShindouMihou/Nexus/tree/master/examples/authentication)
- [Permissions Authentication Middleware](https://github.com/ShindouMihou/Nexus/blob/f8942c4eca80ea5da92a71c14ae3b6d12cbf0e79/src/main/java/pw/mihou/nexus/features/command/interceptors/commons/modules/auth/NexusAuthMiddleware.java#L21)
- [Role Authentication Middleware](https://github.com/ShindouMihou/Nexus/blob/f8942c4eca80ea5da92a71c14ae3b6d12cbf0e79/src/main/java/pw/mihou/nexus/features/command/interceptors/commons/modules/auth/NexusAuthMiddleware.java#L59)
- [User Authentication Middleware](https://github.com/ShindouMihou/Nexus/blob/f8942c4eca80ea5da92a71c14ae3b6d12cbf0e79/src/main/java/pw/mihou/nexus/features/command/interceptors/commons/modules/auth/NexusAuthMiddleware.java#L102)

You can also view a demonstration of "Shared Fields" from the shared_fields example:
- [Shared Fields Example](https://github.com/ShindouMihou/Nexus/tree/master/examples/shared_fields)

> We do not recommend using `event.getInteraction().respondLater()` or similar methods but instead use the `event.respondLater()` methods which **respects middlewares**. 
> To explain this further, middlewares are allowed to request to Discord for an extension time to process the middleware's functions which is done through Nexus and this creates an `InteractionOriginalResponseUpdater` that Nexus stores for other middlewares to use or for the command to use. 
> This is explained more on **Intercepting Commands**

After creating the command class, you can then tell Nexus to include it by using:
```java
Nexus nexus = ...;
nexus.listenOne(new SomeCommand());
```

You can also create a `NexusCommand` without enabling the event dispatcher (for cases when you want to see the result of the command generation) by using:
```java
Nexus nexus = ...;
NexusCommand command = nexus.defineOne(new SomeCommand());
```

## ♒ Intercepting Commands
Nexus includes the ability to intercept specific commands by including either the `middlewares` or `afterwares` field that takes a `List<String>` with the values inside the `List` being the key names of the interceptors. The framework provides several middlewares by default that you can add to your command or globally (via the `Nexus.addGlobalMiddleware(...)` method).
- [Common Interceptors](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/interceptors/commons/NexusCommonInterceptors.java)
- [Common Interceptors Implementation](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/interceptors/commons/core/NexusCommonInterceptorsCore.java)
- [Nexus Auth Middlewares Implementation](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/interceptors/commons/modules/auth/NexusAuthMiddleware.java)

You can create your own middleware by using the method: `NexusCommandInterceptor.addMiddleware("middleware.name", event -> ...)`

You can create your own afterware by using the method: `NexusCommandInterceptor.addAfterware("afterware.name", event -> ...)`

These two methods utilizes Java's Lambdas to allow creating of the interceptors without the need of creating a new class but if you want to move the handling to their own classes then you can simply implement the [`NexusMiddleware`](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/interceptors/facades/NexusMiddleware.java) or [`NexusAfterware`](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/interceptors/facades/NexusAfterware.java) interface.
```java
public class SomeMiddleware implements NexusMiddleware {

    @Override
    public void onBeforeCommand(NexusMiddlewareEvent event) { }

}
```

Middlewares can control the execution of a command by utilizing the methods that [`NexusMiddlewareEvent`](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/facade/NexusMiddlewareEvent.java) provides which includes:
- `next()` : Tells Nexus to move forward to the next middleware to process if any, otherwise executes the command.
- `stop()` : Stops the command from executing, this causes an `Interaction has failed` result in the Discord client of the user.
- `stop(NexusMessage)` : Stops the command from executing with a response sent which can be either an Embed or a String.
- `stopIf(boolean, NexusMessage)` : Same as above but only stops if the boolean provided is equals to true.
- `stopIf(Predicate, NexusMessage)`: Same as above but evaluates the result from the function provided with Predicate.
- `stopIf(Predicate)` : Same as above but doesn't send a message response.

You do not need to tell Nexus to do `next()` since the default response of a middleware will always be `next()`.
```java
public class ServerOnlyMiddleware implements NexusMiddleware {

    @Override
    public void onBeforeCommand(NexusMiddlewareEvent event) {
       event.stopIf(event.getServer().isEmpty());
    }

}
```
The above tells a simple example of a middleware the prevents the execution if the server is not present.
```java
Nexus nexus = ...
NexusCommandInterceptor.addMiddleware("middlewares.gate.server", new ServerOnlyMiddleware());
nexus.addGlobalMiddleware("middlewares.gate.server");
```

Command Interceptors doesn't have any set of rules but we recommend following a similar scheme as package-classes of Java (e.g. `nexus.auth.permissions`) to make it easier to read but once again it is not required.

Interceptors can access shared fields of a command as long as the field is defined with `@Share` which then will allow the interceptor to freely access it via the `event.getCommand().get("fieldName", Type.class)` which returns an `Optional<Type>`. You can see examples of these being used from the following references:
- [Permissions Authentication Middleware](https://github.com/ShindouMihou/Nexus/blob/f8942c4eca80ea5da92a71c14ae3b6d12cbf0e79/src/main/java/pw/mihou/nexus/features/command/interceptors/commons/modules/auth/NexusAuthMiddleware.java#L21)
- [Role Authentication Middleware](https://github.com/ShindouMihou/Nexus/blob/f8942c4eca80ea5da92a71c14ae3b6d12cbf0e79/src/main/java/pw/mihou/nexus/features/command/interceptors/commons/modules/auth/NexusAuthMiddleware.java#L59)
- [User Authentication Middleware](https://github.com/ShindouMihou/Nexus/blob/f8942c4eca80ea5da92a71c14ae3b6d12cbf0e79/src/main/java/pw/mihou/nexus/features/command/interceptors/commons/modules/auth/NexusAuthMiddleware.java#L102)
- [Shared Fields Example](https://github.com/ShindouMihou/Nexus/tree/master/examples/shared_fields)

Middlewares that can take more than 3 seconds to complete should always use a delayed response which tells Nexus to generate a `InteractionOriginalResponseUpdater` for the command to use. A delayed response doesn't mean that the interaction is already answered, it just tells Discord that we are taking a bit longer to respond. You can use the following methods to perform a delayed response:
- `askDelayedResponse()`: Asks for a delayed response, this won't allow you to respond to the command directly.
- `askDelayedResponseAsEphemeral()`: Same as above but as ephemeral.
- `askDelayedResponseAsEphemeralIf(boolean)`: Same as above but responds as an ephemeral if the boolean is true.
- `askDelayedResponseAsEphemeralIf(Predicate)`: Same as above but evaluates the boolean from the Predicate.

You are free to answer the interaction yourself but it will not be communicated cross-middleware and to the command. The methods above will allow middlewares and the command to use a single `InteractionOriginalResponseUpdater`.

## 🏜️ Synchronizing Commands
Nexus includes built-in synchronization methods for slash commands that are modifiable to one's liking. To understand how to synchronize commands to Discord, please visit our examples instead:
- [Synchronization Example](https://github.com/ShindouMihou/Nexus/tree/master/examples/synchronization)

You can modify the synchronization methods by implementing the following interface:
- [NexusSynchronizeMethods](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/synchronizer/overwrites/NexusSynchronizeMethods.java)

For reference, you can view the default methods that Nexus uses:
- [NexusDefaultSynchronizeMethods](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/synchronizer/overwrites/defaults/NexusDefaultSynchronizeMethods.java)

After defining your own synchronize methods, you can then add this line at startup:
```java
NexusSynchronizer.SYNCHRONIZE_METHODS.set(<Your Synchronize Methods Class>);
```

What is the use-case for modifying the synchronize methods?
- [x] Customizable synchronize methods were implemented due to the requirements of one of the bots under development that uses custom `bulkOverwrite...` methods that aren't included in the official Javacord fork. Nexus needs to adapt to those requirements as well and therefore synchronize methods are completely customizable.

The default synchronize methods should be more than enough for a bot that runs on a single cluster but for easier multi-cluster usage, it is recommended to use only one cluster to handle the synchronization of commands with a custom Javacord fork that allows for bulk-overwriting and updating of slash commands in servers using only the server id. 
- You can refer to [BeemoBot/Javacord#1](https://github.com/BeemoBot/Javacord/pull/1) for more information.

## 💻 Logging
The framework logs through SLF4J by default but one can also use our console logging adapter to log to console in a similar format to Javacord's fallback logger. You can read more about how to use the console logging adapter on [v1.0.0-alpha3.0.6 Release Notes](https://github.com/ShindouMihou/Nexus/releases/tag/v1.0.0-alpha3.06).

One can also create their own logging adapter by creating a class that implements [`NexusLoggingAdapter`](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/core/logger/adapters/NexusLoggingAdapter.java) and routing Nexus to use those methods.
```java
Nexus.setLogger(<Your Logging Adapter Class Here>);
```

### ⏰ Rate-limiting
You can rate-limit or add cooldowns to commands by including a simple `Duration cooldown = ...` field onto your command and including the `NexusCommonInterceptors.NEXUS_RATELIMITER` onto the list of middlewares of a command. The rate-limiter is made into a middleware to allow for custom implementations with the cooldown accessible from the [`NexusCommand`](https://github.com/ShindouMihou/Nexus/blob/master/src/main/java/pw/mihou/nexus/features/command/facade/NexusCommand.java) instance directly.

## 📰 Pagination
Nexus includes a simple pagination implementation which one can use easily, you can view an example implementation of this from the examples below:
- [Poem Command](https://github.com/ShindouMihou/Nexus/blob/master/examples/PoemCommand.java)

# 🌇 Nexus is used by
- [Mana](https://manabot.fun): The original reason for Nexus' creation, Mana is an anime Discord bot that brings anime into communities.
- More to be added, feel free to create an issue if you want to add yours here!

# 📚 License
Nexus follows Apache 2.0 license which allows the following permissions:
- ✔ Commercial Use
- ✔ Modification
- ✔ Distribution
- ✔ Patent use
- ✔ Private use

The contributors and maintainers of Nexus are not to be held liability over any creations that uses Nexus. We also forbid trademark use of
the library and there is no warranty as stated by Apache 2.0 license. You can read more about the Apache 2.0 license on [GitHub](https://github.com/ShindouMihou/Nexus/blob/master/LICENSE).
