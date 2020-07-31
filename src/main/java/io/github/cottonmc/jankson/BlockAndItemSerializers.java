package io.github.cottonmc.jankson;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.Marshaller;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

public class BlockAndItemSerializers {

	public static ItemStack getItemStack(JsonObject json, Marshaller m) {
		String itemIdString = json.get(String.class, "item");
		Item item = Registry.ITEM.getOrEmpty(new Identifier(Objects.requireNonNull(itemIdString)))
				.orElse(Items.AIR);
		ItemStack stack = new ItemStack(item);
		if (json.containsKey("count")) {
			Integer count = json.get(Integer.class, "count");
			if (count!=null) {
				stack.setCount(count);
			}
		}
		return stack;
	}

	public static ItemStack getItemStackPrimitive(String s, Marshaller m) {
		Item item = Registry.ITEM.getOrEmpty(new Identifier(s)).orElse(Items.AIR);
		return new ItemStack(item);
	}

	public static JsonElement saveItemStack(ItemStack stack, Marshaller m) {
		JsonPrimitive id = new JsonPrimitive(Registry.ITEM.getId(stack.getItem()).toString());
		if (stack.getCount()==1) return id;
	
		JsonObject result = new JsonObject();
		result.put("item", new JsonPrimitive(Registry.ITEM.getId(stack.getItem()).toString()));
		result.put("count", new JsonPrimitive(stack.getCount()));
		return result;
	}
	
	public static Block getBlockPrimitive(String blockIdString, Marshaller m) {
		Optional<Block> blockOpt = Registry.BLOCK.getOrEmpty(new Identifier(blockIdString));
		return blockOpt.orElse(null);
	}
	
	public static JsonElement saveBlock(Block block, Marshaller m) {
		return new JsonPrimitive(Registry.BLOCK.getId(block).toString());
	}
	
	
	public static BlockState getBlockStatePrimitive(String blockIdString, Marshaller m) {
		Optional<Block> blockOpt = Registry.BLOCK.getOrEmpty(new Identifier(blockIdString));
		return blockOpt.map(Block::getDefaultState).orElse(null);
	}
	
	/**
	 * @param json A json object representing a BlockState
	 * @return the BlockState represented, or null if the object does not represent a valid BlockState.
	 */
	public static BlockState getBlockState(JsonObject json, Marshaller m) {
		String blockIdString = json.get(String.class, "block");
		
		Block block = Registry.BLOCK.getOrEmpty(new Identifier(Objects.requireNonNull(blockIdString)))
				.orElse(null);
		if (block==null) return null;
		
		BlockState state = block.getDefaultState();
		JsonObject stateObject = json.getObject("BlockStateTag");
		if (stateObject==null) stateObject = json;
		
		Collection<Property<?>> properties = state.getProperties();
		for(String key : stateObject.keySet()) {
			if (stateObject==json && (key.equals("BlockStateTag") || key.equals("block"))) continue;
			
			for(Property<?> property : properties) {
				if (property.getName().equals(key)) {
					String val = stateObject.get(String.class, key);
					state = withProperty(state, property, val);
					break;
				}
			}
		}
		
		return state;
	}
	
	public static JsonElement saveBlockState(BlockState state, Marshaller m) {
		BlockState defaultState = state.getBlock().getDefaultState();
		
		if (state.equals(defaultState)) {
			//Use a String for the blockID only
			return new JsonPrimitive( Registry.BLOCK.getId(state.getBlock()).toString() );
			
		} else {
			JsonObject result = new JsonObject();
			result.put("block", new JsonPrimitive( Registry.BLOCK.getId(state.getBlock()).toString() ));
			JsonObject stateObject = result;
			for(Property<?> property : state.getProperties()) {
				String key = property.getName();
				if (key.equals("block") || key.equals("BlockStateTag")) { //Certain dangerous keys mean we should partition off blockstate properties
					stateObject = new JsonObject();
					result.put("BlockStateTag", stateObject);
					break;
				}
			}
			
			for(Property<?> property : state.getProperties()) {
				if (state.get(property).equals(defaultState.get(property))) continue;
				String key = property.getName();
				String val = getProperty(state, property);
				stateObject.put(key, new JsonPrimitive(val));
			}
			
			return result;
		}
	}
	
	public static <T extends Comparable<T>> BlockState withProperty(BlockState state, Property<T> property, String stringValue) {
		Optional<T> val = property.parse(stringValue);
		if (val.isPresent()) {
			return state.with(property, val.get());
		} else {
			return state;
		}
	}
	
	public static <T extends Comparable<T>> String getProperty(BlockState state, Property<T> property) {
		return property.getName();
	}
	
	public static Biome getBiome(String s, Marshaller m) {
		return Registry.BIOME.get(new Identifier(s));
	}
}
