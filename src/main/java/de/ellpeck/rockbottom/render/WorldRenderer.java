package de.ellpeck.rockbottom.render;

import de.ellpeck.rockbottom.RockBottom;
import de.ellpeck.rockbottom.api.Constants;
import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.assets.IAssetManager;
import de.ellpeck.rockbottom.api.entity.Entity;
import de.ellpeck.rockbottom.api.event.impl.WorldRenderEvent;
import de.ellpeck.rockbottom.api.render.entity.IEntityRenderer;
import de.ellpeck.rockbottom.api.render.tile.ITileRenderer;
import de.ellpeck.rockbottom.api.tile.Tile;
import de.ellpeck.rockbottom.api.world.IChunk;
import de.ellpeck.rockbottom.api.world.TileLayer;
import de.ellpeck.rockbottom.particle.ParticleManager;
import de.ellpeck.rockbottom.util.Util;
import de.ellpeck.rockbottom.world.World;
import de.ellpeck.rockbottom.world.entity.player.EntityPlayer;
import de.ellpeck.rockbottom.world.entity.player.InteractionManager;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WorldRenderer{

    public static final Color[] SKY_COLORS = new Color[50];
    public static final Color[] BACKGROUND_COLORS = new Color[Constants.MAX_LIGHT+1];
    public static final Color[] MAIN_COLORS = new Color[Constants.MAX_LIGHT+1];

    public static void init(){
        float step = 1F/(Constants.MAX_LIGHT+1);
        for(int i = 0; i <= Constants.MAX_LIGHT; i++){
            float modifier = step+i*step;

            MAIN_COLORS[i] = new Color(modifier, modifier, modifier, 1F);
            BACKGROUND_COLORS[i] = new Color(modifier*0.5F, modifier*0.5F, modifier*0.5F, 1F);
        }

        Color sky = new Color(0x4C8DFF);
        for(int i = 0; i < SKY_COLORS.length; i++){
            float percent = (float)i/(float)SKY_COLORS.length;
            SKY_COLORS[i] = sky.darker(1F-percent);
        }
    }

    public void render(IGameInstance game, IAssetManager manager, ParticleManager particles, Graphics g, World world, EntityPlayer player, InteractionManager input){
        int scale = game.getWorldScale();
        g.scale(scale, scale);

        int skyLight = (int)(world.getSkylightModifier()*(SKY_COLORS.length-1));
        g.setBackground(SKY_COLORS[game.isLightDebug() ? SKY_COLORS.length-1 : skyLight]);

        double width = game.getWidthInWorld();
        double height = game.getHeightInWorld();
        float transX = (float)(player.x-width/2);
        float transY = (float)(-player.y-height/2);

        List<Entity> entities = new ArrayList<>();

        int topLeftX = Util.toGridPos(transX);
        int topLeftY = Util.toGridPos(-transY+1);
        int bottomRightX = Util.toGridPos(transX+width);
        int bottomRightY = Util.toGridPos(-transY-height);

        int minX = Math.min(topLeftX, bottomRightX);
        int minY = Math.min(topLeftY, bottomRightY);
        int maxX = Math.max(topLeftX, bottomRightX);
        int maxY = Math.max(topLeftY, bottomRightY);

        for(int gridX = minX; gridX <= maxX; gridX++){
            for(int gridY = minY; gridY <= maxY; gridY++){
                if(world.isChunkLoaded(gridX, gridY)){
                    IChunk chunk = world.getChunkFromGridCoords(gridX, gridY);

                    for(int x = 0; x < Constants.CHUNK_SIZE; x++){
                        for(int y = 0; y < Constants.CHUNK_SIZE; y++){
                            int tileX = chunk.getX()+x;
                            int tileY = chunk.getY()+y;

                            if(tileX >= transX-1 && -tileY >= transY-1 && tileX < transX+width && -tileY < transY+height){
                                Tile tile = chunk.getTileInner(x, y);
                                byte light = chunk.getCombinedLightInner(x, y);

                                if(!game.isBackgroundDebug()){
                                    if(!tile.isFullTile() || game.isForegroundDebug()){
                                        Tile tileBack = chunk.getTileInner(TileLayer.BACKGROUND, x, y);
                                        ITileRenderer rendererBack = tileBack.getRenderer();
                                        if(rendererBack != null){
                                            rendererBack.render(game, manager, g, world, tileBack, tileX, tileY, tileX-transX, -tileY-transY, BACKGROUND_COLORS[game.isLightDebug() ? Constants.MAX_LIGHT : light]);

                                            if(input.breakingLayer == TileLayer.BACKGROUND){
                                                this.doBreakAnimation(input, manager, tileX, tileY, transX, transY);
                                            }
                                        }
                                    }
                                }

                                if(!game.isForegroundDebug()){
                                    ITileRenderer renderer = tile.getRenderer();
                                    if(renderer != null){
                                        renderer.render(game, manager, g, world, tile, tileX, tileY, tileX-transX, -tileY-transY, MAIN_COLORS[game.isLightDebug() ? Constants.MAX_LIGHT : light]);

                                        if(input.breakingLayer == TileLayer.MAIN){
                                            this.doBreakAnimation(input, manager, tileX, tileY, transX, transY);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    entities.addAll(chunk.getAllEntities());
                }
            }
        }

        particles.render(game, manager, g, world, transX, transY);

        entities.stream().sorted(Comparator.comparingInt(Entity:: getRenderPriority)).forEach(entity -> {
            if(entity.shouldRender()){
                IEntityRenderer renderer = entity.getRenderer();
                if(renderer != null){
                    int light = world.getCombinedLight(Util.floor(entity.x), Util.floor(entity.y));
                    renderer.render(game, manager, g, world, entity, (float)entity.x-transX, (float)-entity.y-transY+1F, MAIN_COLORS[game.isLightDebug() ? Constants.MAX_LIGHT : light]);
                }
            }
        });

        RockBottomAPI.getEventHandler().fireEvent(new WorldRenderEvent(game, manager, g, world, player));

        g.resetTransform();
    }

    private void doBreakAnimation(InteractionManager input, IAssetManager manager, int tileX, int tileY, float transX, float transY){
        if(input.breakProgress > 0){
            if(tileX == input.breakTileX && tileY == input.breakTileY){
                Image brk = manager.getImage(RockBottom.internalRes("break."+Util.ceil(input.breakProgress*8F)));
                brk.draw(tileX-transX, -tileY-transY, 1F, 1F);
            }
        }
    }
}
