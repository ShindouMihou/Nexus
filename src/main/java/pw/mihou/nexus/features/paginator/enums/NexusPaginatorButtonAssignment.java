package pw.mihou.nexus.features.paginator.enums;

import org.javacord.api.entity.message.component.ButtonStyle;

/**
 * {@link  NexusPaginatorButtonAssignment} declares the assignment of this
 * button onto the {@link  pw.mihou.nexus.features.paginator.facade.NexusPaginator}.
 */
public enum NexusPaginatorButtonAssignment {
    SELECT(ButtonStyle.SUCCESS),
    CANCEL(ButtonStyle.DANGER),
    NEXT(ButtonStyle.PRIMARY),
    PREVIOUS(ButtonStyle.PRIMARY);

    /**
     * This is the declared default button style
     * of this button which is used by {@link  pw.mihou.nexus.features.paginator.facade.NexusPaginator} when
     * a button is somehow declared as a link button.
     */
    public final ButtonStyle style;

    /**
     * Creates a new {@link  NexusPaginatorButtonAssignment} assignment.
     *
     * @param style The default button style.
     */
    NexusPaginatorButtonAssignment(ButtonStyle style) {
        this.style = style;
    }
}
