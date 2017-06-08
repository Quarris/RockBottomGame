package de.ellpeck.rockbottom.game.render.tile;

import de.ellpeck.rockbottom.game.RockBottom;
import de.ellpeck.rockbottom.game.assets.AssetManager;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.tile.Tile;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class DefaultTileRenderer<T extends Tile> implements ITileRenderer<T>{

    public final String texture;

    public DefaultTileRenderer(String texture){
        this.texture = "tiles."+texture;
    }

    @Override
    public void render(RockBottom game, AssetManager manager, Graphics g, IWorld world, T tile, int x, int y, float renderX, float renderY, Color filter){
        manager.getImage(this.texture).draw(renderX, renderY, 1F, 1F, filter);
    }

    @Override
    public void renderItem(RockBottom game, AssetManager manager, Graphics g, T tile, int meta, float x, float y, float scale, Color filter){
        manager.getImage(this.texture).draw(x, y, scale, scale, filter);
    }

    @Override
    public Image getParticleTexture(RockBottom game, AssetManager manager, Graphics g, T tile, int meta){
        return manager.getImage(this.texture);
    }
}