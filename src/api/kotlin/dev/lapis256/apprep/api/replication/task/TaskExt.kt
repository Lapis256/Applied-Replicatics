package dev.lapis256.apprep.api.replication.task

import com.buuz135.replication.api.task.ReplicationTask


var ReplicationTask.internalMatterStacks
    get() = (this as MEReplicationTask).`apprep$getInternalMatterStacks`()
    set(value) = (this as MEReplicationTask).`apprep$setInternalMatterStacks`(value)
