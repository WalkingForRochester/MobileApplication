package com.walkingforrochester.walkingforrochester.android.model

data class Leader(
    val collectionPosition: Long,
    val accountId: Long,
    val firstName: String,
    val nickname: String,
    val imgUrl: String,
    val collection: Long,
    val distance: Double,
    val duration: Long
) {
    companion object {
        val collectionComparator = compareBy<Leader> { it.collectionPosition }
        val distanceComparator = compareByDescending<Leader> { it.distance }.thenBy { it.collectionPosition }
        val durationComparator = compareByDescending<Leader> { it.duration }.thenBy { it.collectionPosition }
    }
}

