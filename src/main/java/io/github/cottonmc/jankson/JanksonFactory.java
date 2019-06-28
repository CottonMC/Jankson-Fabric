package io.github.cottonmc.jankson;

import blue.endless.jankson.Jankson;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

public class JanksonFactory {
	public static Jankson.Builder builder() {
		return Jankson.builder()
				.registerTypeAdapter(ItemStack.class, BlockAndItemSerializers::getItemStack)
				.registerPrimitiveTypeAdapter(ItemStack.class, BlockAndItemSerializers::getItemStackPrimitive)
				.registerSerializer(ItemStack.class, (t, marshaller)->{ return BlockAndItemSerializers.saveItemStack(t); })
				
				.registerPrimitiveTypeAdapter(Block.class, BlockAndItemSerializers::getBlockPrimitive)
				.registerSerializer(Block.class, (t, marshaller)->{ return BlockAndItemSerializers.saveBlock(t); })
				
				.registerTypeAdapter(BlockState.class, BlockAndItemSerializers::getBlockState)
				.registerPrimitiveTypeAdapter(BlockState.class, BlockAndItemSerializers::getBlockStatePrimitive)
				.registerSerializer(BlockState.class, (t, marshaller)->{ return BlockAndItemSerializers.saveBlockState(t); })
				;
	}
	
	public static Jankson createJankson() {
		return builder().build();
	}
	
}
