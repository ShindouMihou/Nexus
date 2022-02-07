package pw.mihou.nexus.features.paginator.core;

import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;

public abstract class NexusPaginatorEventCore implements ButtonClickListener {

    @Override
    public abstract void onButtonClick(ButtonClickEvent buttonClickEvent);

}
