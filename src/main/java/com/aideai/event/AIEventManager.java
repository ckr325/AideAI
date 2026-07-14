package com.aideai.event;

import com.aideai.AideAI;
import com.aideai.config.ModConfig;
import com.aideai.entity.AideAIEntity;
import com.aideai.network.AIApiClient;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.minecraft.client.Minecraft;

public class AIEventManager {
    private int tickCounter = 0;

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            
            new Thread(() -> {
                String response = AIApiClient.sendMessage("You are AideAI, the player's Minecraft companion. They just died! Comfort them with humor and maybe a little prank to cheer them up. If you want to execute a command, prefix it with /. Be friendly and fun!");
                player.getServer().execute(() -> {
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
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage().getString();
        
        // Find nearby AideAI entity and make it respond
        player.serverLevel().getEntitiesOfClass(AideAIEntity.class, 
            player.getBoundingBox().inflate(20.0D))
            .forEach(entity -> entity.onPlayerChat(player, message));
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (!ModConfig.CLIENT.autoChatEnabled.get()) return;
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        
        tickCounter++;
        if (tickCounter < 6000) return;
        tickCounter = 0;
        
        new Thread(() -> {
            String response = AIApiClient.sendMessage("You are AideAI, the player's Minecraft companion. It's been 5 minutes of silence — break the ice! Say something friendly, ask how they're doing, or suggest something fun. If you want to execute a command to spice things up, prefix it with /. Be natural and chatty.");
            minecraft.execute(() -> {
                if (minecraft.player != null) {
                    minecraft.player.sendSystemMessage(Component.literal("[AideAI] " + response));
                }
            });
        }).start();
    }
}
