package de.ellpeck.rockbottom.construction;

import de.ellpeck.rockbottom.api.GameContent;
import de.ellpeck.rockbottom.api.construction.compendium.construction.ConstructionRecipe;
import de.ellpeck.rockbottom.api.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class ConstructionRegistry {
    public static ConstructionRecipe chest;
    public static ConstructionRecipe simpleFurnace;
    public static ConstructionRecipe mortar;
    public static ConstructionRecipe constructionTable;

    public static void postInit() {
        chest = getManual(GameContent.TILE_CHEST.getItem());
        simpleFurnace = getManual(GameContent.TILE_SIMPLE_FURNACE.getItem());
        mortar = getManual(GameContent.TILE_MORTAR.getItem());
        constructionTable = getManual(GameContent.TILE_CONSTRUCTION_TABLE.getItem());
    }

    private static ConstructionRecipe getManual(Item item) {
        return ConstructionRecipe.getManual(item.getName());
    }

	private static ConstructionRecipe getConstruction(Item item) {
		return ConstructionRecipe.getConstruction(item.getName());
	}
}
