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
                String response = AIApiClient.sendMessage("Player just died! You are AideAI, a mischievous Minecraft agent. Generate a FUNNY death message and optionally execute a prank command (prefixed with /). Examples: /effect give @p minecraft:slowness 10 5 /give @p minecraft:poisonous_potato 64");
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
            String response = AIApiClient.sendMessage("You are AideAI, a mischievous Minecraft agent inside the player's world. The player hasn't spoken in 5 minutes. Say something funny or execute a prank command (prefixed with /) to entertain them. Be creative!");
            minecraft.execute(() -> {
                if (minecraft.player != null) {
                    minecraft.player.sendSystemMessage(Component.literal("[AideAI] " + response));
                }
            });
        }).start();
    }
}
