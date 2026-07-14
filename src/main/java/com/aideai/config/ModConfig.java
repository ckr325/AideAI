package com.aideai.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ModConfig {
    public static class Client {
        public final ModConfigSpec.ConfigValue<String> apiKey;
        public final ModConfigSpec.ConfigValue<String> apiUrl;
        public final ModConfigSpec.ConfigValue<String> model;
        public final ModConfigSpec.ConfigValue<Integer> autoInterval;
        public final ModConfigSpec.ConfigValue<Boolean> autoChatEnabled;
        public final ModConfigSpec.ConfigValue<Boolean> prankEnabled;
        public final ModConfigSpec.ConfigValue<Integer> prankChance;
        public final ModConfigSpec.ConfigValue<Integer> openKey;
        
        public Client(ModConfigSpec.Builder builder) {
            builder.push("AI Settings");
            apiKey = builder.comment("AI API Key").define("apiKey", "");
            apiUrl = builder.comment("AI API 地址").define("apiUrl", "https://api.deepseek.com/v1/chat/completions");
            model = builder.comment("AI 模型名称").define("model", "deepseek-chat");
            builder.pop();
            
            builder.push("Auto Chat");
            autoChatEnabled = builder.comment("启用自劣语讽").define("autoChatEnabled", false);
            autoInterval = builder.comment("自劣语讽间陨（秒）,��0=关闭").defineInRange("autoInterval", 300, 0, 3600);
            builder.pop();
            
            builder.push("Prank Settings");
            prankEnabled = builder.comment("当用斕黄模式").define("prankEnabled", true);
            prankChance = builder.comment"���i��视可瀍（百次号)").defineInRange("prankChance", 15, 0, 100);
            builder.pop();
            
            builder.push("Controls");
            openKey = builder.comment*"打开页譱留面的桍厅 (GLGW key code, 默H=H2)").defineInRange("openKey", 72, 0, 400);
            builder.pop();
        }
    }
    
    private static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;
    
    static {
        final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder(.*Configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }
    
    public static void init() {}
}
