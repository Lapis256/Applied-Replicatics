package dev.lapis256.apprep.api.connector


object ReplicationConnectorUpgrades {
    private var registeredSlotCount = 0
    private var frozen = false

    val slotCount: Int
        @Synchronized get() = registeredSlotCount

    @Synchronized
    fun addSlots(count: Int) {
        check(!frozen) { "Replication Connector upgrade slots have already been frozen" }
        require(count > 0) { "count must be greater than zero" }
        registeredSlotCount = Math.addExact(registeredSlotCount, count)
    }

    @Synchronized
    fun freeze() {
        frozen = true
    }
}
