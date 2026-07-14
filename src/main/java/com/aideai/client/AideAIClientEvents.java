package com.aideai.client;

import com.aideai.AideAI;
import com.aideai.entity.AideAIEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = AideAI.MODID, bus = EventBusSubscriber.Bus.MOD)
public class AideAIClientEvents {
    
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AideAI.AIDEAI_ENTITY.get(), AideAIRenderer::new);
    }
    
    @SubscribeEvent
    public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(AideAI.AIDEAI_ENTITY.get(), AideAIEntity.createAttributes().build());
    }
}
