package com.centinela.app.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.UserManager

object DeviceOwnerManager {

    fun esDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun configurarComoLauncherFijo(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, AdminReceiver::class.java)
        if (!dpm.isDeviceOwnerApp(context.packageName)) return

        dpm.setLockTaskPackages(admin, arrayOf(context.packageName))
        dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
        dpm.setStatusBarDisabled(admin, true)
        addKioskRestrictions(dpm, admin)

        val intentFilter = android.content.IntentFilter(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_HOME)
            addCategory(android.content.Intent.CATEGORY_DEFAULT)
        }
        dpm.addPersistentPreferredActivity(
            admin, intentFilter,
            ComponentName(context.packageName, "com.centinela.app.MainActivity")
        )

        dpm.setUninstallBlocked(admin, context.packageName, true)
    }

    fun liberarLauncherFijo(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, AdminReceiver::class.java)
        if (!dpm.isDeviceOwnerApp(context.packageName)) return
        dpm.setUninstallBlocked(admin, context.packageName, false)
        dpm.clearPackagePersistentPreferredActivities(admin, context.packageName)
        dpm.setLockTaskPackages(admin, emptyArray())
        dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
        dpm.setStatusBarDisabled(admin, false)
        removeKioskRestrictions(dpm, admin)
    }

    private fun addKioskRestrictions(dpm: DevicePolicyManager, admin: ComponentName) {
        listOf(
            UserManager.DISALLOW_CONFIG_DEFAULT_APPS,
            UserManager.DISALLOW_SAFE_BOOT,
            UserManager.DISALLOW_FACTORY_RESET
        ).forEach { restriction ->
            dpm.addUserRestriction(admin, restriction)
        }
    }

    private fun removeKioskRestrictions(dpm: DevicePolicyManager, admin: ComponentName) {
        listOf(
            UserManager.DISALLOW_CONFIG_DEFAULT_APPS,
            UserManager.DISALLOW_SAFE_BOOT,
            UserManager.DISALLOW_FACTORY_RESET
        ).forEach { restriction ->
            dpm.clearUserRestriction(admin, restriction)
        }
    }
}

