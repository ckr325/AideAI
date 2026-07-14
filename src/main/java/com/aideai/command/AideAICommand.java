package com.aideai.command;

import com.aideai.config.ModConfig;
import com.aideai.network.AIApiClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class AideAICommand {
    
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(Commands.literal("aideai")
            .then(Commands.literal("chat")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String message = StringArgumentType.getString(ctx, "message");
                        CommandSourceStack source = ctx.getSource();
                        
                        new Thread(() -> {
                            String response = AIApiClient.sendMessage("You are AideAI, the player's Minecraft companion and agent. You have a fun, friendly personality and love to chat. You can also execute commands to help the player.\n\nFORMAT: You MUST respond in EXACTLY this format:\nMESSAGE: <your chat reply here>\nCOMMAND: <the Minecraft command to execute, or NONE if just chatting>\n\nRules:\n1. Be a friend — chat naturally, be funny, be supportive, be mischievous\n2. If the player asks for something (diamonds, teleport, weather, mobs, etc.), put the command after COMMAND:\n3. If the player is just chatting (\"hi\", \"bored\", \"what's up\"), put NONE after COMMAND:\n4. Always keep it fun and engaging\n\nExamples:\n   - Player: \"hi\" → MESSAGE: Hey! Ready to cause some trouble? 😈\nCOMMAND: NONE\n   - Player: \"give me diamonds\" → MESSAGE: Say please! Just kidding, here you go!\nCOMMAND: /give @p diamond_block 1\n\nPlayer says: \"" + message + "\". Now respond in the required format:");
                            source.getServer().execute(() -> {
                                // Parse MESSAGE and COMMAND from response
                                String chatMsg = response;
                                String cmd = null;
                                if (response.contains("COMMAND:")) {
                                    int msgEnd = response.indexOf("COMMAND:");
                                    chatMsg = response.substring(0, msgEnd).replace("MESSAGE:", "").trim();
                                    String cmdPart = response.substring(msgEnd + 8).trim();
                                    if (!cmdPart.equalsIgnoreCase("NONE") && !cmdPart.isEmpty()) {
                                        cmd = cmdPart;
                                    }
                                }
                                source.sendSuccess(() -> Component.literal("[AideAI] " + chatMsg), false);
                                if (cmd != null && source.getEntity() instanceof ServerPlayer) {
                                    ServerPlayer player = (ServerPlayer) source.getEntity();
                                    player.getServer().getCommands().performPrefixedCommand(
                                        player.createCommandSourceStack(), cmd);
                                }
                            });
                        }).start();
                        
                        return 1;
                    })
                )
            )
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
            .then(Commands.literal("summon")
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    if (source.getEntity() instanceof ServerPlayer) {
                        ServerPlayer player = (ServerPlayer) source.getEntity();
                        com.aideai.AideAI.AIDEAI_ENTITY.get().spawn(
                            player.serverLevel(),
                            player.blockPosition(),
                            net.minecraft.world.entity.MobSpawnType.COMMAND);
                        source.sendSuccess(() -> Component.literal("[AideAI] Summoned!"), true);
                    }
                    return 1;
                })
            )
            .then(Commands.literal("help")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "=== AideAI Help ===\n" +
                        "/aideai chat <msg>   - Chat with AI\n" +
                        "/aideai setkey <key>  - Set API Key\n" +
                        "/aideai seturl <url>  - Set API URL\n" +
                        "/aideai toggle        - Toggle auto chat\n" +
                        "/aideai summon        - Summon AideAI NPC\n" +
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
