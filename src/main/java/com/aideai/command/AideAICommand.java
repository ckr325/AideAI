package com.aideai.command;

import com.aideai.config.ModConfig;
import com.aideai.network.AIApiClient;
import com.mojang.brigadier.ArgumentTypes;
import net.minecraft.commands.CommandDispatcher;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class AideAICommand {
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<?> dispatcher = event.getDispatcher();
        
        dispatcher.register(Commands.literal("aideai")
            .then(Commands.literal("setkey")
                .then(Commands.argument("key", ArgumentTypes.greedyString())
                    .executes(ctx -> {
                        String key = ArgumentTypes.getString(ctx, "key");
                        ModConfig.CLIENT.apiKey.set(key);
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("\u007a[AideAI] API Key 已设罞！"), true);
                        return 1;
                    })
            )
            .then(Commands.literal("seturl")
                .then(Commands.argument("url", ArgumentTypes.greedyString())
                    .executes(ctx -> {
                        String url = ArgumentTypes.getString(ctx, "url");
                        ModConfig.CLIENT.apiUrl.set(url);
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("\u007a[AideAI] API 地址" + url), true);
                        return 1;
                    })
            )
            .then(Commands.literal("toggle")
                .executes(ctx -> {
                    boolean current = ModConfig.CLIENT.autoChatEnabled.get();
                    ModConfig.CLIENT.autoChatEnabled.set(!current);
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("\u007a[AideAI] 自劣语设已" + (!current ? "启用" : "cye�<�")), true);
                    return 1;
                })
            )
            .then(Commands.literal("help")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "\u0076=== AideAI 帮助 ===\n" +
                        "\u007e/aideai setkey <key> \u0077- 设置 API Key\n" +
                        "\u00e7/aideai seturl <url> \u0077- 设罞 API 地址" +
                        "\u00e7/aideai toggle \u0077- 开关自劣语设导卡\n" +
                        "\u00e7㈋H 门 色开加语设甩吗！"
                    ), false);
                    return 1;
                })
            )
            .executes(ctx -> {
                ctx.getSource().sendSuccess(
                    () -> Component.literal("\u000e[AideAI] 返回： /aideai help"), true);
                return 1;
            })
        );
    }
}
