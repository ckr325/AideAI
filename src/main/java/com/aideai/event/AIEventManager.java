package com.aideai.event;

import com.aideai.AideAI;
import com.aideai.network.AIApiClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.server.level.ServerPlayer;

public class AIEventManager {
    private int tickCounter = 0;
    private long lastAutoChatTime = 0;
    private boolean firstJoin = true;

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            
            new Thread(() -> {
                String response = AIApiClient.sendMessage("Player died, generate a funny respawn message");
                Minecraft.getInstance().execute(() -> {
                    player.sendSystemMessage(Component.literal("[AideAI] " + response));
                    String command = AIApiClient.extractCommand(response);
                    if (command != null) {
                        player.getServer().getCommands().performPrefixedCommand(
                            player.createCommandSourceStack(), command);
                    }
                });
            }).start();
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < 200) return;
        tickCounter = 0;
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && !minecraft.player.isAlive()) {
            new Thread(() -> {
                String response = AIApiClient.sendMessage("Player is watching, say something");
                minecraft.execute(() -> {
                    minecraft.player.sendSystemMessage(Component.literal("[AideAI] " + response));
                });
            }).start();
        }
    }
}
