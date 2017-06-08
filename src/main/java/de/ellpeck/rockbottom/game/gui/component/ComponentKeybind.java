package de.ellpeck.rockbottom.game.gui.component;

import de.ellpeck.rockbottom.game.RockBottom;
import de.ellpeck.rockbottom.game.assets.AssetManager;
import de.ellpeck.rockbottom.game.data.settings.Settings;
import de.ellpeck.rockbottom.game.gui.menu.GuiKeybinds;
import org.newdawn.slick.Input;

public class ComponentKeybind extends ComponentButton{

    private final GuiKeybinds gui;
    private final Settings.Keybind bind;

    public ComponentKeybind(GuiKeybinds gui, int id, int x, int y, Settings.Keybind bind){
        super(gui, id, x, y, 150, 16, null);
        this.gui = gui;
        this.bind = bind;
    }

    @Override
    protected String getText(){
        AssetManager manager = RockBottom.get().assetManager;
        return manager.localize("key."+this.bind.name)+": "+(this.isActive() ? "<?>" : Input.getKeyName(this.bind.key));
    }

    @Override
    public boolean onKeyboardAction(RockBottom game, int button, char character){
        if(this.isActive()){
            this.bind.key = button;
            this.gui.activeKeybind = -1;

            return true;
        }
        else{
            return super.onKeyboardAction(game, button, character);
        }
    }

    @Override
    public boolean onPressed(RockBottom game){
        if(!this.isActive()){
            this.gui.activeKeybind = this.id;
            return true;
        }
        return false;
    }

    private boolean isActive(){
        return this.gui.activeKeybind == this.id;
    }
}