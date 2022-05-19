package io.github.cottonmc.jankson;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.score.LootScoreProviderType;
import net.minecraft.network.MessageType;
import net.minecraft.particle.ParticleType;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatType;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.structure.rule.PosRuleTestType;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.floatprovider.FloatProviderType;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler.NoiseParameters;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSourceType;
import net.minecraft.world.gen.FlatLevelGeneratorPreset;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.size.FeatureSizeType;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import net.minecraft.world.gen.heightprovider.HeightProviderType;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;
import net.minecraft.world.gen.structure.StructureType;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;
import net.minecraft.world.gen.trunk.TrunkPlacerType;
import net.minecraft.world.poi.PointOfInterestType;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;

public class JanksonFactory {
	public static Jankson.Builder builder() {
		Jankson.Builder builder = Jankson.builder();
		
		builder
			.registerDeserializer(String.class, ItemStack.class, BlockAndItemSerializers::getItemStackPrimitive)
			.registerDeserializer(JsonObject.class, ItemStack.class, BlockAndItemSerializers::getItemStack)
			.registerSerializer(ItemStack.class, BlockAndItemSerializers::saveItemStack);
				
		builder
			.registerDeserializer(String.class, BlockState.class, BlockAndItemSerializers::getBlockStatePrimitive)
			.registerDeserializer(JsonObject.class, BlockState.class, BlockAndItemSerializers::getBlockState)
			.registerSerializer(BlockState.class, BlockAndItemSerializers::saveBlockState);
		
		builder
			.registerDeserializer(String.class, Identifier.class,         (s,m)->new Identifier(s))
			.registerSerializer(Identifier.class, (i,m)->new JsonPrimitive(i.toString()))
			;
		
		//All the things you could potentially specify with just a registry ID
		register(builder, Activity.class,                   Registry.ACTIVITY);
		register(builder, Block.class,                      Registry.BLOCK);
		register(builder, BlockEntityType.class,            Registry.BLOCK_ENTITY_TYPE);
		register(builder, BlockPredicateType.class,         Registry.BLOCK_PREDICATE_TYPE);
		register(builder, BlockStateProviderType.class,     Registry.BLOCK_STATE_PROVIDER_TYPE);
		register(builder, Carver.class,                     Registry.CARVER);
		register(builder, ChunkStatus.class,                Registry.CHUNK_STATUS);
		register(builder, PlacementModifierType.class,      Registry.PLACEMENT_MODIFIER_TYPE);
		register(builder, Enchantment.class,                Registry.ENCHANTMENT);
		register(builder, EntityAttribute.class,            Registry.ATTRIBUTE);
		register(builder, EntityType.class,                 Registry.ENTITY_TYPE);
		register(builder, Feature.class,                    Registry.FEATURE);
		register(builder, FeatureSizeType.class,            Registry.FEATURE_SIZE_TYPE);
		register(builder, FloatProviderType.class,          Registry.FLOAT_PROVIDER_TYPE);
		register(builder, Fluid.class,                      Registry.FLUID);
		register(builder, FoliagePlacerType.class,          Registry.FOLIAGE_PLACER_TYPE);
		register(builder, GameEvent.class,                  Registry.GAME_EVENT);
		register(builder, HeightProviderType.class,         Registry.HEIGHT_PROVIDER_TYPE);
		register(builder, IntProviderType.class,            Registry.INT_PROVIDER_TYPE);
		register(builder, Item.class,                       Registry.ITEM);
		register(builder, LootConditionType.class,          Registry.LOOT_CONDITION_TYPE);
		register(builder, LootFunctionType.class,           Registry.LOOT_FUNCTION_TYPE);
		register(builder, LootNbtProviderType.class,        Registry.LOOT_NBT_PROVIDER_TYPE);
		register(builder, LootNumberProviderType.class,     Registry.LOOT_NUMBER_PROVIDER_TYPE);
		register(builder, LootPoolEntryType.class,          Registry.LOOT_POOL_ENTRY_TYPE);
		register(builder, LootScoreProviderType.class,      Registry.LOOT_SCORE_PROVIDER_TYPE);
		register(builder, MemoryModuleType.class,           Registry.MEMORY_MODULE_TYPE);
		register(builder, PaintingVariant.class,            Registry.PAINTING_VARIANT);
		register(builder, ParticleType.class,               Registry.PARTICLE_TYPE);
		register(builder, PointOfInterestType.class,        Registry.POINT_OF_INTEREST_TYPE);
		register(builder, PositionSourceType.class,         Registry.POSITION_SOURCE_TYPE);
		register(builder, PosRuleTestType.class,            Registry.POS_RULE_TEST);
		register(builder, Potion.class,                     Registry.POTION);
		register(builder, RecipeSerializer.class,           Registry.RECIPE_SERIALIZER);
		register(builder, RecipeType.class,                 Registry.RECIPE_TYPE);
		register(builder, RuleTestType.class,               Registry.RULE_TEST);
		register(builder, Schedule.class,                   Registry.SCHEDULE);
		register(builder, ScreenHandlerType.class,          Registry.SCREEN_HANDLER);
		register(builder, SensorType.class,                 Registry.SENSOR_TYPE);
		register(builder, SoundEvent.class,                 Registry.SOUND_EVENT);
		register(builder, StatType.class,                   Registry.STAT_TYPE);
		register(builder, StatusEffect.class,               Registry.STATUS_EFFECT);
		register(builder, StructurePieceType.class,         Registry.STRUCTURE_PIECE);
		register(builder, StructurePoolElementType.class,   Registry.STRUCTURE_POOL_ELEMENT);
		register(builder, StructureProcessorType.class,     Registry.STRUCTURE_PROCESSOR);
		register(builder, TreeDecoratorType.class,          Registry.TREE_DECORATOR_TYPE);
		register(builder, TrunkPlacerType.class,            Registry.TRUNK_PLACER_TYPE);
		register(builder, VillagerProfession.class,         Registry.VILLAGER_PROFESSION);
		register(builder, VillagerType.class,               Registry.VILLAGER_TYPE);
		register(builder, Registry.class,                   Registry.REGISTRIES);
		
		register(builder, ConfiguredCarver.class,           BuiltinRegistries.CONFIGURED_CARVER);
		register(builder, PlacedFeature.class,       	    BuiltinRegistries.PLACED_FEATURE);
		register(builder, StructureType.class,              BuiltinRegistries.STRUCTURE);
		register(builder, StructureSet.class,          	    BuiltinRegistries.STRUCTURE_SET);
		register(builder, StructureProcessorList.class,     BuiltinRegistries.STRUCTURE_PROCESSOR_LIST);
		register(builder, StructurePool.class,              BuiltinRegistries.STRUCTURE_POOL);
		register(builder, Biome.class,                      BuiltinRegistries.BIOME);
		register(builder, NoiseParameters.class,            BuiltinRegistries.NOISE_PARAMETERS);
		register(builder, DensityFunction.class,            BuiltinRegistries.DENSITY_FUNCTION);
		register(builder, ChunkGeneratorSettings.class,     BuiltinRegistries.CHUNK_GENERATOR_SETTINGS);
		register(builder, WorldPreset.class,                BuiltinRegistries.WORLD_PRESET);
		register(builder, FlatLevelGeneratorPreset.class,   BuiltinRegistries.FLAT_LEVEL_GENERATOR_PRESET);
		register(builder, MessageType.class,                BuiltinRegistries.MESSAGE_TYPE);
		
		return builder;
	}
	
	private static <T> void register(Jankson.Builder builder, Class<T> clazz, Registry<? extends T> registry) {
		builder.registerDeserializer(String.class, clazz, (s,m)->lookupDeserialize(s, registry));
		builder.registerSerializer(clazz, (o,m)->lookupSerialize(o, registry));
	}
	
	private static <T> T lookupDeserialize(String s, Registry<T> registry) {
		return registry.get(new Identifier(s));
	}
	
	private static <T, U extends T> JsonElement lookupSerialize(T t, Registry<U> registry) {
		@SuppressWarnings("unchecked") //Widening cast happening because of generic type parameters in the registry class
		Identifier id = registry.getId((U)t);
		if (id==null) return JsonNull.INSTANCE;
		return new JsonPrimitive(id.toString());
	}
	
	
	public static Jankson createJankson() {
		return builder().build();
	}
	
}
