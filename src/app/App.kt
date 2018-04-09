package app

import components.*
import components.cidrfinder.clipboardChip
import components.cidrfinder.networkCIDR
import kotlinext.js.assign
import org.w3c.dom.get
import org.w3c.dom.set
import react.*
import react.dom.div
import react.dom.form
import kotlin.browser.localStorage
import kotlin.browser.window
import kotlin.math.pow

interface AppState : RState {
    var rootNetmask: Netmask?
    var existingSubnets: List<Pair<String?, Netmask?>>?
    var requiredSubnets: IntArray
}

interface AppProps : RProps {
}

data class StoredState(
        val rootNetwork: String?,
        val existingSubnets: Array<Pair<String?, String?>>?,
        val requiredSubnets: Array<Int>
)

fun AppState.toStoredState(): StoredState = StoredState(
        rootNetmask.toString(),
        existingSubnets?.map { it.first to it.second?.toString() }?.toTypedArray(),
        requiredSubnets.toTypedArray()
)

fun AppState.fromStoredState(stored: StoredState) {
    rootNetmask = stored.rootNetwork?.let { parseNetmask(it) }
    existingSubnets = stored.existingSubnets?.map { Pair(it.first, it.second?.let { parseNetmask(it) }) }
    requiredSubnets = stored.requiredSubnets.toIntArray()
}

class App(props: AppProps) : RComponent<AppProps, AppState>(props) {

    override fun AppState.init(props: AppProps) {
        val storedState = localStorage["storedState"]

        if (storedState != null) {
            fromStoredState(JSON.parse<StoredState>(storedState))
        } else {
            rootNetmask = Netmask(0, 0)
            existingSubnets = emptyList()
            requiredSubnets = intArrayOf(1, 0, 0, 0)
        }
    }

    private fun checkCollision(proposed: Netmask, existing: List<Netmask?>): Netmask? {
        var collision = existing.find { it != null && it.contains(proposed) }
        if (collision != null) {
            return collision
        } else {
            collision = existing.find { it != null && proposed.contains(it) }
            if (collision != null) {
                return proposed
            }
        }

        return null
    }

    private fun updateState(buildState: AppState.() -> Unit) {
        setState({ assign(it, buildState) }) {
            // save the state to local storage, once we've been properly updated
            window.setTimeout({ localStorage.set("storedState", JSON.stringify(state.toStoredState())) }, 0)
        }
    }

    private fun allocateSubnets(): List<Netmask?> {
        val rootNetmask = state.rootNetmask
        val existingNetworks = state.existingSubnets?.map { it.second }?.filterNotNull() ?: emptyList()
        val requiredSubnets = state.requiredSubnets

        if (rootNetmask == null || requiredSubnets.isEmpty()) {
            return emptyList()
        }

        val output = ArrayList<Netmask?>(requiredSubnets.size)
        var base = rootNetmask.first

        requiredSubnets.forEach { requiredBits ->
            if (requiredBits == 0) {
                output.add(null)
            } else {
                var proposed: Netmask
                var collision: Netmask?

                do {
                    proposed = Netmask(base, 32 - requiredBits)
                    collision = checkCollision(proposed, existingNetworks)
                            ?: checkCollision(proposed, output)

                    base = collision?.nextStart() ?: base
                } while (collision != null && rootNetmask.contains(proposed))

                output.add(proposed.takeIf { rootNetmask.contains(proposed) })
                base = proposed.nextStart()
            }
        }

        return output
    }

    private fun RBuilder.requiredSubnet(allocatedSubnets: List<Netmask?>, index: Int, subnetSize: Int) {
        form(action = "#", classes = "mdl-cell mdl-cell--12-col mdl-grid") {
            slider(0, 32, value = subnetSize, cols = 8, labeled = true,
                    label = {
                        if (it == 0) "empty"
                        else (2.0).pow((it ?: 0)).toInt().toString() + " addresses"
                    },
                    onChange = { newSize ->
                        updateState {
                            requiredSubnets = requiredSubnets.copyOf().also { it[index] = newSize }
                        }
                    }
            )

            div(cellClass(3)) {
                val subnetmask = allocatedSubnets.getOrNull(index)
                val prefix = (index + 1).toString()
                if (subnetmask != null) {
                    clipboardChip("Subnet: $subnetmask", subnetmask.toString(), prefix = prefix)
                } else {
                    chip("Subnet not allocated", prefix = prefix, prefixBackgroundColor = "blue-grey")
                }
            }

            div(cellClass(1)) {
                mbutton(icon = MaterialIcon.remove) {
                    updateState {
                        requiredSubnets = requiredSubnets.filterIndexed { i, _ -> i != index }.toIntArray()
                    }
                }
            }
        }
    }

    override fun RBuilder.render() {
        val allocatedSubnets = allocateSubnets() // FIXME: this should be moved somewhere more intelligent methinks

        page("Subnet CIDR Finder") {
            card("Existing Network Structure") {
                form(action = "#", classes = "mdl-cell mdl-cell--12-col mdl-grid") {
                    networkCIDR("Network CIDR", state.rootNetmask, 12, onNetmaskChanged = { newNetmask ->
                        updateState { rootNetmask = newNetmask }
                    })

                    state.existingSubnets?.forEachIndexed { subnetIndex, subnet ->
                        existingSubnet(
                                subnet,
                                onNetmaskChange = { newNetmask ->
                                    updateState {
                                        existingSubnets = state.existingSubnets?.mapIndexed { i, pair ->
                                            if (i == subnetIndex) (pair.first to newNetmask)
                                            else pair
                                        }
                                    }
                                },
                                onNameChange = { newName ->
                                    updateState {
                                        existingSubnets = state.existingSubnets?.mapIndexed { i, pair ->
                                            if (i == subnetIndex) (newName to pair.second)
                                            else pair
                                        }
                                    }
                                }
                        )
                    }

                    existingSubnet(null,
                            onNetmaskChange = { newNetmask ->
                                updateState {
                                    existingSubnets = (state.existingSubnets ?: emptyList()) + ("" to newNetmask)
                                }
                            },
                            onNameChange = { newName ->
                                updateState {
                                    existingSubnets = (state.existingSubnets ?: emptyList()) + (newName to null)
                                }
                            }
                    )
                }
            }

            card("Required Subnets") {
                state.requiredSubnets.forEachIndexed { index, subnetSize ->
                    requiredSubnet(allocatedSubnets, index, subnetSize)
                }

                div("mdl-card__menu") {
                    mbutton(icon = MaterialIcon.add, onClick = {
                        setState {
                            requiredSubnets += 0 // new subnet of size 0
                        }
                    })
                }
            }
        }
    }
}

fun RBuilder.existingSubnet(subnet: Pair<String?, Netmask?>?, onNetmaskChange: (Netmask) -> Unit, onNameChange: (String) -> Unit) {
    networkCIDR("Existing Subnet CIDR", subnet?.second, 6, onNetmaskChange)
    mtextInput(label = "Subnet Name (optional)", value = subnet?.first ?: "", cols = 6, onChanged = onNameChange)
}

fun RBuilder.app() = child(App::class) {}