package com.example.util

import android.os.Build
import java.io.File

object SecurityUtils {

    /**
     * Checks common indicators for rooted Android devices:
     * 1. Test keys in build tags
     * 2. Common su binary locations
     * 3. Presence of busybox/su executable paths
     */
    fun isDeviceRooted(): Boolean {
        return checkBuildTags() || checkSuPaths() || checkSuperuserApk()
    }

    private fun checkBuildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkSuPaths(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun checkSuperuserApk(): Boolean {
        return File("/system/app/Superuser.apk").exists()
    }
}
