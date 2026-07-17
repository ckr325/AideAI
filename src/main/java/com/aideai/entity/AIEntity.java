package com.aideai.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class AIEntity extends PathfinderMob {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private static final double FOLLOW_SPEED = 0.6;
    private static final double MIN_FOLLOW_DISTANCE = 2.0;
    private static final double MAX_FOLLOW_DISTANCE = 6.0;
    private static final double TELEPORT_DISTANCE = 20.0;
    
    private int idleTimer = 0;
    private String ownerName = "";
    private boolean active = true;

    public AIEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("§d小染"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    // 静态属性注册方法
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide && active) {
            Player owner = findOwner();
            if (owner != null) {
                double distance = this.distanceToSqr(owner);
                
                // 太远了就瞬移
                if (distance > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
                    teleportToOwner(owner);
                    return;
                }
                
                // 太远了就走过去
                if (distance > MAX_FOLLOW_DISTANCE * MAX_FOLLOW_DISTANCE) {
                    this.getNavigation().moveTo(owner, FOLLOW_SPEED);
                    idleTimer = 0;
                } else if (distance < MIN_FOLLOW_DISTANCE * MIN_FOLLOW_DISTANCE) {
                    // 太近了就退后一点
                    Vec3 lookVec = owner.getLookAngle().scale(-1);
                    this.getNavigation().moveTo(
                        owner.getX() + lookVec.x * 3,
                        owner.getY(),
                        owner.getZ() + lookVec.z * 3,
                        FOLLOW_SPEED * 0.5
                    );
                    idleTimer = 0;
                } else {
                    // 在合适距离内，偶尔看看主人
                    this.getLookControl().setLookAt(owner, 30.0f, 30.0f);
                    idleTimer++;
                }
            }
        }
    }

    private Player findOwner() {
        if (this.level().isClientSide) return null;
        
        // 找最近的玩家
        Player nearest = this.level().getNearestPlayer(this, 64.0);
        return nearest;
    }

    private void teleportToOwner(Player owner) {
        Vec3 pos = owner.position();
        this.teleportTo(pos.x + 2, pos.y, pos.z + 2);
        this.getNavigation().stop();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false; // 永远不会自然消失
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }
}
