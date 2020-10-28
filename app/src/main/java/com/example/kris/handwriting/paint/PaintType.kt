package com.example.kris.handwriting.paint

enum class PaintType(val value: Int) {
    ERASER(0),
    PEN(1),
    SWIPE_MESH(2),
    MARKER(3);

    companion object {
        fun fromInt(value: Int) = PaintType.values().first { it.value == value }
    }
}

interface PaintTypeChangedListener {
    fun onTypeChanged(type: PaintType)

}