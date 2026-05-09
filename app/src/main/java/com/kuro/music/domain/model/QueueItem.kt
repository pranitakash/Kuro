package com.kuro.music.domain.model

import java.util.UUID

data class QueueItem(
    val song: Song,
    val queueId: String = UUID.randomUUID().toString()
)
