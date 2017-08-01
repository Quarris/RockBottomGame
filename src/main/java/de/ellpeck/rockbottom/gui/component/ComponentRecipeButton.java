package de.ellpeck.rockbottom.gui.component;

import de.ellpeck.rockbottom.api.Constants;
import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.assets.font.FormattingCode;
import de.ellpeck.rockbottom.api.construction.IRecipe;
import de.ellpeck.rockbottom.api.construction.resource.IResUseInfo;
import de.ellpeck.rockbottom.api.construction.resource.ResUseInfo;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.gui.GuiContainer;
import de.ellpeck.rockbottom.api.gui.component.ComponentButton;
import de.ellpeck.rockbottom.api.item.ItemInstance;
import de.ellpeck.rockbottom.api.util.reg.IResourceName;
import de.ellpeck.rockbottom.init.AbstractGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import java.util.ArrayList;
import java.util.List;

public class ComponentRecipeButton extends ComponentButton{

    private static final Color TRANSPARENT = new Color(1F, 1F, 1F, 0.5F);
    private static final IResourceName LOC_CONSTRUCTS = AbstractGame.internalRes("info.constructs");
    private static final IResourceName LOC_USES = AbstractGame.internalRes("info.uses");

    public final AbstractEntityPlayer player;
    public IRecipe recipe;
    public int recipeId;
    public boolean canConstruct;

    public ComponentRecipeButton(GuiContainer gui, int id, int x, int y, int sizeX, int sizeY){
        super(gui, id, x, y, sizeX, sizeY, null);
        this.player = gui.player;
    }

    public void setRecipe(IRecipe recipe, int id, boolean canConstruct){
        this.recipe = recipe;
        this.recipeId = id;
        this.canConstruct = canConstruct;
    }

    @Override
    public void render(IGameInstance game, IAssetManager manager, Graphics g){
        if(this.recipe != null){
            super.render(game, manager, g);

            List<ItemInstance> outputs = this.recipe.getOutputs();
            ItemInstance instance = outputs.get(0);
            RockBottomAPI.getApiHandler().renderItemInGui(game, manager, g, instance, this.x+2F, this.y+2F, 1F, this.canConstruct ? Color.white : TRANSPARENT);
        }
    }

    @Override
    protected String[] getHover(){
        if(this.recipe != null){
            IGameInstance game = AbstractGame.get();
            IAssetManager manager = game.getAssetManager();

            List<IResUseInfo> inputs = this.recipe.getInputs();
            List<ItemInstance> outputs = this.recipe.getOutputs();

            List<String> hover = new ArrayList<>();

            hover.add(manager.localize(LOC_CONSTRUCTS)+":");
            for(ItemInstance inst : outputs){
                hover.add(FormattingCode.YELLOW+" "+inst.getDisplayName()+" x"+inst.getAmount());
            }

            hover.add(manager.localize(LOC_USES)+":");
            for(IResUseInfo info : inputs){
                FormattingCode code;

                if(!this.canConstruct && !this.player.getInv().containsResource(info)){
                    code = FormattingCode.RED;
                }
                else{
                    code = FormattingCode.GREEN;
                }

                ItemInstance inst;

                List<ItemInstance> items = info.getItems();
                if(items.size() > 1){
                    int index = (game.getTotalTicks()/Constants.TARGET_TPS)%(items.size());
                    inst = items.get(index);
                }
                else{
                    inst = items.get(0);
                }

                hover.add(code+" "+inst.getDisplayName()+" x"+inst.getAmount());
            }

            return hover.toArray(new String[hover.size()]);
        }
        else{
            return super.getHover();
        }
    }

    @Override
    public IResourceName getName(){
        return RockBottomAPI.createInternalRes("recipe_button");
    }
}
