package su.terrafirmagreg.core.client.screen.widget;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.LargeVesselScreen;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;

/**
 * Seal button class copied from {@link LargeVesselScreen} for use in any block entity.
 * Requires a block entity with a {@link TFCBlockStateProperties#SEALED} property to be functional.
 */
public class SealButton extends Button {

    public static final int SIZE = 20;
    private static final Component SEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.seal_barrel");
    private static final Component UNSEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.unseal_barrel");

    private final BlockEntity blockEntity;
    private final int buttonId;

    public SealButton(BlockEntity blockEntity, int guiLeft, int guiTop) {
        this(blockEntity, guiLeft, guiTop, 0);
    }

    public SealButton(BlockEntity blockEntity, int guiLeft, int guiTop, int buttonId) {
        super(guiLeft, guiTop, SIZE, SIZE, SEAL, b -> {
        }, RenderHelpers.NARRATION);
        this.blockEntity = blockEntity;
        this.buttonId = buttonId;

        boolean isSealed = blockEntity.getBlockState().getOptionalValue(TFCBlockStateProperties.SEALED).orElse(false);
        setTooltip(Tooltip.create(isSealed ? UNSEAL : SEAL));
    }

    @Override
    public void onPress() {
        PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(buttonId, null));
        playDownSound(Minecraft.getInstance().getSoundManager());
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        boolean isSealed = blockEntity.getBlockState().getOptionalValue(TFCBlockStateProperties.SEALED).orElse(false);
        setTooltip(Tooltip.create(isSealed ? UNSEAL : SEAL));

        final int v = isSealed ? 0 : SIZE;
        graphics.blit(LargeVesselScreen.BACKGROUND, getX(), getY(), 236, v, SIZE, SIZE, 256, 256);
    }
}
