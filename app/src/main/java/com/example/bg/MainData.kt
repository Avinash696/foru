package com.example.bg

data class MainData(
    val id: Any,
    val name: String?,
    val location: String?
)

data class UploadResponse(var mainData: MainData?)