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
            String response = AIApiClient.sendMessage("Player says: \"" + message + "\". Reply in character as AideAI, a mischievous Minecraft AI assistant.");
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
