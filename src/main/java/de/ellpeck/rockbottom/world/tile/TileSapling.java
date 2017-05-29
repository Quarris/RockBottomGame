package de.ellpeck.rockbottom.world.tile;

import de.ellpeck.rockbottom.util.BoundBox;
import de.ellpeck.rockbottom.util.Util;
import de.ellpeck.rockbottom.world.IWorld;
import de.ellpeck.rockbottom.world.TileLayer;
import de.ellpeck.rockbottom.world.World;
import de.ellpeck.rockbottom.world.gen.feature.WorldGenTrees;

public class TileSapling extends TileBasic{

    public TileSapling(){
        super("sapling");
    }

    @Override
    public boolean canPlaceInLayer(TileLayer layer){
        return layer == TileLayer.MAIN;
    }

    @Override
    public boolean isFullTile(){
        return false;
    }

    @Override
    public BoundBox getBoundBox(IWorld world, int x, int y){
        return null;
    }

    @Override
    public void updateRandomly(World world, int x, int y){
        WorldGenTrees trees = new WorldGenTrees();
        trees.generateAt(world, x, y, Util.RANDOM);
    }

    @Override
    public boolean canPlace(World world, int x, int y, TileLayer layer){
        if(super.canPlace(world, x, y, layer)){
            Tile tile = world.getTile(x, y-1);
            if(tile instanceof TileDirt || tile instanceof TileGrass){
                return true;
            }
        }
        return false;
    }
}
