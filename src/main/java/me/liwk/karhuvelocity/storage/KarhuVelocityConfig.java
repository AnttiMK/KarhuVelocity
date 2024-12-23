package me.liwk.karhuvelocity.storage;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@ConfigSerializable
@SuppressWarnings("FieldMayBeFinal")
public class KarhuVelocityConfig {

    @Comment("Configuration file version - do not modify!")
    private int version = 1;
    private String prefix = "<gray>(<aqua><b>%server%</b><gray>) ";
    private String alertMessage = "<white>%player% <gray>failed <aqua>%check% <gray>[x<aqua>%vl%<gray>]";
    private Alerts alerts = new Alerts();

    @Getter
    @ConfigSerializable
    public static class Alerts {
        private String enabled = "<aqua>Karhu <gray>alerts are now <green>enabled<gray>.";
        private String disabled = "<aqua>Karhu <gray>alerts are now <red>disabled<gray>.";
    }
}
