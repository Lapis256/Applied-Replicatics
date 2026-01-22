package dev.lapis256.apprep.data.provider.server

import appeng.core.definitions.AEBlocks
import appeng.core.definitions.AEItems
import appeng.core.definitions.AEParts
import appeng.core.definitions.ItemDefinition
import appeng.datagen.providers.tags.ConventionTags
import appeng.recipes.game.StorageCellDisassemblyRecipe
import appeng.recipes.game.StorageCellUpgradeRecipe
import appeng.recipes.transform.TransformCircumstance
import appeng.recipes.transform.TransformRecipeBuilder
import com.buuz135.replication.ReplicationRegistry
import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder
import dev.lapis256.apprep.integration.megacells.common.MEGACellsIntegration
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGABlocks
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGAItems
import dev.lapis256.apprep.integration.megacells.common.init.AppRepMEGATags
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.init.AppRepBlocks
import dev.lapis256.apprep.common.init.AppRepItems
import dev.lapis256.apprep.data.provider.server.tags.AppRepTags
import dev.lapis256.apprep.extension.MatterCellDefinition
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.*
import net.minecraft.tags.FluidTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import net.neoforged.neoforge.common.conditions.ModLoadedCondition
import net.pedroksl.advanced_ae.recipes.ReactionChamberRecipeBuilder
import java.util.concurrent.CompletableFuture


