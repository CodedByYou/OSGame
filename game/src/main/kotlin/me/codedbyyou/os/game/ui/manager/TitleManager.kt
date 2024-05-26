package me.codedbyyou.os.game.ui.manager

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import me.codedbyyou.os.core.models.Title
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object TitleManager {
    private val titleQueue = mutableListOf<Title>()
    private var lastTitleTime: Long = 0
    var currentTitle: MutableState<Title?> = mutableStateOf(null)
    private val lock = ReentrantLock()

    fun addTitle(title: Title) {
        titleQueue.add(title)
        if (currentTitle.value == null) {
            currentTitle.value = titleQueue.removeAt(0)
            lastTitleTime = System.currentTimeMillis()
        }
    }

    fun getCurrentTitle(): Title? {
        return currentTitle.value
    }

    fun checkAndSetCurrentTitle() {
        lock.withLock {
            if (currentTitle.value == null && titleQueue.isNotEmpty()) {
                currentTitle.value = titleQueue.removeAt(0)
                lastTitleTime = System.currentTimeMillis()
            } else if (currentTitle.value != null && isCurrentTitleExpired()) {
                currentTitle.value = null
                titleQueue.removeIf { it == currentTitle.value }
            }
        }
    }


    private fun isCurrentTitleExpired(): Boolean {
        return currentTitle.value?.let {
            (System.currentTimeMillis() - lastTitleTime) >= it.duration * 1000
        } ?: false
    }

    fun skipCurrentTitle() {
        currentTitle.value = null
    }

    fun clearAllTitles() {
        lock.withLock {
            titleQueue.clear()
            currentTitle.value = null
        }
    }
}
