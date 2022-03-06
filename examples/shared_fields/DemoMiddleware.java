public class DemoMiddleware {

    public static void addMiddleware() {
        NexusCommandInterceptor.addMiddleware("nexus.demo.sharedfields", event ->
                event.stopIf(
                        event.getCommand().get("oneSharedField", String.class).isEmpty() ||
                                event.getCommand().get("onePrivateField", String.class).isPresent()
                )
        );
    }

}
