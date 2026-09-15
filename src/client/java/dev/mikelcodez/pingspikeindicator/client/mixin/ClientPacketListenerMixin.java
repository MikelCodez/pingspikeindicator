package dev.mikelcodez.pingspikeindicator.client.mixin;

import dev.mikelcodez.pingspikeindicator.client.ClientPingCapture;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@Inject(method = "handlePongResponse", at = @At("HEAD"))
	private void onPongResponse(ClientboundPongResponsePacket packet, CallbackInfo ci) {
		long now = Util.getMillis();
		long sentTime = packet.time();
		long elapsed = now - sentTime;
		if (elapsed >= 0 && elapsed < 30_000) {
			ClientPingCapture capture = ClientPingCapture.getActive();
			if (capture != null) {
				capture.onRealTimePongReceived((int) elapsed);
			}
		}
	}
}
