package com.blinker.video.application

/**
 * @author jiangshiyu
 * @date 2025/4/8
 * 构建有向无环图
 */
class TaskNode(
    val task: Task,
    val children: MutableList<TaskNode> = mutableListOf(),
    val parents: MutableList<TaskNode> = mutableListOf(),
) {

    //任务状态
    var status = TaskStatus.IDLE

    val isFinished: Boolean
        get() = status == TaskStatus.FINISHED

    //任务是否可执行
    val isReady: Boolean
        get() = parents.all { it.isFinished }

    /**
     * 添加子节点
     */
    fun addChild(child: TaskNode) {
        if (children.contains(child).not()) {
            children.add(child)
        }
    }

    /**
     * 添加父节点
     */
    fun addParent(parent: TaskNode) {
        if (parents.contains(parent).not()) {
            parents.add(parent)
        }
    }

    override fun toString(): String {
        return "TaskNode(task=${task.name}, children=${children.map { it.task.name }}, parents=${parents.map { it.task.name }}, status=$status)"
    }


}