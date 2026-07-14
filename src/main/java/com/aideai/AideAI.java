package com.aideai;

import com.aideai.client.AideAIClientEvents;
import com.aideai.command.AideAICommand;
import com.aideai.config.ModConfig;
import com.aideai.entity.AideAIEntity;
import com.aideai.event.AIEventManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Mod(AideAI.MODID)
public class AideAI {
    public static final String MODID = "aideai";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    
    public static final DeferredRegister<EntityType<?>> ENTITIES = 
        DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
    
    public static final Supplier<EntityType<AideAIEntity>> AIDEAI_ENTITY = 
        ENTITIES.register("aideai", 
            () -> EntityType.Builder.of(AideAIEntity::new, MobCategory.CREATURE)
                .sized(0.6F, 1.95F)
                .build("aideai"));
    
    public AideAI(IEventBus modEventBus) {
        LOGGER.info("AideAI mod loading...");
        
        ENTITIES.register(modEventBus);
        ModConfig.init();
        NeoForge.EVENT_BUS.register(new AIEventManager());
        NeoForge.EVENT_BUS.register(new AideAIClientEvents());
        
        AideAICommand commandHandler = new AideAICommand();
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, commandHandler::onRegisterCommands);
    }
}
