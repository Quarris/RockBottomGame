package de.ellpeck.rockbottom.api.inventory;

import de.ellpeck.rockbottom.api.data.set.DataSet;
import de.ellpeck.rockbottom.api.item.ItemInstance;

import java.util.ArrayList;
import java.util.List;

public class Inventory implements IInventory{

    protected final List<IInvChangeCallback> callbacks = new ArrayList<>();
    protected final ItemInstance[] slots;

    public Inventory(int slotAmount){
        this.slots = new ItemInstance[slotAmount];
    }

    @Override
    public void set(int id, ItemInstance instance){
        this.slots[id] = instance;
        this.notifyChange(id);
    }

    @Override
    public ItemInstance add(int id, int amount){
        ItemInstance inst = this.slots[id];
        if(inst != null){
            inst.addAmount(amount);

            this.notifyChange(id);
            return inst;
        }
        else{
            return null;
        }
    }

    @Override
    public ItemInstance remove(int id, int amount){
        ItemInstance inst = this.slots[id];
        if(inst != null){
            inst.removeAmount(amount);

            if(inst.getAmount() <= 0){
                this.set(id, null);
                return null;
            }

            this.notifyChange(id);
            return inst;
        }
        else{
            return null;
        }
    }

    @Override
    public ItemInstance get(int id){
        return this.slots[id];
    }

    @Override
    public int getSlotAmount(){
        return this.slots.length;
    }

    @Override
    public void notifyChange(int slot){
        for(IInvChangeCallback callback : this.callbacks){
            callback.onChange(this, slot, this.get(slot));
        }
    }

    @Override
    public void addChangeCallback(IInvChangeCallback callback){
        if(!this.callbacks.contains(callback)){
            this.callbacks.add(callback);
        }
    }

    @Override
    public void removeChangeCallback(IInvChangeCallback callback){
        this.callbacks.remove(callback);
    }

    public ItemInstance add(ItemInstance instance, boolean simulate){
        ItemInstance copy = instance.copy();

        for(int i = 0; i < this.slots.length; i++){
            copy = this.addToSlot(i, copy, simulate);

            if(copy == null){
                return null;
            }
        }

        return copy;
    }

    public ItemInstance addExistingFirst(ItemInstance instance, boolean simulate){
        ItemInstance copy = instance.copy();

        for(int i = 0; i < 2; i++){
            for(int j = 0; j < this.slots.length; j++){
                if(i == 1 || (this.slots[j] != null && this.slots[j].isItemEqual(instance))){
                    copy = this.addToSlot(j, copy, simulate);

                    if(copy == null){
                        return null;
                    }
                }
            }
        }

        return copy;
    }

    public ItemInstance addToSlot(int slot, ItemInstance instance, boolean simulate){
        ItemInstance slotInst = this.slots[slot];

        if(slotInst == null){
            if(!simulate){
                this.set(slot, instance);
            }
            return null;
        }
        else if(slotInst.isItemEqual(instance)){
            int space = slotInst.getMaxAmount()-slotInst.getAmount();

            if(space >= instance.getAmount()){
                if(!simulate){
                    this.add(slot, instance.getAmount());
                }
                return null;
            }
            else{
                if(!simulate){
                    this.add(slot, space);

                    instance.removeAmount(space);
                    if(instance.getAmount() <= 0){
                        return null;
                    }
                }
            }
        }
        return instance;
    }

    public void save(DataSet set){
        for(int i = 0; i < this.slots.length; i++){
            ItemInstance slot = this.slots[i];

            if(slot != null){
                DataSet subset = new DataSet();
                slot.save(subset);
                set.addDataSet("item_"+i, subset);
            }
        }
    }

    public void load(DataSet set){
        for(int i = 0; i < this.slots.length; i++){
            DataSet subset = set.getDataSet("item_"+i);
            if(!subset.isEmpty()){
                this.slots[i] = ItemInstance.load(subset);
            }
            else{
                this.slots[i] = null;
            }
        }
    }
}
