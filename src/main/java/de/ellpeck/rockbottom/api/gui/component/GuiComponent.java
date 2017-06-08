package de.ellpeck.rockbottom.api.gui.component;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.gui.Gui;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class GuiComponent{

    public final Color guiColor = RockBottomAPI.getGame().getSettings().guiColor;
    public final Color colorButton = this.guiColor.multiply(new Color(1F, 1F, 1F, 0.5F));
    public final Color colorButtonUnselected = this.colorButton.darker(0.4F);
    public final Color colorOutline = this.guiColor.darker(0.3F);
    public final int sizeX;
    public final int sizeY;
    public Gui gui;
    public int x;
    public int y;

    public GuiComponent(Gui gui, int x, int y, int sizeX, int sizeY){
        this.gui = gui;
        this.x = x;
        this.y = y;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public void update(IGameInstance game){

    }

    public void render(IGameInstance game, IAssetManager manager, Graphics g){

    }

    public void renderOverlay(IGameInstance game, IAssetManager manager, Graphics g){

    }

    public boolean isMouseOver(IGameInstance game){
        if(Mouse.isInsideWindow()){
            int mouseX = (int)game.getMouseInGuiX();
            int mouseY = (int)game.getMouseInGuiY();

            return mouseX >= this.x && mouseX < this.x+this.sizeX && mouseY >= this.y && mouseY < this.y+this.sizeY;
        }
        else{
            return false;
        }
    }

    public boolean onMouseAction(IGameInstance game, int button, float x, float y){
        return false;
    }

    public boolean onKeyboardAction(IGameInstance game, int button, char character){
        return false;
    }
}
