package dev.lapis256.apprep.common.init

import appeng.menu.AEBaseMenu
import appeng.menu.implementations.MenuTypeBuilder
import dev.lapis256.apprep.api.AppliedReplicaticsAPI
import dev.lapis256.apprep.common.logic.ReplicationConnectorLogicHost
import dev.lapis256.apprep.common.menu.ReplicationConnectorMenu
import net.minecraft.core.registries.Registries
import net.minecraft.world.inventory.MenuType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import kotlin.reflect.KClass


object AppRepMenus {
    val REGISTRY: DeferredRegister<MenuType<*>> = DeferredRegister.create(Registries.MENU, AppliedReplicaticsAPI.MOD_ID)

    private val connector = register("replication_connector", ::ReplicationConnectorMenu, ReplicationConnectorLogicHost::class)
    val REPLICATION_CONNECTOR get() = connector.get()


    private fun <MENU : AEBaseMenu, HOST : Any> register(
        name: String,
        factory: MenuTypeBuilder.TypedMenuFactory<MENU, HOST>,
        host: KClass<HOST>
    ): DeferredHolder<MenuType<*>, MenuType<MENU>> {
        return REGISTRY.register(name) { id ->
            MenuTypeBuilder
                .create(factory, host.java)
                .buildUnregistered(id)
        }
    }
}
