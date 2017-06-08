package de.ellpeck.rockbottom.game.gui;

import de.ellpeck.rockbottom.game.RockBottom;
import de.ellpeck.rockbottom.game.assets.AssetManager;
import de.ellpeck.rockbottom.game.gui.component.ComponentSlot;
import de.ellpeck.rockbottom.game.gui.container.ItemContainer;
import de.ellpeck.rockbottom.game.net.NetHandler;
import de.ellpeck.rockbottom.game.net.packet.toserver.PacketDropItem;
import de.ellpeck.rockbottom.game.util.Util;
import de.ellpeck.rockbottom.game.world.entity.player.EntityPlayer;
import de.ellpeck.rockbottom.game.gui.container.ContainerSlot;
import de.ellpeck.rockbottom.game.item.ItemInstance;
import de.ellpeck.rockbottom.game.world.entity.EntityItem;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class GuiContainer extends Gui{

    public static final Color PROGRESS_COLOR = new Color(0.1F, 0.5F, 0.1F);
    public static final Color FIRE_COLOR = new Color(0.5F, 0.1F, 0.1F);

    public final EntityPlayer player;
    public ItemInstance holdingInst;

    public GuiContainer(EntityPlayer player, int sizeX, int sizeY){
        super(sizeX, sizeY);
        this.player = player;
    }

    @Override
    public void onClosed(RockBottom game){
        if(this.holdingInst != null){
            this.dropHeldItem();
        }

        this.player.closeContainer();
    }

    @Override
    public boolean onMouseAction(RockBottom game, int button, float x, float y){
        if(super.onMouseAction(game, button, x, y)){
            return true;
        }

        if(this.holdingInst != null && button == game.settings.buttonGuiAction1){
            if(!this.isMouseOver(game)){
                this.dropHeldItem();
                this.holdingInst = null;

                return true;
            }
        }

        return false;
    }

    @Override
    public void initGui(RockBottom game){
        super.initGui(game);

        ItemContainer container = this.player.getContainer();
        for(int i = 0; i < container.getSlotAmount(); i++){
            ContainerSlot slot = container.getSlot(i);
            this.components.add(new ComponentSlot(this, slot, i, this.guiLeft+slot.x, this.guiTop+slot.y));
        }
    }

    @Override
    public void render(RockBottom game, AssetManager manager, Graphics g){
        super.render(game, manager, g);

        if(this.holdingInst != null){
            float mouseX = game.getMouseInGuiX();
            float mouseY = game.getMouseInGuiY();

            Util.renderItemInGui(game, manager, g, this.holdingInst, mouseX-4F, mouseY-4F, 0.8F, Color.white);
        }
    }

    private void dropHeldItem(){
        if(NetHandler.isClient()){
            NetHandler.sendToServer(new PacketDropItem(this.player.getUniqueId(), this.holdingInst));
        }
        else{
            EntityItem.spawn(this.player.world, this.holdingInst, this.player.x, this.player.y+1, this.player.facing.x*0.25, 0);
        }
    }

    @Override
    public boolean doesPauseGame(){
        return false;
    }
}