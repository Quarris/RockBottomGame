package de.ellpeck.rockbottom.net.packet.toclient;

import de.ellpeck.rockbottom.RockBottom;
import de.ellpeck.rockbottom.data.set.DataSet;
import de.ellpeck.rockbottom.net.NetUtil;
import de.ellpeck.rockbottom.net.packet.IPacket;
import de.ellpeck.rockbottom.world.World.WorldInfo;
import de.ellpeck.rockbottom.world.entity.player.EntityPlayer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.newdawn.slick.util.Log;

import java.io.IOException;

public class PacketInitialServerData implements IPacket{

    private final DataSet playerSet = new DataSet();
    private WorldInfo info;

    public PacketInitialServerData(EntityPlayer player, WorldInfo info){
        player.save(this.playerSet);
        this.info = info;
    }

    public PacketInitialServerData(){

    }

    @Override
    public void toBuffer(ByteBuf buf) throws IOException{
        NetUtil.writeSetToBuffer(this.playerSet, buf);
        this.info.toBuffer(buf);
    }

    @Override
    public void fromBuffer(ByteBuf buf) throws IOException{
        NetUtil.readSetFromBuffer(this.playerSet, buf);

        this.info = new WorldInfo(null);
        this.info.fromBuffer(buf);
    }

    @Override
    public void handle(RockBottom game, ChannelHandlerContext context){
        game.scheduleAction(() -> {
            if(game.world == null){
                Log.info("Received initial server data, joining world");
                game.joinWorld(this.playerSet, this.info);
            }
            else{
                Log.warn("Received initial server data while already being in a world!");
            }

            return true;
        });
    }
}