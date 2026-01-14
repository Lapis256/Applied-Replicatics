package dev.lapis256.apprep.api.replication.util

import com.buuz135.replication.api.task.IReplicationTask
import com.buuz135.replication.network.task.ReplicationTaskManager


fun ReplicationTaskManager.addTask(task: IReplicationTask) {
    pendingTasks[task.uuid.toString()] = task
}
