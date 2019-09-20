package io.github.cottonmc.jankson;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.container.ContainerType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleType;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatType;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSourceType;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

public class JanksonFactory {
	public static Jankson.Builder builder() {
		Jankson.Builder builder = Jankson.builder();
		
		builder
			.registerDeserializer(String.class, ItemStack.class, BlockAndItemSerializers::getItemStackPrimitive)
			.registerDeserializer(JsonObject.class, ItemStack.class, BlockAndItemSerializers::getItemStack)
			.registerSerializer(ItemStack.class, BlockAndItemSerializers::saveItemStack);
		
		builder
			.registerDeserializer(String.class, Block.class, BlockAndItemSerializers::getBlockPrimitive)
			.registerSerializer(Block.class, BlockAndItemSerializers::saveBlock);
				
		builder
			.registerDeserializer(String.class, BlockState.class, BlockAndItemSerializers::getBlockStatePrimitive)
			.registerDeserializer(JsonObject.class, BlockState.class, BlockAndItemSerializers::getBlockState)
			.registerSerializer(BlockState.class, BlockAndItemSerializers::saveBlockState);
		
		//All the things you could potentially specify with just a registry ID
		builder
			.registerDeserializer(String.class, Activity.class,           (s,m)->Registry.ACTIVITY              .get(new Identifier(s)))
			.registerDeserializer(String.class, Biome.class,              (s,m)->Registry.BIOME                 .get(new Identifier(s)))
			.registerDeserializer(String.class, BiomeSourceType.class,    (s,m)->Registry.BIOME_SOURCE_TYPE     .get(new Identifier(s)))
			.registerDeserializer(String.class, BlockEntityType.class,    (s,m)->Registry.BLOCK_ENTITY          .get(new Identifier(s)))
			.registerDeserializer(String.class, Carver.class,             (s,m)->Registry.CARVER                .get(new Identifier(s)))
			.registerDeserializer(String.class, ChunkGeneratorType.class, (s,m)->Registry.CHUNK_GENERATOR_TYPE  .get(new Identifier(s)))
			.registerDeserializer(String.class, ChunkStatus.class,        (s,m)->Registry.CHUNK_STATUS          .get(new Identifier(s)))
			.registerDeserializer(String.class, ContainerType.class,      (s,m)->Registry.CONTAINER             .get(new Identifier(s)))
			.registerDeserializer(String.class, Decorator.class,          (s,m)->Registry.DECORATOR             .get(new Identifier(s)))
			.registerDeserializer(String.class, DimensionType.class,      (s,m)->Registry.DIMENSION             .get(new Identifier(s)))
			.registerDeserializer(String.class, Enchantment.class,        (s,m)->Registry.ENCHANTMENT           .get(new Identifier(s)))
			.registerDeserializer(String.class, EntityType.class,         (s,m)->Registry.ENTITY_TYPE           .get(new Identifier(s)))
			.registerDeserializer(String.class, Feature.class,            (s,m)->Registry.FEATURE               .get(new Identifier(s)))
			.registerDeserializer(String.class, Fluid.class,              (s,m)->Registry.FLUID                 .get(new Identifier(s)))
			.registerDeserializer(String.class, Item.class,               (s,m)->Registry.ITEM                  .get(new Identifier(s))) //TODO: Support tags?
			.registerDeserializer(String.class, MemoryModuleType.class,   (s,m)->Registry.MEMORY_MODULE_TYPE    .get(new Identifier(s)))
			.registerDeserializer(String.class, PaintingMotive.class,     (s,m)->Registry.MOTIVE                .get(new Identifier(s))) //MOTIF. It's spelled "MOTIF".
			.registerDeserializer(String.class, ParticleType.class,       (s,m)->Registry.PARTICLE_TYPE         .get(new Identifier(s)))
			.registerDeserializer(String.class, PointOfInterestType.class,(s,m)->Registry.POINT_OF_INTEREST_TYPE.get(new Identifier(s)))
			.registerDeserializer(String.class, Potion.class,             (s,m)->Registry.POTION                .get(new Identifier(s)))
			.registerDeserializer(String.class, RecipeSerializer.class,   (s,m)->Registry.RECIPE_SERIALIZER     .get(new Identifier(s)))
			.registerDeserializer(String.class, RecipeType.class,         (s,m)->Registry.RECIPE_TYPE           .get(new Identifier(s)))
			.registerDeserializer(String.class, Registry.class,           (s,m)->Registry.REGISTRIES            .get(new Identifier(s))) //You know you want to do it
			.registerDeserializer(String.class, RuleTest.class,           (s,m)->Registry.RULE_TEST             .get(new Identifier(s)))
			.registerDeserializer(String.class, Schedule.class,           (s,m)->Registry.SCHEDULE              .get(new Identifier(s)))
			.registerDeserializer(String.class, SensorType.class,         (s,m)->Registry.SENSOR_TYPE           .get(new Identifier(s)))
			.registerDeserializer(String.class, SoundEvent.class,         (s,m)->Registry.SOUND_EVENT           .get(new Identifier(s)))
			.registerDeserializer(String.class, StatType.class,           (s,m)->Registry.STAT_TYPE             .get(new Identifier(s)))
			.registerDeserializer(String.class, StatusEffect.class,       (s,m)->Registry.STATUS_EFFECT         .get(new Identifier(s)))
			.registerDeserializer(String.class, StructureFeature.class,        (s,m)->Registry.STRUCTURE_FEATURE     .get(new Identifier(s)))
			.registerDeserializer(String.class, StructurePieceType.class,      (s,m)->Registry.STRUCTURE_PIECE       .get(new Identifier(s)))
			.registerDeserializer(String.class, StructurePoolElementType.class,(s,m)->Registry.STRUCTURE_POOL_ELEMENT.get(new Identifier(s)))
			.registerDeserializer(String.class, StructureProcessorType.class,  (s,m)->Registry.STRUCTURE_PROCESSOR   .get(new Identifier(s)))
			.registerDeserializer(String.class, SurfaceBuilder.class,          (s,m)->Registry.SURFACE_BUILDER       .get(new Identifier(s)))
			.registerDeserializer(String.class, VillagerProfession.class,      (s,m)->Registry.VILLAGER_PROFESSION   .get(new Identifier(s)))
			.registerDeserializer(String.class, VillagerType.class,            (s,m)->Registry.VILLAGER_TYPE         .get(new Identifier(s)))
			;
		
		
		
		return builder;
	}
	
	public static Jankson createJankson() {
		return builder().build();
	}
	
}
