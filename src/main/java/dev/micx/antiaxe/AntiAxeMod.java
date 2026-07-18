package dev.micx.antiaxe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(
    modid = AntiAxeMod.MOD_ID,
    name = "AntiAXE For AA --MICx",
    version = AntiAxeMod.VERSION,
    clientSideOnly = true,
    acceptedMinecraftVersions = "[1.8.9]"
)
public final class AntiAxeMod {

    public static final String MOD_ID = "antiaxeaaonly";
    public static final String VERSION = "1.0.2";

    @Mod.EventHandler
    public void initialize(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type != 0 && event.type != 1) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (!AntiAxeAaContext.isInAA(mc)) return;
        String text = event.message == null ? null : event.message.getUnformattedText();
        AntiAxeRules.ChatAction action = AntiAxeRules.classifyChat(text);
        if (action == AntiAxeRules.ChatAction.ARM) {
            AntiAxeGuard.arm(System.currentTimeMillis());
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[AntiAXE] The Puncher 领取区右键已锁定 10 秒"));
            }
        } else if (action == AntiAxeRules.ChatAction.DISARM) {
            AntiAxeGuard.disarm();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world != null && event.world.isRemote) {
            AntiAxeGuard.disarm();
            AntiAxeAaContext.clear();
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !AntiAxeGuard.isArmed()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!AntiAxeAaContext.isInAA(mc)) return;
        boolean blocking = AntiAxeGuard.isLookingAtClaimZone(mc);
        String text = blocking ? "§c§lAntiAXE §f右键已锁" : "§6AntiAXE §fThe Puncher";
        text += String.format(java.util.Locale.ROOT, " §7%.1fs", AntiAxeGuard.remainingMs() / 1000.0);
        ScaledResolution resolution = new ScaledResolution(mc);
        float scale = blocking ? 1.35f : 1.0f;
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0f);
        float x = resolution.getScaledWidth() / (2.0f * scale)
            - mc.fontRendererObj.getStringWidth(text) / 2.0f;
        float y = (resolution.getScaledHeight() / 2.0f + 26.0f) / scale;
        mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFF);
        GlStateManager.popMatrix();
    }
}
