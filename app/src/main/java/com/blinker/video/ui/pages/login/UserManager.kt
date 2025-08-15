package com.blinker.video.ui.pages.login

import android.content.Intent
import com.blinker.video.dao.BlinkerAppDataBase
import com.blinker.video.model.Author
import com.blinker.video.ui.utils.AppGlobals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.compareTo

/**
 * @author jiangshiyu
 * @date 2025/1/17
 */
object UserManager {

    private val userFlow: MutableStateFlow<Author> = MutableStateFlow(Author())

    suspend fun save(author: Author) {
        BlinkerAppDataBase.getInstance().authorDao().save(author)
        userFlow.emit(author)
    }

    fun isLogin(): Boolean {
        return userFlow.value.expiresTime > System.currentTimeMillis()
    }

    fun loginIfNeed() {
        if (isLogin()) {
            return
        } else {
            val intent = Intent(AppGlobals.getApplication(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            AppGlobals.getApplication().startActivity(intent)
        }
    }


    fun startLogin() {
        val intent = Intent(AppGlobals.getApplication(), LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        AppGlobals.getApplication().startActivity(intent)
    }

    suspend fun getUser(): Flow<Author> {
        loadCache()
        return userFlow
    }

    suspend fun userId(): Long {
        loadCache()
        return userFlow.value.userId
    }

    private suspend fun loadCache() {
        if (!isLogin()) {
            val cache = BlinkerAppDataBase.getInstance().authorDao().getUser()
            cache?.run {
                userFlow.emit(this)
            }
        }
    }

}