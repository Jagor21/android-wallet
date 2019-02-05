/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.core.helpers

import com.mw.beam.beamwallet.core.AppConfig
import java.io.File
import java.text.DecimalFormat

/**
 * Created by vain onnellinen on 10/3/18.
 */


fun Long.convertToBeamString(): String = DecimalFormat("#.########").format(this.toDouble() / 100000000)
fun Long.convertToBeam(): Double = this.toDouble() / 100000000
fun Long.convertToBeamWithSign(isSent: Boolean) = if (isSent) "-${this.convertToBeamString()}" else "+${this.convertToBeamString()}"
fun Double.convertToGroth() = (this * 100000000).toLong()
fun List<*>.prepareForLog() = this.joinToString { it.toString() }
fun ByteArray.toHex(): String {
    val result = StringBuilder()
    this.forEach { result.append(String.format("%02X", it)) }
    return result.toString().toLowerCase()
}

fun Int.convertToString(): String {
    val hex = Integer.toHexString(this)
    val sb = StringBuilder()

    for (i in 0 until hex.length - 1 step 2) {
        sb.append(Integer.parseInt(hex.substring(i, i + 2), 16).toChar())
    }

    return sb.toString()
}

enum class TxSender(val value: Boolean) {
    SENT(true), RECEIVED(false);

    companion object {
        private val map: HashMap<Boolean, TxSender> = HashMap()

        init {
            TxSender.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Boolean): TxSender {
            return map[type] ?: throw IllegalArgumentException("Unknown type of TxSender")
        }
    }
}

enum class TxStatus(val value: Int) {
    Pending(0), InProgress(1), Cancelled(2), Completed(3), Failed(4), Registered(5);

    companion object {
        private val map: HashMap<Int, TxStatus> = HashMap()

        init {
            TxStatus.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int): TxStatus {
            return map[type] ?: throw IllegalArgumentException("Unknown type of TxStatus")
        }
    }
}

enum class UtxoStatus(val value: Int) {
    Unavailable(0), Available(1), Maturing(2), Outgoing(3), Incoming(4), Change(5), Spent(6);

    companion object {
        private val map: HashMap<Int, UtxoStatus> = HashMap()

        init {
            UtxoStatus.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int): UtxoStatus {
            return map[type] ?: throw IllegalArgumentException("Unknown type of UtxoStatus")
        }
    }
}

enum class UtxoKeyType(val value: String) {
    Commission("fees"), Coinbase("mine"), Regular("norm"), Change("chng"),
    Kernel("kern"), Kernel2("kerM"), Identity("iden"),
    ChildKey("SubK"), Bbs("BbsM"), Decoy("dcoy");

    companion object {
        private val map: HashMap<String, UtxoKeyType> = HashMap()

        init {
            UtxoKeyType.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: String): UtxoKeyType {
            return map[type] ?: throw IllegalArgumentException("Unknown type of UtxoKeyType")
        }
    }
}

enum class ExpirePeriod(val value: Long) {
    DAY(86400), NEVER(0)
}

enum class WelcomeMode {
    OPEN, CREATE, RESTORE
}

enum class Status(val value: Int) {
    STATUS_OK(0), STATUS_ERROR(-1);

    companion object {
        private val map: MutableMap<Int, Status> = java.util.HashMap()

        init {
            Status.values().forEach {
                map[it.value] = it
            }
        }

        fun fromValue(type: Int?): Status {
            return map[type] ?: STATUS_ERROR
        }
    }
}

fun removeDatabase() {
    File(AppConfig.DB_PATH, AppConfig.DB_FILE_NAME).delete()
}
