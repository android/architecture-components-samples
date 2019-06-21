package com.example.benchmark

import java.util.*
import java.util.concurrent.Executor

/**
 * Flushable Executor which retains knowledge of queued tasks for state guarantee while under test.
 */
class TestExecutor : Executor {
    private val tasks = LinkedList<Runnable>()

    override fun execute(command: Runnable) {
        tasks.add(command)
    }

    fun flush(): Boolean {
        val consumed = !tasks.isEmpty()

        var task = tasks.poll()
        while (task != null) {
            task.run()
            task = tasks.poll()
        }
        return consumed
    }
}