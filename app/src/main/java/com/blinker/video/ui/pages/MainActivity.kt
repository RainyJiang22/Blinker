package com.blinker.video.ui.pages

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.blinker.video.R
import com.blinker.video.ui.navigation.NavGraphBuilder
import com.blinker.video.ui.pages.publish.CaptureActivity
import com.blinker.video.ui.theme.BlinkerTheme
import com.blinker.video.ui.utils.AppConfig
import com.blinker.video.ui.utils.switchTab
import com.blinker.video.ui.widget.AppBottomBar
import kotlinx.coroutines.delay

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
            if (tab.route == "activity_capture") {
                startActivity(Intent(this, CaptureActivity::class.java))
            } else {
                navController.switchTab(tab.route!!)
            }
            !TextUtils.isEmpty(it.title)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


@Composable
fun BaseAnimation() {

    var angle by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        angle += 1f    // 每次加1都会触发重组
        //大概60fps
        delay(16)
    }

    Box(Modifier.rotate(angle)) {
        // 组件会每帧重组 → 性能差
    }
}

@Composable
fun BigComposable() {
    var count by remember { mutableStateOf(0) }


    Column {
        Text("Header")         // 不变的也会重组
        Button(onClick = { count++ }) {
            Text("Click me")
        }





        Text("You clicked $count times")   // 其实只有这个组件需要更新
    }
}

@Preview
@Composable
fun PreviewImmutable() {
    BlinkerTheme() {
        //ImmutableComposable()
        //OptimizedLayout()
        //BadNestedLayout()
        //CustomSubCompose()
    }
}

@Immutable
data class UserProfile(val id: Int, var name: String)

@Composable
fun ImmutableComposable() {

    var userProfile by remember { mutableStateOf(UserProfile(1, "张三")) }


    Column {

        Button(onClick = {
            //userProfile.name = "12"
            userProfile = userProfile.copy(2, "李四")
        }) {
            Text("userProfile不可变，${userProfile.name},${userProfile.id}")
        }
    }
}

@Stable
data class UiState(var count: Int, var name: String)


@Composable
fun BadExample() {
    var flag by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        flag = !flag // 每次都会触发重组
    }
    Text("Flag is $flag")
}


@Composable
fun BadNestedLayout() {
    Column(modifier = Modifier.fillMaxSize()) {
        // 用户信息模块
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Image(
                    painter = painterResource(R.drawable.icon_tab_main),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("粉丝：1.2万", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Android开发", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Icon(Icons.Filled.Star, contentDescription = "等级")
                    Text("Lv.3", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        // 内容区域
        Column(modifier = Modifier.padding(16.dp)) {
            Text("这是动态正文内容...（200字长文本）", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                repeat(3) {
                    Image(
                        painter = painterResource(R.drawable.icon_tag),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}


//优化
@Composable
fun OptimizedLayout() {
    Column(modifier = Modifier.fillMaxSize()) {
        UseInfoSection()
        ContentSection()
    }
}


@Composable
fun UseInfoSection() {
    //将用户信息独立出来
    Row(
        modifier = Modifier
            .padding(16.dp)
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarWithFans()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Android开发者",
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LevelBadge(level = 8)
        }
    }
}

@Composable
fun LevelBadge(level: Int) {
    Row {
        Icon(Icons.Filled.Star, contentDescription = "等级")
        Text("Lv.$level", modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun AvatarWithFans() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.icon_tab_main),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Text("粉丝：1.2万", fontSize = 12.sp)
    }
}

@Composable
fun ContentSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("这是动态正文内容...（200字长文本）", fontSize = 14.sp)
        ImageGrid(
            listOf(
                R.drawable.icon_tag,
                R.drawable.icon_tag,
                R.drawable.icon_tag
            )
        )
    }
}

@Composable
private fun ImageGrid(images: List<Int>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(200.dp)
    ) {
        items(images) { url ->
            Image(
                painter = painterResource(url),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
fun CustomSubCompose() {
    SubcomposeLayout(modifier = Modifier.wrapContentHeight()) { constraints ->

        //第一次
        val measurable1 = subcompose("part1") {
            Text("部分一")
        }.first().measure(constraints)

        //第二次
        val measurable2 = subcompose("part2") {
            Text("部分二")
        }.first().measure(constraints)

        layout(
            width = measurable1.width + measurable2.width,
            height = maxOf(measurable1.height, measurable2.height)
        ) {
            measurable1.placeRelative(0, 0)
            measurable2.placeRelative(measurable1.width, measurable1.height)
        }
    }
}