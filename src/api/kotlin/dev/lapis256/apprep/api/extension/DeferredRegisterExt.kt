package dev.lapis256.apprep.api.extension

import appeng.block.AEBaseBlock
import appeng.block.AEBaseBlockItem
import appeng.block.AEBaseEntityBlock
import appeng.blockentity.AEBaseBlockEntity
import appeng.blockentity.ClientTickingBlockEntity
import appeng.blockentity.ServerTickingBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegistryObject
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

fun <ITEM : Item> DeferredRegister<Item>.register(
    englishName: String,
    name: String,
    supplier: (Item.Properties) -> ITEM,
): ItemDefinition<ITEM> {
    val deferredItem = register(name) { supplier(Item.Properties()) }
    return ItemDefinition(englishName, deferredItem)
}

fun <BLOCK : Block> DeferredRegister<Block>.register(
    englishName: String,
    name: String,
    supplier: () -> BLOCK,
    itemRegistry: DeferredRegister<Item>,
    itemSupplier: ((Block, Item.Properties) -> BlockItem)? = null,
): BlockDefinition<BLOCK> {
    val deferredBlock = register(name, supplier)
    val deferredItem =
        itemRegistry.register(name) {
            val properties = Item.Properties()
            val block = deferredBlock.get()
            when {
                itemSupplier != null -> itemSupplier(block, properties)
                block is AEBaseBlock -> AEBaseBlockItem(block, properties)
                else -> BlockItem(block, properties)
            }
        }

    return BlockDefinition(englishName, deferredBlock, deferredItem)
}

inline fun <reified ENTITY : AEBaseBlockEntity, BLOCK : AEBaseEntityBlock<ENTITY>> DeferredRegister<BlockEntityType<*>>.registerType(
    name: String,
    crossinline factory: (BlockEntityType<ENTITY>, BlockPos, BlockState) -> ENTITY,
    vararg deferredBlocks: BlockDefinition<BLOCK>,
): RegistryObject<BlockEntityType<ENTITY>> {
    if (deferredBlocks.isEmpty()) {
        error("At least one block must be provided to register a block entity type")
    }

    return register(name) {
        val blocks = deferredBlocks.map(BlockDefinition<BLOCK>::block)
        val typeHolder = AtomicReference<BlockEntityType<ENTITY>>()

        val builder =
            BlockEntityType.Builder.of(
                { pos, state -> factory(typeHolder.get(), pos, state) },
                *blocks.toTypedArray<Block>(),
            )
        BlockEntityTypes.withoutDataFixer(builder).also { type ->
            typeHolder.plain = type

            AEBaseBlockEntity.registerBlockEntityItem(type, blocks[0].asItem())

            val serverTicker = makeTicker(ENTITY::class, ServerTickingBlockEntity::serverTick)
            val clientTicker = makeTicker(ENTITY::class, ClientTickingBlockEntity::clientTick)

            blocks.forEach {
                it.setBlockEntity(ENTITY::class.java, type, clientTicker, serverTicker)
            }
        }
    }
}

inline fun <reified ENTITY : AEBaseBlockEntity, reified TICKING : Any> makeTicker(
    clazz: KClass<ENTITY>,
    crossinline tickerFuncRef: TICKING.() -> Unit,
): BlockEntityTicker<ENTITY>? =
    BlockEntityTicker<ENTITY> { _, _, _, entity -> tickerFuncRef.invoke(entity as TICKING) }
        .takeIf { clazz.isSubclassOf(TICKING::class) }

open class ItemDefinition<ITEM : Item>(
    val englishName: String,
    private val item: RegistryObject<ITEM>,
) : ItemLike {
    fun id() = item.id

    override fun asItem(): ITEM = item.get()

    fun stack(count: Int = 1) = ItemStack(this, count)
}

class BlockDefinition<BLOCK : Block>(
    englishName: String,
    private val block: RegistryObject<BLOCK>,
    item: RegistryObject<BlockItem>,
) : ItemDefinition<BlockItem>(englishName, item) {
    fun block(): BLOCK = block.get()
}
