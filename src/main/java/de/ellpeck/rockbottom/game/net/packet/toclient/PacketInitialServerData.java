package de.ellpeck.rockbottom.game.net.packet.toclient;

import de.ellpeck.rockbottom.api.world.WorldInfo;
import de.ellpeck.rockbottom.game.RockBottom;
import de.ellpeck.rockbottom.game.data.set.DataSet;
import de.ellpeck.rockbottom.api.util.reg.NameToIndexInfo;
import de.ellpeck.rockbottom.game.world.entity.player.EntityPlayer;
import de.ellpeck.rockbottom.game.net.NetUtil;
import de.ellpeck.rockbottom.game.net.packet.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.newdawn.slick.util.Log;

import java.io.IOException;

public class PacketInitialServerData implements IPacket{

    private final DataSet playerSet = new DataSet();
    private WorldInfo info;
    private NameToIndexInfo tileRegInfo;

    public PacketInitialServerData(EntityPlayer player, WorldInfo info, NameToIndexInfo tileRegInfo){
        player.save(this.playerSet);
        this.info = info;
        this.tileRegInfo = tileRegInfo;
    }

    public PacketInitialServerData(){

    }

    @Override
    public void toBuffer(ByteBuf buf) throws IOException{
        NetUtil.writeSetToBuffer(this.playerSet, buf);
        this.info.toBuffer(buf);
        this.tileRegInfo.toBuffer(buf);
    }

    @Override
    public void fromBuffer(ByteBuf buf) throws IOException{
        NetUtil.readSetFromBuffer(this.playerSet, buf);

        this.info = new WorldInfo(null);
        this.info.fromBuffer(buf);

        this.tileRegInfo = new NameToIndexInfo("tile_reg_client_world", null, Short.MAX_VALUE);
        this.tileRegInfo.fromBuffer(buf);
    }

    @Override
    public void handle(RockBottom game, ChannelHandlerContext context){
        game.scheduleAction(() -> {
            if(game.world == null){
                Log.info("Received initial server data, joining world");
                game.joinWorld(this.playerSet, this.info, this.tileRegInfo);
            }
            else{
                Log.warn("Received initial server data while already being in a world!");
            }

            return true;
        });
    }
}