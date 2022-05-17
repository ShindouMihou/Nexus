package pw.mihou.nexus;

import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import pw.mihou.nexus.features.command.facade.NexusCommandEvent;
import pw.mihou.nexus.features.command.facade.NexusHandler;
import pw.mihou.nexus.features.paginator.NexusPaginatorBuilder;
import pw.mihou.nexus.features.paginator.enums.NexusPaginatorButtonAssignment;
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorCursor;
import pw.mihou.nexus.features.paginator.facade.NexusPaginatorEvents;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class PoemCommand implements NexusHandler {

    private String name = "poem";
    private String description = "Do you want to read a poem about life?";

    private static final List<String> texts = Arrays.asList(
            "Tell me not, in mournful numbers,\nLife is but an empty dream!\nFor the soul is dead that slumbers,\nAnd thing sare not what they seem.",
            "Life is real! Life is earnest!\nAnd teh grave is not its goal;\nDust thou art, to dust returnest,\nWas not spoken of the soul.",
            "Not enjoyment, and not sorrow,\nIs our destined end or way;\nBut to act, that each to-morrow\nFind us farther than to day.",
            "Art is long, and Time is fleeting,\nAnd our hearts, though tout and brave,\nStill, like muffled drums, are beating\nFuneral marches to the grave.",
            "In the world's broad field of battle,\nIn the bivouac of Life,\nBe not like dumb, driven cattle!\nBe a hero in the strife!",
            "Trust no Future, howe'er pleasant!\nLet the dead Past bury its dead!\nAct, act in the living PResent!\nHeart within, and God o'erhead!"
    );

    @Override
    public void onEvent(NexusCommandEvent event) {

        event.respondLater().thenAccept(responseUpdater -> new NexusPaginatorBuilder<>(texts)
                .setEventHandler(new NexusPaginatorEvents<String>() {

                    private EmbedBuilder createEmbed(NexusPaginatorCursor<String> cursor) {
                        return new EmbedBuilder()
                                .setTitle("A Poem Of Life ["+cursor.getDisplayablePosition()+"/"+cursor.getMaximumPages()+"]")
                                .setDescription(cursor.getItem())
                                .setFooter("by a certain ancient poet")
                                .setColor(Color.BLUE);
                    }

                    @Override
                    public InteractionOriginalResponseUpdater onInit(InteractionOriginalResponseUpdater updater, NexusPaginatorCursor<String> cursor) {
                        return updater.addEmbed(createEmbed(cursor));
                    }

                    @Override
                    public void onPageChange(NexusPaginatorCursor<String> cursor, ButtonClickEvent event) {
                        event.getButtonInteraction().getMessage().edit(createEmbed(cursor));
                    }

                    @Override
                    public void onSelect(NexusPaginatorCursor<String> cursor, ButtonClickEvent event) {
                        cursor.parent().getParent().destroy(event.getButtonInteraction().getMessage());
                    }
                })
                .setButton(NexusPaginatorButtonAssignment.SELECT, Button.create("customId", ButtonStyle.SUCCESS, "", "🌠"))
                .setButton(NexusPaginatorButtonAssignment.CANCEL, Button.create("customId", ButtonStyle.DANGER, "", "🔥"))
                .build()
                .send(event.getBaseEvent().getInteraction(), responseUpdater)
        );

    }

}
