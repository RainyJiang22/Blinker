package com.blinker.video.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.blinker.video.R

/**
 * @author jiangshiyu
 * @date 2025/5/19
 */


@Composable
fun TabContentDemo() {
    var activeTab by remember { mutableStateOf(0) }

    Column {
        TabTopContent(activeTab) {
            activeTab = it
        }
        HorizontalDivider()
        TabContent(activeTab)
    }

}

@Composable
fun TabTopContent(
    activeTab: Int,
    onClick: (activeClickTab: Int) -> Unit,
) {

    Row(Modifier.fillMaxWidth()) {
        TabButton(
            text = "TAB 1",
            isSelected = activeTab == 0,
            modifier = Modifier.weight(1f)
        ) {
            onClick(0)
        }
        TabButton(
            text = "TAB 2",
            isSelected = activeTab == 1,
            modifier = Modifier.weight(1f)
        ) {
            onClick(1)
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            contentColor = if (isSelected) {
                Color.White
            } else {
                Color.Black
            },
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.padding(8.dp)
    ) {
        Text(text)
    }
}

@Composable
fun TabContent(activeTab: Int) {
    SubcomposeLayout(modifier = Modifier.fillMaxSize()) { constraints ->
        val placeables = subcompose(activeTab) {
            when (activeTab) {
                0 -> {
                    PageOne()
                }

                1 -> {
                    PageTwo()
                }

                else -> {
                    Placeholder()
                }
            }
        }.map { it.measure(constraints) }

        val width = placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.sumOf { it.height }

        layout(width, height) {
            var yOffset = 0
            for (placeable in placeables) {
                placeable.placeRelative(0, yOffset)
                yOffset += placeable.height
            }
        }
    }
}

@Composable
fun PageOne() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("这是页面一内容")
        Spacer(Modifier.height(8.dp))
        Text("内容加载完成！")
    }
}

@Composable
fun PageTwo() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("这是页面二内容")
        Spacer(Modifier.height(8.dp))
        Text("另一页内容也加载啦~")
    }
}

@Composable
fun Placeholder() {
    Box(Modifier.fillMaxSize()) {
        Text("暂无内容")
    }
}

@Preview
@Composable
fun PreviewTabContentDemo() {
    //TabContentDemo()
    // UserProfilePage()
    // TextUnified()
    //BadLazyUsage()
    OptimizedLazyList()
}

@Composable
fun BadLazyUsage() {
    LazyColumn {
        item { Header() } // 静态头部
        items(100) { index -> // 动态列表
            ListItem(index)
        }
        item { Footer() } // 静态尾部
    }
}

@Composable
fun OptimizedLazyList() {
    val items = remember { (0..99).toList() }

    LazyColumn {
        item(key = "header") { // 静态内容添加唯一key
            Header()
        }

        items(
            items,
            key = { it.hashCode() } // 设置唯一标识
        ) { item ->
            OptimizedListItem(item)
        }

        item(key = "footer") {
            Footer()
        }
    }
}

@Composable
private fun OptimizedListItem(item: Int) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            Image(
                painter = painterResource(R.drawable.icon_tag),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Item $item",
                modifier = Modifier.padding(start = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Divider(
            color = Color.LightGray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun ListItem(index: Int) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            Image(
                painter = painterResource(R.drawable.icon_tag),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Text("Item $index", modifier = Modifier.padding(8.dp))
        }
        Divider(color = Color.Gray, thickness = 0.5.dp)
    }
}


@Composable
fun UserProfilePage() {
//    Box {
//        Column {
//            Card {
//                Column {
//                    Row {
//                        Column {
//                            Text("用户名")
//                            Text("个性签名")
//                        }
//                    }
//                }
//            }
//        }
//    }
//    ConstraintLayout {
//        val (name,desc) = createRefs()
//        Text("用户名",Modifier.constrainAs(name) {
//            top.linkTo(parent.top)
//            start.linkTo(parent.start)
//        })
//        Text("个性签名",Modifier.constrainAs(desc){
//            top.linkTo(name.bottom)
//            start.linkTo(parent.start)
//        })
//    }

    //懒加载
//    LazyColumn {
//        items(1000) { index ->
//            Text("Item $index")
//        }
//    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Header() }
        items(102) { index ->
            Text("Item $index")
        }
        item { Footer() }
    }
}

@Composable
fun Header() {
    Text("这是头部")
}

@Composable
fun Footer() {
    Text("已经到底了")
}

@Composable
fun TextUnified() {
    Text(
        text = "Hello Compose",
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(16.dp)
            .shadow(12.dp)
    )
}
