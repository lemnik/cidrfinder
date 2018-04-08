package components

import kotlinext.js.JsObject
import kotlinext.js.Object
import kotlinx.html.ButtonType
import kotlinx.html.HTMLTag
import kotlinx.html.attributesMapOf
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import react.RBuilder
import react.dom.*
import kotlin.browser.document
import kotlin.js.Json

inline fun RBuilder.page(title: String, actions: List<Pair<String, () -> Unit>> = emptyList(), block: RDOMBuilder<HTMLTag>.() -> Unit) {
    div("mdl-layout mdl-js-layout mdl-layout--fixed-header") {
        header("mdl-layout__header") {
            div("mdl-layout__header-row") {
                span("mdl-layout-title") {
                    +title
                }

                div("mdl-layout-spacer") {}

                nav("mdl-navigation mdl-layout--large-screen-only") {
                    actions.forEach {
                        val (title, onClick) = it
                        a("mdl-navigation__link") {
                            +title

                            attrs {
                                onClickFunction = { onClick() }
                            }
                        }
                    }
                }
            }
        }

        tag({
            div("mdl-grid") {
                attrs {
                    jsStyle {
                        maxWidth = 1080
                    }
                }

                block()
            }
        }) {
            HTMLTag("main", it, attributesMapOf("class", "mdl-layout__content"), null, false, false)
        }

        // snackbar
        div("mdl-snackbar mdl-js-snackbar") {
            attrs {
                id = "snackbar"
            }

            div("mdl-snackbar__text") {}
            button(classes = "mdl-snackbar__action", type = ButtonType.button) {}
        }
    }
}

fun toast(message: String) {
    val data = js("{}")
    data.message = message
    document.getElementById("snackbar").asDynamic().MaterialSnackbar.showSnackbar(data)
}