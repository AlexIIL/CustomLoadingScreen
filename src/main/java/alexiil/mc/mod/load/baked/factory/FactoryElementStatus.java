package alexiil.mc.mod.load.baked.factory;

import alexiil.mc.mod.load.event.StatusPushedEvent;

public class FactoryElementStatus extends FactoryElement {
    private final StatusPushedEvent event;

    // TODO: Add event information to the temporary map (so, if this is current, what text its has, how long ago it was,
    // etc, etc)

    public FactoryElementStatus(BakedFactoryStatus factory, StatusPushedEvent event) {
        super(factory);
        this.event = event;
    }
}
