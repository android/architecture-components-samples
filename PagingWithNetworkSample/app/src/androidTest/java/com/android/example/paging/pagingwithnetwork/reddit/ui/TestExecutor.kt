package com.android.example.paging.pagingwithnetwork.reddit.ui

import java.util.*
import java.util.concurrent.Executor

class TestExecutor : Executor {
    val queue = LinkedList<Runnable?>()

    override fun execute(command: Runnable?) {
        queue.add(command)
    }

    fun executeAll() {
        while (queue.isNotEmpty()) {
            queue.pop()?.run()
        }
    }
}