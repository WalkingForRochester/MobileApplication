package com.walkingforrochester.walkingforrochester.android.network.response

import com.squareup.moshi.JsonClass
import com.walkingforrochester.walkingforrochester.android.model.Leader

@JsonClass(generateAdapter = true)
class LeaderResponse(
    val place: Long,
    val accountId: Long,
    val firstName: String?,
    val nickname: String?,
    val imgUrl: String?,
    val collection: Long?,
    val distance: Double?,
    val duration: Long?
) {

    fun toLeader(): Leader = Leader(
        collectionPosition = place,
        accountId = accountId,
        firstName = firstName ?: "",
        nickname = nickname ?: "",
        imgUrl = imgUrl ?: "",
        collection = collection ?: 0L,
        distance = distance ?: 0.0,
        duration = duration ?: 0L
    )
}

fun List<LeaderResponse>.toLeaderList(): List<Leader> {
    return this.asSequence()
        .map {
            it.toLeader()
        }
        .toList()
}