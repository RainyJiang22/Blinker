package com.blinker.video.ui.pages

import android.os.Bundle
import android.text.TextUtils
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.blinker.video.R
import com.blinker.video.ui.widget.AppBottomBar
import com.blinker.video.ui.navigation.NavGraphBuilder
import com.blinker.video.ui.utils.AppConfig
import com.blinker.video.ui.utils.switchTab

class MainActivity : FragmentActivity() {

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        NavGraphBuilder.build(navController, this, R.id.fragment_container)
        val appBottomBar = findViewById<AppBottomBar>(R.id.app_bottom_bar)
        appBottomBar.setOnItemSelectedListener {
            val tab = AppConfig.getBottomConfig().tabs[it.order]
            navController.switchTab(tab.route!!)
            !TextUtils.isEmpty(it.title)
        }
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    BlinkerTheme {
//        Greeting("Android")
//    }
//}