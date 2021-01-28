package leancher.android.ui.pages

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import leancher.android.R
import leancher.android.domain.models.PageTitle
import leancher.android.ui.components.TitleCard
import leancher.android.domain.intents.LeancherIntent
import leancher.android.viewmodels.HomeViewModel
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.platform.AmbientContext

@Composable
fun Home(vm: HomeViewModel) {
    val context = AmbientContext.current

    val homeTitleModel = PageTitle(
        context.getString(R.string.page_home),
        context.getString(R.string.launcher_experience),
        R.drawable.leancher
    )

    // TODO
    var textValue by savedInstanceState { "1234567812345678" }

    Row {
        Column(Modifier.padding(10.dp)) {
            TitleCard(pageTitle = homeTitleModel, null)
        }
    }

    Column(Modifier.padding(10.dp)) {
//        Text(text = vm.greeting)
//        Text(text = "Step Index: ${vm.stepIndex}")
        IWanna()
        ScrollableColumn() {
            Blocks(vm.blocks, vm.renderers)
            NextBlock(vm.nextBlockOptions, vm::blockSelected, vm.renderers)
            if(vm.isFinished) Button(onClick = vm::onStartOver) {
                Text(text = "Start Over")
            }
        }
    }
}


@Composable
fun IWanna() = Text("i wanna …", style = MaterialTheme.typography.subtitle1)

@Composable
fun Blocks(blocks: List<LeancherIntent.Block>, renderers: HomeViewModel.Renderers) =
    Column(modifier = Modifier.padding(vertical = 5.dp)) { blocks.forEach { Block(it, renderers) } }

@Composable
fun NextBlock(
    nextBlockOptions: List<LeancherIntent.Block>,
    blockSelected: (LeancherIntent.Block) -> Unit,
    renderers: HomeViewModel.Renderers
) {
//    val textState = remember { mutableStateOf(TextFieldValue()) }
//    TextField(
//        value = textState.value,
//        onValueChange = { textState.value = it }
//    )
//    Text("Searching for: " + textState.value.text)
    nextBlockOptions.forEach { block ->
        Card(Modifier.clickable(onClick = { blockSelected(block) }).padding(vertical = 5.dp)) { Block(block, renderers) }
    }
}

@Composable
fun Block(block: LeancherIntent.Block, renderers: HomeViewModel.Renderers) = when(block) {
    is LeancherIntent.Block.Text -> Text(text = block.content, style = MaterialTheme.typography.subtitle1)
    is LeancherIntent.Block.Action.Getter.InputGetter -> (renderers.input[block.renderer.id]?.invoke(block.reference) ?: { Text("no renderer for ${block.renderer} specified") }).invoke()
    is LeancherIntent.Block.Action.Getter.IntentGetter -> TODO("render result")
    is LeancherIntent.Block.Action.Setter.ReferenceSetter -> { }
    is LeancherIntent.Block.Action.Setter.IntentDefinitionSetter -> { }
    is LeancherIntent.Block.Message -> Text(text = block.content, style = MaterialTheme.typography.subtitle1)
}