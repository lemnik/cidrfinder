package components

import app.opt
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RState
import react.dom.div
import react.dom.findDOMNode
import react.dom.input
import react.setState

interface SliderProps : GridProps {
    var min: Int
    var max: Int
    var value: Int?
    var onChange: (Int) -> Unit
    var labeled: Boolean
    var label: (Int?) -> String
}

interface SliderState : RState {
    var value: Int?
}

class Slider(props: SliderProps) : MDLComponent<SliderProps, SliderState>(props) {

    override fun SliderState.init(props: SliderProps) {
        value = props.value ?: 0
    }

    private inline fun RBuilder.wrapper(noinline block: RBuilder.() -> Unit) {
        val cols = props.cols
        if (cols != null) {
            div(props.cellClass(props.labeled.opt(-2, 0))) {
                block()
            }
        } else {
            block()
        }
    }

    override fun RBuilder.render() {
        wrapper {
            input(InputType.range, classes = "mdl-slider mdl-js-slider") {
                attrs {
                    min = props.min.toString()
                    max = props.max.toString()
                    value = state.value.toString()

                    onChangeFunction = {
                        val slider = it.target as HTMLInputElement
                        val value = slider.value.toInt()
                        setState {
                            this.value = value
                        }

                        props.onChange(value)
                    }
                }
            }
        }

        if (props.labeled) {
            div(cellClass(2)) {
                +props.label(state.value)
            }
        }
    }

}

fun RBuilder.slider(
        min: Int = 0,
        max: Int = 100,
        value: Int? = null,
        cols: Int? = null,
        labeled: Boolean = false,
        label: (Int?) -> String = {(it ?: 0).toString()},
        onChange: (Int) -> Unit = {}
) = child(Slider::class) {
    attrs.min = min
    attrs.max = max
    attrs.value = value
    attrs.cols = cols
    attrs.labeled = labeled
    attrs.label = label
    attrs.onChange = onChange
}
