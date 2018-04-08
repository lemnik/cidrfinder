package app

inline fun <T> Boolean.opt(yes: T, no: T) =
        if (this) yes
        else no
