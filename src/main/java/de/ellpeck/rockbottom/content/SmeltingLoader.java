package de.ellpeck.rockbottom.content;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ellpeck.rockbottom.api.IGameInstance;
import de.ellpeck.rockbottom.api.RockBottomAPI;
import de.ellpeck.rockbottom.api.construction.IRecipe;
import de.ellpeck.rockbottom.api.construction.resource.IUseInfo;
import de.ellpeck.rockbottom.api.construction.resource.ItemUseInfo;
import de.ellpeck.rockbottom.api.construction.resource.ResUseInfo;
import de.ellpeck.rockbottom.api.construction.smelting.SmeltingRecipe;
import de.ellpeck.rockbottom.api.content.IContentLoader;
import de.ellpeck.rockbottom.api.content.pack.ContentPack;
import de.ellpeck.rockbottom.api.item.Item;
import de.ellpeck.rockbottom.api.item.ItemInstance;
import de.ellpeck.rockbottom.api.mod.IMod;
import de.ellpeck.rockbottom.api.util.Util;
import de.ellpeck.rockbottom.api.util.reg.IResourceName;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class SmeltingLoader implements IContentLoader<IRecipe>{

    private final Set<IResourceName> disabled = new HashSet<>();

    @Override
    public IResourceName getContentIdentifier(){
        return SmeltingRecipe.ID;
    }

    @Override
    public void loadContent(IGameInstance game, IResourceName resourceName, String path, JsonElement element, String elementName, IMod loadingMod, ContentPack pack) throws Exception{
        if(!this.disabled.contains(resourceName)){
            String resPath = path+element.getAsString();

            InputStreamReader reader = new InputStreamReader(ContentManager.getResourceAsStream(resPath), Charsets.UTF_8);
            JsonElement recipeElement = Util.JSON_PARSER.parse(reader);
            reader.close();

            JsonObject object = recipeElement.getAsJsonObject();
            int time = object.get("time").getAsInt();

            JsonObject out = object.get("output").getAsJsonObject();
            Item outItem = RockBottomAPI.ITEM_REGISTRY.get(RockBottomAPI.createRes(out.get("name").getAsString()));
            int outAmount = out.has("amount") ? out.get("amount").getAsInt() : 1;
            int outMeta = out.has("meta") ? out.get("meta").getAsInt() : 0;
            ItemInstance output = new ItemInstance(outItem, outAmount, outMeta);

            JsonObject in = object.get("input").getAsJsonObject();
            String name = in.get("name").getAsString();
            int amount = in.has("amount") ? in.get("amount").getAsInt() : 1;

            IUseInfo input;
            if(Util.isResourceName(name)){
                int meta = in.has("meta") ? in.get("meta").getAsInt() : 0;
                input = new ItemUseInfo(RockBottomAPI.ITEM_REGISTRY.get(RockBottomAPI.createRes(name)), amount, meta);
            }
            else{
                input = new ResUseInfo(name, amount);
            }

            if(IRecipe.forName(resourceName) != null){
                RockBottomAPI.logger().info("Smelting recipe with name "+resourceName+" already exists, not adding recipe for mod "+loadingMod.getDisplayName()+" with content pack "+pack.getName());
            }
            else{
                new SmeltingRecipe(resourceName, input, output, time).register();

                RockBottomAPI.logger().config("Loaded smelting recipe "+resourceName+" for mod "+loadingMod.getDisplayName()+" with time "+time+", input "+input+" and output "+output+" with content pack "+pack.getName());
            }
        }
        else{
            RockBottomAPI.logger().info("Smelting recipe "+resourceName+" will not be loaded for mod "+loadingMod.getDisplayName()+" with content pack "+pack.getName()+" because it was disabled by another content pack!");
        }
    }


    @Override
    public void disableContent(IGameInstance game, IResourceName resourceName){
        this.disabled.add(resourceName);
    }
}