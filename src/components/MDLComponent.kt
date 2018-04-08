package components

import org.w3c.dom.Element
import react.RComponent
import react.RProps
import react.RState
import react.dom.findDOMNode

@JsName("componentHandler")
internal external object MDL {
    @JsName("upgradeElement")
    fun upgradeElement(e: Element): Unit

    @JsName("downgradeElements")
    fun downgradeElements(e: Element): Unit
}

interface GridProps : RProps {
    var cols: Int?
}

fun cellClass(cols: Int) = "mdl-cell mdl-cell--${cols}-col mdl-cell--middle"

fun GridProps.cell() = (cols != null) to cellClass()
fun GridProps.cellClass(mod: Int = 0) = components.cellClass((cols ?: 0) + mod)

abstract class MDLComponent<P : RProps, S : RState>(props: P) : RComponent<P, S>(props) {

    override fun componentDidMount(): Unit {
        val node = findDOMNode(this)
        if (node != null) {
            MDL.upgradeElement(node)
        }
    }

    override fun componentWillUnmount(): Unit {
        val node = findDOMNode(this)
        if (node != null) {
            MDL.downgradeElements(node)
        }
    }

    companion object {
        private var idIdx = 0
        fun unique() = "__formRef${++idIdx}"
    }

}

fun classes(baseClasses: String, vararg optionalClasses: Pair<Boolean, String>) =
        baseClasses + (
                optionalClasses
                        .filter { it.first }
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString(separator = " ", transform = { it.second }, prefix = if (baseClasses.isEmpty()) "" else " ")
                        ?: ""
                )