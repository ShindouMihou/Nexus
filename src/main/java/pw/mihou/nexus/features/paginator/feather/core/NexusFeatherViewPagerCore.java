package pw.mihou.nexus.features.paginator.feather.core;

import pw.mihou.nexus.features.paginator.feather.facades.NexusFeatherViewPager;

public class NexusFeatherViewPagerCore implements NexusFeatherViewPager {

    private final String key;
    private final String type;

     public NexusFeatherViewPagerCore(String key, String type) {
        this.key = key;
        this.type = type;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getType() {
        return type;
    }
}
