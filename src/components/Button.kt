package components

import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.dom.button

fun RBuilder.mbutton(
        label: String? = null,
        icon: MaterialIcon? = null,
        floatingActionButton: Boolean = false,
        colored: Boolean = false,
        ripple: Boolean = false,
        raised: Boolean = true,
        cols: Int? = null,
        onClick: () -> Unit = {}
) {
    button(classes = classes("mdl-button mdl-js-button",
            floatingActionButton to "mdl-button--fab",
            colored to "mdl-button--colored",
            ripple to "mdl-js-ripple-effect",
            raised to "mdl-button--raised",
            (label == null && icon != null) to "mdl-button--icon",
            (cols != null) to cellClass(cols ?: 0))) {

        if (label != null) {
            +label!!
        }

        icon(icon)

        attrs {
            onClickFunction = {
                onClick()
            }
        }
    }
}