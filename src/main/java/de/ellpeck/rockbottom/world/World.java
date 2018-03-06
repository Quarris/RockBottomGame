package de.ellpeck.rockbottom.world;

import de.ellpeck.rockbottom.api.Constants;
import de.ellpeck.rockbottom.api.GameContent;
import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.data.set.DataSet;
import de.ellpeck.rockbottom.api.data.set.ModBasedDataSet;
import de.ellpeck.rockbottom.api.entity.Entity;
import de.ellpeck.rockbottom.api.entity.player.AbstractEntityPlayer;
import de.ellpeck.rockbottom.api.event.EventResult;
import de.ellpeck.rockbottom.api.event.impl.PlayerJoinWorldEvent;
import de.ellpeck.rockbottom.api.event.impl.WorldSaveEvent;
import de.ellpeck.rockbottom.api.event.impl.WorldTickEvent;
import de.ellpeck.rockbottom.api.net.chat.component.ChatComponentTranslation;
import de.ellpeck.rockbottom.api.render.IPlayerDesign;
import de.ellpeck.rockbottom.api.tile.Tile;
import de.ellpeck.rockbottom.api.tile.entity.TileEntity;
import de.ellpeck.rockbottom.api.tile.state.TileState;
import de.ellpeck.rockbottom.api.toast.Toast;
import de.ellpeck.rockbottom.api.util.*;
import de.ellpeck.rockbottom.api.util.reg.IResourceName;
import de.ellpeck.rockbottom.api.util.reg.NameToIndexInfo;
import de.ellpeck.rockbottom.api.world.DynamicRegistryInfo;
import de.ellpeck.rockbottom.api.world.IChunk;
import de.ellpeck.rockbottom.api.world.IWorld;
import de.ellpeck.rockbottom.api.world.WorldInfo;
import de.ellpeck.rockbottom.api.world.gen.IWorldGenerator;
import de.ellpeck.rockbottom.api.world.gen.biome.Biome;
import de.ellpeck.rockbottom.api.world.layer.TileLayer;
import de.ellpeck.rockbottom.init.AbstractGame;
import de.ellpeck.rockbottom.net.packet.toclient.PacketEntityChange;
import de.ellpeck.rockbottom.net.packet.toclient.PacketParticles;
import de.ellpeck.rockbottom.net.packet.toclient.PacketSound;
import de.ellpeck.rockbottom.net.packet.toclient.PacketTime;
import de.ellpeck.rockbottom.net.server.ConnectedPlayer;
import de.ellpeck.rockbottom.world.entity.player.EntityPlayer;
import io.netty.channel.Channel;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

public class World implements IWorld{

    public final List<IChunk> loadedChunks = new ArrayList<>();
    public final List<AbstractEntityPlayer> players = new ArrayList<>();
    protected final Map<Pos2, IChunk> chunkLookup = new HashMap<>();
    protected final WorldInfo info;
    private final DynamicRegistryInfo regInfo;
    private final List<IWorldGenerator> generators;
    private final List<IWorldGenerator> retroactiveGenerators;
    protected final List<AbstractEntityPlayer> playersUnmodifiable;
    protected File directory;
    protected File chunksDirectory;
    protected File playerDirectory;
    protected File additionalDataFile;
    protected int saveTicksCounter;
    private ModBasedDataSet additionalData;