class AppRepRecipeProvider(output: PackOutput, provider: CompletableFuture<HolderLookup.Provider>) : RecipeProvider(output, provider) {
    override fun buildRecipes(output: RecipeOutput) {
        val basicMaterials = CellMaterials(
            AppRepItems.MATTER_CELL_HOUSING,
            AEBlocks.QUARTZ_GLASS,
            ConventionTags.REDSTONE,
            AppRepTags.REPLICA_INGOT
        )
        cellHousing(output, basicMaterials)
        matterCell(output, AppRepItems.MATTER_CELL_1K, basicMaterials)
        matterCell(output, AppRepItems.MATTER_CELL_4K, basicMaterials)
        matterCell(output, AppRepItems.MATTER_CELL_16K, basicMaterials)
        matterCell(output, AppRepItems.MATTER_CELL_64K, basicMaterials)
        matterCell(output, AppRepItems.MATTER_CELL_256K, basicMaterials)

        val megaMaterials = CellMaterials(
            AppRepMEGAItems.MEGA_MATTER_CELL_HOUSING,
            AEBlocks.QUARTZ_VIBRANT_GLASS,
            ConventionTags.SKY_STONE_DUST,
            AppRepMEGATags.Items.SKY_REPLICA_INGOT
        )
        cellHousing(output.withMEGACells(), megaMaterials)
        matterCell(output.withMEGACells(), AppRepMEGAItems.MEGA_CELL_1M, megaMaterials)
        matterCell(output.withMEGACells(), AppRepMEGAItems.MEGA_CELL_4M, megaMaterials)
        matterCell(output.withMEGACells(), AppRepMEGAItems.MEGA_CELL_16M, megaMaterials)
        matterCell(output.withMEGACells(), AppRepMEGAItems.MEGA_CELL_64M, megaMaterials)
        matterCell(output.withMEGACells(), AppRepMEGAItems.MEGA_CELL_256M, megaMaterials)


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AppRepBlocks.REPLICATION_CONNECTOR)
            .pattern("aea")
            .pattern("bcd")
            .pattern("aea")
            .define('a', AppRepTags.REPLICA_STORAGE_BLOCK)
            .define('b', ConventionTags.PATTERN_PROVIDER)
            .define('c', ReplicationRegistry.Blocks.MATTER_TANK)
            .define('d', AEParts.STORAGE_BUS)
            .define('e', AEItems.ENGINEERING_PROCESSOR)
            .unlockedBy("has_replica_block", has(AppRepTags.REPLICA_STORAGE_BLOCK))
            .save(output)

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AppRepMEGABlocks.SKY_REPLICA_BLOCK)
            .pattern("aaa")
            .pattern("aaa")
            .pattern("aaa")
            .define('a', AppRepMEGATags.Items.SKY_REPLICA_INGOT)
            .unlockedBy("has_sky_replica_ingot", has(AppRepMEGATags.Items.SKY_REPLICA_INGOT))
            .save(output.withMEGACells())


        TransformRecipeBuilder.transform(
            output.withMEGACells(),
            AppliedReplicaticsAPI.rl("transform/sky_replica_ingot"),
            AppRepMEGAItems.SKY_REPLICA_INGOT, 2,
            TransformCircumstance.fluid(FluidTags.LAVA),
            Ingredient.of(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED),
            Ingredient.of(AppRepTags.REPLICA_INGOT),
            Ingredient.of(AEBlocks.SKY_STONE_BLOCK)
        )


        cellUpgradeRecipes(
            output,
            AppRepItems.MATTER_CELL_HOUSING,
            AppRepItems.MATTER_CELL_1K,
            AppRepItems.MATTER_CELL_4K,
            AppRepItems.MATTER_CELL_16K,
            AppRepItems.MATTER_CELL_64K,
            AppRepItems.MATTER_CELL_256K
        )

        cellUpgradeRecipes(
            output.withMEGACells(),
            AppRepMEGAItems.MEGA_MATTER_CELL_HOUSING,
            AppRepMEGAItems.MEGA_CELL_1M,
            AppRepMEGAItems.MEGA_CELL_4M,
            AppRepMEGAItems.MEGA_CELL_16M,
            AppRepMEGAItems.MEGA_CELL_64M,
            AppRepMEGAItems.MEGA_CELL_256M
        )


        ReactionChamberRecipeBuilder.react(AppRepMEGAItems.SKY_REPLICA_INGOT, 64, 200000)
            .input(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 16)
            .input(AppRepTags.REPLICA_INGOT, 16)
            .input(AEBlocks.SKY_STONE_BLOCK, 16)
            .fluid(FluidTags.LAVA, 500)
            .save(output.withMEGACells("advanced_ae"), AppliedReplicaticsAPI.rl("reaction_chamber/sky_replica_ingot"))

        CrystalAssemblerRecipeBuilder.assemble(AppRepMEGAItems.SKY_REPLICA_INGOT, 8)
            .input(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 4)
            .input(AppRepTags.REPLICA_INGOT, 4)
            .input(AEBlocks.SKY_STONE_BLOCK, 4)
            .fluid(FluidTags.LAVA, 100)
            .save(output.withMEGACells("extendedae"), AppliedReplicaticsAPI.rl("crystal_assembler/sky_replica_ingot"))
    }

    private data class CellMaterials(
        val housing: ItemDefinition<*>,
        val glass: ItemLike,
        val dust: TagKey<Item>,
        val ingot: TagKey<Item>
    )

    private fun cellHousing(output: RecipeOutput, materials: CellMaterials) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, materials.housing)
            .pattern("aba")
            .pattern("b b")
            .pattern("ddd")
            .define('a', materials.glass)
            .define('b', materials.dust)
            .define('d', materials.ingot)
            .unlockedBy("has/${materials.dust.location.path}", has(materials.dust))
            .save(output, AppliedReplicaticsAPI.rl("cells/${materials.housing.id().path}"))
    }

    private fun matterCell(output: RecipeOutput, cell: MatterCellDefinition, materials: CellMaterials) {
        val component = cell.tier.componentSupplier.get()
        val tierPrefix = cell.tier.namePrefix.lowercase()

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, cell)
            .pattern("aba")
            .pattern("bcb")
            .pattern("ddd")
            .define('a', materials.glass)
            .define('b', materials.dust)
            .define('c', component)
            .define('d', materials.ingot)
            .unlockedBy("has_cell_component_$tierPrefix", has(component))
            .save(output, AppliedReplicaticsAPI.rl("cells/matter_cell_$tierPrefix"))
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, cell)
            .requires(materials.housing)
            .requires(component)
            .unlockedBy("has_cell_component_$tierPrefix", has(component))
            .unlockedBy("has_matter_cell_housing", has(materials.housing))
            .save(output, AppliedReplicaticsAPI.rl("cells/matter_cell_${tierPrefix}_storage"))
    }

    private fun cellUpgradeRecipes(output: RecipeOutput, housing: ItemLike, vararg cells: MatterCellDefinition) {
        cells.forEachIndexed { i, cellDef ->
            val inputId = cellDef.id()
            val resultComponent = cellDef.tier.componentSupplier.get()

            cellDisassembly(output, housing, cellDef)

            cells.drop(i + 1).forEach { toCellDef ->
                val inputComponent = toCellDef.tier.componentSupplier.get()
                val recipeId = inputId.withPath { path: String? -> "upgrade/${path}_to_${toCellDef.tier.namePrefix.lowercase()}" }

                output.accept(
                    recipeId,
                    StorageCellUpgradeRecipe(
                        cellDef.asItem(), inputComponent.asItem(),
                        toCellDef.asItem(), resultComponent.asItem()
                    ),
                    null
                )
            }
        }
    }

    private fun cellDisassembly(consumer: RecipeOutput, housing: ItemLike, cellDef: MatterCellDefinition) {
        val results = listOf(cellDef.tier.componentSupplier.get(), housing)
        consumer.accept(
            cellDef.id().withPrefix("cell_disassembly/"),
            StorageCellDisassemblyRecipe(
                cellDef.asItem(),
                results.map { it.asItem().defaultInstance }
            ),
            null
        )
    }

    private fun RecipeOutput.withModLoaded(vararg modId: String) =
        withConditions(*modId.map(::ModLoadedCondition).toTypedArray())

    private fun RecipeOutput.withMEGACells(vararg others: String) = withModLoaded(MEGACellsIntegration.MOD_ID, *others)
}
