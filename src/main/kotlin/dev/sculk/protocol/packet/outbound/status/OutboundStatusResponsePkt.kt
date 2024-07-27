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

package dev.sculk.protocol.packet.outbound.status

import dev.sculk.protocol.packet.OutboundPacket
import dev.sculk.protocol.utils.writeString
import java.io.DataOutputStream

class OutboundStatusResponsePkt(override val packetId: Int = 0x00) : OutboundPacket {
    override fun ser(output: DataOutputStream) {
        output.writeString("{\"version\":{\"name\":\"1.20.4\",\"protocol\":765},\"players\":{\"max\":100,\"online\":0,\"sample\":[]},\"description\":{\"text\":\"This is pain and suffering\"},\"favicon\":\"\",\"enforcesSecureChat\":false,\"previewsChat\":false}")
    }
}
