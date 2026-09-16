package dev.mikelcodez.pingspikeindicator.client.mixin;

import dev.mikelcodez.pingspikeindicator.client.ClientPingCapture;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@Inject(method = "handlePing", at = @At("HEAD"))
	private void onPingReceived(ClientboundPingPacket packet, CallbackInfo ci) {
		ClientPingCapture capture = ClientPingCapture.getActive();
		if (capture != null) {
			capture.onPingPacketReceived(packet.getId());
		}
	}
}
