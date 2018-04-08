package components

import org.w3c.dom.HTMLTextAreaElement
import react.RBuilder
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Promise

fun copySuccess() {
    toast("Copied to clipboard")
}

fun fallbackCopyToClipboard(value: String) {
    var textArea = document.createElement("textarea") as HTMLTextAreaElement
    textArea.value = value
    document.body?.appendChild(textArea)

    textArea.focus()
    textArea.select()

    try {
        document.execCommand("copy")
        copySuccess()
    } catch (err: Throwable) {
        // fallback didn't work either
    }

    document.body?.removeChild(textArea)
}

fun copyToClipboard(value: String) {
    val clipboard = window.navigator.asDynamic().clipboard

    if (clipboard != null && clipboard.writeText != null) {
        clipboard.writeText(value)?.unsafeCast<Promise<Any?>>()!!
                .then {
                    copySuccess()
                }
                .catch {
                    fallbackCopyToClipboard(value)
                }
    } else {
        fallbackCopyToClipboard(value)
    }
}

inline fun RBuilder.clipboardChip(
        label: String,
        clipboardContent: String = label,
        cols: Int? = null,
        prefix: String? = null,
        prefixBackgroundColor: String = "light-blue",
        prefixTextColor: String = "white"
) {
    chip(label, cols, prefix, prefixBackgroundColor, prefixTextColor, MaterialIcon.content_copy) {
        copyToClipboard(clipboardContent)
    }
}