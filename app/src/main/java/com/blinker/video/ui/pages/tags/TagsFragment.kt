@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)

package com.blinker.video.ui.pages.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.blinker.video.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.blinker.video.model.Feed
import com.blinker.video.model.TYPE_VIDEO
import com.blinker.video.plugin.runtime.NavDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author jiangshiyu
 * @date 2024/12/9
 */
@NavDestination(type = NavDestination.NavType.Fragment, route = "tags_fragment")
class TagsFragment : Fragment() {
    val items = Mock.feeds()
    val items2 = Mock.feeds2()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).also {
            it.setContent {
                WaterFull()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    @Composable
    fun SwipeRefresh(content: @Composable () -> Unit) {
        val scope = rememberCoroutineScope()
        var refreshing by remember { mutableStateOf(false) }
        val refreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = {
            scope.launch {
                refreshing = true
                delay(2000)
                refreshing = false
            }
        })
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pullRefresh(refreshState)
        ) {
            content()
            PullRefreshIndicator(
                refreshing = refreshing,
                state = refreshState,
                modifier = Modifier.align(
                    Alignment.TopCenter
                )
            )
        }
    }

    @Preview
    @Composable
    fun WaterFull() {
        var data by remember { mutableStateOf(items) }
        var hasMore by remember { mutableStateOf(true) }
        var isLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        // 父组件中的加载函数（启动协程）
        val loadMore: () -> Unit = {
            if (!isLoading && hasMore) {
                isLoading = true
                scope.launch {
                    delay(2000)
                    data = data + items2
                    hasMore = false
                    isLoading = false
                }
            }
        }

        SwipeRefresh {
            LazyVerticalStaggeredGrid(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                columns = StaggeredGridCells.Fixed(2),
                content = {
                    itemsIndexed(data) { index, item ->
                        StaggeredGridItem(item = item)
                    }
                    item(span = StaggeredGridItemSpan.FullLine) {
                        if (hasMore) {
                            LoadingState(
                                onLoadMore = loadMore
                            )
                        } else {
                            Text(
                                text = "已经没有更多了",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth(1f)
                                    .height(45.dp)
                            )
                        }

                    }
                }
            )
        }
    }


    @Composable
    fun StaggeredGridItem(item: Feed) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Column {
                Box(modifier = Modifier.padding(8.dp)) {
                    GlideImage(
                        url = item.cover ?: "",
                        palette = true,
                        contentScale = ContentScale.FillHeight,
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
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
                    item.ugc?.let {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(Color.LightGray.copy(alpha = 0.5f))
                                .fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                tint = Color.White,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = it.likeCount.toString(),
                                fontSize = TextUnit(12f, TextUnitType.Sp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Filled.Comment,
                                tint = Color.White,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                            )
                            Text(
                                text = it.likeCount.toString(),
                                fontSize = TextUnit(12f, TextUnitType.Sp)
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
                item.author?.let {
                    Row(modifier = Modifier.padding(horizontal = 5.dp)) {
                        GlideImage(
                            url = it.avatar, modifier = Modifier
                                .size(25.dp)
                                .clip(
                                    RoundedCornerShape(20.dp)
                                )
                        )
                        Text(
                            text = it.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.Gray,
                            modifier = Modifier.offset(x = 8.dp, y = 2.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun LoadingState(onLoadMore: () -> Unit) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(45.dp)
        ) {

            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(requireContext().getColor(R.color.color_theme))
            )
            Spacer(Modifier.size(8.dp))
            Text(text = "正在加载中...")
        }
        LaunchedEffect(Unit) {
            onLoadMore()
        }
    }
}
