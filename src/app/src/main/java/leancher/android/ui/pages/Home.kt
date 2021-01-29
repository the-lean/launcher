package leancher.android.ui.pages

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import leancher.android.R
import leancher.android.domain.models.PageTitle
import leancher.android.domain.intents.LeancherIntent
import leancher.android.viewmodels.HomeViewModel
import androidx.compose.ui.platform.AmbientContext

@Composable
fun Home(vm: HomeViewModel) {
    val context = AmbientContext.current

    val homeTitleModel = PageTitle(
        context.getString(R.string.page_home),
        context.getString(R.string.launcher_experience),
        R.drawable.leancher
    )

//    // TODO
//    var textValue by savedInstanceState { "1234567812345678" }

//    Row {
//        Column(Modifier.padding(10.dp)) {
//            TitleCard(pageTitle = homeTitleModel, null)
//        }
//    }

    Column(Modifier.padding(10.dp)) {
        IWanna(vm::onStartOver)
        ScrollableColumn {
            Blocks(vm.blocks, vm.renderers)
            NextBlock(vm.nextBlockOptions, vm::blockSelected)
            if(vm.isFinished) Button(onClick = vm::onStartOver) {
                Text(text = "Start Over")
            }
        }
    }
}


@Composable
fun IWanna(startOver: () -> Unit) =
    Text(
        modifier = Modifier.clickable { startOver() },
        text = "i wanna …",
        style = MaterialTheme.typography.subtitle1
    )

@Composable
fun Blocks(blocks: List<LeancherIntent.Block>, renderers: HomeViewModel.Renderers) =
    Column(modifier = Modifier.padding(vertical = 5.dp)) { blocks.forEach { Block(it, renderers) } }

@Composable
fun NextBlock(
    nextBlockOptions: List<LeancherIntent.Block>,
    blockSelected: (LeancherIntent.Block) -> Unit
) {
//    val textState = remember { mutableStateOf(TextFieldValue()) }
//    TextField(
//        value = textState.value,
//        onValueChange = { textState.value = it }
//    )
//    Text("Searching for: " + textState.value.text)

    val filter = ""
    Column {
        Text(text = "Filter: \"$filter\"")
        nextBlockOptions.forEach { block -> NextBlockOption(filter, block, blockSelected) }
    }
}

@Composable
fun Block(block: LeancherIntent.Block, renderers: HomeViewModel.Renderers) = when(block) {
    is LeancherIntent.Block.Text -> Text(text = block.content, style = MaterialTheme.typography.subtitle1)
    is LeancherIntent.Block.Action.Getter.InputGetter -> (renderers.input[block.renderer.id]?.invoke(block.reference) ?: { Text("no renderer for ${block.renderer} specified") }).invoke()
    is LeancherIntent.Block.Action.Getter.IntentGetter -> (renderers.output[block.renderer.id] ?: {Text("no renderer for ${block.renderer} specified")}).invoke()
    is LeancherIntent.Block.Message -> Snackbar(text = { Text(text = block.content) })
    else -> { }
}

@Composable
fun NextBlockOption(
    filter: String,
    block: LeancherIntent.Block,
    blockSelected: (LeancherIntent.Block) -> Unit
) = when(block) {
    is LeancherIntent.Block.Text -> Text(
        modifier = Modifier.clickable { blockSelected(block) },
        text = block.content,
        style = MaterialTheme.typography.subtitle1
    )
    else -> { }
}