package de.ellpeck.rockbottom.gui.menu;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.data.settings.Settings;
import de.ellpeck.rockbottom.api.gui.Gui;
import de.ellpeck.rockbottom.api.gui.component.ComponentButton;
import de.ellpeck.rockbottom.api.gui.component.ComponentSlider;
import de.ellpeck.rockbottom.gui.component.ComponentColorPicker;
import de.ellpeck.rockbottom.gui.component.ComponentToggleButton;
import de.ellpeck.rockbottom.init.AbstractGame;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class GuiGraphics extends Gui{

    public GuiGraphics(Gui parent){
        super(304, 150, parent);
    }

    @Override
    public void initGui(IGameInstance game){
        super.initGui(game);
        Settings settings = game.getSettings();
        IAssetManager assetManager = game.getAssetManager();

        this.components.add(new ComponentToggleButton(this, 0, this.guiLeft, this.guiTop, 150, 16, settings.hardwareCursor, "button.hardware_cursor", assetManager.localize(AbstractGame.internalRes("info.hardware_cursor"))));
        this.components.add(new ComponentToggleButton(this, 5, this.guiLeft, this.guiTop+20, 150, 16, settings.cursorInfos, "button.cursor_infos", assetManager.localize(AbstractGame.internalRes("info.cursor_infos"))));
        this.components.add(new ComponentSlider(this, 7, this.guiLeft, this.guiTop+40, 150, 16, (int)(settings.textSpeed*10F), 1, 100, new ComponentSlider.ICallback(){
            @Override
            public void onLetGo(float mouseX, float mouseY, int min, int max, int number){
                settings.textSpeed = (float)number/10F;
            }
        }, assetManager.localize(AbstractGame.internalRes("button.text_speed"))));

        this.components.add(new ComponentSlider(this, 3, this.guiLeft+154, this.guiTop, 150, 16, settings.renderScale, 1, 128, new ComponentSlider.ICallback(){
            @Override
            public void onLetGo(float mouseX, float mouseY, int min, int max, int number){
                settings.renderScale = number;
            }
        }, assetManager.localize(AbstractGame.internalRes("button.render_scale"))));
        this.components.add(new ComponentSlider(this, 2, this.guiLeft+154, this.guiTop+20, 150, 16, settings.guiScale, 1, 8, new ComponentSlider.ICallback(){
            @Override
            public void onLetGo(float mouseX, float mouseY, int min, int max, int number){
                settings.guiScale = number;
                game.getDataManager().savePropSettings(settings);
                game.getGuiManager().setReInit();
            }
        }, assetManager.localize(AbstractGame.internalRes("button.gui_scale"))));
        this.components.add(new ComponentSlider(this, 4, this.guiLeft+154, this.guiTop+40, 150, 16, settings.targetFps, 30, 256, new ComponentSlider.ICallback(){
            @Override
            public void onLetGo(float mouseX, float mouseY, int min, int max, int number){
                settings.targetFps = number;
            }
        }, assetManager.localize(AbstractGame.internalRes("button.target_fps"))){
            @Override
            protected String getText(){
                return this.number >= this.max ? this.text+": "+assetManager.localize(AbstractGame.internalRes("info.unlimited")) : super.getText();
            }
        });
        this.components.add(new ComponentToggleButton(this, 8, this.guiLeft+154, this.guiTop+60, 150, 16, !settings.fullscreen, "button.fullscreen"));
        this.components.add(new ComponentToggleButton(this, 9, this.guiLeft+154, this.guiTop+80, 150, 16, !settings.vsync, "button.vsync"));
        this.components.add(new ComponentToggleButton(this, 10, this.guiLeft+154, this.guiTop+100, 150, 16, !settings.smoothLighting, "button.smooth_lighting"));

        this.components.add(new ComponentColorPicker(this, this.guiLeft+55, this.guiTop+70, 40, 40, settings.guiColor, new ComponentColorPicker.ICallback(){
            @Override
            public void onLetGo(float mouseX, float mouseY, Color color){
                settings.guiColor = color;
                game.getDataManager().savePropSettings(settings);
                game.getGuiManager().setReInit();
            }
        }, false));
        this.components.add(new ComponentButton(this, 6, this.guiLeft+99, this.guiTop+94, 16, 16, "!", assetManager.localize(AbstractGame.internalRes("info.reset"))));

        this.components.add(new ComponentButton(this, -1, this.guiLeft+this.sizeX/2-40, this.guiTop+this.sizeY-16, 80, 16, assetManager.localize(AbstractGame.internalRes("button.back"))));
    }

    @Override
    public void render(IGameInstance game, IAssetManager manager, Graphics g){
        super.render(game, manager, g);

        manager.getFont().drawCenteredString(this.guiLeft+75, this.guiTop+62, manager.localize(AbstractGame.internalRes("info.gui_color")), 0.35F, false);
    }

    @Override
    public boolean onButtonActivated(IGameInstance game, int button){
        Settings settings = game.getSettings();

        if(button == -1){
            game.getGuiManager().openGui(this.parent);
            return true;
        }
        else if(button == 0){
            settings.hardwareCursor = !settings.hardwareCursor;
            game.getAssetManager().reloadCursor(game);
            return true;
        }
        else if(button == 5){
            settings.cursorInfos = !settings.cursorInfos;
            return true;
        }
        else if(button == 6){
            settings.guiColor = new Color(Settings.DEFAULT_GUI_R, Settings.DEFAULT_GUI_G, Settings.DEFAULT_GUI_B);
            game.getDataManager().savePropSettings(settings);
            game.getGuiManager().setReInit();
            return true;
        }
        else if(button == 8){
            settings.fullscreen = !settings.fullscreen;
            game.getDataManager().savePropSettings(settings);
            game.setFullscreen(settings.fullscreen);
            return true;
        }
        else if(button == 9){
            settings.vsync = !settings.vsync;
            Display.setVSyncEnabled(settings.vsync);
            return true;
        }
        else if(button == 10){
            settings.smoothLighting = !settings.smoothLighting;
            return true;
        }
        return false;
    }
}
