package com.blinker.video.application

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * @author jiangshiyu
 * @date 2025/4/8
 */
interface Task {

    /**
     * 任务名称
     */
    val name:String

    /**
     * 任务依赖，表示当前任务依赖的其他任务
     * 只有当依赖的任务全部完成后，当前任务才会执行
     */
    val dependencies: List<Class<out Task>>
        get() = emptyList()

    /**
     * 任务执行的调度器
     * 默认在IO线程执行
     */
    val dispatcher:CoroutineDispatcher
        get() = Dispatchers.IO



   /**
    * 是否是异步任务
    * true: 任务会在后台线程执行，不会阻塞主线程
    * false: 任务会在主线程执行，会阻塞主线程
    */
    val isAsync: Boolean
        get() = true

    /**
     * 任务执行方法
     * 在此方法中实现任务的具体逻辑
     */
    suspend fun execute()



}