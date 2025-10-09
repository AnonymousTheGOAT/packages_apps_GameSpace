/*
 * SPDX-FileCopyrightText: The Android Open Source Project
 * SPDX-FileCopyrightText: TheParasiteProject
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.preferences

import android.os.AsyncTask
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.os.ServiceManager
import android.util.Log
import androidx.annotation.VisibleForTesting

class SystemPropPoker private constructor() {
    private var mBlockPokes = false

    fun blockPokes() {
        mBlockPokes = true
    }

    fun unblockPokes() {
        mBlockPokes = false
    }

    fun poke() {
        if (!mBlockPokes) {
            createPokerTask().execute()
        }
    }

    @VisibleForTesting
    fun createPokerTask(): PokerTask {
        return PokerTask()
    }

    class PokerTask : AsyncTask<Void, Void, Void>() {

        @VisibleForTesting
        fun listServices(): Array<String>? {
            return ServiceManager.listServices()
        }

        @VisibleForTesting
        fun checkService(service: String): IBinder? {
            return ServiceManager.checkService(service)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val services = listServices()
            if (services == null) {
                Log.e(TAG, "There are no services, how odd")
                return null
            }
            for (service in services) {
                val obj = checkService(service)
                if (obj != null) {
                    val parcel = Parcel.obtain()
                    try {
                        obj.transact(IBinder.SYSPROPS_TRANSACTION, parcel, null, 0)
                    } catch (e: RemoteException) {
                        // Ignore
                    } catch (e: Exception) {
                        Log.i(
                            TAG,
                            "Someone wrote a bad service '" +
                                service +
                                "' that doesn't like to be poked",
                            e,
                        )
                    }
                    parcel.recycle()
                }
            }
            return null
        }
    }

    companion object {
        private val TAG: String = "SystemPropPoker"

        @Volatile private var instance: SystemPropPoker? = null

        fun getInstance() =
            instance ?: synchronized(this) { instance ?: SystemPropPoker().also { instance = it } }
    }
}
