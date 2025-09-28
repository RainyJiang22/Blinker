package com.blinker.video.ui.pages.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.blinker.video.R
import com.blinker.video.databinding.LayoutFragmentUserBinding
import com.blinker.video.base.BaseFragment
import com.blinker.video.model.Author
import com.blinker.video.model.Feed
import com.blinker.video.model.TYPE_VIDEO
import com.blinker.video.plugin.runtime.NavDestination
import com.blinker.video.ui.pages.login.UserManager
import com.blinker.video.ui.pages.tags.GlideImage
import com.blinker.video.ui.pages.tags.Mock
import com.blinker.video.ui.utils.PixUtil.screenWidthDp
import kotlinx.coroutines.launch
import kotlin.text.compareTo
import kotlin.text.get

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
@NavDestination(type = NavDestination.NavType.Fragment, route = "user_fragment")
class UserFragment : BaseFragment() {
    private val items = Mock.feeds2()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).also {
            it.setContent {
                ProfileScreen()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    @OptIn(ExperimentalFoundationApi::class)
    @Preview
    @Composable
    fun ProfileScreen() {
        var selectedIndex by remember { mutableStateOf(0) }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 头部信息
            item {
                ProfileHeader()
            }
            stickyHeader {
                TabRow(
                    selectedTabIndex = selectedIndex,
                    modifier = Modifier
                        .height(50.dp)
                        .shadow(3.dp),
                    backgroundColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            color = colorResource(id = R.color.color_theme),
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex])
                        )
                    },
                ) {
                    var selected = selectedIndex == 0
                    Tab(
                        content = {
                            Text(
                                text = "作品",
                                fontSize = TextUnit(14f, TextUnitType.Sp),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = { selectedIndex = 0 },
                        selected = selectedIndex == 0,
                        selectedContentColor = colorResource(id = R.color.color_theme),
                        unselectedContentColor = Color.Gray,
                    )
                    selected = selectedIndex == 1
                    Tab(
                        content = {
                            Text(
                                text = "喜欢",
                                fontSize = TextUnit(14f, TextUnitType.Sp),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = { selectedIndex = 1 },
                        selected = selectedIndex == 1,
                        selectedContentColor = colorResource(id = R.color.color_theme),
                        unselectedContentColor = Color.Gray,
                    )
                }
            }
            for (i in items.indices step 2) {
                item {
                    Row {
                        ProfileItem(items[i])
                        if (i + 1 < items.size) {
                            ProfileItem(items[i + 1])
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ProfileHeader() {
        var author by remember { mutableStateOf(Author()) }
        val scope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            LaunchedEffect(Unit) {
                scope.launch {
                    UserManager.getUser().collect {
                        author = it
                    }
                }
            }
            GlideImage(
                url = "https://jetpack2023.oss-cn-shanghai.aliyuncs.com/df3f82defa03442f8d9e0056fc454e75.png",
                overSize = 50,
                contentScale = ContentScale.FillBounds,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .alpha(0.75f)
                    .blur(20.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                GlideImage(
                    url = "http://qzapp.qlogo.cn/qzapp/102047280/5F7F0BA8432AD1DF1FC172698494737E/100",
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "神秘的皮皮虾",
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier
                        .padding(
                            top = 10.dp, bottom = 10.dp
                        )
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    @Composable
    fun ProfileItem(item: Feed) {
        val itemWidth = screenWidthDp() / 2
        Column(modifier = Modifier.width(itemWidth.dp)) {
            Box(modifier = Modifier.padding(8.dp)) {
                GlideImage(
                    url = item.cover ?: "",
                    palette = true,
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 8.dp, topEnd = 8.dp
                            )
                        )
                        .fillMaxWidth()
                        .height(200.dp)
                )
                if (item.itemType == TYPE_VIDEO) {
                    Icon(
                        imageVector = Icons.Filled.VideoLabel,
                        tint = Color.White,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopStart)
                            .offset(x = 6.dp, y = 6.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(
                            Color.LightGray.copy(alpha = 0.5f)
                        )
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                ) {
                    item.ugc?.let {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            tint = Color.White,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = it.likeCount.toString(),
                            fontSize = TextUnit(12f, TextUnitType.Sp),
                        )
                        Spacer(
                            modifier = Modifier.width(10.dp)
                        )
                        Icon(
                            imageVector = Icons.Filled.Comment,
                            tint = Color.White,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = it.commentCount.toString(),
                            fontSize = TextUnit(12f, TextUnitType.Sp),
                        )
                    }
                }
            }
            item.feedsText?.let {
                Text(
                    text = it,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
        }
    }

}