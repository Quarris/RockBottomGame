package de.ellpeck.rockbottom.game.world.tile;

import de.ellpeck.rockbottom.api.util.BoundBox;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.TileLayer;
import de.ellpeck.rockbottom.game.gui.GuiChest;
import de.ellpeck.rockbottom.game.gui.container.ContainerChest;
import de.ellpeck.rockbottom.game.net.NetHandler;
import de.ellpeck.rockbottom.game.render.tile.ChestTileRenderer;
import de.ellpeck.rockbottom.game.render.tile.ITileRenderer;
import de.ellpeck.rockbottom.game.world.entity.Entity;
import de.ellpeck.rockbottom.game.world.entity.player.EntityPlayer;
import de.ellpeck.rockbottom.game.world.tile.entity.TileEntity;
import de.ellpeck.rockbottom.game.world.tile.entity.TileEntityChest;

public class TileChest extends TileBasic{

    public TileChest(){
        super("chest");
    }

    @Override
    protected ITileRenderer createRenderer(String name){
        return new ChestTileRenderer();
    }

    @Override
    public boolean canProvideTileEntity(){
        return true;
    }

    @Override
    public TileEntity provideTileEntity(IWorld world, int x, int y){
        return new TileEntityChest(world, x, y);
    }

    @Override
    public boolean onInteractWith(IWorld world, int x, int y, EntityPlayer player){
        TileEntityChest chest = world.getTileEntity(x, y, TileEntityChest.class);
        if(chest != null){
            player.openGuiContainer(new GuiChest(player), new ContainerChest(player, chest));
            return true;
        }
        return false;
    }

    @Override
    public BoundBox getBoundBox(IWorld world, int x, int y){
        return null;
    }

    @Override
    public boolean canPlace(IWorld world, int x, int y, TileLayer layer){
        return super.canPlace(world, x, y, layer) && world.getTile(x, y-1).isFullTile();
    }

    @Override
    public void onChangeAround(IWorld world, int x, int y, TileLayer layer, int changedX, int changedY, TileLayer changedLayer){
        if(layer == changedLayer){
            if(!world.getTile(layer, x, y-1).isFullTile()){
                world.destroyTile(x, y, layer, null, true);
            }
        }
    }

    @Override
    public void onDestroyed(IWorld world, int x, int y, Entity destroyer, TileLayer layer, boolean forceDrop){
        super.onDestroyed(world, x, y, destroyer, layer, forceDrop);

        if(!NetHandler.isClient()){
            TileEntityChest chest = world.getTileEntity(x, y, TileEntityChest.class);
            if(chest != null){
                chest.dropInventory(chest.inventory);
            }
        }
    }

    @Override
    public boolean isFullTile(){
        return false;
    }
}