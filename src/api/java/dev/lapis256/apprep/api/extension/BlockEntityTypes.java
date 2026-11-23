package dev.lapis256.apprep.api.extension;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntityTypes {
    private BlockEntityTypes() {
    }

    public static <T extends BlockEntity> BlockEntityType<T> withoutDataFixer(BlockEntityType.Builder<T> builder) {
        // Forge uses null for mod block entities without a vanilla DataFixer schema.
        return builder.build(null);
    }
}
