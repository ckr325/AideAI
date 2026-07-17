package com.aideai;

import com.aideai.config.ModConfig;
import com.aideai.network.DeepSeekClient;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import org.slf4j.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Mod(AideAI.MOD_ID)
public class AideAI {
    public static final String MOD_ID = "aideai";
    private static final Logger LOGGER = LogUtils.getLogger();
    
    // 自动触发相关
    private static long lastChatTime = System.currentTimeMillis();
    private static boolean autoTriggerEnabled = false;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // 游戏上下文缓存（每5秒更新一次）
    private static String cachedGameContext = "";
    private static long lastContextUpdate = 0;

    public AideAI(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("AideAI 模组初始化...");
        
        modContainer.registerConfig(Type.COMMON, ModConfig.SPEC, "aideai.toml");
        
        modEventBus.addListener(this::onClientSetup);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, 
            (client, parent) -> new com.aideai.config.ConfigScreen(parent));
        
        NeoForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("AideAI 客户端设置完成 - 已劫持聊天输入，单人模式AI对话");
        // 启动自动触发检查任务（每10秒检查一次）
        scheduler.scheduleAtFixedRate(AideAI::checkAutoTrigger, 30, 10, TimeUnit.SECONDS);
        // 启动上下文更新任务（每5秒刷新）
        scheduler.scheduleAtFixedRate(AideAI::updateGameContext, 5, 5, TimeUnit.SECONDS);
    }

    // ========== 游戏上下文获取 ==========
    
    private static void updateGameContext() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        
        Player player = mc.player;
        Level level = mc.level;
        BlockPos pos = player.blockPosition();
        
        // 维度名称
        String dimension = level.dimension().location().toString();
        String dimName = switch (dimension) {
            case "minecraft:overworld" -> "主世界";
            case "minecraft:the_nether" -> "下界";
            case "minecraft:the_end" -> "末地";
            default -> dimension;
        };
        
        // 时间
        long dayTime = level.getDayTime() % 24000;
        String timeStr = dayTime < 13000 ? "白天" : "夜晚";
        
        // 生物群系
        String biome = level.getBiome(pos).getRegisteredName();
        
        // 玩家状态
        int health = (int) player.getHealth();
        int maxHealth = (int) player.getMaxHealth();
        int hunger = player.getFoodData().getFoodLevel();
        int xp = player.totalExperience;
        
        // 手持物品
        ItemStack mainHand = player.getMainHandItem();
        String handItem = mainHand.isEmpty() ? "空手" : mainHand.getHoverName().getString();
        
        // 背包物品数量（简化：统计工具、武器、食物等）
        int toolCount = 0;
        int foodCount = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty()) {
                String itemId = stack.getItem().getDescriptionId();
                if (itemId.contains("pickaxe") || itemId.contains("axe") || 
                    itemId.contains("shovel") || itemId.contains("sword") ||
                    itemId.contains("hoe")) toolCount++;
                if (itemId.contains("food") || itemId.contains("beef") || 
                    itemId.contains("pork") || itemId.contains("bread") ||
                    itemId.contains("apple") || itemId.contains("carrot")) foodCount++;
            }
        }
        
        // 附近实体数（粗略统计玩家附近生物）
        int nearbyEntities = level.getEntities().size();
        
        // 状态效果
        String effects = "";
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION)) effects += "夜视 ";
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY)) effects += "隐身 ";
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) effects += "中毒 ";
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)) effects += "再生 ";
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.JUMP)) effects += "跳跃提升 ";
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED)) effects += "速度 ";
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED)) effects += "急迫 ";
        if (!effects.isEmpty()) effects = "，状态:" + effects.trim();
        
        // 是否在空中（掉落）
        boolean isFalling = !player.onGround() && player.fallDistance > 0.5;
        String fallInfo = isFalling ? "，正在掉落中！" : "";
        
        cachedGameContext = String.format(
            "【当前状态】你正在%s的%s，坐标(%d,%d,%d)，%s%s。血量%d/%d，饥饿值%d，经验值%d。手持%s，背包中有%d件工具/武器，%d件食物%s%s",
            dimName, biome, pos.getX(), pos.getY(), pos.getZ(), timeStr, fallInfo,
            health, maxHealth, hunger, xp, handItem, toolCount, foodCount, effects, 
            nearbyEntities > 10 ? "，附近有" + nearbyEntities + "个实体" : ""
        );
        
        lastContextUpdate = System.currentTimeMillis();
    }
    
    private static String getGameContext() {
        // 如果上下文过期，强制刷新
        if (System.currentTimeMillis() - lastContextUpdate > 6000) {
            updateGameContext();
        }
        return cachedGameContext;
    }

    // ========== 自动触发逻辑 ==========
    
    public static void resetTimer() {
        lastChatTime = System.currentTimeMillis();
    }
    
    private static void checkAutoTrigger() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        
        long elapsed = System.currentTimeMillis() - lastChatTime;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
        
        double probability = getTriggerProbability(minutes);
        
        if (probability > 0 && Math.random() < probability) {
            String context = getGameContext();
            String prompt = "[系统提示：你已沉默" + minutes + "分钟了。当前游戏情况：" + context + "。作为病娇AI女友，根据当前游戏情况主动找主人说句话。注意：不要使用[指令]格式，只需要说一句符合你病娇人设的话]";
            DeepSeekClient.sendMessage(prompt).thenAccept(reply -> {
                if (reply != null && mc.player != null) {
                    String cleanReply = reply.replaceAll("\\[指令\\].*?(\\n|$)", "").trim();
                    mc.player.displayClientMessage(
                        Component.literal("§d❤ " + cleanReply), false);
                }
            });
            // 触发后重置计时器
            lastChatTime = System.currentTimeMillis();
        }
    }
    
    private static double getTriggerProbability(long minutes) {
        if (minutes >= 5) return 1.0;      // 5分钟 → 100%
        if (minutes >= 4) return 0.5;      // 4分钟 → 50%
        if (minutes >= 3) return 0.4;      // 3分钟 → 40%
        if (minutes >= 2) return 0.2;      // 2分钟 → 20%
        return 0.0;                         // 小于2分钟不触发
    }

    // ========== 聊天事件 ==========

    @SubscribeEvent
    public void onChatMessage(ClientChatEvent event) {
        String message = event.getMessage();
        
        // 取消原版发送
        event.setCanceled(true);
        
        // 重置自动触发计时器（用户主动说话了）
        resetTimer();
        
        // 获取当前游戏上下文
        String context = getGameContext();
        
        // 在聊天栏显示用户消息
        Minecraft.getInstance().player.displayClientMessage(
            Component.literal("§e你: " + message), false);
        
        // 发送给AI，自动携带游戏上下文
        String enhancedMessage = context + "\n" + message;
        DeepSeekClient.sendMessage(enhancedMessage).thenAccept(reply -> {
            if (reply != null) {
                // 执行AI回复中的指令
                executeCommands(reply);
                // 移除指令部分显示纯对话
                String displayReply = reply.replaceAll("\\[指令\\].*?(\\n|$)", "").trim();
                if (displayReply.isEmpty()) {
                    displayReply = "§7指令已执行";
                }
                Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("§bAI: " + displayReply), false);
            } else {
                Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("§cAI回复失败，请检查配置或网络"), false);
            }
        });
    }
    
    private void executeCommands(String reply) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[指令\\]\\s*(/[^\\n]*)");
        java.util.regex.Matcher matcher = pattern.matcher(reply);
        
        while (matcher.find()) {
            String command = matcher.group(1).trim();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().player.connection.sendCommand(
                        command.startsWith("/") ? command.substring(1) : command
                    );
                });
                Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("§d[AideAI] 自动执行指令: §f" + command), false);
            }
        }
    }
}
