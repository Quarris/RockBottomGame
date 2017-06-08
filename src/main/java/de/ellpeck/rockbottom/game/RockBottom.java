package de.ellpeck.rockbottom.game;

import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.game.assets.AssetManager;
import de.ellpeck.rockbottom.game.construction.ConstructionRegistry;
import de.ellpeck.rockbottom.game.data.DataManager;
import de.ellpeck.rockbottom.game.data.set.DataSet;
import de.ellpeck.rockbottom.game.data.settings.Settings;
import de.ellpeck.rockbottom.game.gui.*;
import de.ellpeck.rockbottom.game.gui.menu.GuiMainMenu;
import de.ellpeck.rockbottom.game.gui.menu.GuiMenu;
import de.ellpeck.rockbottom.game.net.NetHandler;
import de.ellpeck.rockbottom.game.net.chat.ChatLog;
import de.ellpeck.rockbottom.game.net.client.ClientWorld;
import de.ellpeck.rockbottom.game.net.packet.toserver.PacketDisconnect;
import de.ellpeck.rockbottom.game.particle.ParticleManager;
import de.ellpeck.rockbottom.game.render.WorldRenderer;
import de.ellpeck.rockbottom.api.util.IAction;
import de.ellpeck.rockbottom.game.util.Util;
import de.ellpeck.rockbottom.api.util.reg.NameToIndexInfo;
import de.ellpeck.rockbottom.game.world.World;
import de.ellpeck.rockbottom.api.world.WorldInfo;
import de.ellpeck.rockbottom.game.world.entity.player.EntityPlayer;
import de.ellpeck.rockbottom.game.world.entity.player.InteractionManager;
import org.newdawn.slick.*;
import org.newdawn.slick.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RockBottom extends BasicGame{

    public static final String VERSION = "0.0.3";
    private static RockBottom instance;

    private final List<IAction> scheduledActions = new ArrayList<>();
    public DataManager dataManager;
    public Settings settings;
    public EntityPlayer player;
    public GuiManager guiManager;
    public InteractionManager interactionManager;
    public ChatLog chatLog;
    public World world;
    public AssetManager assetManager;
    public ParticleManager particleManager;
    public int tpsAverage;
    public int fpsAverage;
    public UUID uniqueId;
    public boolean isDebug;
    public boolean isLightDebug;
    public boolean isForegroundDebug;
    public boolean isBackgroundDebug;
    private Container container;
    private WorldRenderer worldRenderer;
    private long lastPollTime;
    private int tpsAccumulator;
    private int fpsAccumulator;

    public RockBottom(){
        super("Rock Bottom "+VERSION);

        Log.info("Setting game instance to "+this);
        instance = this;
    }

    public static RockBottom get(){
        return instance;
    }

    @Override
    public void init(GameContainer container) throws SlickException{
        Log.info("----- Initializing game -----");

        this.dataManager = new DataManager(this);

        this.settings = new Settings();
        this.dataManager.loadPropSettings(this.settings);

        this.container = (Container)container;
        this.container.setTargetFrameRate(this.settings.targetFps);

        this.assetManager = new AssetManager();
        this.assetManager.create(this);

        ContentRegistry.init();
        ConstructionRegistry.init();
        WorldRenderer.init();

        this.guiManager = new GuiManager();
        this.interactionManager = new InteractionManager();
        this.chatLog = new ChatLog();

        this.worldRenderer = new WorldRenderer();
        this.particleManager = new ParticleManager();

        Log.info("----- Done initializing game -----");
        this.quitWorld();
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException{
        this.tpsAccumulator++;

        long time = container.getTime();
        if(time-this.lastPollTime >= 1000){
            this.tpsAverage = this.tpsAccumulator;
            this.fpsAverage = this.fpsAccumulator;

            this.tpsAccumulator = 0;
            this.fpsAccumulator = 0;

            this.lastPollTime = time;
        }

        synchronized(this.scheduledActions){
            for(int i = 0; i < this.scheduledActions.size(); i++){
                IAction action = this.scheduledActions.get(i);

                if(action.run()){
                    this.scheduledActions.remove(i);
                    i--;
                }
            }
        }

        if(NetHandler.isClient()){
            if(!NetHandler.isConnectedToServer()){
                this.quitWorld();
            }
        }

        if(this.world != null && this.player != null){
            Gui gui = this.guiManager.getGui();
            if(gui == null || !gui.doesPauseGame() || NetHandler.isActive()){
                this.world.update(this);
                this.interactionManager.update(this);

                this.particleManager.update(this);
            }
        }

        this.guiManager.update(this);
    }

    @Override
    public void mousePressed(int button, int x, int y){
        this.interactionManager.onMouseAction(this, button);
    }

    @Override
    public void keyPressed(int key, char c){
        if(this.guiManager.getGui() == null){
            if(key == this.settings.keyMenu.key){
                this.openIngameMenu();
                return;
            }
            else if(key == Input.KEY_F1){
                this.isDebug = !this.isDebug;
                return;
            }
            else if(key == Input.KEY_F2){
                this.isLightDebug = !this.isLightDebug;
                return;
            }
            else if(key == Input.KEY_F3){
                this.isForegroundDebug = !this.isForegroundDebug;
                return;
            }
            else if(key == Input.KEY_F4){
                this.isBackgroundDebug = !this.isBackgroundDebug;
                return;
            }
            else if(key == this.settings.keyInventory.key){
                this.player.openGuiContainer(new GuiInventory(this.player), this.player.inventoryContainer);
                return;
            }
            else if(key == this.settings.keyChat.key && NetHandler.isActive()){
                this.guiManager.openGui(new GuiChat());
                return;
            }
        }

        this.interactionManager.onKeyboardAction(this, key, c);
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException{
        this.fpsAccumulator++;

        if(this.isInWorld()){
            this.worldRenderer.render(this, this.assetManager, this.particleManager, g, this.world, this.player, this.interactionManager);

            if(this.isDebug){
                DebugRenderer.render(this, this.assetManager, this.world, this.player, container, g);
            }
        }

        g.setLineWidth(this.settings.guiScale);
        this.guiManager.render(this, this.assetManager, g, this.player);
    }

    public boolean isInWorld(){
        return this.world != null;
    }

    public void startWorld(File worldFile, WorldInfo info){
        Log.info("Starting world with file "+worldFile);

        NameToIndexInfo tileRegInfo = new NameToIndexInfo("tile_reg_world", new File(worldFile, "name_to_index_info.dat"), Short.MAX_VALUE);
        this.dataManager.loadPropSettings(tileRegInfo);

        tileRegInfo.populate(RockBottomAPI.TILE_REGISTRY);

        if(tileRegInfo.needsSave()){
            this.dataManager.savePropSettings(tileRegInfo);
        }

        this.world = new World(info, tileRegInfo);
        this.world.initFiles(worldFile);

        if(info.seed == 0){
            info.seed = Util.RANDOM.nextLong();
        }

        this.player = this.world.createPlayer(this.uniqueId, null);
        this.world.addEntity(this.player);

        this.guiManager.reInitSelf(this);
        this.guiManager.closeGui();

        Log.info("Successfully started world with file "+worldFile);
    }

    public void joinWorld(DataSet playerSet, WorldInfo info, NameToIndexInfo tileRegInfo){
        Log.info("Joining world");

        this.world = new ClientWorld(info, tileRegInfo);

        this.player = this.world.createPlayer(this.uniqueId, null);
        this.player.load(playerSet);
        this.world.addEntity(this.player);

        this.guiManager.reInitSelf(this);
        this.guiManager.closeGui();

        Log.info("Successfully joined world");
    }

    public void quitWorld(){
        Log.info("Quitting current world");

        if(NetHandler.isClient()){
            Log.info("Sending disconnection packet");
            NetHandler.sendToServer(new PacketDisconnect(this.player.getUniqueId()));
        }

        NetHandler.shutdown();

        this.world = null;
        this.player = null;

        this.guiManager.reInitSelf(this);
        this.guiManager.openGui(new GuiMainMenu());

        Log.info("Successfully quit current world");
    }

    public void openIngameMenu(){
        this.guiManager.openGui(new GuiMenu());

        if(!this.world.isClient()){
            this.world.save();
        }
    }

    public void scheduleAction(IAction action){
        synchronized(this.scheduledActions){
            this.scheduledActions.add(action);
        }
    }

    public Container getContainer(){
        return this.container;
    }

    public double getWidthInWorld(){
        return (double)this.container.getWidth()/(double)this.settings.renderScale;
    }

    public double getHeightInWorld(){
        return (double)this.container.getHeight()/(double)this.settings.renderScale;
    }

    public double getWidthInGui(){
        return (double)this.container.getWidth()/(double)this.settings.guiScale;
    }

    public double getHeightInGui(){
        return (double)this.container.getHeight()/(double)this.settings.guiScale;
    }

    public float getMouseInGuiX(){
        return (float)this.container.getInput().getMouseX()/(float)this.settings.guiScale;
    }

    public float getMouseInGuiY(){
        return (float)this.container.getInput().getMouseY()/(float)this.settings.guiScale;
    }
}