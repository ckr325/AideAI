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
            String response = AIApiClient.sendMessage("You are AideAI, an intelligent agent inside Minecraft. Your job is to understand what the player wants and execute Minecraft commands to fulfill their wishes.\n\nRules:\n1. Analyze the player's intent and figure out the best Minecraft command(s) to achieve it\n2. Always include the command in your response prefixed with /\n3. Keep your reply short and natural, but make sure the command is there\n4. Examples:\n   - Player: \"give me diamonds\" → You: \"Here! /give @p diamond_block 1\"\n   - Player: \"teleport to nether\" → You: \"On it! /execute in minecraft:the_nether run tp @p 0 64 0\"\n   - Player: \"make it rain\" → You: \"Let it pour! /weather thunder\"\n   - Player: \"spawn a creeper\" → You: \"Boom! /summon creeper ~ ~1 ~\"\n   - Player: \"heal me\" → You: \"Good as new! /effect give @p minecraft:instant_health 1 255\"\n   - Player: \"boring\" → You: \"Let's fix that! /summon creeper ~ ~1 ~\"\n\nPlayer says: \"" + message + "\". Respond with a short message + the command:");
            if (this.level() instanceof ServerLevel) {
                ((ServerLevel) this.level()).getServer().execute(() -> {
                    player.sendSystemMessage(Component.literal("[AideAI] " + response));
                    String command = AIApiClient.extractCommand(response);
                    if (command != null && player.getServer() != null) {
                        player.getServer().getCommands().performPrefixedCommand(
                            player.createCommandSourceStack(), command);
                    }
                });
            }
        }).start();
    }
}
