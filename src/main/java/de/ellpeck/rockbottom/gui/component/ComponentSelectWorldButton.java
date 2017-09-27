package de.ellpeck.rockbottom.gui.component;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.IGraphics;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.assets.font.FormattingCode;
import de.ellpeck.rockbottom.api.assets.font.IFont;
import de.ellpeck.rockbottom.api.gui.Gui;
import de.ellpeck.rockbottom.api.gui.IGuiManager;
import de.ellpeck.rockbottom.api.gui.component.ComponentButton;
import de.ellpeck.rockbottom.api.util.reg.IResourceName;
import de.ellpeck.rockbottom.api.world.WorldInfo;

import java.io.File;
import java.util.Date;

public class ComponentSelectWorldButton extends ComponentButton{

    private static final IResourceName RES_LAST_MODIFIED = RockBottomAPI.createInternalRes("info.last_modified");
    private static final IResourceName RES_SEED = RockBottomAPI.createInternalRes("info.seed");

    public final File worldFile;
    private final WorldInfo info;
    private final String lastModified;

    public ComponentSelectWorldButton(Gui gui, int x, int y, File file){
        super(gui, x, y, 186, 24, null, null);
        this.worldFile = file;

        this.info = new WorldInfo(this.worldFile);
        this.info.load();

        this.lastModified = RockBottomAPI.getGame().getAssetManager().getLocalizedDateFormat().format(new Date(WorldInfo.lastModified(this.worldFile)));
    }

    @Override
    public void render(IGameInstance game, IAssetManager manager, IGraphics g, int x, int y){
        super.render(game, manager, g, x, y);
        IFont font = manager.getFont();
        font.drawCutOffString(x+2F, y+1F, this.worldFile.getName(), 0.45F, this.width-4, false, false);
        font.drawString(x+2F, y+12F, FormattingCode.GRAY+manager.localize(RES_LAST_MODIFIED)+": "+this.lastModified, 0.25F);
        font.drawString(x+2F, y+18F, FormattingCode.GRAY+manager.localize(RES_SEED)+": "+this.info.seed, 0.25F);
    }

    @Override
    public boolean onPressed(IGameInstance game){
        IGuiManager gui = game.getGuiManager();
        gui.fadeOut(20, () -> {
            game.startWorld(this.worldFile, this.info);
            gui.fadeIn(20, null);
        });
        return true;
    }

    @Override
    public IResourceName getName(){
        return RockBottomAPI.createInternalRes("select_world_button");
    }
}
