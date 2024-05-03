package me.codedbyyou.os.client.game.manager

import me.codedbyyou.os.core.models.Title

object TitleManager {
    private val titleQueue = mutableListOf<Title>()
    private var lastTitleTime: Long = 0
    private var currentTitle: Title? = null

    fun addTitle(title: Title) {
        titleQueue.add(title)
    }

    fun getCurrentTitle(): Title? {
        if (currentTitle == null && titleQueue.isNotEmpty()) {
            currentTitle = titleQueue.removeAt(0)
            lastTitleTime = System.currentTimeMillis()
        } else if (currentTitle != null && isCurrentTitleExpired()) {
            if (titleQueue.isNotEmpty()) {
                currentTitle = titleQueue.removeAt(0)
                lastTitleTime = System.currentTimeMillis()
            } else {
                currentTitle = null
            }
        }
        return currentTitle
    }

    fun getLastTitleTime(): Long = lastTitleTime

    fun hasTitle(): Boolean = currentTitle != null || titleQueue.isNotEmpty()

    fun isCurrentTitleExpired(): Boolean {
        return (System.currentTimeMillis() - lastTitleTime) >= currentTitle!!.duration * 1000
    }

    fun skipCurrentTitle() {
        if (titleQueue.isNotEmpty()) {
            currentTitle = titleQueue.removeAt(0)
            lastTitleTime = System.currentTimeMillis()
        }
    }
}
