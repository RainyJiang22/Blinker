package com.blinker.video.application

/**
 * @author jiangshiyu
 * @date 2025/4/8
 */
enum class TaskStatus {
    /**
     * 初始状态
     */
    IDLE,
    /**
     * 正在执行
     */
    RUNNING,

    /**
     * 已完成
     */
    FINISHED,

    /**
     * 执行失败
     */
    FAILED
}