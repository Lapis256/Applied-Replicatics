package dev.lapis256.apprep.mixin.replication;

import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.task.IReplicationTask;
import com.buuz135.replication.network.MatterNetwork;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.lapis256.apprep.api.replication.matter_network.MatterNetworkListener;
import dev.lapis256.apprep.api.replication.matter_network.MatterNetworkListenerHolder;
import dev.lapis256.apprep.mixin_impl.replication.MixinImplMatterNetwork;
import dev.lapis256.apprep.mixin_impl.replication.MixinImplMatterNetworkKt;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MatterNetwork.class)
public class MixinMatterNetwork implements MatterNetworkListenerHolder {
    @Unique
    private final MixinImplMatterNetwork apprep$impl = new MixinImplMatterNetwork();

    @Override
    public boolean apprep$addListener(@NotNull MatterNetworkListener listener) {
        return apprep$impl.addListener(listener);
    }

    @Override
    public boolean apprep$removeListener(@NotNull MatterNetworkListener listener) {
        return apprep$impl.removeListener(listener);
    }

    @Definition(id = "add", method = "Ljava/util/List;add(Ljava/lang/Object;)Z")
    @Definition(id = "matterStacksHolders", field = "Lcom/buuz135/replication/network/MatterNetwork;matterStacksHolders:Ljava/util/List;")
    @Expression("this.matterStacksHolders.add(?)")
    @ModifyExpressionValue(method = "update", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean apprep$onMatterStacksHolderAdded(boolean added) {
        apprep$impl.onAddedTanksSupplier();
        return added;
    }

    @Definition(id = "add", method = "Ljava/util/List;add(Ljava/lang/Object;)Z")
    @Definition(id = "matterStacksSuppliers", field = "Lcom/buuz135/replication/network/MatterNetwork;matterStacksSuppliers:Ljava/util/List;")
    @Expression("this.matterStacksSuppliers.add(?)")
    @ModifyExpressionValue(method = "update", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean apprep$onMatterStackSupplierAdded(boolean added) {
        apprep$impl.onAddedTanksSupplier();
        return added;
    }

    @Definition(id = "add", method = "Ljava/util/List;add(Ljava/lang/Object;)Z")
    @Definition(id = "matterStacksConsumers", field = "Lcom/buuz135/replication/network/MatterNetwork;matterStacksConsumers:Ljava/util/List;")
    @Expression("this.matterStacksConsumers.add(?)")
    @ModifyExpressionValue(method = "update", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean apprep$onMatterStacksConsumerAdded(boolean added) {
        apprep$impl.onAddedTanksSupplier();
        return added;
    }

    @Definition(id = "remove", method = "Ljava/util/List;remove(Ljava/lang/Object;)Z")
    @Definition(id = "matterStacksHolders", field = "Lcom/buuz135/replication/network/MatterNetwork;matterStacksHolders:Ljava/util/List;")
    @Expression("this.matterStacksHolders.remove(?)")
    @ModifyExpressionValue(method = "removeElement", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean apprep$onMatterStackHolderRemoved(boolean removed) {
        apprep$impl.onRemovedTanksSupplier(removed);
        return removed;
    }

    @Definition(id = "remove", method = "Ljava/util/List;remove(Ljava/lang/Object;)Z")
    @Definition(id = "matterStacksSuppliers", field = "Lcom/buuz135/replication/network/MatterNetwork;matterStacksSuppliers:Ljava/util/List;")
    @Expression("this.matterStacksSuppliers.remove(?)")
    @ModifyExpressionValue(method = "removeElement", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean apprep$onMatterStackSupplierRemoved(boolean removed) {
        apprep$impl.onRemovedTanksSupplier(removed);
        return removed;
    }

    @Definition(id = "remove", method = "Ljava/util/List;remove(Ljava/lang/Object;)Z")
    @Definition(id = "matterStacksConsumers", field = "Lcom/buuz135/replication/network/MatterNetwork;matterStacksConsumers:Ljava/util/List;")
    @Expression("this.matterStacksConsumers.remove(?)")
    @ModifyExpressionValue(method = "removeElement", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean apprep$onMatterStackConsumerRemoved(boolean removed) {
        apprep$impl.onRemovedTanksSupplier(removed);
        return removed;
    }

    @Inject(method = "onTankValueChanged", at = @At("TAIL"))
    private void apprep$onTankValueChanged(IMatterType matterType, CallbackInfo ci) {
        apprep$impl.onTankValueChanged();
    }

    @Definition(id = "add", method = "Ljava/util/List;add(Ljava/lang/Object;)Z")
    @Definition(id = "chipSuppliers", field = "Lcom/buuz135/replication/network/MatterNetwork;chipSuppliers:Ljava/util/List;")
    @Expression("this.chipSuppliers.add(?)")
    @ModifyExpressionValue(method = "update", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean apprep$onChipSupplierAdded(boolean added) {
        apprep$impl.onAddedChipSupplier();
        return added;
    }

    @Definition(id = "remove", method = "Ljava/util/List;remove(Ljava/lang/Object;)Z")
    @Definition(id = "chipSuppliers", field = "Lcom/buuz135/replication/network/MatterNetwork;chipSuppliers:Ljava/util/List;")
    @Expression("this.chipSuppliers.remove(?)")
    @ModifyExpressionValue(method = "removeElement", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean apprep$onChipSupplierRemoved(boolean removed) {
        apprep$impl.onRemovedChipSupplier(removed);
        return removed;
    }

    @Inject(method = "onChipValuesChanged", at = @At("TAIL"))
    private void apprep$onChipValuesChanged(CallbackInfo ci) {
        apprep$impl.onChipValuesChanged();
    }

    @Inject(method = "cancelTask", at = @At(value = "INVOKE", target = "Lcom/buuz135/replication/api/task/IReplicationTask;getStoredMatterStack()Ljava/util/HashMap;"))
    private void apprep$onCancelTask(String task, Level level, CallbackInfo ci, @Local(name = "replicationTask") IReplicationTask replicationTask) {
        MixinImplMatterNetworkKt.returnInternalMatterStacks(level, replicationTask);
    }
}
