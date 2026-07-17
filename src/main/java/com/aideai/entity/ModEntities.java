package com.aideai.entity;

import com.aideai.AideAI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, AideAI.MOD_ID);

    public static final Supplier<EntityType<AIEntity>> AI_ENTITY =
            ENTITIES.register("ai_entity", () ->
                    EntityType.Builder.of(AIEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.95f) // 和玩家一样大小
                            .clientTrackingRange(64)
                            .updateInterval(2)
                            .build("ai_entity")
            );
}
