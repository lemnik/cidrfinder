package components

import react.RBuilder
import react.RState
import react.dom.div
import react.dom.h2

interface CardProps : GridProps {
    var title: String?
    var content: RBuilder.() -> Unit
}

class Card(props: CardProps) : MDLComponent<CardProps, RState>(props) {

    override fun RBuilder.render() {
        div("mdl-card mdl-shadow--2dp ${props.cellClass()}") {
            val title = props.title
            if (title != null) {
                div("mdl-card__title") {
                    h2("mdl-card__title-text") {
                        +title
                    }
                }

                div("mdl-card__supporting-text mdl-grid") {
                    props.content(this)
                }
            } else {
                props.content(this)
            }
        }
    }

}

fun RBuilder.card(title: String? = null, cols: Int = 12, content: RBuilder.() -> Unit) = child(Card::class) {
    attrs.title = title
    attrs.cols = cols
    attrs.content = content
}