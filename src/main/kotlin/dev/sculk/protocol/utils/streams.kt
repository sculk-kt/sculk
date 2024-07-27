/*
 * Copyright 2024 sculk.dev and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sculk.protocol.utils

import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.RuntimeException
import java.util.*

private const val MAX_INT_BYTES = 5
private const val MAX_LONG_BYTES = 10

private const val DATA_BIT_MASK = 127
private const val CONTINUATION_BIT_MASK = 128
private const val DATA_BITS_PER_BYTE = 7

private fun hasContinuationBit(data: Byte): Boolean = (data.toInt() and CONTINUATION_BIT_MASK) == CONTINUATION_BIT_MASK

fun DataOutputStream.writeVarInt(value: Int) {
    var v = value
    while ((v and -CONTINUATION_BIT_MASK) != 0) {
        writeByte((v and DATA_BIT_MASK) or CONTINUATION_BIT_MASK)
        v = v ushr DATA_BITS_PER_BYTE
    }
    writeByte(v)
}

fun DataOutputStream.writeVarLong(value: Long) {
    var v = value
    while ((v and (-CONTINUATION_BIT_MASK).toLong()) != 0L) {
        writeByte(((v and DATA_BIT_MASK.toLong()) or CONTINUATION_BIT_MASK.toLong()).toInt())
        v = v ushr DATA_BITS_PER_BYTE
    }
    writeByte(v.toInt())
}

fun DataOutputStream.writeString(value: String) {
    val bytes = value.toByteArray(Charsets.UTF_8)
    writeVarInt(bytes.size)
    write(bytes)
}

fun DataOutputStream.writeUuid(value: UUID) {
    val mostSignificantBits = value.mostSignificantBits
    val leastSignificantBits = value.leastSignificantBits
    writeLong(mostSignificantBits)
    writeLong(leastSignificantBits)
}

fun DataInputStream.readVarInt(): Int {
    var value = 0
    var shift = 0
    var read: Byte

    do {
        read = readByte()
        value = value or ((read.toInt() and DATA_BIT_MASK) shl (shift++ * DATA_BITS_PER_BYTE))
        if (shift > MAX_INT_BYTES) {
            throw RuntimeException("VarInt is too big")
        }
    } while (hasContinuationBit(read))

    return value
}

fun DataInputStream.readVarLong(): Long {
    var value = 0L
    var shift = 0
    var byteRead: Byte

    do {
        byteRead = readByte()
        value = value or ((byteRead.toLong() and DATA_BIT_MASK.toLong()) shl (shift++ * DATA_BITS_PER_BYTE))
        if (shift > MAX_LONG_BYTES) {
            throw RuntimeException("VarLong is too big")
        }
    } while (hasContinuationBit(byteRead))

    return value
}

fun DataInputStream.readString(): String {
    val len = readVarInt()
    val bytes = ByteArray(len)
    readFully(bytes)
    return bytes.toString(Charsets.UTF_8)
}

fun DataInputStream.readUuid(): UUID {
    val mostSignificantBits = readLong()
    val leastSignificantBits = readLong()
    return UUID(mostSignificantBits, leastSignificantBits)
}