    public World(WorldInfo info, DynamicRegistryInfo regInfo){
        this.info = info;
        this.regInfo = regInfo;

        List<IWorldGenerator> generators = new ArrayList<>();
        List<IWorldGenerator> retroactiveGenerators = new ArrayList<>();

        for(Class<? extends IWorldGenerator> genClass : RockBottomAPI.WORLD_GENERATORS.getUnmodifiable().values()){
            try{
                IWorldGenerator generator = genClass.getConstructor().newInstance();
                generator.initWorld(this);

                if(generator.generatesRetroactively()){
                    retroactiveGenerators.add(generator);
                }
                generators.add(generator);
            }
            catch(Exception e){
                RockBottomAPI.logger().log(Level.WARNING, "Couldn't initialize world generator with class "+genClass, e);
            }
        }

        Comparator comp = Comparator.comparingInt(IWorldGenerator:: getPriority).reversed();
        generators.sort(comp);
        retroactiveGenerators.sort(comp);

        this.generators = Collections.unmodifiableList(generators);
        this.retroactiveGenerators = Collections.unmodifiableList(retroactiveGenerators);

        RockBottomAPI.logger().info("Added a total of "+this.generators.size()+" generators to world ("+(this.retroactiveGenerators.size()+" of which can generate retroactively)"));

        this.playersUnmodifiable = Collections.unmodifiableList(this.players);
    }

    public void initFiles(File worldDirectory){
        this.directory = worldDirectory;
        this.chunksDirectory = new File(worldDirectory, "chunks");
        this.playerDirectory = new File(worldDirectory, "players");

        this.additionalDataFile = new File(worldDirectory, "additional_data.dat");
        if(this.additionalDataFile.exists()){
            this.additionalData = new ModBasedDataSet();
            this.additionalData.read(this.additionalDataFile);
        }
    }

    protected void checkListSync(){
        if(this.loadedChunks.size() != this.chunkLookup.size()){
            throw new IllegalStateException("LoadedChunks and ChunkLookup are out of sync!");
        }
    }

    protected void updateChunks(IGameInstance game){
        for(int i = 0; i < this.loadedChunks.size(); i++){
            IChunk chunk = this.loadedChunks.get(i);
            chunk.update(game);

            if(chunk.shouldUnload()){
                this.unloadChunk(chunk);
                i--;
            }
        }
    }

    public void update(AbstractGame game){
        this.checkListSync();

        if(RockBottomAPI.getEventHandler().fireEvent(new WorldTickEvent(this)) != EventResult.CANCELLED){
            this.updateChunks(game);

            this.info.totalTimeInWorld++;

            this.info.currentWorldTime++;
            if(this.info.currentWorldTime >= Constants.TIME_PER_DAY){
                this.info.currentWorldTime = 0;
            }

            if(this.isServer() && this.info.totalTimeInWorld%80 == 0){
                RockBottomAPI.getNet().sendToAllPlayers(this, new PacketTime(this.info.currentWorldTime, this.info.totalTimeInWorld));
            }

            this.saveTicksCounter++;
            if(this.saveTicksCounter >= game.getAutosaveInterval()*Constants.TARGET_TPS){
                this.saveTicksCounter = 0;

                this.save();
            }
        }
    }

    @Override
    public void addEntity(Entity entity){
        IChunk chunk = this.getChunk(entity.x, entity.y);
        chunk.addEntity(entity);

        if(entity instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer)entity;

            if(this.getPlayer(player.getUniqueId()) == null){
                this.players.add(player);
            }
            else{
                RockBottomAPI.logger().warning("Tried adding player "+player.getName()+" with id "+player.getUniqueId()+" to world that already contained it!");
            }
        }

