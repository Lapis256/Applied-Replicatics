package dev.lapis256.apprep.api.connector

import appeng.api.networking.IGridNodeService
import appeng.api.networking.IManagedGridNode


object ReplicationConnectorExtensions {
    private data class Registration<T : IGridNodeService>(
        val serviceType: Class<T>,
        val factory: (ReplicationConnectorExtensionContext) -> T,
    )

    private val registrations = mutableListOf<Registration<out IGridNodeService>>()
    private var frozen = false

    @Synchronized
    fun <T : IGridNodeService> registerNodeService(
        serviceType: Class<T>,
        factory: (ReplicationConnectorExtensionContext) -> T,
    ) {
        check(!frozen) { "Replication Connector extensions have already been frozen" }
        require(registrations.none { it.serviceType == serviceType }) {
            "A Replication Connector service is already registered for ${serviceType.name}"
        }
        registrations += Registration(serviceType, factory)
    }

    inline fun <reified T : IGridNodeService> registerNodeService(
        noinline factory: (ReplicationConnectorExtensionContext) -> T,
    ) {
        registerNodeService(T::class.java, factory)
    }

    @Synchronized
    fun freeze() {
        frozen = true
    }

    @Synchronized
    fun install(
        node: IManagedGridNode,
        context: ReplicationConnectorExtensionContext,
    ): List<ReplicationConnectorExtension> {
        check(frozen) { "Replication Connector extensions must be frozen before use" }
        return registrations.mapNotNull { install(node, context, it) }
    }

    private fun <T : IGridNodeService> install(
        node: IManagedGridNode,
        context: ReplicationConnectorExtensionContext,
        registration: Registration<T>,
    ): ReplicationConnectorExtension? {
        val service = registration.factory(context)
        node.addService(registration.serviceType, service)
        return service as? ReplicationConnectorExtension
    }
}
