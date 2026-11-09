package dev.lapis256.apprep.common.compat;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyTypes;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;

/** Decode-only compatibility; no legacy key type is registered or sent to clients. */
public final class LegacyMatterKeys {
    private static final Identifier CURRENT = Identifier.parse("apprep:m");
    private static final Set<String> FIELDS = Set.of("#t", "matter", "#");

    private LegacyMatterKeys() {}

    public static <T> Optional<AEKey> recover(DynamicOps<T> ops, MapLike<T> input) {
        T discriminator = input.get(AEKey.TYPE_FIELD);
        T matter = input.get("matter");
        if (discriminator == null || matter == null
                || !ops.getStringValue(discriminator).result().orElse("").equals("rep_ae2_bridge:matter")) {
            return Optional.empty();
        }
        // Unexpected payload fields may carry semantics we cannot safely translate.
        if (input.entries().anyMatch(entry -> !FIELDS.contains(ops.getStringValue(entry.getFirst()).result().orElse("")))) {
            return Optional.empty();
        }
        var current = AEKeyTypes.getAll().stream().filter(type -> type.getId().equals(CURRENT)).findFirst();
        if (current.isEmpty()) return Optional.empty();
        T translated = ops.createMap(Stream.of(Pair.of(ops.createString("type"), matter)));
        return ops.getMap(translated).flatMap(map -> current.get().codec().decode(ops, map))
                .result().map(key -> (AEKey) key);
    }
}
