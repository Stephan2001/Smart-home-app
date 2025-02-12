package com.adr.opsc7312_poe

data class ParseTheme (
    val themeId: Int,
    val name: String,
    var isActive: Boolean,
    val devices: List<DeviceDto>
)
