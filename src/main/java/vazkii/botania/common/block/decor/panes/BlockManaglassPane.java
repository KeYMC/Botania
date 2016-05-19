/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Sep 30, 2015, 10:16:46 PM (GMT)]
 */
package vazkii.botania.common.block.decor.panes;

import net.minecraft.util.BlockRenderLayer;
import vazkii.botania.common.block.ModBlocks;

import javax.annotation.Nonnull;

public class BlockManaglassPane extends BlockModPane {

	public BlockManaglassPane() {
		super(ModBlocks.manaGlass);
	}

	@Nonnull
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

}
