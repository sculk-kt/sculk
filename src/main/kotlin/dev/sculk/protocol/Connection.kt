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

package dev.sculk.protocol

import dev.sculk.protocol.packet.InboundPacket
import dev.sculk.protocol.packet.OutboundPacket
import dev.sculk.protocol.utils.Boundedness
import dev.sculk.protocol.utils.ConnectionState
import dev.sculk.protocol.utils.readVarInt
import dev.sculk.protocol.utils.writeVarInt
import java.io.*
import java.net.Socket

class Connection(private val socket: Socket) : Closeable {
    private val input: DataInputStream = DataInputStream(socket.getInputStream())
    private val output: DataOutputStream = DataOutputStream(socket.getOutputStream())
    var state: ConnectionState = ConnectionState.HANDSHAKE

    override fun close() {
        input.close()
        output.close()
        socket.close()
    }

    fun send(packet: OutboundPacket) {
        val byteOutput = ByteArrayOutputStream()
        val dataOutput = DataOutputStream(byteOutput)

        dataOutput.writeVarInt(packet.packetId)
        packet.ser(dataOutput)

        val packetData = byteOutput.toByteArray()
        output.writeVarInt(packetData.size)
        output.write(packetData)
    }

    fun readPackets() {
        try {
            while (true) {
                val len = input.readVarInt()
                val id = input.readVarInt()

                val packet = PacketRegistry.getPacketInstance(Boundedness.Inbound, state, id) as InboundPacket
                packet.deser(input)
                packet.handle(this)
            }
        } catch (e: EOFException) {
            // do nothing
        } finally {
            close()
        }
    }
}
