package org.mantodea.more_attributes.messages;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.mantodea.more_attributes.datas.ClassData;
import org.mantodea.more_attributes.datas.ClassLoader;
import org.mantodea.more_attributes.utils.ClassUtils;
import org.mantodea.more_attributes.utils.ModifierUtils;

import java.util.function.Supplier;

public record SyncClassToServerMessage(ClassData data) {

    public SyncClassToServerMessage(FriendlyByteBuf buf) {
        this(getData(buf));
    }

    public static ClassData getData(FriendlyByteBuf buf) {
        var classData = new ClassData();

        classData.name = buf.readUtf();

        var attributeSize = buf.readInt();

        for (int i = 0; i < attributeSize; i++) {
            var attr = buf.readUtf();

            var level = buf.readInt();

            classData.attributes.put(attr, level);
        }

        var startItemSize = buf.readInt();
        for (int i = 0; i < startItemSize; i++) {
            JsonObject itemObj = new JsonObject();

            String id = buf.readUtf();
            itemObj.addProperty("item", id);

            int count = buf.readInt();
            itemObj.addProperty("count", count);

            boolean hasNbt = buf.readBoolean();
            if (hasNbt) {
                String nbtString = buf.readUtf();
                itemObj.addProperty("nbt", nbtString);
            }

            classData.startItemsRecord.add(itemObj);
        }

        return classData;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(data.name);

        buf.writeInt(data.attributes.size());

        for (var entry : data.attributes.entrySet()) {
            buf.writeUtf(entry.getKey());

            buf.writeInt(entry.getValue());
        }

        buf.writeInt(data.startItemsRecord.size());

        for (var item : data.startItemsRecord) {
            var id = GsonHelper.getAsString(item, "item");
            buf.writeUtf(id);
            int count = GsonHelper.getAsInt(item, "count", 1);
            buf.writeInt(count);
            buf.writeBoolean(item.has("nbt"));
            if (item.has("nbt")) {
                String nbtString = GsonHelper.getAsString(item, "nbt");
                buf.writeUtf(nbtString);
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            ClassLoader.convert_item();

            if (player == null || data == null) return;
            ClassUtils.setPlayerClass(player, data);

            for (var entry : data.startItems) {
                if (entry != null) {
                    ItemHandlerHelper.giveItemToPlayer(player, entry);
                }
            }
            player.swing(InteractionHand.MAIN_HAND);
        });

        ctx.setPacketHandled(true);
    }
}
