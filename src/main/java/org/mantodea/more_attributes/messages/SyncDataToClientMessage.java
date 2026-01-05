package org.mantodea.more_attributes.messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.mantodea.more_attributes.datas.*;
import org.mantodea.more_attributes.datas.ClassLoader;
import org.mantodea.more_attributes.ui.SelectClassScreen;
import org.mantodea.more_attributes.utils.AttributeUtils;
import org.mantodea.more_attributes.utils.ClassUtils;
import org.mantodea.more_attributes.utils.ModifierUtils;
import org.mantodea.more_attributes.utils.SlotUtils;

import java.util.List;
import java.util.function.Supplier;

public record SyncDataToClientMessage(JsonArray data) {

    public static boolean hasSync = false;

    public SyncDataToClientMessage(FriendlyByteBuf buf) {
        this(JsonParser.parseString(buf.readUtf()).getAsJsonArray());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(data.toString());
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(this::handle);
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        Minecraft minecraft = Minecraft.getInstance();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ClassData.class, ClassData.deserializer)
                .create();

        Player player = minecraft.player;

        ClassLoader.Classes = gson.fromJson(data.get(1), new TypeToken<List<ClassData>>() {}.getType());
        AttributeLoader.Attributes = gson.fromJson(data.get(2), new TypeToken<List<AttributeData>>() {}.getType());
        DetailLoader.Details = gson.fromJson(data.get(3), new TypeToken<List<DetailData>>() {}.getType());
        ItemModifierLoader.Modifiers = gson.fromJson(data.get(4), new TypeToken<List<ItemModifierData>>() {}.getType());

        for (var data : DetailLoader.Details) {
            if (!data.mod.equals("more_attributes"))
            {
                var attribute = ForgeRegistries.ATTRIBUTES.getEntries().stream().filter(
                        entry -> {
                            ResourceLocation location = entry.getKey().location();
                            return location.getNamespace().equals(data.mod) && location.getPath().contains(data.name);
                        }
                ).findFirst().orElse(null);

                if (attribute == null)
                    continue;

                AttributeUtils.OtherModDetailAttributes.put(data.mod + ":" + data.name, attribute.getValue());
            }
        }

        SlotUtils.getSlots();

        ModifierUtils.DetailModifiers.initialize();

        if (player != null) {
            ClassUtils.setPlayerClass(player, gson.fromJson(data.get(0), ClassData.class));
        }

        hasSync = true;
    }
}
