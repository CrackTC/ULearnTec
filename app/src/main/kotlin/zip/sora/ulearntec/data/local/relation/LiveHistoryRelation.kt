package zip.sora.ulearntec.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import zip.sora.ulearntec.data.local.entity.LiveEntity
import zip.sora.ulearntec.data.local.entity.LiveHistoryEntity

data class LiveHistoryRelation(
    @Embedded val live: LiveEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "live_id"
    )
    val history: LiveHistoryEntity?
)
