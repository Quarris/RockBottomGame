package de.ellpeck.rockbottom.construction;

import de.ellpeck.rockbottom.api.GameContent;
import de.ellpeck.rockbottom.api.construction.compendium.construction.ConstructionRecipe;
import de.ellpeck.rockbottom.api.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class ConstructionRegistry {

    public static final List<ConstructionRecipe> BRITTLE_TOOLS = new ArrayList<>();
    public static final List<ConstructionRecipe> STONE_TOOLS = new ArrayList<>();
    public static final List<ConstructionRecipe> COPPER_TOOLS = new ArrayList<>();
    public static ConstructionRecipe ladder;
    public static ConstructionRecipe chest;
    public static ConstructionRecipe grassTorch;
    public static ConstructionRecipe simpleFurnace;
    public static ConstructionRecipe torch;
    public static ConstructionRecipe mortar;
    public static ConstructionRecipe pestle;
    public static ConstructionRecipe simpleHoe;
    public static ConstructionRecipe constructionTable;
    public static ConstructionRecipe smithingTable;

    public static void postInit() {
        BRITTLE_TOOLS.add(getManual(GameContent.ITEM_BRITTLE_PICKAXE));
        BRITTLE_TOOLS.add(getManual(GameContent.ITEM_BRITTLE_AXE));
        BRITTLE_TOOLS.add(getManual(GameContent.ITEM_BRITTLE_SHOVEL));
        BRITTLE_TOOLS.add(getManual(GameContent.ITEM_BRITTLE_SWORD));

        STONE_TOOLS.add(getManual(GameContent.ITEM_STONE_PICKAXE));
        STONE_TOOLS.add(getManual(GameContent.ITEM_STONE_AXE));
        STONE_TOOLS.add(getManual(GameContent.ITEM_STONE_SHOVEL));
        STONE_TOOLS.add(getManual(GameContent.ITEM_STONE_SWORD));
        STONE_TOOLS.add(getManual(GameContent.ITEM_WRENCH));
        STONE_TOOLS.add(getManual(GameContent.ITEM_SAW));
        STONE_TOOLS.add(getManual(GameContent.ITEM_HAMMER));
        STONE_TOOLS.add(getManual(GameContent.ITEM_MALLET));
        STONE_TOOLS.add(getManual(GameContent.ITEM_CHISEL));

        COPPER_TOOLS.add(getManual(GameContent.ITEM_COPPER_PICKAXE));
        COPPER_TOOLS.add(getManual(GameContent.ITEM_COPPER_AXE));
        COPPER_TOOLS.add(getManual(GameContent.ITEM_COPPER_SHOVEL));
        COPPER_TOOLS.add(getManual(GameContent.ITEM_COPPER_SWORD));


        ladder = getConstruction(GameContent.TILE_LADDER.getItem());
        chest = getConstruction(GameContent.TILE_CHEST.getItem());
        grassTorch = getManual(GameContent.TILE_GRASS_TORCH.getItem());
        simpleFurnace = getConstruction(GameContent.TILE_SIMPLE_FURNACE.getItem());
        torch = getManual(GameContent.TILE_TORCH.getItem());
        mortar = getManual(GameContent.TILE_MORTAR.getItem());
        pestle = getManual(GameContent.ITEM_PESTLE);
        simpleHoe = getManual(GameContent.ITEM_SIMPLE_HOE);
        constructionTable = getManual(GameContent.TILE_CONSTRUCTION_TABLE.getItem());
        smithingTable = getConstruction(GameContent.TILE_SMITHING_TABLE.getItem());
    }

    private static ConstructionRecipe getManual(Item item) {
        return ConstructionRecipe.getManual(item.getName());
    }

    private static ConstructionRecipe getConstruction(Item item) {
    	return ConstructionRecipe.getConstruction(item.getName());
	}
}
