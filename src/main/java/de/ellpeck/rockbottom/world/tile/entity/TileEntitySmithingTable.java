package de.ellpeck.rockbottom.world.tile.entity;

import de.ellpeck.rockbottom.api.GameContent;
import de.ellpeck.rockbottom.api.construction.ConstructionTool;
import de.ellpeck.rockbottom.api.data.set.DataSet;
import de.ellpeck.rockbottom.api.inventory.CombinedInventory;
import de.ellpeck.rockbottom.api.item.Item;
import de.ellpeck.rockbottom.api.item.ItemInstance;
import de.ellpeck.rockbottom.api.tile.entity.ICraftingStation;
import de.ellpeck.rockbottom.api.tile.entity.IFilteredInventory;
import de.ellpeck.rockbottom.api.tile.entity.TileEntity;
import de.ellpeck.rockbottom.api.tile.entity.TileInventory;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;

public class TileEntitySmithingTable extends TileEntity implements ICraftingStation {

	private final TileInventory hammerSlot = new TileInventory(this, inst -> inst != null && inst.getItem() == GameContent.ITEM_HAMMER);
	private final CombinedInventory inventory = new CombinedInventory(this.hammerSlot);

	public TileEntitySmithingTable(IWorld world, int x, int y, TileLayer layer) {
		super(world, x, y, layer);
	}

	@Override
	public IFilteredInventory getTileInventory() {
		return inventory;
	}

	@Override
	public ItemInstance getTool(Item tool) {
		if (tool == GameContent.ITEM_HAMMER) {
			return hammerSlot.get(0);
		}
		return null;
	}

	@Override
	public void save(DataSet set, boolean forSync) {
		if (!forSync) {
			this.inventory.save(set);
		}
	}

	@Override
	public void load(DataSet set, boolean forSync) {
		if (!forSync) {
			this.inventory.load(set);
		}
	}
}
