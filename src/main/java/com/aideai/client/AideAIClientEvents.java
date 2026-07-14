package com.aideai.client;

import com.aideai.AideAI;
import com.aideai.entity.AideAIEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class AideAIClientEvents {
    
    @SubscribeEvent
    public void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AideAI.AIDEAI_ENTITY.get(), AideAIRenderer::new);
    }
    
    @SubscribeEvent
    public void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(AideAI.AIDEAI_ENTITY.get(), AideAIEntity.createAttributes().build());
    }
}
