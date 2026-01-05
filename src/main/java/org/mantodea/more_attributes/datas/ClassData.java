package org.mantodea.more_attributes.datas;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassData extends ConditionalContent {
    public ClassData() {
        for (var attribute : AttributeLoader.Attributes) {
            attributes.put(attribute.name, 0);
        }
    }

    public String name = "";

    public Map<String, Integer> attributes = new HashMap<>();

    public List<JsonObject> startItemsRecord = new ArrayList<>();
    public List<ItemStack> startItems = new ArrayList<>();

    public static JsonDeserializer<ClassData> deserializer = (json, typeOfT, context) -> {
        var jsonObject = json.getAsJsonObject();
        var res = new ClassData();
        if (jsonObject.has("name")) {
            res.name = jsonObject.get("name").getAsString();
        }
        if (jsonObject.has("attributes")) {
            JsonObject attrObject = jsonObject.getAsJsonObject("attributes");
            for (Map.Entry<String, JsonElement> entry : attrObject.entrySet()) {
                res.attributes.put(entry.getKey(), entry.getValue().getAsInt());
            }
        }
        if (jsonObject.has("startItems")) {
            JsonArray itemsArray = jsonObject.getAsJsonArray("startItems");
            for (JsonElement itemElement : itemsArray) {
                res.startItemsRecord.add(itemElement.getAsJsonObject());
            }
        }
        return res;
    };

    public static JsonSerializer<ClassData> serializer = (src, typeOfSrc, context) -> {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", src.name);

        if (src.attributes != null && !src.attributes.isEmpty()) {
            JsonObject attrObject = new JsonObject();
            for (Map.Entry<String, Integer> entry : src.attributes.entrySet()) {
                attrObject.addProperty(entry.getKey(), entry.getValue());
            }
            jsonObject.add("attributes", attrObject);
        }

        if (src.startItemsRecord != null && !src.startItemsRecord.isEmpty()) {
            JsonArray itemsArray = new JsonArray();
            for (var item : src.startItemsRecord) {
                itemsArray.add(item);
            }
            jsonObject.add("startItems", itemsArray);
        }

        return jsonObject;
    };

    public void convert_item() {
        if (!startItems.isEmpty())
            return;
        for (var item_data : startItemsRecord) {
            if (!ForgeRegistries.ITEMS.containsKey(new ResourceLocation(GsonHelper.getAsString(item_data, "item"))))
                continue;
            startItems.add(CraftingHelper.getItemStack(item_data.getAsJsonObject(), true));
        }
    }
}
