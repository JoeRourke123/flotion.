package utils

import kotlinx.cinterop.get
import kotlinx.cinterop.toKString
import platform.posix.getenv
import platform.posix.getpwuid
import platform.posix.getuid

/**
 * Returns either the environment variable HOME - or HOME calculated from user data.
 */
fun getHomeDirectory(): String? = getenv("HOME")?.toKString() ?: (getpwuid(getuid())?.get(0)?.pw_dir?.toKString())

