package dev.lapis256.apprep.mixin.replication;

import com.buuz135.replication.api.task.IReplicationTask;
import com.buuz135.replication.block.tile.ReplicatorBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost;
import dev.lapis256.apprep.mixin_impl.replication.MixinImplReplicatorBlockEntityKt;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ReplicatorBlockEntity.class)
public abstract class MixinReplicatorBlockEntity {
    @Shadow(remap = false)
    private IReplicationTask cachedReplicationTask;

    @ModifyExpressionValue(method = "setRemoved", at = @At(value = "FIELD", target = "Lcom/buuz135/replication/block/tile/ReplicatorBlockEntity;cachedReplicationTask:Lcom/buuz135/replication/api/task/IReplicationTask;", opcode = Opcodes.GETFIELD, ordinal = 0, remap = false))
    private IReplicationTask apprep$keepTaskOnChunkUnload(IReplicationTask original) {
        return MixinImplReplicatorBlockEntityKt.keepTaskOnChunkUnload(original, (ReplicatorBlockEntity) (Object) this);
    }

    @ModifyExpressionValue(method = "replicateItem", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;equals(Ljava/lang/Object;)Z"))
    private boolean apprep$setConnectorHost(boolean original, @Share("connectorHost") LocalRef<ReplicationConnectorLogicHost> hostRef) {
        return MixinImplReplicatorBlockEntityKt.setConnectorHost(original, ((ReplicatorBlockEntity) (Object) this), hostRef, cachedReplicationTask);
    }

    @WrapOperation(method = "replicateItem", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraftforge/items/ItemHandlerHelper;insertItem(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;", ordinal = 1))
    private ItemStack apprep$insertResultToConnector(IItemHandler i, ItemStack dest, boolean stack, Operation<ItemStack> original,
            @Share("connectorHost") LocalRef<ReplicationConnectorLogicHost> hostRef) {
        if (MixinImplReplicatorBlockEntityKt.insertResultToConnector(hostRef.get(), cachedReplicationTask)) {
            return ItemStack.EMPTY;
        }
        return original.call(i, dest, stack);
    }
}
