package dev.lapis256.apprep.mixin.ae2;

import appeng.api.stacks.AEKey;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import dev.lapis256.apprep.common.compat.LegacyMatterKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "appeng.api.stacks.AEKey$1", remap = false)
public abstract class LegacyMatterKeyMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private <T> void apprep$recoverLegacyMatter(DynamicOps<T> ops, MapLike<T> input,
            DataResult<AEKey> decoded, CallbackInfoReturnable<DataResult<AEKey>> callback) {
        if (decoded.isError()) {
            LegacyMatterKeys.recover(ops, input).ifPresent(key ->
                    callback.setReturnValue(DataResult.success(key, Lifecycle.stable())));
        }
    }
}
