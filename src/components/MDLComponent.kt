package components

import kotlinx.html.RP
import org.w3c.dom.Element
import react.*
import react.dom.findDOMNode

@JsName("componentHandler")
internal external object MDL {
    @JsName("upgradeElement")
    fun upgradeElement(e: Element)

    @JsName("downgradeElements")
    fun downgradeElements(e: Element)
}

interface GridProps : RProps {
    var cols: Int?
}

fun cellClass(cols: Int) = "mdl-cell mdl-cell--${cols}-col mdl-cell--middle"

fun GridProps.cell() = (cols != null) to cellClass()
fun GridProps.cellClass(mod: Int = 0) = components.cellClass((cols ?: 0) + mod)

abstract class MDLComponent<P : RProps, S : RState>(props: P) : RComponent<P, S>(props) {

    override fun componentDidMount() {
        MDL.upgradeElement(findDOMNode(this))
    }

    override fun componentWillUnmount() {
        MDL.downgradeElements(findDOMNode(this))
    }

    companion object {
        private var idIdx = 0
        fun unique() = "__formRef${++idIdx}"
    }

}

interface UpgradeWrapperProps : RProps {
    var block: RBuilder.() -> Unit
}

class UpgradeWrapperComponent(props: UpgradeWrapperProps) : MDLComponent<UpgradeWrapperProps, RState>(props) {
    override fun RBuilder.render() {
        props.block(this)
    }
}

fun RBuilder.upgrade(block: RBuilder.() -> Unit) = child(UpgradeWrapperComponent::class) {
    attrs.block = block
}

fun classes(baseClasses: String, vararg optionalClasses: Pair<Boolean, String>) =
        baseClasses + (
                optionalClasses
                        .filter { it.first }
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString(separator = " ", transform = { it.second }, prefix = if (baseClasses.isEmpty()) "" else " ")
                        ?: ""
                )