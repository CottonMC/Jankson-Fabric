package io.github.cottonmc.jankson;

import java.util.Optional;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.Marshaller;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockAndItemSerializers {
	public static ItemStack getItemStack(JsonObject json, Marshaller m) {
		return ItemStack.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, System.err::println);
	}

	public static ItemStack getItemStackPrimitive(String s, Marshaller m) {
		return new ItemStack(Registry.ITEM.get(new Identifier(s)));
	}

	public static JsonElement saveItemStack(ItemStack stack, Marshaller m) {
		return ItemStack.CODEC.encode(stack, JanksonOps.INSTANCE, JanksonOps.INSTANCE.empty()).getOrThrow(false, System.err::println);
	}
	
	public static Block getBlockPrimitive(String blockIdString, Marshaller m) {
		Optional<Block> blockOpt = Registry.BLOCK.getOrEmpty(new Identifier(blockIdString));
		return blockOpt.orElse(null);
	}
	
	public static JsonElement saveBlock(Block block, Marshaller m) {
		return new JsonPrimitive(Registry.BLOCK.getId(block).toString());
	}

	public static BlockState getBlockStatePrimitive(String blockIdString, Marshaller m) {
		return Registry.BLOCK.getOrEmpty(new Identifier(blockIdString)).map(Block::getDefaultState).orElse(null);
	}
	
	/**
	 * @param json A json object representing a BlockState
	 * @return the BlockState represented, or null if the object does not represent a valid BlockState.
	 */
	public static BlockState getBlockState(JsonObject json, Marshaller m) {
		return BlockState.CODEC.parse(JanksonOps.INSTANCE, json).getOrThrow(false, System.err::println);
	}
	
	public static JsonElement saveBlockState(BlockState state, Marshaller m) {
		return BlockState.CODEC.encode(state, JanksonOps.INSTANCE, JanksonOps.INSTANCE.empty()).getOrThrow(false, System.err::println);
	}
}
