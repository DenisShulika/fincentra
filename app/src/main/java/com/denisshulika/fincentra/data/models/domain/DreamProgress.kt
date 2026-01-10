package com.denisshulika.fincentra.data.models.domain

data class DreamProgress(
    val dream: Dream,
    val currentAvailable: Double,
    val progress: Float
)