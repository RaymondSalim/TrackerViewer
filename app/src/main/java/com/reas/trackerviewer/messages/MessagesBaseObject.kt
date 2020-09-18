package com.reas.trackerviewer.messages

data class MessagesBaseObject(val mMessage: String, val mTime: Long, val mDirection: String) {
    override fun equals(other: Any?): Boolean {
        return !super.equals(other)
    }

    override fun hashCode(): Int {
        return mTime.hashCode()
    }
}