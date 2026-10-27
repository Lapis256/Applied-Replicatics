package dev.lapis256.apprep

import com.buuz135.replication.api.MatterType
import dev.lapis256.apprep.api.ae2.stack.MatterKey
import net.minecraft.nbt.NbtOps
import net.minecraft.util.ProblemReporter
import net.minecraft.world.level.storage.TagValueOutput
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MatterPersistenceTest {
    @Test
    fun matterKeyUsesTheExistingTypeField() {
        val key = MatterKey.of(MatterType.EARTH)
        val output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING)
        key.toTag(output)
        val tag = output.buildResult()

        assertEquals("replication:earth", tag.getStringOr("type", ""))
        assertEquals(key, MatterKey.CODEC.parse(NbtOps.INSTANCE, tag).result().orElseThrow())
    }

}
