package com.aideai.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    
    public static final ModConfigSpec.ConfigValue<String> API_KEY;
    public static final ModConfigSpec.ConfigValue<String> MODEL;
    public static final ModConfigSpec.DoubleValue TEMPERATURE;
    public static final ModConfigSpec.IntValue MAX_TOKENS;
    public static final ModConfigSpec.ConfigValue<String> API_BASE_URL;
    
    static {
        BUILDER.push("deepseek");
        
        API_KEY = BUILDER
            .comment("DeepSeek API 密钥")
            .define("api_key", "your-api-key-here");
        
        API_BASE_URL = BUILDER
            .comment("API 基础地址")
            .define("api_base_url", "https://api.deepseek.com");
        
        MODEL = BUILDER
            .comment("使用的模型")
            .define("model", "deepseek-chat");
        
        TEMPERATURE = BUILDER
            .comment("生成温度 (0.0 - 2.0)")
            .defineInRange("temperature", 0.7, 0.0, 2.0);
        
        MAX_TOKENS = BUILDER
            .comment("每次生成的最大Token数")
            .defineInRange("max_tokens", 2048, 64, 8192);
        
        BUILDER.pop();
        
        SPEC = BUILDER.build();
    }
}
