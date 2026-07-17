package com.aideai.entity.client;

import com.aideai.AideAI;
import com.aideai.entity.AIEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

public class AIEntityRenderer extends VillagerRenderer {
    
    // 用村民纹理的基础上改色调 —— 这里先用村民原纹理
    private static final ResourceLocation TEXTURE = 
            ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");

    public AIEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 可以给实体加个发光效果
        this.shadowRadius = 0.5f;
    }

    @Override
    public ResourceLocation getTextureLocation(Villager entity) {
        return TEXTURE;
    }
}
