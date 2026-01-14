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
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(ReplicatorBlockEntity.class)
public abstract class MixinReplicatorBlockEntity {
    @Shadow
    private IReplicationTask cachedReplicationTask;

    @ModifyExpressionValue(method = "replicateItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;equals(Ljava/lang/Object;)Z"))
    private boolean apprep$setConnectorHost(boolean original, @Share("connectorHost") LocalRef<ReplicationConnectorLogicHost> hostRef) {
        return MixinImplReplicatorBlockEntityKt.setConnectorHost(original, ((ReplicatorBlockEntity) (Object) this), hostRef, cachedReplicationTask);
    }

    @WrapOperation(method = "replicateItem", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/items/ItemHandlerHelper;insertItem(Lnet/neoforged/neoforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;", ordinal = 3))
    private ItemStack apprep$insertResultToConnector(IItemHandler i, ItemStack dest, boolean stack, Operation<ItemStack> original, @Share("connectorHost") LocalRef<ReplicationConnectorLogicHost> hostRef) {
        if (MixinImplReplicatorBlockEntityKt.insertResultToConnector(hostRef.get(), cachedReplicationTask)) {
            return ItemStack.EMPTY;
        }
        return original.call(i, dest, stack);
    }
}
