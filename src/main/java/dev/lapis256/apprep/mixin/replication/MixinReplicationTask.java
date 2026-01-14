package dev.lapis256.apprep.mixin.replication;

import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import com.buuz135.replication.api.task.ReplicationTask;
import com.buuz135.replication.calculation.MatterCompound;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.lapis256.apprep.api.replication.task.MEReplicationTask;
import dev.lapis256.apprep.mixin_impl.replication.MixinImplReplicationTask;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;


@Mixin(ReplicationTask.class)
public abstract class MixinReplicationTask implements MEReplicationTask {
    @Shadow
    public abstract HashMap<Long, List<MatterStack>> getStoredMatterStack();

    @Unique
    private final MixinImplReplicationTask apprep$impl = new MixinImplReplicationTask(this::extractMatter);

    @ModifyExpressionValue(method = "storeMatterStacksFor", at = @At(value = "INVOKE", target = "Lcom/buuz135/replication/calculation/ReplicationCalculation;getMatterCompound(Lnet/minecraft/world/item/ItemStack;)Lcom/buuz135/replication/calculation/MatterCompound;"))
    private MatterCompound apprep$extractMatterStacks(MatterCompound original, @Local(argsOnly = true) BlockPos pos, @Share("oldOriginal") LocalRef<MatterCompound> oldOriginalRef, @Cancellable CallbackInfo ci) {
        return apprep$impl.extractMatterStacks(original, pos, oldOriginalRef, ci, getStoredMatterStack());
    }

    @ModifyArg(method = "storeMatterStacksFor", at = @At(value = "INVOKE", target = "Lcom/buuz135/replication/api/matter_fluid/MatterStack;<init>(Lcom/buuz135/replication/api/IMatterType;D)V"), index = 1)
    private double apprep$restoreMatterAmount(double originalAmount, @Share("oldOriginal") LocalRef<MatterCompound> oldOriginalRef, @Local(name = "type") IMatterType type) {
        return apprep$impl.restoreMatterAmount(originalAmount, oldOriginalRef.get(), type);
    }

    @ModifyReturnValue(method = "serializeNBT(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private CompoundTag apprep$serializeNBT(CompoundTag original, @Local(argsOnly = true) HolderLookup.Provider provider) {
        return serializeAdditionalNBT(provider, original);
    }

    @Inject(method = "deserializeNBT(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void apprep$deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt, CallbackInfo ci) {
        deserializeAdditionalNBT(provider, nbt);
    }

    @Override
    public void apprep$setInternalMatterStacks(@NotNull Object2LongMap<@NotNull IMatterType> stacks) {
        apprep$impl.setInternalMatterStacks(stacks);
    }

    @Override
    public @NotNull Object2LongMap<@NotNull IMatterType> apprep$getInternalMatterStacks() {
        return apprep$impl.getInternalMatterStacks();
    }
}
