package com.aideai.command;

import com.aideai.config.ModConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class AideAICommand {
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(Commands.literal("aideai")
            .then(Commands.literal("setkey")
                .then(Commands.argument("key", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String key = StringArgumentType.getString(ctx, "key");
                        ModConfig.CLIENT.apiKey.set(key);
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("[AideAI] API Key set!"), true);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("seturl")
                .then(Commands.argument("url", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String url = StringArgumentType.getString(ctx, "url");
                        ModConfig.CLIENT.apiUrl.set(url);
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("[AideAI] API URL: " + url), true);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("toggle")
                .executes(ctx -> {
                    boolean current = ModConfig.CLIENT.autoChatEnabled.get();
                    ModConfig.CLIENT.autoChatEnabled.set(!current);
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("[AideAI] Auto chat " + (!current ? "enabled" : "disabled")), true);
                    return 1;
                })
            )
            .then(Commands.literal("help")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "=== AideAI Help ===\n" +
                        "/aideai setkey <key>  - Set API Key\n" +
                        "/aideai seturl <url>  - Set API URL\n" +
                        "/aideai toggle        - Toggle auto chat\n" +
                        "/aideai help          - Show this help"
                    ), false);
                    return 1;
                })
            )
            .executes(ctx -> {
                ctx.getSource().sendSuccess(
                    () -> Component.literal("[AideAI] Type /aideai help"), true);
                return 1;
            })
        );
    }
}
