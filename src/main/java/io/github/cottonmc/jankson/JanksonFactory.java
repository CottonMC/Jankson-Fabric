package io.github.cottonmc.jankson;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.DeserializationException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.numbers.NumberFormatType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.jsonrpc.IncomingRpcMethod;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.world.attribute.AttributeType;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public final class JanksonFactory {
	private JanksonFactory() {
	}

	public static Jankson.Builder builder() {
		Jankson.Builder builder = Jankson.builder();
				
		builder
			.registerDeserializer(String.class, BlockState.class, BlockAndItemSerializers::getBlockStatePrimitive)
			.registerDeserializer(JsonObject.class, BlockState.class, BlockAndItemSerializers::getBlockState)
			.registerSerializer(BlockState.class, BlockAndItemSerializers::saveBlockState);
		
		builder
			.registerDeserializer(String.class, Identifier.class, (s, m) -> Identifier.parse(s))
			.registerSerializer(Identifier.class, (i, m)->new JsonPrimitive(i.toString()))
			;

		//All the things you could potentially specify with just a registry ID
		//Note: specifically excludes dynamic registries since we can't have static access to them.
		register(builder, Activity.class,                    BuiltInRegistries.ACTIVITY);
		register(builder, ArgumentTypeInfo.class,            BuiltInRegistries.COMMAND_ARGUMENT_TYPE);
		register(builder, Attribute.class,                   BuiltInRegistries.ATTRIBUTE);
		register(builder, AttributeType.class,               BuiltInRegistries.ATTRIBUTE_TYPE);
		register(builder, Block.class,                       BuiltInRegistries.BLOCK);
		register(builder, BlockEntityType.class,             BuiltInRegistries.BLOCK_ENTITY_TYPE);
		register(builder, BlockPredicateType.class,          BuiltInRegistries.BLOCK_PREDICATE_TYPE);
		register(builder, ChunkStatus.class,                 BuiltInRegistries.CHUNK_STATUS);
		register(builder, ConsumeEffect.Type.class,          BuiltInRegistries.CONSUME_EFFECT_TYPE);
		register(builder, ContextKeySet.class,               BuiltInRegistries.CONTEXT_KEY_SET);
		register(builder, CreativeModeTab.class,             BuiltInRegistries.CREATIVE_MODE_TAB);
		register(builder, CriterionTrigger.class,            BuiltInRegistries.TRIGGER_TYPES);
		register(builder, DataComponentPredicate.Type.class, BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE);
		register(builder, DebugSubscription.class,           BuiltInRegistries.DEBUG_SUBSCRIPTION);
		register(builder, EntityType.class,                  BuiltInRegistries.ENTITY_TYPE);
		register(builder, EnvironmentAttribute.class,        BuiltInRegistries.ENVIRONMENT_ATTRIBUTE);
		register(builder, FeatureSizeType.class,             BuiltInRegistries.FEATURE_SIZE_TYPE);
		register(builder, Fluid.class,                       BuiltInRegistries.FLUID);
		register(builder, FoliagePlacerType.class,           BuiltInRegistries.FOLIAGE_PLACER_TYPE);
		register(builder, GameEvent.class,                   BuiltInRegistries.GAME_EVENT);
		register(builder, GameRule.class,                    BuiltInRegistries.GAME_RULE);
		register(builder, HeightProviderType.class,          BuiltInRegistries.HEIGHT_PROVIDER_TYPE);
		register(builder, IncomingRpcMethod.class,           BuiltInRegistries.INCOMING_RPC_METHOD);
		register(builder, Item.class,                        BuiltInRegistries.ITEM);
		register(builder, MapDecorationType.class,           BuiltInRegistries.MAP_DECORATION_TYPE);
		register(builder, MemoryModuleType.class,            BuiltInRegistries.MEMORY_MODULE_TYPE);
		register(builder, MenuType.class,                    BuiltInRegistries.MENU);
		register(builder, MobEffect.class,                   BuiltInRegistries.MOB_EFFECT);
		register(builder, NumberFormatType.class,            BuiltInRegistries.NUMBER_FORMAT_TYPE);
		register(builder, OutgoingRpcMethod.class,           BuiltInRegistries.OUTGOING_RPC_METHOD);
		register(builder, ParticleType.class,                BuiltInRegistries.PARTICLE_TYPE);
		register(builder, PoiType.class,                     BuiltInRegistries.POINT_OF_INTEREST_TYPE);
		register(builder, PositionSourceType.class,          BuiltInRegistries.POSITION_SOURCE_TYPE);
		register(builder, PosRuleTestType.class,             BuiltInRegistries.POS_RULE_TEST);
		register(builder, Potion.class,                      BuiltInRegistries.POTION);
		register(builder, RecipeBookCategory.class,          BuiltInRegistries.RECIPE_BOOK_CATEGORY);
		register(builder, RecipeDisplay.Type.class,          BuiltInRegistries.RECIPE_DISPLAY);
		register(builder, RecipeSerializer.class,            BuiltInRegistries.RECIPE_SERIALIZER);
		register(builder, RecipeType.class,                  BuiltInRegistries.RECIPE_TYPE);
		register(builder, RootPlacerType.class,              BuiltInRegistries.ROOT_PLACER_TYPE);
		register(builder, RuleBlockEntityModifierType.class, BuiltInRegistries.RULE_BLOCK_ENTITY_MODIFIER);
		register(builder, RuleTestType.class,                BuiltInRegistries.RULE_TEST);
		register(builder, SensorType.class,                  BuiltInRegistries.SENSOR_TYPE);
		register(builder, SlotDisplay.Type.class,            BuiltInRegistries.SLOT_DISPLAY);
		register(builder, SoundEvent.class,                  BuiltInRegistries.SOUND_EVENT);
		register(builder, StatType.class,                    BuiltInRegistries.STAT_TYPE);
		register(builder, StructurePieceType.class,          BuiltInRegistries.STRUCTURE_PIECE);
		register(builder, StructurePoolElementType.class,    BuiltInRegistries.STRUCTURE_POOL_ELEMENT);
		register(builder, StructureType.class,               BuiltInRegistries.STRUCTURE_TYPE);
		register(builder, TicketType.class,                  BuiltInRegistries.TICKET_TYPE);
		register(builder, TreeDecoratorType.class,           BuiltInRegistries.TREE_DECORATOR_TYPE);
		register(builder, TrunkPlacerType.class,             BuiltInRegistries.TRUNK_PLACER_TYPE);
		register(builder, VillagerProfession.class,          BuiltInRegistries.VILLAGER_PROFESSION);
		register(builder, VillagerType.class,                BuiltInRegistries.VILLAGER_TYPE);
		register(builder, Registry.class,                           BuiltInRegistries.REGISTRY);
		
		return builder;
	}
	
	private static <T> void register(Jankson.Builder builder, Class<T> clazz, Registry<? extends T> registry) {
		builder.registerDeserializer(String.class, clazz, (s,m)->lookupDeserialize(s, registry));
		builder.registerSerializer(clazz, (o,m)->lookupSerialize(o, registry));
	}
	
	private static <T> T lookupDeserialize(String s, Registry<T> registry) {
		return registry.getValue(Identifier.parse(s));
	}
	
	private static <T, U extends T> JsonElement lookupSerialize(T t, Registry<U> registry) {
		@SuppressWarnings("unchecked") //Widening cast happening because of generic type parameters in the registry class
		Identifier id = registry.getKey((U)t);
		if (id==null) return JsonNull.INSTANCE;
		return new JsonPrimitive(id.toString());
	}
	
	
	public static Jankson createJankson() {
		return builder().build();
	}

	/**
	 * Registers a codec-based serializer and deserializer to a Jankson builder.
	 *
	 * @param builder the builder
	 * @param type    the serialized type
	 * @param codec   the codec
	 * @param <T>     the serialized type
	 * @since 12.0.0
	 */
	public static <T> void registerCodecBasedSerializer(Jankson.Builder builder, Class<T> type, Codec<T> codec) {
		registerCodecBasedSerializer(builder, type, codec, JanksonOps.INSTANCE);
	}

	/**
	 * Registers a codec-based serializer and deserializer to a Jankson builder.
	 * This overload can use a different {@link DynamicOps} instance (e.g. {@link net.minecraft.resources.RegistryOps}).
	 *
	 * @param builder the builder
	 * @param type    the serialized type
	 * @param codec   the codec
	 * @param ops     the {@link DynamicOps}
	 * @param <T>     the serialized type
	 * @since 12.0.0
	 */
	public static <T> void registerCodecBasedSerializer(Jankson.Builder builder, Class<T> type, Codec<T> codec, DynamicOps<JsonElement> ops) {
		builder.registerDeserializer(JsonElement.class, type, (jsonElement, m) -> {
			return codec.parse(ops, jsonElement).getOrThrow(DeserializationException::new);
		});

		builder.registerSerializer(type, (t, marshaller) -> {
			return codec.encodeStart(ops, t).getOrThrow();
		});
	}
}
