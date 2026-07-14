package com.aideai;

import com.aideai.command.AideAICommand;
import com.aideai.config.ModConfig;
import com.aideai.event.AIEventManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AideAI.MODID)
public class AideAI {
    public static final String MODID = "aideai";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    
    public AideAI(IEventBus modEventBus) {
        LOGGER.info("AideAI mod loading...");
        
        ModConfig.init();
        NeoForge.EVENT_BUS.register(new AIEventManager());
        NeoForge.EVENT_BUS.register(new AideAICommand());
    }
}
