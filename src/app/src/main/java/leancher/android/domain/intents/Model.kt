package leancher.android.domain.intents

sealed class BlockId {
    data class TextId(val text: String) : BlockId()
    data class GetterId(val id: String, val reference: String) : BlockId()
    data class SetterId(val id: String) : BlockId()
    data class MessageId(val message: String) : BlockId()
}

class LeancherIntent(val blocks: List<Block>) {
    constructor(vararg blocks: Block) : this(blocks.asList())

    fun matches(ids: List<BlockId>) : Boolean =
        (ids zip blocks.map { block -> block.id })
            .fold(true) { acc, pair ->
                acc && pair.run { first == second }
            }

    sealed class Block {
        abstract val id: BlockId

        override fun equals(other: Any?): Boolean = other is Block && this.id == other.id

        data class Text(val content: String) : Block() {
            override val id = BlockId.TextId(content)
        }

        sealed class Action : Block() {
            sealed class Getter : Action() {
                data class InputGetter(val reference: IntentDefinition.Value.Reference, val renderer: InputRenderer) : Getter() {
                    override val id = BlockId.GetterId(renderer.id, reference.key)
                }
                class IntentGetter(val definition: IntentDefinition, val reference: IntentDefinition.Value.Reference, val renderer: DataRenderer) : Getter() {
                    override val id = BlockId.GetterId(definition.id, reference.key)
                }
            }

            sealed class Setter : Action() {
                data class ReferenceSetter(val reference: IntentDefinition.Value.Reference) : Setter() {
                    override val id = BlockId.SetterId("ref:${reference.key}")
                }
                class IntentDefinitionSetter(val definition: IntentDefinition) : Setter() {
                    override val id = BlockId.SetterId("intent:${definition.id}")
                }
            }

            data class InputRenderer(val id: String)
            data class DataRenderer(val id: String)

            data class IntentDefinition(
                val id: String,
                val additional: Additional? = null,
                val extras: List<Extra>? = null) {
                data class Extra(val id: String, val value: Value)

                sealed class ChooserDefinition {
                    object NoChooser : ChooserDefinition()
                    data class Chooser(val title: String) : ChooserDefinition()
                }

                sealed class Additional {
                    data class Data(val content: Value) : Additional()
                    data class Type(val content: Value) : Additional()
                }

                sealed class Value {
                    data class Reference(val key: String) : Value()
                    data class Constant(val value: Any) : Value()
                }
            }
        }

        data class Message(val content: String) : Block() {
            override val id = BlockId.MessageId(content)
        }
    }
}