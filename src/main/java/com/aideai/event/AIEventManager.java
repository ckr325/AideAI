package com.aideai.event;

import com.aideai.AideAI;
import com.aideai.config.ModConfig;
import com.aideai.network.AIApiClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ClientTickEvent;
import net.minecraft.server.level.ServerPlayer;

public class AIEventManager {
    private int tickCounter = 0;

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
    public void onClientTick(ClientTickEvent.Post event) {
        if (!ModConfig.CLIENT.autoChatEnabled.get()) return;
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        
        tickCounter++;
        if (tickCounter < 6000) return;  // 6000 ticks = ~5 minutes
        tickCounter = 0;
        
        new Thread(() -> {
            String response = AIApiClient.sendMessage("say something interesting in Minecraft chat");
            minecraft.execute(() -> {
                if (minecraft.player != null) {
                    minecraft.player.sendSystemMessage(Component.literal("[AideAI] " + response));
                }
            });
        }).start();
    }
}
