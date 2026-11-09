package qa;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import com.buuz135.replication.ReplicationRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;

public final class LegacyKeyCases {
  private static CompoundTag legacy(String matter) {
    var tag = new CompoundTag();
    tag.putString("#t", "rep_ae2_bridge:matter");
    tag.putString("matter", matter);
    return tag;
  }

  public static void run(MinecraftServer server) throws Exception {
    int registrySize = AEKeyTypes.getAll().size();
    var ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
    var golden = new java.util.HashMap<String, CompoundTag>();
    var bridge = java.nio.file.Path.of(System.getProperty("itv.qa.legacyBridge"));
    try (var loader =
        new java.net.URLClassLoader(
            new java.net.URL[] {bridge.toUri().toURL()}, LegacyKeyCases.class.getClassLoader())) {
      Class<?> oldType =
          Class.forName("net.unfamily.repae2bridge.ae2.ReplicationMatterKey", true, loader);
      var factory = oldType.getMethod("ofId", Identifier.class);
      var codec =
          ((com.mojang.serialization.MapCodec<AEKey>) oldType.getField("MAP_CODEC").get(null))
              .codec();
      for (var id : ReplicationRegistry.MATTER_TYPES_REGISTRY.keySet()) {
        if (id.getPath().equals("empty")) continue;
        AEKey old = (AEKey) factory.invoke(null, id);
        var tag = (CompoundTag) codec.encodeStart(ops, old).getOrThrow();
        tag.putString("#t", old.getType().getId().toString());
        golden.put(id.toString(), tag);
      }
    }
    for (var id : ReplicationRegistry.MATTER_TYPES_REGISTRY.keySet()) {
      if (id.getPath().equals("empty")) continue;
      Checks.test(
          "legacy_matter_" + id,
          () -> {
            var tag = golden.get(id.toString());
            Checks.require(
                tag.equals(legacy(id.toString())),
                "Shipped old codec format differs from the expected mapping");
            var key = AEKey.CODEC.parse(ops, tag).getOrThrow();
            Checks.require(
                key.getType().getId().equals(Identifier.parse("apprep:m"))
                    && key.getId().equals(id),
                "Legacy matter not restored");
            var encoded = (CompoundTag) AEKey.CODEC.encodeStart(ops, key).getOrThrow();
            Checks.require(
                encoded.getString("#t").orElseThrow().equals("apprep:m"),
                "Legacy format was re-emitted");
            Checks.require(!encoded.contains("matter"), "Old payload leaked");
            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess());
            try {
              AEKey.writeKey(buffer, key);
              Checks.require(
                  key.equals(AEKey.readKey(buffer)), "Current client packet roundtrip failed");
            } finally {
              buffer.release();
            }
            for (long amount : new long[] {1L, 5L, 123456789L, 9007199254740993L}) {
              var stackTag = legacy(id.toString());
              stackTag.putLong("#", amount);
              var stack = GenericStack.CODEC.parse(ops, stackTag).getOrThrow();
              Checks.require(
                  stack.what().equals(key) && stack.amount() == amount, "Quantity changed");
              var again =
                  GenericStack.CODEC
                      .parse(ops, GenericStack.CODEC.encodeStart(ops, stack).getOrThrow())
                      .getOrThrow();
              Checks.require(again.equals(stack), "Generic stack roundtrip changed quantity/key");
            }
          });
    }
    Checks.test(
        "legacy_unknown_matter_preserved_as_missing_content",
        () -> {
          var tag = legacy("synthetic_missing:no_such_matter");
          var key = AEKey.CODEC.parse(ops, tag).getOrThrow();
          Checks.require(
              key instanceof AEItemKey item && item.is(AEItems.MISSING_CONTENT),
              "Unknown matter fabricated");
          Checks.require(
              tag.equals(key.get(AEComponents.MISSING_CONTENT_AEKEY_DATA).copyTag()),
              "Unknown raw data lost");
        });
    Checks.test(
        "legacy_extra_fields_preserved",
        () -> {
          var tag = legacy("replication:metallic");
          tag.putString("future_semantics", "keep");
          var key = AEKey.CODEC.parse(ops, tag).getOrThrow();
          Checks.require(
              key instanceof AEItemKey item && item.is(AEItems.MISSING_CONTENT),
              "Unknown fields discarded");
          Checks.require(
              tag.equals(key.get(AEComponents.MISSING_CONTENT_AEKEY_DATA).copyTag()),
              "Unknown fields lost");
        });
    Checks.test(
        "ordinary_item_roundtrip_unchanged",
        () -> {
          var key = AEItemKey.of(Items.DIAMOND);
          Checks.require(
              key.equals(
                  AEKey.CODEC
                      .parse(ops, AEKey.CODEC.encodeStart(ops, key).getOrThrow())
                      .getOrThrow()),
              "Ordinary item changed");
        });
    Checks.test(
        "key_registry_unchanged",
        () ->
            Checks.require(
                AEKeyTypes.getAll().size() == registrySize
                    && AEKeyTypes.getAll().stream()
                        .noneMatch(t -> t.getId().getNamespace().equals("rep_ae2_bridge")),
                "Registered a client-incompatible legacy channel"));
  }
}
