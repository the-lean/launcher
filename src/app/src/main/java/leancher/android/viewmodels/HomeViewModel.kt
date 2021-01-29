package leancher.android.viewmodels

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import leancher.android.domain.intents.BlockId
import leancher.android.domain.intents.LeancherIntent
import leancher.android.domain.intents.LeancherIntent.Block.Action.IntentDefinition.Additional.Data
import leancher.android.domain.intents.LeancherIntent.Block.Action.IntentDefinition.Additional.Type
import leancher.android.domain.intents.LeancherIntent.Block.Action.IntentDefinition.Value
import leancher.android.domain.intents.LeancherIntent.Block.Action.IntentDefinition.Value.Constant
import leancher.android.domain.intents.LeancherIntent.Block.Action.IntentDefinition.Value.Reference
import java.io.Serializable

fun List<LeancherIntent>.blocksFor(step: Int): List<LeancherIntent.Block> =
    mapNotNull { intent -> intent.blocks.getOrNull(step, ) }
fun List<LeancherIntent.Block>.ids(): List<BlockId> =
    map(LeancherIntent.Block::id)

class HomeModel(
    val store: IScopedStateStore
) {}

interface IScopedStateStore {
    fun <TState>saveState (key: String, state: TState): Unit
    fun <TState>loadState (key: String): TState?
}

class ViewModelFactory<TViewModel : ViewModel?>(private val getViewModel: () -> TViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = getViewModel() as T
}

typealias InputRenderer = @Composable (setResult: (Any) -> Unit) -> Unit
typealias OutputRenderer = @Composable () -> Unit

class HomeViewModel(
    private val model: HomeModel,
    inputRenderers: Map<String, InputRenderer>,
    outputRenderers: Map<String, OutputRenderer>,
    private val actions: Actions
) : ViewModel() {

    private val intents = leancher.android.domain.intents.intents

    var stepIndex by mutableStateOf(0)
        private set

    var blocks: List<LeancherIntent.Block> by mutableStateOf(listOf())
        private set

    var nextBlockOptions: List<LeancherIntent.Block> by mutableStateOf(nextBlockOptions())
        private set

    var isFinished: Boolean by mutableStateOf(false)
        private set

    class Renderers(
        val input: Map<String, (Reference) -> (@Composable () -> Unit)>,
        val output: Map<String, @Composable () -> Unit>
    )
    val renderers = Renderers (
        inputRenderers.mapValues { (_, renderFunction) -> { reference -> { renderFunction { value -> setInputResult(reference, value) } } } },
        outputRenderers
    )

    private fun setInputResult(reference: Reference, value: Any) {
        values[reference.key] = value
        finishBlock()
    }

    private fun showBlock(block: LeancherIntent.Block) {
        blocks += block
    }

    private fun finishBlock() {
        stepIndex++
        nextBlockOptions = nextBlockOptions()

        next()
    }

    private fun next() {
        when (nextBlockOptions.size) {
            0 -> isFinished = true
            1 -> {
                val block = nextBlockOptions[0]
                showBlock(block)
                execute(block)
            }
        }
    }

    private fun execute(block: LeancherIntent.Block) {
        when(block) {
            is LeancherIntent.Block.Action.Getter.IntentGetter -> {
                Log.i("HOMEVIEWMODEL", "Intent Getter")
                finishBlock()
            }
            is LeancherIntent.Block.Action.Setter.ReferenceSetter -> {
                actions.executeIntent(values[block.reference.key] as Intent)
                finishBlock()
            }
            is LeancherIntent.Block.Action.Setter.IntentDefinitionSetter -> {
                actions.executeIntent(createIntent(block.definition))
                finishBlock()
            }
            is LeancherIntent.Block.Message -> {
                finishBlock()
            }
            else -> { /* no side effect has to be executed */ }
        }
//        finishBlock()
    }

    fun blockSelected(block: LeancherIntent.Block) {
        showBlock(block)
        finishBlock()
    }

    fun onStartOver() {
        stepIndex = 0
        blocks = listOf()
        nextBlockOptions = nextBlockOptions()
        isFinished = false
    }

    private fun nextBlockOptions() = intents.filter { intent -> intent.matches(blocks.ids()) }.blocksFor(stepIndex)

    private val values: MutableMap<String, Any> = mutableMapOf()

    private fun <T>resolveValue(registry: Map<String, Any>, value: Value): T? =
        when(value) {
            is Reference -> registry[value.key]
            is Constant -> value.value
        } as T?

    private fun createIntent(definition: LeancherIntent.Block.Action.IntentDefinition): Intent = Intent(definition.id).apply {
        when(definition.additional) {
            is Data -> setDataAndNormalize(Uri.parse(resolveValue(values, definition.additional.content)))
            is Type -> setTypeAndNormalize(resolveValue(values, definition.additional.content))
        }

        definition.extras?.forEach { definition -> putExtra(definition.id, resolveValue<Serializable>(values, definition.value)) }
    }

    data class Actions(
        val executeIntent: (Intent) -> Unit,
        val isIntentCallable: (Intent) -> Boolean
    )
}