package com.alex.utils

import java.awt.Color

object Constants {
    const val NO_COMPRESSION: Int = 0
    const val BZIP2_COMPRESSION: Int = 1
    const val GZIP_COMPRESSION: Int = 2
    const val MAX_VALID_ARCHIVE_LENGTH: Int = 1000000
    const val INTERFACE_DEFINITIONS_INDEX: Int = 3
    const val MAPS_INDEX: Int = 5
    const val MODELS_INDEX: Int = 7
    const val SPRITES_INDEX: Int = 8
    const val INDEXED_IMAGES_INDEX: Int = 8
    const val OBJECTS_DEFINITIONS_INDEX: Int = 18
    const val ITEM_DEFINITIONS_INDEX: Int = 19
    const val LOADER_IMAGES_INDEX: Int = 32
    const val LOADER_INDEXED_IMAGES_INDEX: Int = 34
    const val CLIENT_BUILD: Int = 562
    const val ENCRYPTED_CACHE: Boolean = true
    const val NPC_DEFINITIONS_INDEX: Int = 18
    const val LEFT_SCROLLPANE_WIDTH: Int = 229
    const val RIGHT_SCROLLPANE_WIDTH: Int = 300
    const val CONTENT_PADDING: Int = 10
    const val SEARCHBOX_WIDTH: Int = 68
    const val SEARCHBOX_HEIGHT: Int = 32
    const val BUTTON_WIDTH: Int = 115
    const val BUTTON_HEIGHT: Int = 32
    const val BUTTON_HEIGHT_SMALL: Int = 24
    const val BUTTON_WIDTH_SMALL: Int = 75

    const val DEFAULT_EDITOR_WIDTH: Int = 1366
    const val DEFAULT_EDITOR_HEIGHT: Int = 768
    const val TOPROW_HEIGHT: Int = CONTENT_PADDING + SEARCHBOX_HEIGHT
    const val PREMADE_COMP_WIDTH: Int = LEFT_SCROLLPANE_WIDTH
    const val PREMADE_COMP_HEIGHT: Int = 112
    const val INFO_FIELD_WIDTH: Int = 300
    const val INFO_FIELD_HEIGHT: Int = BUTTON_HEIGHT_SMALL
    val BG_FILL_COLOR: Color = Color.lightGray
    const val VIEWPORT_WIDTH: Int = DEFAULT_EDITOR_WIDTH - LEFT_SCROLLPANE_WIDTH - RIGHT_SCROLLPANE_WIDTH
    const val VIEWPORT_HEIGHT: Int = 503
}
