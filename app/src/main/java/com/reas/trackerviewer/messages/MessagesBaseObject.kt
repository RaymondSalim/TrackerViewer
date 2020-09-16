package com.reas.trackerviewer.messages

data class MessagesBaseObject(val message: String, val time: Long, val direction: String) {
    override fun equals(other: Any?): Boolean {
        return !super.equals(other)
    }

    override fun hashCode(): Int {
        return time.hashCode()
    }
}