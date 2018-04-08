package app

import components.*
import components.composite.networkCIDR
import react.*
import react.dom.div
import react.dom.form
import kotlin.math.pow

interface AppState : RState {
    var rootNetmask: Netmask?
    var existingSubnets: List<Pair<String?, Netmask?>>?
    var requiredSubnets: IntArray
}

interface AppProps : RProps {
}

class App(props: AppProps) : RComponent<AppProps, AppState>(props) {

    override fun AppState.init(props: AppProps) {
        rootNetmask = Netmask(0, 0)
        existingSubnets = emptyList()
        requiredSubnets = intArrayOf(1, 0, 0, 0)
    }

    private fun allocateSubnets(): List<Netmask?> {
        val rootNetmask = state.rootNetmask
        val existingNetworks = state.existingSubnets?.map { it.second }?.filterNotNull() ?: emptyList()
        val requiredSubnets = state.requiredSubnets

        if (rootNetmask == null || requiredSubnets?.isEmpty()) {
            return emptyList()
        }

        val output = ArrayList<Netmask?>(requiredSubnets.size)
        var base = rootNetmask.first

        requiredSubnets.forEach { requiredBits ->
            if (requiredBits == 0) {
                output.add(null)
            } else {
                var collision: Netmask? = null
                var proposed: Netmask

                do {
                    proposed = Netmask(base, 32 - requiredBits)
                    collision = existingNetworks.find { it.contains(proposed) }
                    if (collision != null) {
                        base = collision.nextStart()
                    } else {
                        collision = existingNetworks.find { proposed.contains(it) }
                        if (collision != null) {
                            base = proposed.nextStart()
                        }
                    }

                    collision = output.find { it != null && it.contains(proposed) }
                    if (collision != null) {
                        base = collision.nextStart()
                    } else {
                        collision = output.find { it != null && proposed.contains(it) }
                        if (collision != null) {
                            base = proposed.nextStart()
                        }
                    }
                } while (collision != null && rootNetmask.contains(proposed))

                output.add(proposed.takeIf { rootNetmask.contains(proposed) })
                base = proposed.nextStart()
            }
        }

        return output
    }

    private inline fun RBuilder.requiredSubnet(allocatedSubnets: List<Netmask?>, index: Int, subnetSize: Int) {
        form(action = "#", classes = "mdl-cell mdl-cell--12-col mdl-grid") {
            slider(0, 32, value = subnetSize, cols = 8, labeled = true,
                    label = {
                        if (it == 0) "empty"
                        else (2.0).pow((it ?: 0)).toInt().toString() + " addresses"
                    },
                    onChange = { newSize ->
                        setState {
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
                    setState {
                        requiredSubnets = requiredSubnets.filterIndexed { i, _ -> i != index }.toIntArray()
                    }
                }
            }
        }
    }

    override fun RBuilder.render() {
        val allocatedSubnets = allocateSubnets() // FIXME: this should be moved somewhere more intelligent methinks

        page("CIDR Finder") {
            card("Existing Network Structure") {
                form(action = "#", classes = "mdl-cell mdl-cell--12-col mdl-grid") {
                    networkCIDR("Network CIDR", state.rootNetmask, 12, onNetmaskChanged = { newNetmask ->
                        setState { rootNetmask = newNetmask }
                    })

                    state.existingSubnets?.forEachIndexed { subnetIndex, subnet ->
                        existingSubnet(
                                subnet,
                                onNetmaskChange = { newNetmask ->
                                    setState {
                                        existingSubnets = state.existingSubnets?.mapIndexed { i, pair ->
                                            if (i == subnetIndex) (pair.first to newNetmask)
                                            else pair
                                        }
                                    }
                                },
                                onNameChange = { newName ->
                                    setState {
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
                                setState {
                                    existingSubnets = (state.existingSubnets ?: emptyList()) + ("" to newNetmask)
                                }
                            },
                            onNameChange = { newName ->
                                setState {
                                    existingSubnets = (state.existingSubnets ?: emptyList()) + (newName to null)
                                }
                            }
                    )
                }
            }

            card("Required Subnets") {
                state.requiredSubnets?.forEachIndexed { index, subnetSize ->
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
    mtextInput(label = "Subnet Name (optional)", cols = 6, onChanged = onNameChange)
}

fun RBuilder.app() = child(App::class) {}