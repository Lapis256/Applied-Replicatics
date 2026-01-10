package dev.lapis256.apprep.mixin.titanium;

import com.hrznstudio.titanium.block_network.Network;
import com.hrznstudio.titanium.block_network.element.NetworkElement;
import com.llamalad7.mixinextras.sugar.Local;
import dev.lapis256.apprep.api.titanium.network_element.NetworkElementListener;
import dev.lapis256.apprep.api.titanium.network_element.NetworkElementListenerHolder;
import dev.lapis256.apprep.mixin_impl.titanium.MixinImplNetworkElement;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(NetworkElement.class)
public class MixinNetworkElement implements NetworkElementListenerHolder {
    @Shadow
    protected Network network;

    @Unique
    private final MixinImplNetworkElement apprep$impl = new MixinImplNetworkElement();

    @Override
    public boolean apprep$addListener(@NotNull NetworkElementListener listener) {
        return apprep$impl.addListener(listener);
    }

    @Override
    public boolean apprep$removeListener(@NotNull NetworkElementListener listener) {
        return apprep$impl.removeListener(listener);
    }

    @Inject(method = "joinNetwork", at = @At("TAIL"))
    private void apprep$onJoinNetwork(CallbackInfo ci, @Local(argsOnly = true) Network network) {
        apprep$impl.onAddedNetwork(network);
    }

    @Inject(method = "leaveNetwork", at = @At("HEAD"))
    private void apprep$onLeaveNetwork(CallbackInfo ci) {
        apprep$impl.onRemoveNetwork(network);
    }
}
