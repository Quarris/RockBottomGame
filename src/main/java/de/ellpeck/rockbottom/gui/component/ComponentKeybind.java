package de.ellpeck.rockbottom.gui.component;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.data.settings.Keybind;
import de.ellpeck.rockbottom.api.data.settings.Settings;
import de.ellpeck.rockbottom.api.gui.component.ComponentButton;
import de.ellpeck.rockbottom.api.util.reg.IResourceName;
import de.ellpeck.rockbottom.gui.menu.GuiKeybinds;

public class ComponentKeybind extends ComponentButton{

    private final GuiKeybinds gui;
    private final Keybind bind;
    private final int id;

    public ComponentKeybind(GuiKeybinds gui, int id, int x, int y, Keybind bind){
        super(gui, x, y, 100, 16, null, null);
        this.gui = gui;
        this.id = id;
        this.bind = bind;
    }

    @Override
    protected String getText(){
        return this.isSelected() ? "<?>" : this.bind.getDisplayName();
    }

    @Override
    public boolean onKeyPressed(IGameInstance game, int button){
        if(this.isSelected()){
            this.setKeybind(game, button, false);
            this.gui.selectedKeybind = -1;

            return true;
        }
        else{
            return super.onKeyPressed(game, button);
        }
    }

    @Override
    public boolean onMouseAction(IGameInstance game, int button, float x, float y){
        if(this.isSelected()){
            this.setKeybind(game, button, true);
            this.gui.selectedKeybind = -1;

            return true;
        }
        else{
            return super.onMouseAction(game, button, x, y);
        }
    }

    private void setKeybind(IGameInstance game, int button, boolean isMouse){
       Settings.BindInfo info = game.getSettings().keybinds.get(this.bind.getName().toString());
       info.key = button;
       info.isMouse = isMouse;
    }

    @Override
    public boolean onPressed(IGameInstance game){
        if(!this.isSelected()){
            this.gui.selectedKeybind = this.id;
            return true;
        }
        return false;
    }

    public boolean isSelected(){
        return this.gui.selectedKeybind == this.id;
    }

    @Override
    public IResourceName getName(){
        return RockBottomAPI.createInternalRes("keybind");
    }
}
