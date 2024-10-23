package io.github.cottonmc.jankson;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.score.LootScoreProviderType;
import net.minecraft.particle.ParticleType;
import net.minecraft.potion.Potion;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.scoreboard.number.NumberFormatType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatType;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.structure.rule.PosRuleTestType;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.structure.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.floatprovider.FloatProviderType;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSourceType;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.size.FeatureSizeType;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import net.minecraft.world.gen.heightprovider.HeightProviderType;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;
import net.minecraft.world.gen.root.RootPlacerType;
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
			.registerDeserializer(String.class, Identifier.class, (s, m) -> Identifier.of(s))
			.registerSerializer(Identifier.class, (i,m)->new JsonPrimitive(i.toString()))
			;
		
		//All the things you could potentially specify with just a registry ID
		//Note: specifically excludes dynamic registries since we can't have static access to them.
		register(builder, Activity.class,                    Registries.ACTIVITY);
		register(builder, ArgumentSerializer.class,          Registries.COMMAND_ARGUMENT_TYPE);
		register(builder, Block.class,                       Registries.BLOCK);
		register(builder, BlockEntityType.class,             Registries.BLOCK_ENTITY_TYPE);
		register(builder, BlockPredicateType.class,          Registries.BLOCK_PREDICATE_TYPE);
		register(builder, BlockStateProviderType.class,      Registries.BLOCK_STATE_PROVIDER_TYPE);
		register(builder, Carver.class,                      Registries.CARVER);
		register(builder, CatVariant.class,                  Registries.CAT_VARIANT);
		register(builder, ChunkStatus.class,                 Registries.CHUNK_STATUS);
		register(builder, ConsumeEffect.Type.class,          Registries.CONSUME_EFFECT_TYPE);
		register(builder, Criterion.class,                   Registries.CRITERION);
		register(builder, EntityAttribute.class,             Registries.ATTRIBUTE);
		register(builder, EntityType.class,                  Registries.ENTITY_TYPE);
		register(builder, Feature.class,                     Registries.FEATURE);
		register(builder, FeatureSizeType.class,             Registries.FEATURE_SIZE_TYPE);
		register(builder, FloatProviderType.class,           Registries.FLOAT_PROVIDER_TYPE);
		register(builder, Fluid.class,                       Registries.FLUID);
		register(builder, FoliagePlacerType.class,           Registries.FOLIAGE_PLACER_TYPE);
		register(builder, FrogVariant.class,                 Registries.FROG_VARIANT);
		register(builder, GameEvent.class,                   Registries.GAME_EVENT);
		register(builder, HeightProviderType.class,          Registries.HEIGHT_PROVIDER_TYPE);
		register(builder, IntProviderType.class,             Registries.INT_PROVIDER_TYPE);
		register(builder, Item.class,                        Registries.ITEM);
		register(builder, ItemGroup.class,                   Registries.ITEM_GROUP);
		register(builder, ItemSubPredicate.Type.class,       Registries.ITEM_SUB_PREDICATE_TYPE);
		register(builder, LootConditionType.class,           Registries.LOOT_CONDITION_TYPE);
		register(builder, LootFunctionType.class,            Registries.LOOT_FUNCTION_TYPE);
		register(builder, LootNbtProviderType.class,         Registries.LOOT_NBT_PROVIDER_TYPE);
		register(builder, LootNumberProviderType.class,      Registries.LOOT_NUMBER_PROVIDER_TYPE);
		register(builder, LootPoolEntryType.class,           Registries.LOOT_POOL_ENTRY_TYPE);
		register(builder, LootScoreProviderType.class,       Registries.LOOT_SCORE_PROVIDER_TYPE);
		register(builder, MapDecorationType.class,           Registries.MAP_DECORATION_TYPE);
		register(builder, MemoryModuleType.class,            Registries.MEMORY_MODULE_TYPE);
		register(builder, NumberFormatType.class,            Registries.NUMBER_FORMAT_TYPE);
		register(builder, ParticleType.class,                Registries.PARTICLE_TYPE);
		register(builder, PlacementModifierType.class,       Registries.PLACEMENT_MODIFIER_TYPE);
		register(builder, PointOfInterestType.class,         Registries.POINT_OF_INTEREST_TYPE);
		register(builder, PositionSourceType.class,          Registries.POSITION_SOURCE_TYPE);
		register(builder, PosRuleTestType.class,             Registries.POS_RULE_TEST);
		register(builder, Potion.class,                      Registries.POTION);
		register(builder, RecipeBookCategory.class,          Registries.RECIPE_BOOK_CATEGORY);
		register(builder, RecipeDisplay.Serializer.class,    Registries.RECIPE_DISPLAY);
		register(builder, RecipeSerializer.class,            Registries.RECIPE_SERIALIZER);
		register(builder, RecipeType.class,                  Registries.RECIPE_TYPE);
		register(builder, RootPlacerType.class,              Registries.ROOT_PLACER_TYPE);
		register(builder, RuleBlockEntityModifierType.class, Registries.RULE_BLOCK_ENTITY_MODIFIER);
		register(builder, RuleTestType.class,                Registries.RULE_TEST);
		register(builder, Schedule.class,                    Registries.SCHEDULE);
		register(builder, ScreenHandlerType.class,           Registries.SCREEN_HANDLER);
		register(builder, SensorType.class,                  Registries.SENSOR_TYPE);
		register(builder, SlotDisplay.Serializer.class,      Registries.SLOT_DISPLAY);
		register(builder, SoundEvent.class,                  Registries.SOUND_EVENT);
		register(builder, StatType.class,                    Registries.STAT_TYPE);
		register(builder, StatusEffect.class,                Registries.STATUS_EFFECT);
		register(builder, StructurePlacementType.class,      Registries.STRUCTURE_PLACEMENT);
		register(builder, StructurePieceType.class,          Registries.STRUCTURE_PIECE);
		register(builder, StructurePoolElementType.class,    Registries.STRUCTURE_POOL_ELEMENT);
		register(builder, StructureProcessorType.class,      Registries.STRUCTURE_PROCESSOR);
		register(builder, StructureType.class,               Registries.STRUCTURE_TYPE);
		register(builder, TreeDecoratorType.class,           Registries.TREE_DECORATOR_TYPE);
		register(builder, TrunkPlacerType.class,             Registries.TRUNK_PLACER_TYPE);
		register(builder, VillagerProfession.class,          Registries.VILLAGER_PROFESSION);
		register(builder, VillagerType.class,                Registries.VILLAGER_TYPE);
		register(builder, Registry.class,                    Registries.REGISTRIES);
		
		return builder;
	}
	
	private static <T> void register(Jankson.Builder builder, Class<T> clazz, Registry<? extends T> registry) {
		builder.registerDeserializer(String.class, clazz, (s,m)->lookupDeserialize(s, registry));
		builder.registerSerializer(clazz, (o,m)->lookupSerialize(o, registry));
	}
	
	private static <T> T lookupDeserialize(String s, Registry<T> registry) {
		return registry.get(Identifier.of(s));
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
