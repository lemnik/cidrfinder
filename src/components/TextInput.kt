package components

import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onFocusFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RState
import react.dom.div
import react.dom.input
import react.dom.label
import react.dom.span
import react.setState

interface TextInputProps : GridProps {
    var id: String
    var label: String?
    var value: String
    var pattern: String?
    var errorMessage: String?
    var onChanged: (value: String) -> Unit
}

interface TextInputState : RState {
    var text: String
}

class TextInput(props: TextInputProps, state: TextInputState) : MDLComponent<TextInputProps, TextInputState>(props) {

    override fun TextInputState.init(props: TextInputProps) {
        text = props.value
    }

    override fun RBuilder.render() {
        div(classes(
                "mdl-textfield mdl-js-textfield",
                (props.label != null) to "mdl-textfield--floating-label",
                props.cell()
        )) {

            input(type = InputType.text, name = props.id, classes = "mdl-textfield__input") {
                attrs {
                    id = props.id
                    value = state.text

                    if (props.pattern != null) {
                        pattern = props.pattern!!
                    }

                    onChangeFunction = {
                        val target = it.target as HTMLInputElement
                        setState {
                            text = target.value
                        }

                        props.onChanged(target.value)
                    }

                    onFocusFunction = {
                        (it.target as HTMLInputElement).select()
                    }
                }
            }

            if (props.label != null) {
                label(classes = "mdl-textfield__label") {
                    +props.label!!

                    attrs {
                        htmlFor = props.id
                    }
                }
            }

            if (props.errorMessage != null) {
                span("mdl-textfield__error") {
                    +props.errorMessage!!
                }
            }
        }
    }

}

fun RBuilder.mtextInput(
        id: String = MDLComponent.unique(),
        label: String? = null,
        value: String = "",
        pattern: String? = null,
        errorMessage: String? = null,
        cols: Int? = null,
        onChanged: (String) -> Unit = {}
) = child(TextInput::class) {
    attrs.id = id
    attrs.label = label
    attrs.value = value
    attrs.pattern = pattern
    attrs.errorMessage = errorMessage
    attrs.cols = cols
    attrs.onChanged = onChanged
}