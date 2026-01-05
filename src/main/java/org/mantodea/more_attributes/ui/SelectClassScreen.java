package org.mantodea.more_attributes.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.mantodea.more_attributes.datas.AttributeData;
import org.mantodea.more_attributes.datas.ClassData;
import org.mantodea.more_attributes.datas.ClassLoader;
import org.mantodea.more_attributes.messages.AttributesChannel;
import org.mantodea.more_attributes.messages.SyncClassToServerMessage;
import org.mantodea.more_attributes.utils.*;

import java.util.List;
import java.util.Map;

public class SelectClassScreen extends Screen {

    public int page;

    public int posX;

    public int posY;

    public int backgroundWidth = 400;

    public int backgroundHeight = 200;

    public Coordinates coordinates;

    public ResourceLocation classIcon;

    public MutableComponent className;

    public List<MutableComponent> descriptions;

    public ClassData classData;

    public Style sung = Style.EMPTY.withFont(new ResourceLocation("more_attributes:chusung"));

    public SelectClassScreen() {
        super(Component.empty());

        super.minecraft = Minecraft.getInstance();

        page = 0;
    }

    @Override
    public void init()
    {
        if (minecraft != null)
        {
            posX = (super.width - backgroundWidth) / 2;

            posY = (super.height - backgroundHeight) /2;

            coordinates = new Coordinates(posX, posY, backgroundWidth, backgroundHeight, font);

            updateClass();

            updateButtons();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);

        renderClassIcon(graphics);

        renderClassName(graphics);

        renderClassDescription(graphics);

        renderItems(graphics, mouseX, mouseY);

        renderLevelAttributes(graphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics) {
        super.renderBackground(graphics);

        graphics.blit(ResourceLocationUtils.GUI.Background, posX, posY, 0, 0F, 0F, backgroundWidth, backgroundHeight, backgroundWidth, backgroundHeight);
        graphics.blit(ResourceLocationUtils.GUI.ClassIconBox, coordinates.classIconBoxPosX, coordinates.classIconBoxPosY, 0, 0, 130, 87, 130, 87);
    }

    public void renderClassIcon(GuiGraphics graphics)
    {
        graphics.blit(classIcon, coordinates.classIconBoxPosX, coordinates.classIconBoxPosY, 0F, 0F, 130, 81, 130, 81);
    }

    public void renderClassName(GuiGraphics graphics)
    {
        int lineHeight = font.lineHeight;

        int classNamePosX = posX + 83 + (60 - font.width(className.getVisualOrderText())) / 2;
        int classNamePosY = posY + 92 + (15 - lineHeight) / 2;

        RenderUtils.renderBorderScaleText(graphics, font, className, classNamePosX, classNamePosY, 0x603B38, 0xF0DCB7, 1.5f);
    }

    public void renderClassDescription(GuiGraphics graphics)
    {
        int descriptionPosX = coordinates.descriptionPosX;

        int descriptionPosY = coordinates.descriptionPosY;

        for(MutableComponent description : descriptions)
        {
            RenderUtils.renderBorderScaleText(graphics, font, description, descriptionPosX, descriptionPosY, 0x603B38, 0xF0DCB7, 1);
            descriptionPosY += font.lineHeight;
        }
    }

    public void renderItems(GuiGraphics graphics, int mouseX, int mouseY) {
        int lineHeight = font.lineHeight;
        int lineWidth = 22;
        int itemSize = 24;
        int x = coordinates.startItemPosX;
        int y = coordinates.startItemPosY + 10;
        int spacing = 0;
        int lineSpacing = 0;
        int quantityBreak = 8; //每循环多少次物品时换行

        for (int i = 0; i < classData.startItems.size(); i++)
        {
            var entry = classData.startItems.get(i);
            Item item = entry.getItem();

            if (item == null) continue;

            ItemStack stack = new ItemStack(item, entry.getCount());

            if (stack.isEmpty()) continue;

            graphics.renderItem(stack, x + spacing, y + lineSpacing);
            graphics.renderItemDecorations(font, stack, x + spacing, y + lineSpacing);

            if (mouseX >= x + spacing && mouseX <= x + spacing + itemSize && mouseY >= y + lineSpacing && mouseY <= y + lineSpacing + itemSize) {
                TooltipFlag tooltipFlag = minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
                graphics.renderComponentTooltip(font, stack.getTooltipLines(null, tooltipFlag), mouseX, mouseY);
            }

            spacing += lineWidth;

            if ((i + 1) % quantityBreak == 0){
                spacing = 0;
                lineSpacing += (int) (lineHeight * 2.5);
            }
        }
    }

    public void renderLevelAttributes(GuiGraphics graphics, int mouseX, int mouseY) {
        int lineHeight = font.lineHeight;

        int attributePosX = coordinates.startAttributesPosX;

        int attributePosY = coordinates.startAttributesPosY;

        int attributeLevelPosX = attributePosX + 30;

        MutableComponent detail = Component.translatable("more_attributes.ui.detail").withStyle(sung);

        int detailPosX = attributePosX + 100;

        int detailWidth = font.width(detail.getVisualOrderText());

        for (Map.Entry<String, Integer> entry : classData.attributes.entrySet())
        {
            String name = entry.getKey();

            AttributeData data = AttributeUtils.getAttributeData(name);

            if(data == null || !ModUtils.checkCondition(data.displayCondition))
                continue;

            int level = entry.getValue();

            ResourceLocation icon = ResourceLocationUtils.GUI.getAttributeIcon(name);

            graphics.blit(icon, attributePosX - 16, attributePosY - 5, 0, 0f, 0f, 12, 12, 12, 12);

            RenderUtils.renderBorderScaleText(graphics, font, Component.translatable(LangUtils.getAttributeNameKey(name)).withStyle(sung), attributePosX, attributePosY, 0x603B38, 0xF0DCB7, 1);

            RenderUtils.renderBorderScaleText(graphics, font, Component.literal(String.valueOf(level)).withStyle(sung), attributeLevelPosX, attributePosY, 0x603B38, 0xF0DCB7, 1);

            RenderUtils.renderBorderScaleText(graphics, font, detail, detailPosX, attributePosY, 0x603B38, 0xF0DCB7, 1);

            if(mouseX >= detailPosX && mouseX <= detailWidth + detailPosX && mouseY >= attributePosY && mouseY <= attributePosY + lineHeight)
            {
                List<Component> details = AttributeUtils.getAttributeDetails(data, level);

                graphics.renderComponentTooltip(font, details, mouseX, mouseY);
            }

            attributePosY += lineHeight + 4;
        }
    }

    public void updateClass()
    {
        classData = ClassLoader.Classes.size() > page ? ClassLoader.Classes.get(page) : new ClassData();

        className = Component.translatable(LangUtils.getClassNameKey(classData.name)).withStyle(sung);

        classIcon = ResourceLocationUtils.GUI.getClassIcon(classData.name);

        descriptions = ClassUtils.getClassDescription(classData.name, font);
    }

    public void updateButtons() {
        ImageButton selectButton = new ImageButton(coordinates.selectButtonPosX, coordinates.selectButtonPosY, 60, 15,
            Component.translatable("more_attributes.ui.select"),
            ResourceLocationUtils.GUI.Select,
            ResourceLocationUtils.GUI.SelectHover
        );
        selectButton.setPress(b -> selectClass());
        addRenderableWidget(selectButton);

        if (page > 0)
        {
            ImageButton previous = new ImageButton(coordinates.previousPagePosX, coordinates.turnPagePosY, 17, 24,
                Component.empty(),
                ResourceLocationUtils.GUI.PreviousPage,
                ResourceLocationUtils.GUI.PreviousPageHover
            );
            previous.setPress(b -> previousPage());
            addRenderableWidget(previous);
        }

        if (page < ClassLoader.Classes.size() - 1)
        {
            ImageButton next = new ImageButton(coordinates.nextPagePosX, coordinates.turnPagePosY, 17, 24,
                    Component.empty(),
                    ResourceLocationUtils.GUI.NextPage,
                    ResourceLocationUtils.GUI.NextPageHover
            );
            next.setPress(b -> nextPage());
            addRenderableWidget(next);
        }
    }

    public void selectClass()
    {
        AttributesChannel.sendToServer(new SyncClassToServerMessage(classData));

        if (minecraft != null) {
            if (minecraft.player != null) {
                ClassUtils.setPlayerClass(minecraft.player, classData);
            }

            minecraft.setScreen(null);
        }
    }

    public void previousPage()
    {
        if(page > 0)
        {
            page--;
            clearWidgets();

            updateClass();

            updateButtons();
        }
    }

    public void nextPage()
    {
        if(page < ClassLoader.Classes.size() - 1)
        {
            page++;
            clearWidgets();

            updateClass();

            updateButtons();
        }
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
//        return getMinecraft().player == null || ClassUtils.hasSelectClass(getMinecraft().player);
        return false;
    }
}
