package com.aideai.entity.client;

import com.aideai.entity.AIEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class AIEntityRenderer extends LivingEntityRenderer<AIEntity, PlayerModel<AIEntity>> {
    
    // 使用默认的玩家皮肤纹理（Alex 模型）
    private static final ResourceLocation DEFAULT_SKIN = 
            ResourceLocation.withDefaultNamespace("textures/entity/steve.png");

    public AIEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AIEntity entity) {
        return DEFAULT_SKIN;
    }
}
