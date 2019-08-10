package de.ellpeck.rockbottom.construction;

import de.ellpeck.rockbottom.api.GameContent;
import de.ellpeck.rockbottom.api.Registries;
import de.ellpeck.rockbottom.api.construction.compendium.ICompendiumRecipe;
import de.ellpeck.rockbottom.api.construction.compendium.construction.ConstructionRecipe;
import de.ellpeck.rockbottom.api.item.Item;
import de.ellpeck.rockbottom.api.util.reg.ResourceName;

public final class ConstructionRegistry {
    public static ICompendiumRecipe chest;
    public static ICompendiumRecipe simpleFurnace;
    public static ICompendiumRecipe mortar;
    public static ICompendiumRecipe constructionTable;

    public static void postInit() {
        chest = getRecipe(GameContent.TILE_CHEST.getItem().getName());
        simpleFurnace = getRecipe(GameContent.TILE_SIMPLE_FURNACE.getItem().getName());
        mortar = getRecipe(GameContent.TILE_MORTAR.getItem().getName());
        constructionTable = getRecipe(GameContent.TILE_CONSTRUCTION_TABLE.getItem().getName());
    }

    public static ICompendiumRecipe getRecipe(ResourceName name) {
        return Registries.ALL_RECIPES.get(name);
    }
}
