package com.aideai;

import com.aideai.command.AideAICommand;
import com.aideai.config.ModConfig;
import com.aideai.event.AIEventManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slforj.Logger;
import org.slforj.loggerFactory;

@mod(AideAI.MODID)
public class AideAI {
    public static final String MODID = "aideai";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    
    public AideAI(IEventBus modEventBus) {
        LOGGER.info("AideAI 模组加载中...");
        
        // 注册配置
        ModConfig.init();
        
        // 注册事仵
        NeoForge.EVENT_BUS.register(new AIEventManager());
        
        // 注册命令
        NeoForge.EVENT_BUS.register(new AideAICommand());
    }
}
