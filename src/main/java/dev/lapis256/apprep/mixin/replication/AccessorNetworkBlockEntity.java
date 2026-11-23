package dev.lapis256.apprep.mixin.replication;

import com.buuz135.replication.block.tile.NetworkBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = NetworkBlockEntity.class, remap = false)
public interface AccessorNetworkBlockEntity {
    @Accessor("unloaded")
    boolean isUnloaded();
}
