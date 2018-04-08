package components

import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.dom.a
import react.dom.span

fun RBuilder.chip(
        label: String,
        cols: Int? = null,
        prefix: String? = null,
        prefixBackgroundColor: String = "light-blue",
        prefixTextColor: String = "white",
        actionIcon: MaterialIcon? = null,
        onActionClick: () -> Unit = {}
) {
    span(classes("mdl-chip",
            (prefix != null) to "mdl-chip--contact",
            (cols != null) to cellClass(cols ?: 0)
    )) {
        if (prefix != null) {
            span("mdl-chip__contact mdl-color--${prefixBackgroundColor} mdl-color-text--${prefixTextColor}") {
                +prefix
            }
        }

        span("mdl-chip__text") { +label }

        if (actionIcon != null) {
            a("#", classes = "mdl-chip__action") {
                icon(actionIcon)

                attrs {
                    onClickFunction = { onActionClick() }
                }
            }
        }
    }
}