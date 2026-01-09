package dev.lapis256.apprep.api.extension

import appeng.block.AEBaseBlock
import appeng.block.AEBaseBlockItem
import appeng.block.AEBaseEntityBlock
import appeng.blockentity.AEBaseBlockEntity
import appeng.blockentity.ClientTickingBlockEntity
import appeng.blockentity.ServerTickingBlockEntity
import appeng.core.definitions.BlockDefinition
import appeng.core.definitions.DeferredBlockEntityType
import appeng.core.definitions.ItemDefinition
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


fun <ITEM : Item> DeferredRegister.Items.register(englishName: String, name: String, supplier: (Item.Properties) -> ITEM): ItemDefinition<ITEM> {
    val deferredItem = registerItem(name, supplier)
    return ItemDefinition(englishName, deferredItem)
}

fun <BLOCK : Block> DeferredRegister.Blocks.register(
    englishName: String,
    name: String,
    supplier: () -> BLOCK,
    itemRegistry: DeferredRegister.Items,
    itemSupplier: ((Block, Item.Properties) -> BlockItem)? = null
): BlockDefinition<BLOCK> {
    val deferredBlock = register(name, supplier)
    val deferredItem = itemRegistry.registerItem(name) { properties ->
        val block = deferredBlock.get()
        when {
            itemSupplier != null -> itemSupplier(block, properties)
            block is AEBaseBlock -> AEBaseBlockItem(block, properties)
            else -> BlockItem(block, properties)
        }
    }

    val itemDef = ItemDefinition(englishName, deferredItem)
    return BlockDefinition(englishName, deferredBlock, itemDef)
}

inline fun <reified ENTITY : AEBaseBlockEntity, BLOCK : AEBaseEntityBlock<ENTITY>> DeferredRegister<BlockEntityType<*>>.registerType(
    name: String,
    crossinline factory: (BlockEntityType<ENTITY>, BlockPos, BlockState) -> ENTITY,
    vararg deferredBlocks: BlockDefinition<BLOCK>
): DeferredBlockEntityType<ENTITY> {
    if (deferredBlocks.isEmpty()) {
        error("At least one block must be provided to register a block entity type")
    }

    val deferred = register(name) { ->
        val blocks = deferredBlocks.map(BlockDefinition<BLOCK>::block).toTypedArray()
        val typeHolder = AtomicReference<BlockEntityType<ENTITY>>()

        BlockEntityType.Builder.of(
            { pos, state -> factory(typeHolder.get(), pos, state) },
            *blocks
        ).build(@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") null)
            .also { type ->
                typeHolder.plain = type

                AEBaseBlockEntity.registerBlockEntityItem(type, blocks[0].asItem())

                val serverTicker = makeTicker(ENTITY::class, ServerTickingBlockEntity::serverTick)
                val clientTicker = makeTicker(ENTITY::class, ClientTickingBlockEntity::clientTick)

                blocks.forEach {
                    it.setBlockEntity(ENTITY::class.java, type, clientTicker, serverTicker)
                }
            }
    }

    return DeferredBlockEntityType(ENTITY::class.java, deferred)
}

inline fun <reified ENTITY : AEBaseBlockEntity, reified TICKING : Any> makeTicker(
    clazz: KClass<ENTITY>,
    crossinline tickerFuncRef: TICKING.() -> Unit
): BlockEntityTicker<ENTITY>? {
    return BlockEntityTicker<ENTITY> { _, _, _, entity -> tickerFuncRef.invoke(entity as TICKING) }
        .takeIf { clazz.isSubclassOf(TICKING::class) }
}
