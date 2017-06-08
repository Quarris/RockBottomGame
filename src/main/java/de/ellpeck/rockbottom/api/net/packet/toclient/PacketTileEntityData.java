package de.ellpeck.rockbottom.api.net.packet.toclient;

import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.net.packet.IPacket;
import de.ellpeck.rockbottom.api.tile.entity.TileEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public class PacketTileEntityData implements IPacket{

    private final ByteBuf tileBuf = Unpooled.buffer();
    private int x;
    private int y;

    public PacketTileEntityData(int x, int y, TileEntity tile){
        this.x = x;
        this.y = y;
        tile.toBuf(this.tileBuf);
    }

    public PacketTileEntityData(){

    }

    @Override
    public void toBuffer(ByteBuf buf) throws IOException{
        buf.writeInt(this.x);
        buf.writeInt(this.y);

        buf.writeInt(this.tileBuf.readableBytes());
        buf.writeBytes(this.tileBuf);
    }

    @Override
    public void fromBuffer(ByteBuf buf) throws IOException{
        this.x = buf.readInt();
        this.y = buf.readInt();

        int readable = buf.readInt();
        buf.readBytes(this.tileBuf, readable);
    }

    @Override
    public void handle(IGameInstance game, ChannelHandlerContext context){
        game.scheduleAction(() -> {
            if(game.getWorld() != null){
                TileEntity tile = game.getWorld().getTileEntity(this.x, this.y);
                if(tile != null){
                    tile.fromBuf(this.tileBuf);
                }
                return true;
            }
            else{
                return false;
            }
        });
    }
}
