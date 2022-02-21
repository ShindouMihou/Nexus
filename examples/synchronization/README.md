 # 🍓 Synchronization and Dynamic Commands
 Nexus now supports dynamic updating of server slash commands and also a better synchronization that doesn't require 
 you to find a spot to find when the final shard starts before you could start synchronizing which is powered by Nexus's 
 entire new shard wrapping engine: EngineX.
 
The new synchronization function of Nexus works by utilizing the power of batch override for slash commands to synchronize Discord's 
database of the bot's slash commands with what Nexus repositories knows. There are methods such as `upsert` and `delete` which goes against 
the batch override and performs single updates for cases where you aren't sure about doing a batch update.

You can view a full example of how this synchronization looks from the `Main.java` file on this folder.

# 🥞 EngineX & Synchronization
EngineX is a critical factor into the new synchronization system as it allows Nexus to queue any synchronization requests for a specific shard 
or for any shard available to take without the end-user finding a way where to place the synchronization method. It's workings are a bit complicated but 
you can view the full source code here. 
- [View Source](https://github.com/ShindouMihou/Nexus/tree/master/src/main/java/pw/mihou/nexus/core/enginex)