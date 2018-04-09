package app

import kotlin.math.pow

typealias Address = Int

fun toAddress(ip: String): Address {
    val b = ip.split('.').map { it.toIntOrNull() }.filterNotNull().filter { it in 0..255 }
    if (b.size != 4) {
        throw Error("Not a valid IP address: $ip")
    }

    return (b[0] shl 24) or (b[1] shl 16) or (b[2] shl 8) or b[3]
}

fun toString(ip: Address) = arrayOf(
        (ip ushr 24) and 0xff,
        (ip ushr 16) and 0xff,
        (ip ushr 8) and 0xff,
        ip and 0xff
).joinToString(".")

class Netmask(val base: Address, val bits: Int) {

    val netmask: Address
        get() = if (bits > 0) ((0xffffffff shl (32 - bits)) ushr 0).toInt() else 0
    val hostmask: Address
        get() = bits.inv()
    val size: Int
        get() = (2.0).pow(32 - this.bits).toInt()
    val first: Address
        get() = if (bits <= 30) this.base + 1 else this.base
    val last: Address
        get() = if (bits <= 30) (this.base + this.size - 2) else (this.base + this.size - 1)
    val broadcast: Address?
        get() = if (bits <= 30) (this.base + this.size - 1) else null

    fun contains(address: Address): Boolean =
        (address and netmask) == (base and netmask)

    fun contains(netmask: Netmask) =
            contains(netmask.base and netmask.netmask) and contains((netmask.broadcast ?: netmask.last) and netmask.netmask)

    override fun toString(): String =
            "${toString(base)}/${bits}"

    fun nextStart(): Address = this.base + this.size + 1

}

fun parseNetmask(cidr: String): Netmask {
    val (net, mask) = cidr.split('/')
    val bits = mask.toInt()
    if (bits < 0 || bits > 32) {
        throw Error("Not a valid netmask for CIDR: $cidr")
    }

    val base = toAddress(net)

    return Netmask(base, bits)
}