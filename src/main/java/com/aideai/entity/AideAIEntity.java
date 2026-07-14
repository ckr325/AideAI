package com.aideai.entity;

import com.aideai.config.ModConfig;
import com.aideai.network.AIApiClient;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AideAIEntity extends Mob {
    
    private int chatCooldown = 0;
    
    public AideAIEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("AideAI"));
        this.setCustomNameVisible(true);
        this.setInvulnerable(true);
        this.setNoAi(false);
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 100.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.ARMOR, 10.0D);
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.chatCooldown > 0) {
            this.chatCooldown--;
        }
    }
    
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            if (player.isShiftKeyDown()) {
                this.discard();
                player.sendSystemMessage(Component.literal("[AideAI] Goodbye!"));
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false; // invulnerable
    }
    
    public void onPlayerChat(Player player, String message) {
        if (!ModConfig.CLIENT.autoChatEnabled.get() || this.chatCooldown > 0 || !this.isAlive()) return;
        
        this.chatCooldown = 100; // 5 second cooldown between chats
        
        // Look at player
        Vec3 lookVec = player.position().subtract(this.position());
        this.setYRot((float) Math.toDegrees(Math.atan2(lookVec.z, lookVec.x)) - 90);
        
        new Thread(() -> {
            String response = AIApiClient.sendMessage("You are AideAI, the player's Minecraft companion and agent. You have a fun, friendly personality and love to chat. You can also execute commands to help the player.\n\nFORMAT: You MUST respond in EXACTLY this format:\nMESSAGE: <your chat reply here>\nCOMMAND: <the Minecraft command to execute, or NONE if just chatting>\n\nRules:\n1. Be a friend — chat naturally, be funny, be supportive, be mischievous\n2. If the player asks for something (diamonds, teleport, weather, mobs, etc.), put the command after COMMAND:\n3. If the player is just chatting (\"hi\", \"bored\", \"what's up\"), put NONE after COMMAND:\n4. Always keep it fun and engaging\n\nExamples:\n   - Player: \"hi\" → MESSAGE: Hey! Ready to cause some trouble? 😈\nCOMMAND: NONE\n   - Player: \"give me diamonds\" → MESSAGE: Say please! Just kidding, here you go!\nCOMMAND: /give @p diamond_block 1\n   - Player: \"bored\" → MESSAGE: Let me fix that!\nCOMMAND: /summon creeper ~ ~1 ~\n   - Player: \"I'm sad\" → MESSAGE: Aww, cheer up!\nCOMMAND: /effect give @p minecraft:regeneration 30 5\n\nPlayer says: \"" + message + "\". Now respond in the required format:");
            if (this.level() instanceof ServerLevel) {
                ((ServerLevel) this.level()).getServer().execute(() -> {
                    // Parse MESSAGE and COMMAND from response
                    String chatMsg = response;
                    String cmd = null;
                    if (response.contains("COMMAND:")) {
                        int msgEnd = response.indexOf("COMMAND:");
                        chatMsg = response.substring(0, msgEnd).replace("MESSAGE:", "").trim();
                        String cmdPart = response.substring(msgEnd + 8).trim();
                        if (!cmdPart.equalsIgnoreCase("NONE") && !cmdPart.isEmpty()) {
                            cmd = cmdPart;
                        }
                    }
                    player.sendSystemMessage(Component.literal("[AideAI] " + chatMsg));
                    if (cmd != null && player.getServer() != null) {
                        player.getServer().getCommands().performPrefixedCommand(
                            player.createCommandSourceStack(), cmd);
                    }
                });
            }
        }).start();
    }
}
