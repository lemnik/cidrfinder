package components.composite

import app.Netmask
import app.parseNetmask
import components.mtextInput
import react.*

const val VALID_CIDR_PATTERN = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\/\\d{1,2}"

interface NetworkCIDRState : RState {
    var netmask: Netmask?
}

interface NetworkCIDRProps : RProps {
    var cols: Int
    var label: String
    var netmask: Netmask?
    var onNetmaskChanged: (Netmask) -> Unit
}

class NetworkCIDRInput(props: NetworkCIDRProps) : RComponent<NetworkCIDRProps, NetworkCIDRState>(props) {

    override fun NetworkCIDRState.init(props: NetworkCIDRProps) {
        netmask = props.netmask
    }

    override fun RBuilder.render() {
        mtextInput(
                label = props.label + (state.netmask?.size?.let { " ($it addresses)" } ?: ""),
                pattern = VALID_CIDR_PATTERN,
                errorMessage = "Must be in the form 0.0.0.0/0",
                value = state.netmask?.toString() ?: "",
                cols = props.cols,
                onChanged = onChanged@{
                    if (!it.matches(VALID_CIDR_PATTERN)) {
                        return@onChanged
                    }

                    try {
                        val mask = parseNetmask(it)
                        setState {
                            this.netmask = mask
                        }

                        props.onNetmaskChanged(mask)
                    } catch (e: Error) {
                        // we totally just ignore these
                    }
                }
        )
    }

}

fun RBuilder.networkCIDR(
        label: String,
        netmask: Netmask?,
        cols: Int = 12,
        onNetmaskChanged: (Netmask) -> Unit = {}
) = child(NetworkCIDRInput::class) {
    attrs.label = label
    attrs.netmask = netmask
    attrs.cols = cols
    attrs.onNetmaskChanged = onNetmaskChanged
}