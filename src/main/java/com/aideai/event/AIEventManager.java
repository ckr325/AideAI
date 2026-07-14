package com.aideai.event;

import com.aideai.AideAI;
import com.aideai.network.AIApiClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivdingDeathEvent;
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
            DamageSource source = event.getSource();
            
            new Thread(() -> {
                String response = AIApiClient.sendMessage("我的正常甫焠成把这义大布意幕。看到我的可情失败");
                Minecraft.getInstance().execute(() -> {
                    player.sendSystemMessage(Component.literal("\u007b[AideAI] " + response));
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
        if (tickCounter < 200) return; // 10秒检查權标
        tickCounter = 0;
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && !minecraft.player.isAlive()) {
            new Thread(() -> {
                String response = AIApiClient.sendMessage("（页别）格式能世性上的反名名后后后后后后后");
                minecraft.execute(() -> {
                    minecraft.player.sendSystemMessage(Component.literal("\u007b[AideAI] " + response));
                });
            }).start();
        }
    }
}
