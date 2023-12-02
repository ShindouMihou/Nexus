# Components

Reusing code is definitely one of the things that we'd like to do, and we can do so under 
Nexus.R by simply creating an extension function over `React.Component` which would allow you to reuse 
code easily. Although, it is noted that there are some drawbacks to this.

### Drawbacks
1. You cannot simply create `Writable` as components are ran inside the `render` function which  will re-render 
the entire component, meaning that creating `Writable` inside `render` will result in the `Writable` being recreated instead.
2. Methods such as `onInitialRender` and `onRender` are not available. Components are simply functions ran inside `render`
which means anything inside cannot exist  before the function itself is called  which is also when the function happens to 
render.

Other than aforementioned drawbacks, components are an incredibly easy way to reuse code for cases such as 
hybrid command bots i.e. bots that uses both slash and message commands.