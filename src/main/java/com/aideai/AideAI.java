package com.aideai;

import com.aideai.config.ModConfig;
import com.aideai.entity.AIEntity;
import com.aideai.entity.ModEntities;
import com.aideai.network.DeepSeekClient;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
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

    // 实体跟踪
    private static AIEntity aiEntityInstance = null;
    private static boolean entitySpawned = false;

    public AideAI(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("AideAI 模组初始化...");
        
        modContainer.registerConfig(Type.COMMON, ModConfig.SPEC, "aideai.toml");
        
        // 注册实体类型
        ModEntities.ENTITIES.register(modEventBus);
        
        // 注册实体属性（通用阶段，非客户端）
        modEventBus.addListener(this::registerEntityAttributes);
        // 注册实体渲染器（IModBusEvent，必须用 modEventBus）
        modEventBus.addListener(this::onRegisterRenderers);
        
        modEventBus.addListener(this::onClientSetup);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, 
            (client, parent) -> new com.aideai.config.ConfigScreen(parent));
        
        NeoForge.EVENT_BUS.register(this);
    }

    // 注册实体属性
    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.AI_ENTITY.get(), AIEntity.createAttributes().build());
    }

    // 注册实体渲染器（IModBusEvent，必须用 modEventBus 注册）
    private void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.AI_ENTITY.get(), 
            com.aideai.entity.client.AIEntityRenderer::new);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("AideAI 客户端设置完成 - 已劫持聊天输入，单人模式AI对话");
        
        entitySpawned = false;
        
        // 启动自动触发检查任务（每10秒检查一次）
        scheduler.scheduleAtFixedRate(AideAI::checkAutoTrigger, 30, 10, TimeUnit.SECONDS);
        // 启动上下文更新任务（每5秒刷新）
        scheduler.scheduleAtFixedRate(AideAI::updateGameContext, 5, 5, TimeUnit.SECONDS);
        // 启动实体生成检查（每2秒检查一次）
        scheduler.scheduleAtFixedRate(AideAI::checkAndSpawnEntity, 10, 2, TimeUnit.SECONDS);
    }

    // ========== 实体生成逻辑 ==========
    
    private static void checkAndSpawnEntity() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        
        if (!entitySpawned && !mc.level.isClientSide) {
            // 只在服务端生成，自动同步到客户端
            spawnAIEntity(mc.player, mc.level);
        }
    }
    
    private static void spawnAIEntity(Player player, Level level) {
        if (level.isClientSide) return;
        
        // 检查实体是否已经存在
        if (aiEntityInstance != null && aiEntityInstance.isAlive()) {
            entitySpawned = true;
            return;
        }
        
        // 在玩家旁边生成实体
        BlockPos spawnPos = player.blockPosition().offset(3, 0, 3);
        AIEntity entity = new AIEntity(ModEntities.AI_ENTITY.get(), level);
        entity.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        level.addFreshEntity(entity);
        aiEntityInstance = entity;
        entitySpawned = true;
        
        LOGGER.info("小染已出现在世界中！");
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
        
        // 附近实体数（统计玩家附近32格内的生物）
        int nearbyEntities = level.getEntities(player, player.getBoundingBox().inflate(32)).size();
        
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
        
        // ====== 转换成有话题感的自然描述 ======
        // 1. 地点描述
        String locationDesc;
        if (biome.contains("ocean") || biome.contains("river")) locationDesc = "一片水域旁";
        else if (biome.contains("plains") || biome.contains("forest") || biome.contains("taiga")) locationDesc = "一片开阔的野外";
        else if (biome.contains("desert")) locationDesc = "干旱的沙漠里";
        else if (biome.contains("mountain") || biome.contains("hills")) locationDesc = "山地上";
        else if (biome.contains("swamp")) locationDesc = "潮湿的沼泽里";
        else if (biome.contains("jungle")) locationDesc = "茂密的丛林里";
        else if (biome.contains("snow") || biome.contains("ice")) locationDesc = "冰雪覆盖的地方";
        else if (biome.contains("cave") || biome.contains("deep")) locationDesc = "幽深的地下洞穴中";
        else if (biome.contains("nether") || biome.contains("hell")) locationDesc = "炽热的下界";
        else if (biome.contains("end")) locationDesc = "虚无的末地";
        else locationDesc = biome.contains(":") ? biome.split(":")[1].replace("_", " ") : biome;
        
        // 2. 主人近况描述
        String statusDesc;
        if (health <= 4) statusDesc = "伤势很重，生命垂危！";
        else if (health <= 8) statusDesc = "受了些伤，需要休息";
        else if (hunger <= 6) statusDesc = "肚子饿了，需要吃点东西";
        else if (health == maxHealth && hunger >= 18) statusDesc = "状态很好，精神饱满";
        else if (isFalling) statusDesc = "正在从高处掉落！";
        else statusDesc = "状态还不错";
        
        // 3. 手持物品的自然描述
        String handDesc;
        if (mainHand.isEmpty()) handDesc = "手里空空的";
        else if (handItem.contains("剑") || handItem.contains("sword")) handDesc = "手里握着" + handItem + "，一副准备战斗的样子";
        else if (handItem.contains("镐") || handItem.contains("pickaxe")) handDesc = "正拿着" + handItem + "，一看就是在挖矿";
        else if (handItem.contains("斧") || handItem.contains("axe")) handDesc = "拎着" + handItem + "，像是在砍树";
        else if (handItem.contains("弓") || handItem.contains("bow")) handDesc = "拿着" + handItem + "，准备远程射击";
        else if (handItem.contains("食物") || handItem.contains("apple") || handItem.contains("bread")) handDesc = "拿着" + handItem + "，正要吃东西";
        else handDesc = "手上拿着" + handItem;
        
        // 4. 附近情况
        String nearbyDesc;
        if (nearbyEntities > 20) nearbyDesc = "周围好热闹，有" + nearbyEntities + "个生物在你附近";
        else if (nearbyEntities > 5) nearbyDesc = "附近有" + nearbyEntities + "个生物在活动";
        else if (nearbyEntities > 0) nearbyDesc = "周围很安静，零星有几个生物";
        else nearbyDesc = "附近空无一人，只有你";
        
        // 5. 时间感
        String timeFeel = dayTime < 6000 ? "清晨" : dayTime < 13000 ? "白天" : dayTime < 18000 ? "黄昏" : "深夜";
        
        cachedGameContext = String.format(
            "【小染的视角】现在是%s，你在%s的%s。你%s，%s，%s。%s。%s%s",
            timeFeel, dimName, locationDesc, handDesc, statusDesc, 
            hunger <= 6 ? "肚子饿得咕咕叫了" : "不饿",
            nearbyDesc, effects.isEmpty() ? "" : "你身上有" + effects + "的效果", 
            fallInfo
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
            String prompt = "(你已经盯着主人的背影看了" + minutes + "分钟了，心里有点不是滋味。他完全没注意到你。你深吸一口气，决定主动叫他一声...)\n\n" + context + "\n\n（小染的内心：开口说句话吧，就像平时那样自然地叫他）";
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
        
        // 发送给AI，自动携带游戏上下文 —— 用自然视角包裹
        String enhancedMessage = context + "\n\n" + message;
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
