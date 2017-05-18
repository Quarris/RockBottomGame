package de.ellpeck.rockbottom.gui;

import de.ellpeck.rockbottom.RockBottom;
import de.ellpeck.rockbottom.gui.component.ComponentProgressBar;
import de.ellpeck.rockbottom.world.entity.player.EntityPlayer;
import de.ellpeck.rockbottom.world.tile.entity.TileEntitySmelter;
import org.newdawn.slick.Color;

public class GuiSmelter extends GuiContainer{

    private final TileEntitySmelter tile;

    public GuiSmelter(EntityPlayer player, TileEntitySmelter tile){
        super(player, 198, 150);
        this.tile = tile;
    }

    @Override
    public void initGui(RockBottom game){
        super.initGui(game);

        this.components.add(new ComponentProgressBar(this, this.guiLeft+80, this.guiTop+15, 40, 8, PROGRESS_COLOR, false, this.tile::getSmeltPercentage));
        this.components.add(new ComponentProgressBar(this, this.guiLeft+74, this.guiTop+30, 8, 18, FIRE_COLOR, true, this.tile:: getFuelPercentage));
    }
}