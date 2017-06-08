package de.ellpeck.rockbottom.game.gui.component;

import de.ellpeck.rockbottom.game.RockBottom;
import de.ellpeck.rockbottom.game.assets.AssetManager;
import de.ellpeck.rockbottom.game.gui.Gui;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ComponentProgressBar extends GuiComponent{

    private final Color progressColor;
    private final Color backgroundColor;

    private final ICallback callback;
    private final boolean isVertical;

    public ComponentProgressBar(Gui gui, int x, int y, int sizeX, int sizeY, Color progressColor, boolean isVertical, ICallback callback){
        super(gui, x, y, sizeX, sizeY);
        this.callback = callback;
        this.isVertical = isVertical;
        this.progressColor = progressColor;
        this.backgroundColor = progressColor.multiply(new Color(0.5F, 0.5F, 0.5F, 0.35F));
    }

    @Override
    public void render(RockBottom game, AssetManager manager, Graphics g){
        super.render(game, manager, g);

        float percent = this.callback.getProgress();

        g.setColor(this.backgroundColor);
        g.fillRect(this.x, this.y, this.sizeX, this.sizeY);

        g.setColor(this.progressColor);
        if(this.isVertical){
            float height = percent*this.sizeY;
            g.fillRect(this.x, this.y+this.sizeY-height, this.sizeX, height);
        }
        else{
            float width = percent*this.sizeX;
            g.fillRect(this.x, this.y, width, this.sizeY);
        }

        g.setColor(Color.black);
        g.drawRect(this.x, this.y, this.sizeX, this.sizeY);
    }

    public interface ICallback{

        float getProgress();
    }
}