package de.ellpeck.rockbottom.render.tile;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.render.tile.MultiTileRenderer;
import de.ellpeck.rockbottom.api.tile.MultiTile;
import de.ellpeck.rockbottom.api.tile.state.TileState;
import de.ellpeck.rockbottom.api.util.Pos2;
import de.ellpeck.rockbottom.api.util.reg.IResourceName;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.TileLayer;
import de.ellpeck.rockbottom.world.tile.TileCombiner;
import de.ellpeck.rockbottom.world.tile.TileSeparator;
import de.ellpeck.rockbottom.world.tile.entity.TileEntityCombiner;
import de.ellpeck.rockbottom.world.tile.entity.TileEntitySeparator;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import java.util.HashMap;
import java.util.Map;

public class CombinerTileRenderer extends MultiTileRenderer<TileCombiner>{

    protected final Map<Pos2, IResourceName> texturesActive = new HashMap<>();

    public CombinerTileRenderer(IResourceName texture, MultiTile tile){
        super(texture, tile);

        for(int x = 0; x < tile.getWidth(); x++){
            for(int y = 0; y < tile.getHeight(); y++){
                if(tile.isStructurePart(x, y)){
                    this.texturesActive.put(new Pos2(x, y), this.texture.addSuffix(".active."+x+"."+y));
                }
            }
        }
    }

    @Override
    public void render(IGameInstance game, IAssetManager manager, Graphics g, IWorld world, TileCombiner tile, TileState state, int x, int y, TileLayer layer, float renderX, float renderY, float scale, Color[] light){
        Pos2 innerCoord = tile.getInnerCoord(state);
        IResourceName tex;

        Pos2 mainPos = tile.getMainPos(x, y, state);
        TileEntityCombiner tileEntity = world.getTileEntity(mainPos.getX(), mainPos.getY(), TileEntityCombiner.class);
        if(tileEntity != null && tileEntity.isActive()){
            tex = this.texturesActive.get(innerCoord);
        }
        else{
            tex = this.textures.get(innerCoord);
        }

        manager.getTexture(tex).drawWithLight(renderX, renderY, scale, scale, light);
    }
}