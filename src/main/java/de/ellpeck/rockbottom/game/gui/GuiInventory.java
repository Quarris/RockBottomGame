package de.ellpeck.rockbottom.game.gui;

import de.ellpeck.rockbottom.game.RockBottom;
import de.ellpeck.rockbottom.game.assets.AssetManager;
import de.ellpeck.rockbottom.game.assets.font.FormattingCode;
import de.ellpeck.rockbottom.game.construction.BasicRecipe;
import de.ellpeck.rockbottom.game.construction.ConstructionRegistry;
import de.ellpeck.rockbottom.game.construction.IRecipe;
import de.ellpeck.rockbottom.game.gui.component.ComponentFancyToggleButton;
import de.ellpeck.rockbottom.game.gui.component.ComponentRecipeButton;
import de.ellpeck.rockbottom.game.gui.component.ComponentSlider;
import de.ellpeck.rockbottom.game.gui.container.ContainerInventory;
import de.ellpeck.rockbottom.game.inventory.IInvChangeCallback;
import de.ellpeck.rockbottom.game.inventory.IInventory;
import de.ellpeck.rockbottom.game.item.ItemInstance;
import de.ellpeck.rockbottom.game.net.NetHandler;
import de.ellpeck.rockbottom.game.net.packet.toserver.PacketManualConstruction;
import de.ellpeck.rockbottom.game.world.entity.player.EntityPlayer;
import org.newdawn.slick.Graphics;

import java.util.ArrayList;
import java.util.List;

public class GuiInventory extends GuiContainer implements IInvChangeCallback{

    private static boolean isConstructionOpen;
    private static boolean shouldShowAll;
    private static int craftAmount = 1;

    private final List<ComponentRecipeButton> constructionButtons = new ArrayList<>();

    public GuiInventory(EntityPlayer player){
        super(player, 158, 83);
    }

    @Override
    public void initGui(RockBottom game){
        super.initGui(game);

        this.components.add(new ComponentFancyToggleButton(this, 0, this.guiLeft-14, this.guiTop, 12, 12, !isConstructionOpen, "gui.construction", game.assetManager.localize("button.construction")));

        if(isConstructionOpen){
            this.components.add(new ComponentSlider(this, 2, this.guiLeft-104, this.guiTop+71, 88, 12, craftAmount, 1, 128, new ComponentSlider.ICallback(){
                @Override
                public void onNumberChange(float mouseX, float mouseY, int min, int max, int number){
                    craftAmount = number;
                }
            }, game.assetManager.localize("button.construction_amount")));
            this.components.add(new ComponentFancyToggleButton(this, 1, this.guiLeft-14, this.guiTop+14, 12, 12, !shouldShowAll, "gui.all_construction", game.assetManager.localize("button.all_construction")));
            this.initConstructionButtons();
        }
    }

    protected void initConstructionButtons(){
        if(!this.constructionButtons.isEmpty()){
            this.components.removeAll(this.constructionButtons);
            this.constructionButtons.clear();
        }

        int x = 0;
        int y = 0;

        for(int i = 0; i < ConstructionRegistry.MANUAL_RECIPES.size(); i++){
            BasicRecipe recipe = ConstructionRegistry.MANUAL_RECIPES.get(i);
            boolean matches = IRecipe.matchesInv(recipe, this.player.inv);

            if(matches || shouldShowAll){
                this.constructionButtons.add(new ComponentRecipeButton(this, 3+i, this.guiLeft-104+x, this.guiTop+y, 16, 16, recipe, ConstructionRegistry.MANUAL_RECIPES.indexOf(recipe), matches));

                x += 18;
                if((i+1)%5 == 0){
                    y += 18;
                    x = 0;
                }
            }
        }

        this.components.addAll(this.constructionButtons);
    }

    @Override
    public void render(RockBottom game, AssetManager manager, Graphics g){
        if(isConstructionOpen){
            if(this.constructionButtons.isEmpty()){
                manager.getFont().drawSplitString(this.guiLeft-104, this.guiTop, FormattingCode.GRAY+manager.localize("info.need_items"), 0.25F, 88);
            }
        }

        super.render(game, manager, g);
    }

    @Override
    public void onOpened(RockBottom game){
        super.onOpened(game);
        this.player.inv.addChangeCallback(this);
    }

    @Override
    public void onClosed(RockBottom game){
        super.onClosed(game);
        this.player.inv.removeChangeCallback(this);
    }

    @Override
    protected void initGuiVars(RockBottom game){
        super.initGuiVars(game);

        if(isConstructionOpen){
            this.guiLeft += 52;
        }
    }

    @Override
    public boolean onButtonActivated(RockBottom game, int button){
        if(button == 1){
            shouldShowAll = !shouldShowAll;
            this.initConstructionButtons();
            return true;
        }
        else if(button == 0){
            isConstructionOpen = !isConstructionOpen;
            this.initGui(game);
            return true;
        }
        else{
            for(ComponentRecipeButton but : this.constructionButtons){
                if(but.id == button){
                    if(but.canConstruct){
                        if(NetHandler.isClient()){
                            NetHandler.sendToServer(new PacketManualConstruction(game.player.getUniqueId(), but.recipeId, craftAmount));
                        }
                        else{
                            ContainerInventory.doManualCraft(game.player, but.recipe, craftAmount);
                        }
                        return true;
                    }
                    else{
                        break;
                    }
                }
            }
            return super.onButtonActivated(game, button);
        }
    }

    @Override
    public void onChange(IInventory inv, int slot, ItemInstance newInstance){
        if(isConstructionOpen){
            this.initConstructionButtons();
        }
    }
}