# Passing States (Writables)

As writables aren't simple properties, we can't just simply pass them to another function outside of the `Nexus.R` scope and expect it to work the same 
especially since Kotlin doesn't support mutable function arguments. As such, in this example, we demonstrate how to properly pass writables from the 
`Nexus.R` scope to another function by passing the Delegate class (`React.Writable`) itself. 

In the example, we also demonstrate how the `React.Writable` can then be delegated again inside the other function and 
will still re-render the message when needed.