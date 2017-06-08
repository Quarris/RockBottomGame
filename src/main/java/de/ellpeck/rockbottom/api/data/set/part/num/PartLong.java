package de.ellpeck.rockbottom.api.data.set.part.num;

import de.ellpeck.rockbottom.api.data.set.part.BasicDataPart;

import java.io.DataInput;
import java.io.DataOutput;

public class PartLong extends BasicDataPart<Long>{

    public PartLong(String name){
        super(name);
    }

    public PartLong(String name, Long data){
        super(name, data);
    }

    @Override
    public void write(DataOutput stream) throws Exception{
        stream.writeLong(this.data);
    }

    @Override
    public void read(DataInput stream) throws Exception{
        this.data = stream.readLong();
    }
}
