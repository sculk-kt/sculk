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

import dev.sculk.protocol.packet.Packet
import dev.sculk.protocol.packet.inbound.handshake.InboundHandshakePkt
import dev.sculk.protocol.packet.inbound.status.InboundPingRequestPkt
import dev.sculk.protocol.packet.inbound.status.InboundStatusRequestPkt
import dev.sculk.protocol.packet.outbound.status.OutboundPingResponsePkt
import dev.sculk.protocol.packet.outbound.status.OutboundStatusResponsePkt
import dev.sculk.protocol.utils.Boundedness
import dev.sculk.protocol.utils.ConnectionState
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate", "Unused")
object PacketRegistry {
    private data class PacketEntry(
        val id: Int,
        val state: ConnectionState,
        val boundedness: Boundedness,
        val packetClass: KClass<out Packet>
    )

    private val REGISTRY = mutableSetOf<PacketEntry>()

    private fun registerPacket(packet: KClass<out Packet>, requiredState: ConnectionState, boundedness: Boundedness) {
        val p = packet.java.getDeclaredConstructor().newInstance()
        REGISTRY.add(PacketEntry(p.packetId, requiredState, boundedness, packet))
    }

    init {
        // Handshake
        registerPacket(InboundHandshakePkt::class, ConnectionState.HANDSHAKE, Boundedness.Inbound)

        // Status
        registerPacket(InboundStatusRequestPkt::class, ConnectionState.STATUS, Boundedness.Inbound)
        registerPacket(InboundPingRequestPkt::class, ConnectionState.STATUS, Boundedness.Inbound)

        registerPacket(OutboundStatusResponsePkt::class, ConnectionState.STATUS, Boundedness.Outbound)
        registerPacket(OutboundPingResponsePkt::class, ConnectionState.STATUS, Boundedness.Outbound)
    }

    fun getPacketClass(boundedness: Boundedness, requiredState: ConnectionState, id: Int): KClass<out Packet>? = REGISTRY
        .find { it.id == id && it.state == requiredState && it.boundedness == boundedness }?.packetClass

    fun getPacketInstance(boundedness: Boundedness, requiredState: ConnectionState, id: Int): Packet? {
        val packet = getPacketClass(boundedness, requiredState, id) ?: throw ClassNotFoundException("No packet with id $id")
        return packet.java.getDeclaredConstructor().newInstance()
    }
}
