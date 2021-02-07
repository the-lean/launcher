package leancher.android.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import leancher.android.viewmodels.Block
import leancher.android.viewmodels.CurrentBlock
import leancher.android.viewmodels.HomeViewModel

val blockOffset = 40.dp

@Composable
fun Home(vm: HomeViewModel) {
    @Composable
    fun Header() = Text(
        modifier = Modifier.fillMaxWidth(),
        text = "I WANNA…",
        style = MaterialTheme.typography.h1.merge(
            SpanStyle(
                fontSize = 64.sp,
                fontWeight = FontWeight.Black
            )
        )
    )

    Column(Modifier.padding(0.dp)) {
        Header()
        Spacer(Modifier.height(16.dp))
        if (vm.currentBlock == null)
            InputRequester(vm::start)
        else
            Box(Modifier.fillMaxSize()) {
                Blocks(vm.blocks)
                CurrentBlock(vm.currentBlock!!, blockIndex = vm.blocks.size)
            }
    }

}


@Composable
fun BlockCard(blockIndex: Int, content: @Composable () -> Unit) =
    Card(
        elevation = 16.dp,
        shape = RoundedCornerShape(topLeft = 24.dp, topRight = 24.dp),
        backgroundColor = Color.LightGray,
        contentColor = Color.Red,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = blockOffset.times(blockIndex))
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }

@Composable
fun InputRequester(start: () -> Unit) = Box(modifier = Modifier
    .fillMaxSize()
    .clickable { start() }) {
    Text(text = "click to begin")
}

@Composable
fun CurrentBlock(currentBlock: CurrentBlock, blockIndex: Int) {
    BlockCard(blockIndex) {
        when (currentBlock) {
            is CurrentBlock.Input -> currentBlock.renderer.invoke()
            is CurrentBlock.Selection -> Selection(currentBlock)
        }
    }
}

@Composable
fun Selection(currentBlock: CurrentBlock.Selection) {
    Column {
        var filter by savedInstanceState(saver = TextFieldValue.Saver) { TextFieldValue() }
        BasicTextField(
            value = filter,
            onValueChange = { filter = it },
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                imeAction = ImeAction.Done // Go, Next, NoAction, None
                // TODO: requesting focus: https://stackoverflow.com/questions/64947249/jetpack-compose-setting-imeaction-does-not-close-or-change-focus-for-the-keyboa
            ),
            decorationBox = {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
//                    Text(text = filter.annotatedString)
                    for (option in currentBlock.options)
                        if (option.text.contains(filter.text))
                            Text(
                                text = option.text,
                                modifier = Modifier.clickable { option.select() },
                                style = MaterialTheme.typography.subtitle1
                            )
                }
            }
        )
    }
}


@Composable
fun Blocks(blocks: List<Block>) =
    blocks.withIndex().forEach { (index, block) ->
        BlockCard(blockIndex = index) {
            FinishedBlock(block)
        }
    }


@Composable
fun FinishedBlock(block: Block) = when (block) {
    is Block.Text -> Text(text = block.content, style = MaterialTheme.typography.subtitle1)
    is Block.Result -> block.renderer.invoke()
    is Block.Message -> Snackbar(text = { Text(text = block.content) })
    else -> { /* nothing to render */ }
}