        if(this.isServer()){
            RockBottomAPI.getNet().sendToAllPlayersWithLoadedPosExcept(this, new PacketEntityChange(entity, false), entity.x, entity.y, entity);
        }
    }

    @Override
    public void addTileEntity(TileEntity tile){
        IChunk chunk = this.getChunk(tile.x, tile.y);
        chunk.addTileEntity(tile);
    }

    @Override
    public void removeEntity(Entity entity){
        IChunk chunk = this.getChunk(entity.x, entity.y);
        this.removeEntity(entity, chunk);
    }

    @Override
    public void removeTileEntity(TileLayer layer, int x, int y){
        IChunk chunk = this.getChunk(x, y);
        chunk.removeTileEntity(layer, x, y);
    }

    @Override
    public TileEntity getTileEntity(TileLayer layer, int x, int y){
        IChunk chunk = this.getChunk(x, y);
        return chunk.getTileEntity(layer, x, y);
    }

    @Override
    public TileEntity getTileEntity(int x, int y){
        return this.getTileEntity(TileLayer.MAIN, x, y);
    }

    @Override
    public <T extends TileEntity> T getTileEntity(TileLayer layer, int x, int y, Class<T> tileClass){
        IChunk chunk = this.getChunk(x, y);
        return chunk.getTileEntity(layer, x, y, tileClass);
    }

    @Override
    public <T extends TileEntity> T getTileEntity(int x, int y, Class<T> tileClass){
        return this.getTileEntity(TileLayer.MAIN, x, y, tileClass);
    }

    @Override
    public void reevaluateTickBehavior(TileEntity tile){
        IChunk chunk = this.getChunk(tile.x, tile.y);
        chunk.reevaluateTickBehavior(tile);
    }

    @Override
    public List<Entity> getAllEntities(){
        List<Entity> entities = new ArrayList<>();
        for(IChunk chunk : this.loadedChunks){
            entities.addAll(chunk.getAllEntities());
        }
        return entities;
    }

    @Override
    public List<TileEntity> getAllTileEntities(){
        List<TileEntity> tiles = new ArrayList<>();
        for(IChunk chunk : this.loadedChunks){
            tiles.addAll(chunk.getAllTileEntities());
        }
        return tiles;
    }

    @Override
    public List<TileEntity> getAllTickingTileEntities(){
        List<TileEntity> tiles = new ArrayList<>();
        for(IChunk chunk : this.loadedChunks){
            tiles.addAll(chunk.getAllTickingTileEntities());
        }
        return tiles;
    }

    @Override
    public Entity getEntity(UUID id){
        for(IChunk chunk : this.loadedChunks){
            Entity entity = chunk.getEntity(id);
            if(entity != null){
                return entity;
            }
        }
        return null;
    }

    @Override
    public List<Entity> getEntities(BoundBox area){
        return this.getEntities(area, null, null);
    }

    @Override
    public List<Entity> getEntities(BoundBox area, Predicate<Entity> test){
        return this.getEntities(area, null, test);
    }

    @Override
    public <T extends Entity> List<T> getEntities(BoundBox area, Class<T> type){
        return this.getEntities(area, type, null);
    }

    @Override
    public <T extends Entity> List<T> getEntities(BoundBox area, Class<T> type, Predicate<T> test){
        int minChunkX = Util.toGridPos(area.getMinX()-Constants.CHUNK_SIZE/2);
        int minChunkY = Util.toGridPos(area.getMinY()-Constants.CHUNK_SIZE/2);
        int maxChunkX = Util.toGridPos(area.getMaxX()+Constants.CHUNK_SIZE/2);
        int maxChunkY = Util.toGridPos(area.getMaxY()+Constants.CHUNK_SIZE/2);

        List<T> entities = new ArrayList<>();
        for(int x = minChunkX; x <= maxChunkX; x++){
            for(int y = minChunkY; y <= maxChunkY; y++){
                IChunk chunk = this.getChunkFromGridCoords(x, y);
                entities.addAll(chunk.getEntities(area, type, test));
            }
        }
        return entities;
    }

    @Override
    public int getIdForState(TileState state){
        IResourceName name = RockBottomAPI.TILE_STATE_REGISTRY.getId(state);
        if(name != null){
            return this.getTileRegInfo().getId(name);
        }
        else{
            return -1;
        }
    }

    @Override
    public TileState getStateForId(int id){
        IResourceName name = this.getTileRegInfo().get(id);
        return RockBottomAPI.TILE_STATE_REGISTRY.get(name);
    }

    @Override
    public byte getCombinedLight(int x, int y){
        IChunk chunk = this.getChunk(x, y);
        return chunk.getCombinedLight(x, y);
    }

    @Override
    public byte getCombinedVisualLight(int x, int y){
        byte light = this.getCombinedLight(x, y);

        if(!this.isDedicatedServer()){
            AbstractEntityPlayer player = RockBottomAPI.getGame().getPlayer();
            double dist = Util.distanceSq(x+0.5D, y, player.x, player.y);
            if(dist <= 20D){
                byte newLight = (byte)(0.35D*(20D-dist));
                if(light < newLight){
                    light = newLight;
                }
            }
        }

        return light;
    }

    @Override
    public boolean isStoryMode(){
        return this.info.storyMode;
    }

    @Override
    public byte getSkyLight(int x, int y){
        IChunk chunk = this.getChunk(x, y);
        return chunk.getSkyLight(x, y);
    }

    @Override
    public byte getArtificialLight(int x, int y){
        IChunk chunk = this.getChunk(x, y);
        return chunk.getArtificialLight(x, y);
    }

    @Override
    public void setSkyLight(int x, int y, byte light){
        IChunk chunk = this.getChunk(x, y);
        chunk.setSkyLight(x, y, light);
    }

    @Override
    public void setArtificialLight(int x, int y, byte light){
        IChunk chunk = this.getChunk(x, y);
        chunk.setArtificialLight(x, y, light);
    }

    @Override
    public void scheduleUpdate(int x, int y, TileLayer layer, int scheduledMeta, int time){
        IChunk chunk = this.getChunk(x, y);
        chunk.scheduleUpdate(x, y, layer, scheduledMeta, time);
    }

    @Override
    public boolean isChunkLoaded(int x, int y){
        return this.chunkLookup.containsKey(new Pos2(x, y));
    }

    @Override
    public boolean isPosLoaded(int x, int y){
        return this.isChunkLoaded(Util.toGridPos(x), Util.toGridPos(y));
    }

    @Override
    public void scheduleUpdate(int x, int y, TileLayer layer, int time){
        this.scheduleUpdate(x, y, layer, 0, time);
    }

    @Override
    public void setDirty(int x, int y){
        IChunk chunk = this.getChunk(x, y);
        chunk.setDirty(x, y);
    }

    @Override
    public int getLowestAirUpwards(TileLayer layer, int x, int y){
        return this.getLowestAirUpwards(layer, x, y, false);
    }

    @Override
    public int getLowestAirUpwards(TileLayer layer, int x, int y, boolean ignoreReplaceableTiles){
        IChunk chunk = this.getChunk(x, y);
        return chunk.getLowestAirUpwards(layer, x, y, ignoreReplaceableTiles);
    }

    @Override
    public Biome getBiome(int x, int y){
        IChunk chunk = this.getChunk(x, y);
        return chunk.getBiome(x, y);
    }

    @Override
    public void setBiome(int x, int y, Biome biome){
        IChunk chunk = this.getChunk(x, y);
        chunk.setBiome(x, y, biome);
    }

    @Override
    public boolean isClient(){
        return RockBottomAPI.getNet().isClient();
    }

    @Override
    public boolean isServer(){
        return RockBottomAPI.getNet().isServer();
    }

    @Override
    public boolean isDedicatedServer(){
        return RockBottomAPI.getGame().isDedicatedServer();
    }

    @Override
    public boolean isLocalPlayer(Entity entity){
        return RockBottomAPI.getNet().isThePlayer(entity);
    }

    @Override
    public void callRetroactiveGeneration(){
        for(IChunk chunk : this.loadedChunks){
            chunk.callRetroactiveGeneration();
        }
    }

    @Override
    public long getSeed(){
        return this.info.seed;
    }

    @Override
    public WorldInfo getWorldInfo(){
        return this.info;
    }

    @Override
    public NameToIndexInfo getTileRegInfo(){
        return this.regInfo.getTiles();
    }

    @Override
    public int getIdForBiome(Biome biome){
        IResourceName name = RockBottomAPI.BIOME_REGISTRY.getId(biome);
        if(name != null){
            return this.getBiomeRegInfo().getId(name);
        }
        else{
            return -1;
        }
    }

    @Override
    public Biome getBiomeForId(int id){
        IResourceName name = this.getBiomeRegInfo().get(id);
        return RockBottomAPI.BIOME_REGISTRY.get(name);
    }

    @Override
    public NameToIndexInfo getBiomeRegInfo(){
        return this.regInfo.getBiomes();
    }

    @Override
    public DynamicRegistryInfo getRegInfo(){
        return this.regInfo;
    }

    @Override
    public int getCurrentTime(){
        return this.info.currentWorldTime;
    }

    @Override
    public int getTotalTime(){
        return this.info.totalTimeInWorld;
    }

    @Override
    public void setCurrentTime(int time){
        this.info.currentWorldTime = time;
    }

    @Override
    public void setTotalTime(int time){
        this.info.totalTimeInWorld = time;
    }

    @Override
    public IChunk getChunk(double x, double y){
        return this.getChunkFromGridCoords(Util.toGridPos(x), Util.toGridPos(y));
    }

    @Override
    public IChunk getChunkFromGridCoords(int gridX, int gridY){
        IChunk chunk = this.chunkLookup.get(new Pos2(gridX, gridY));

        if(chunk == null){
            chunk = this.loadChunk(gridX, gridY);
        }

        return chunk;
    }

    protected Chunk loadChunk(int gridX, int gridY){
        Chunk chunk = new Chunk(this, gridX, gridY);
        this.loadedChunks.add(chunk);
        this.chunkLookup.put(new Pos2(gridX, gridY), chunk);

        DataSet set = new DataSet();
        set.read(new File(this.chunksDirectory, "c_"+gridX+"_"+gridY+".dat"));
        chunk.loadOrCreate(set);

        return chunk;
    }

    @Override
    public void unloadChunk(IChunk chunk){
        this.saveChunk(chunk);

        this.loadedChunks.remove(chunk);
        this.chunkLookup.remove(new Pos2(chunk.getGridX(), chunk.getGridY()));
    }

    @Override
    public TileState getState(int x, int y){
        return this.getState(TileLayer.MAIN, x, y);
    }

    @Override
    public TileState getState(TileLayer layer, int x, int y){
        IChunk chunk = this.getChunk(x, y);
        return chunk.getState(layer, x, y);
    }

    @Override
    public void setState(int x, int y, TileState tile){
        this.setState(TileLayer.MAIN, x, y, tile);
    }

    @Override
    public void setState(TileLayer layer, int x, int y, TileState tile){
        IChunk chunk = this.getChunk(x, y);
        chunk.setState(layer, x, y, tile);
    }

    @Override
    public void notifyNeighborsOfChange(int x, int y, TileLayer layer){
        for(Direction direction : Direction.ADJACENT_INCLUDING_NONE){
            int offX = x+direction.x;
            int offY = y+direction.y;

            if(this.isPosLoaded(offX, offY)){
                for(TileLayer other : TileLayer.getAllLayers()){
                    if(direction != Direction.NONE || layer != other){
                        this.getState(other, offX, offY).getTile().onChangeAround(this, offX, offY, other, x, y, layer);
                    }
                }
            }
        }
    }

    @Override
    public void save(){
        long timeStarted = Util.getTimeMillis();
        int amount = 0;

        RockBottomAPI.getEventHandler().fireEvent(new WorldSaveEvent(this, RockBottomAPI.getGame().getDataManager()));

        for(IChunk chunk : this.loadedChunks){
            if(this.saveChunk(chunk)){
                amount++;
            }
        }

        this.info.save();

        for(AbstractEntityPlayer player : this.players){
            this.savePlayer(player);
        }

        if(this.additionalData != null){
            this.additionalData.write(this.additionalDataFile);
        }

        if(amount > 0){
            long time = Util.getTimeMillis()-timeStarted;
            RockBottomAPI.logger().info("Saved "+amount+" chunks, took "+time+"ms.");

            if(!this.isDedicatedServer()){
                RockBottomAPI.getGame().getToaster().displayToast(new Toast(RockBottomAPI.createInternalRes("gui.save_world"), new ChatComponentTranslation(RockBottomAPI.createInternalRes("info.saved")), new ChatComponentTranslation(RockBottomAPI.createInternalRes("info.saved_chunks"), String.valueOf(amount), String.valueOf((float)time/1000F)), 160));
            }
        }
    }

    @Override
    public List<AbstractEntityPlayer> getAllPlayers(){
        return this.playersUnmodifiable;
    }

    @Override
    public void removeEntity(Entity entity, IChunk chunk){
        chunk.removeEntity(entity);

        if(entity instanceof EntityPlayer){
            this.players.remove(entity);
        }

        entity.onRemoveFromWorld();

        if(this.isServer()){
            RockBottomAPI.getNet().sendToAllPlayersWithLoadedPosExcept(this, new PacketEntityChange(entity, true), chunk.getX(), chunk.getY(), entity);
        }
    }

    @Override
    public boolean isDaytime(){
        float light = this.getSkylightModifier(true);
        return light >= 0.7F;
    }

    @Override
    public boolean isNighttime(){
        return !this.isDaytime();
    }

    @Override
    public File getFolder(){
        return this.directory;
    }

    @Override
    public File getPlayerFolder(){
        return this.playerDirectory;
    }

    @Override
    public File getChunksFolder(){
        return this.chunksDirectory;
    }

    @Override
    public String getName(){
        return this.directory.getName();
    }

    @Override
    public void playSound(AbstractEntityPlayer player, IResourceName name, double x, double y, double z, float pitch, float volume){
        if(this.isLocalPlayer(player)){
            RockBottomAPI.getGame().getAssetManager().getSound(name).playAt(pitch, volume, x, y, z);
        }
        else{
            player.sendPacket(new PacketSound(name, x, y, z, pitch, volume));
        }
    }

    @Override
    public void broadcastSound(AbstractEntityPlayer player, IResourceName name, float pitch, float volume){
        if(this.isLocalPlayer(player)){
            RockBottomAPI.getGame().getAssetManager().getSound(name).play(pitch, volume);
        }
        else{
            player.sendPacket(new PacketSound(name, pitch, volume));
        }
    }

    @Override
    public void playSound(IResourceName name, double x, double y, double z, float pitch, float volume, AbstractEntityPlayer except){
        if(this.isServer()){
            RockBottomAPI.getNet().sendToAllPlayersWithLoadedPosExcept(this, new PacketSound(name, x, y, z, pitch, volume), x, y, except);
        }

        if(!this.isDedicatedServer() && !this.isLocalPlayer(except)){
            RockBottomAPI.getGame().getAssetManager().getSound(name).playAt(pitch, volume, x, y, z);
        }
    }

    @Override
    public void broadcastSound(IResourceName name, float pitch, float volume, AbstractEntityPlayer except){
        if(this.isServer()){
            RockBottomAPI.getNet().sendToAllPlayersExcept(this, new PacketSound(name, pitch, volume), except);
        }

        if(!this.isDedicatedServer() && !this.isLocalPlayer(except)){
            RockBottomAPI.getGame().getAssetManager().getSound(name).play(pitch, volume);
        }
    }

    @Override
    public void playSound(IResourceName name, double x, double y, double z, float pitch, float volume){
        this.playSound(name, x, y, z, pitch, volume, null);
    }

    @Override
    public void broadcastSound(IResourceName name, float pitch, float volume){
        this.broadcastSound(name, pitch, volume, null);
    }

    @Override
    public void savePlayer(AbstractEntityPlayer player){
        DataSet playerSet = new DataSet();
        player.save(playerSet);

        playerSet.write(new File(this.playerDirectory, player.getUniqueId().toString()+".dat"));
    }

    @Override
    public List<IWorldGenerator> getSortedGenerators(){
        return this.generators;
    }

    @Override
    public List<IWorldGenerator> getSortedRetroactiveGenerators(){
        return this.retroactiveGenerators;
    }

    @Override
    public EntityPlayer createPlayer(UUID id, IPlayerDesign design, Channel channel){
        EntityPlayer player = channel != null ? new ConnectedPlayer(this, id, design, channel) : new EntityPlayer(this, id, design);

        File file = new File(this.playerDirectory, id+".dat");
        if(file.exists()){
            DataSet set = new DataSet();
            set.read(file);

            player.load(set);
            RockBottomAPI.logger().info("Loading player "+design.getName()+" with unique id "+id+"!");
        }
        else{
            player.resetAndSpawn(RockBottomAPI.getGame());
            RockBottomAPI.logger().info("Adding new player "+design.getName()+" with unique id "+id+" to world!");
        }

        RockBottomAPI.getEventHandler().fireEvent(new PlayerJoinWorldEvent(player, channel != null));

        return player;
    }

    @Override
    public AbstractEntityPlayer getPlayer(UUID id){
        for(AbstractEntityPlayer player : this.players){
            if(id.equals(player.getUniqueId())){
                return player;
            }
        }
        return null;
    }

    @Override
    public AbstractEntityPlayer getPlayer(String name){
        for(AbstractEntityPlayer player : this.players){
            if(name.equals(player.getName())){
                return player;
            }
        }
        return null;
    }

    protected boolean saveChunk(IChunk chunk){
        if(chunk.needsSave()){
            DataSet set = new DataSet();
            chunk.save(set);

            set.write(new File(this.chunksDirectory, "c_"+chunk.getGridX()+"_"+chunk.getGridY()+".dat"));
            return true;
        }
        return false;
    }

    @Override
    public void destroyTile(int x, int y, TileLayer layer, Entity destroyer, boolean shouldDrop){
        TileState state = this.getState(layer, x, y);

        state.getTile().onDestroyed(this, x, y, destroyer, layer, shouldDrop);

        if(this.isServer()){
            RockBottomAPI.getNet().sendToAllPlayersWithLoadedPos(this, PacketParticles.tile(this, x, y, state), x, y);
        }

        if(!this.isDedicatedServer()){
            RockBottomAPI.getGame().getParticleManager().addTileParticles(this, x, y, state);
        }

        IResourceName sound = state.getTile().getBreakSound(this, x, y, layer, destroyer);
        if(sound != null){
            this.playSound(sound, x+0.5, y+0.5, layer.index(), 1F, 1F);
        }

        this.setState(layer, x, y, GameContent.TILE_AIR.getDefState());
    }

    @Override
    public int getSpawnX(){
        return 0;
    }

    @Override
    public void causeLightUpdate(int x, int y){
        Counter recurseCount = new Counter(0);

        try{
            this.causeLightUpdate(x, y, recurseCount);

            if(recurseCount.get() >= 100){
                RockBottomAPI.logger().config("Updated light at "+x+", "+y+" using "+recurseCount.get()+" recursive calls");
            }
        }
        catch(StackOverflowError e){
            RockBottomAPI.logger().severe("Failed to update light at "+x+" "+y+" after too many ("+recurseCount.get()+") recursive calls");
        }
    }

    private void causeLightUpdate(int x, int y, Counter recurseCount){
        for(Direction direction : Direction.SURROUNDING_INCLUDING_NONE){
            int dirX = x+direction.x;
            int dirY = y+direction.y;

            if(this.isPosLoaded(dirX, dirY)){
                boolean change = false;

                byte skylightThere = this.getSkyLight(dirX, dirY);
                byte calcedSkylight = this.calcLight(dirX, dirY, true);
                if(calcedSkylight != skylightThere){
                    this.setSkyLight(dirX, dirY, calcedSkylight);
                    change = true;
                }

                byte artLightThere = this.getArtificialLight(dirX, dirY);
                byte calcedArtLight = this.calcLight(dirX, dirY, false);
                if(calcedArtLight != artLightThere){
                    this.setArtificialLight(dirX, dirY, calcedArtLight);
                    change = true;
                }

                if(change){
                    this.causeLightUpdate(dirX, dirY, recurseCount.add(1));
                }
            }
        }
    }

    public void calcInitialSkylight(int x1, int y1, int x2, int y2){
        for(int x = x2; x >= x1; x--){
            for(int y = y2; y >= y1; y--){
                byte light = this.calcLight(x, y, true);
                this.setSkyLight(x, y, light);
            }
        }

        for(int x = x1; x <= x2; x++){
            for(int y = y1; y <= y2; y++){
                byte light = this.calcLight(x, y, true);
                this.setSkyLight(x, y, light);
            }
        }
    }

    private byte calcLight(int x, int y, boolean isSky){
        byte maxLight = 0;

        for(Direction direction : Direction.SURROUNDING){
            int dirX = x+direction.x;
            int dirY = y+direction.y;

            if(this.isPosLoaded(dirX, dirY)){
                byte light = isSky ? this.getSkyLight(dirX, dirY) : this.getArtificialLight(dirX, dirY);
                if(light > maxLight){
                    maxLight = light;
                }
            }
        }

        maxLight *= this.getTileModifier(x, y, isSky);

        byte emitted = this.getTileLight(x, y, isSky);
        if(emitted > maxLight){
            maxLight = emitted;
        }

        return (byte)Math.min(Constants.MAX_LIGHT, maxLight);
    }

    private byte getTileLight(int x, int y, boolean isSky){
        int highestLight = 0;
        boolean nonAir = false;

        for(TileLayer layer : TileLayer.getAllLayers()){
            Tile tile = this.getState(layer, x, y).getTile();
            if(!tile.isAir()){
                int light = tile.getLight(this, x, y, layer);
                if(light > highestLight){
                    highestLight = light;
                }

                nonAir = true;
            }
        }

        if(nonAir){
            if(!isSky){
                return (byte)highestLight;
            }
        }
        else if(isSky){
            return Constants.MAX_LIGHT;
        }
        return 0;
    }

    private float getTileModifier(int x, int y, boolean isSky){
        float smallestMod = 1F;
        boolean nonAir = false;

        for(TileLayer layer : TileLayer.getAllLayers()){
            Tile tile = this.getState(layer, x, y).getTile();
            if(!tile.isAir()){
                float mod = tile.getTranslucentModifier(this, x, y, layer, isSky);
                if(mod < smallestMod){
                    smallestMod = mod;
                }

                nonAir = true;
            }
        }

        if(nonAir){
            return smallestMod;
        }
        else{
            return isSky ? 1F : 0.8F;
        }
    }

    public float getSkylightModifier(boolean doMinMax){
        float mod = ((float)Math.sin(2*Math.PI*((double)this.info.currentWorldTime/(double)Constants.TIME_PER_DAY))+1F)/2F;

        if(doMinMax){
            return Math.min(1F, mod+0.15F);
        }
        else{
            return mod;
        }
    }

    @Override
    public boolean hasAdditionalData(){
        return this.additionalData != null;
    }

    @Override
    public ModBasedDataSet getAdditionalData(){
        return this.additionalData;
    }

    @Override
    public void setAdditionalData(ModBasedDataSet set){
        this.additionalData = set;
    }

    @Override
    public ModBasedDataSet getOrCreateAdditionalData(){
        if(this.additionalData == null){
            this.additionalData = new ModBasedDataSet();
        }
        return this.additionalData;
    }
}
