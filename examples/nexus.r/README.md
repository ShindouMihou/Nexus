# nexus.R

React Native + Svelte for Discord Bots. Nexus.R is an innovative way to create and send responses or messages
to any text channel or interaction from slash commands to messages commands and even Users or text channels 
with reactivity, enabling quick and simple re-rendering of messages upon state changes.

### Features
- [x] Write-once, Use Everywhere
  - Nexus.R allows you to easily reuse responses whether it was written for slash commands or 
  for messages with hardly any changes.
- [x] States in Discord Bot
  - Nexus.R introduces the infamous states of JavaScript world into Discord bots, allowing you to create 
  messages that will re-render itself upon different state changes, just like a reactive website!
- [x] Webdev-like  Feel
  - In order to make Discord bot development more accessible to even web developers, we've made the 
  feature feel like writing web code (JSX) but  simpler!

### Table  of Contents

As Nexus.R is incredibly simple, you can read the examples and pretty much get a gist of how to use it. Here 
are the examples we think that you should read first though:
1. [**Starting out with Nexus.R**](): It's all the same, except for how it's started. *Read which one you prefer to see.*
   2. [Slash Commands](SlashCommand.kt)
   3. [Context Menus](ContextMenu.kt)
   4. [Message Events i.e. Message Commands](MessageEvent.kt)
   5. [Interactions i.e. Buttons and Select  Menus](Interaction.kt)
6. [**Passing State to Another Function**](%5B1%5D_passing_state): As states are not simply the same regular Kotlin properties, there needs a
little bit of a very tiny change when you want to pass state to another function outside of the `Nexus.R` scope.
7. [**Creating Components**](%5B2%5D_components): Reusing code is also important in coding, and components are just one way to reuse code 
in Nexus.R, but there are drawbacks. It is recommended to read this after reading the above as it talks about important points in Nexus.R.