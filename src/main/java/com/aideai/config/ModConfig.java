package com.aideai.config;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    public static class Client {
        public final ModConfigSpec.ConfigValue<String> apiKey;
        public final ModConfigSpec.ConfigValue<String> apiUrl;
        public final ModConfigSpec.ConfigValue<String> model;
        public final ModConfigSpec.ConfigValue<Integer> autoInterval;
        public final ModConfigSpec.ConfigValue<Boolean> autoChatEnabled;
        
        public Client(ModConfigSpec.Builder builder) {
            builder.push("AI Settings");
            apiKey = builder.comment("AI API Key").define("apiKey", "");
            apiUrl = builder.comment("AI API URL").define("apiUrl", "https://api.deepseek.com/v1/chat/completions");
            model = builder.comment("AI Model name").define("model", "deepseek-chat");
            builder.pop();
            
            builder.push("Auto Chat");
            autoChatEnabled = builder.comment("Enable auto chat").define("autoChatEnabled", false);
            autoInterval = builder.comment("Auto chat interval (seconds)").defineInRange("autoInterval", 300, 0, 3600);
            builder.pop();
        }
    }
    
    private static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;
    
    static {
        final var pair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = pair.getRight();
        CLIENT = pair.getLeft();
    }
    
    public static void init() {
        ModLoadingContext.get().getActiveContainer().registerConfig(Type.CLIENT, CLIENT_SPEC, "aideai-client.toml");
    }
}
