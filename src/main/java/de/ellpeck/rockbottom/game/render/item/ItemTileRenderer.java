package de.ellpeck.rockbottom.game.render.item;

import de.ellpeck.rockbottom.game.RockBottom;
import de.ellpeck.rockbottom.game.assets.AssetManager;
import de.ellpeck.rockbottom.game.render.tile.ITileRenderer;
import de.ellpeck.rockbottom.api.tile.Tile;
import de.ellpeck.rockbottom.game.item.ItemInstance;
import de.ellpeck.rockbottom.game.item.ItemTile;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ItemTileRenderer implements IItemRenderer<ItemTile>{

    @Override
    public void render(RockBottom game, AssetManager manager, Graphics g, ItemTile item, ItemInstance instance, float x, float y, float scale, Color filter){
        Tile tile = item.getTile();
        if(tile != null){
            ITileRenderer renderer = tile.getRenderer();
            if(renderer != null){
                renderer.renderItem(game, manager, g, tile, instance.getMeta(), x, y, scale, filter);
            }
        }
    }

    @Override
    public void renderOnMouseCursor(RockBottom game, AssetManager manager, Graphics g, ItemTile item, ItemInstance instance, float x, float y, float scale, Color filter){
        this.render(game, manager, g, item, instance, x+0.1F*scale, y, scale*0.75F, filter);
    }
}