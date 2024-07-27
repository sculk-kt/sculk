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

package dev.sculk.protocol.packet.inbound.handshake

import dev.sculk.protocol.Connection
import dev.sculk.protocol.packet.InboundPacket
import dev.sculk.protocol.utils.ConnectionState
import dev.sculk.protocol.utils.readString
import dev.sculk.protocol.utils.readVarInt
import java.io.DataInputStream

class InboundHandshakePkt : InboundPacket {
    override val packetId: Int = 0x00

    private var protocolVersion: Int = 0
    private var serverAddress: String = ""
    private var serverPort: Int = 0
    private var nextState: Int = 0

    override fun deser(input: DataInputStream) {
        protocolVersion = input.readVarInt()
        serverAddress = input.readString()
        serverPort = input.readUnsignedShort()
        nextState = input.readVarInt()
    }

    override fun handle(connection: Connection) {
        when (nextState) {
            1 -> connection.state = ConnectionState.STATUS
            2 -> connection.state = ConnectionState.LOGIN
        }
    }
}
