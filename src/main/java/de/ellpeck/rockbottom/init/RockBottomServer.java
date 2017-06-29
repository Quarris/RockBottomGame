package de.ellpeck.rockbottom.init;

import de.ellpeck.rockbottom.Main;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.data.set.DataSet;
import de.ellpeck.rockbottom.api.data.settings.Settings;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.entity.player.IInteractionManager;
import de.ellpeck.rockbottom.api.gui.IGuiManager;
import de.ellpeck.rockbottom.api.particle.IParticleManager;
import de.ellpeck.rockbottom.api.render.IPlayerDesign;
import de.ellpeck.rockbottom.api.util.reg.NameToIndexInfo;
import de.ellpeck.rockbottom.api.world.WorldInfo;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

import java.io.File;
import java.util.UUID;

public class RockBottomServer extends AbstractGame{

    public static void init(){
        doInit(new RockBottomServer());
    }

    @Override
    public void init(GameContainer container) throws SlickException{
        super.init(container);

        File file = new File(this.dataManager.getWorldsDir(), "world_server");

        WorldInfo info = new WorldInfo(file);
        if(file.isDirectory()){
            info.load();
        }

        this.startWorld(file, info);

        try{
            RockBottomAPI.getNet().init(null, Main.port, true);
        }
        catch(Exception e){
            Log.error("Couldn't start server", e);
        }
    }

    @Override
    protected Container makeContainer() throws SlickException{
        return new ContainerServer(this);
    }

    @Override
    public int getAutosaveInterval(){
        return 60;
    }

    @Override
    public void joinWorld(DataSet playerSet, WorldInfo info, NameToIndexInfo tileRegInfo, NameToIndexInfo biomeRegInfo){
        throw new UnsupportedOperationException("Cannot join a world on a dedicated server");
    }

    @Override
    public void openIngameMenu(){
        throw new UnsupportedOperationException("Cannot open the ingame menu on a dedicated server");
    }

    @Override
    public int getGuiScale(){
        throw new UnsupportedOperationException("Cannot get gui scale on a dedicated server");
    }

    @Override
    public int getWorldScale(){
        throw new UnsupportedOperationException("Cannot get world scale on a dedicated server");
    }

    @Override
    public double getWidthInWorld(){
        throw new UnsupportedOperationException("Cannot get the width in the world on a dedicated server");
    }

    @Override
    public double getHeightInWorld(){
        throw new UnsupportedOperationException("Cannot get the height in the world on a dedicated server");
    }

    @Override
    public double getWidthInGui(){
        throw new UnsupportedOperationException("Cannot get the width in the gui on a dedicated server");
    }

    @Override
    public double getHeightInGui(){
        throw new UnsupportedOperationException("Cannot get the height in the gui on a dedicated server");
    }

    @Override
    public float getMouseInGuiX(){
        throw new UnsupportedOperationException("Cannot get mouse coordinates on a dedicated server");
    }

    @Override
    public float getMouseInGuiY(){
        throw new UnsupportedOperationException("Cannot get mouse coordinates on a dedicated server");
    }

    @Override
    public AbstractEntityPlayer getPlayer(){
        throw new UnsupportedOperationException("Cannot get the player on a dedicated server");
    }

    @Override
    public IGuiManager getGuiManager(){
        throw new UnsupportedOperationException("Cannot get the gui manager on a dedicated server");
    }

    @Override
    public IInteractionManager getInteractionManager(){
        throw new UnsupportedOperationException("Cannot get the interaction manager on a dedicated server");
    }

    @Override
    public IAssetManager getAssetManager(){
        throw new UnsupportedOperationException("Cannot get the asset manager on a dedicated server");
    }

    @Override
    public IParticleManager getParticleManager(){
        throw new UnsupportedOperationException("Cannot get the particle manager on a dedicated server");
    }

    @Override
    public UUID getUniqueId(){
        throw new UnsupportedOperationException("Cannot get the unique id on a dedicated server");
    }

    @Override
    public boolean isDebug(){
        throw new UnsupportedOperationException("Cannot get debug mode on a dedicated server");
    }

    @Override
    public boolean isLightDebug(){
        throw new UnsupportedOperationException("Cannot get debug mode on a dedicated server");
    }

    @Override
    public boolean isForegroundDebug(){
        throw new UnsupportedOperationException("Cannot get debug mode on a dedicated server");
    }

    @Override
    public boolean isBackgroundDebug(){
        throw new UnsupportedOperationException("Cannot get debug mode on a dedicated server");
    }

    @Override
    public boolean isItemInfoDebug(){
        throw new UnsupportedOperationException("Cannot get debug mode on a dedicated server");
    }

    @Override
    public void setFullscreen(boolean fullscreen){
        throw new UnsupportedOperationException("Cannot set fullscreen on a dedicated server");
    }

    @Override
    public IPlayerDesign getPlayerDesign(){
        throw new UnsupportedOperationException("Cannot get player design on a dedicated server");
    }

    @Override
    public boolean isDedicatedServer(){
        return true;
    }

    @Override
    public void setUniqueId(UUID id){
        throw new UnsupportedOperationException("Cannot set unique id on a dedicated server");
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException{
        throw new UnsupportedOperationException("Cannot render on a dedicated server");
    }

    @Override
    public Settings getSettings(){
        throw new UnsupportedOperationException("Cannot get settings on a dedicated server");
    }
}