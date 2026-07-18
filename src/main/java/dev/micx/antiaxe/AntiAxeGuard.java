package dev.micx.antiaxe;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Local-input guard invoked before vanilla can choose an interaction action. */
public final class AntiAxeGuard {

    private static volatile long armedUntil;
    private static volatile AxisAlignedBB claimBox;
    private static volatile Object claimWorld;

    private AntiAxeGuard() { }

    public static void arm(long now) {
        armedUntil = now + AntiAxeRules.CLAIM_WINDOW_MS;
        claimBox = null;
        claimWorld = null;
    }

    public static void disarm() {
        armedUntil = 0L;
        claimBox = null;
        claimWorld = null;
    }

    public static boolean isArmed() {
        long now = System.currentTimeMillis();
        if (!AntiAxeRules.isArmed(now, armedUntil)) {
            if (armedUntil > 0L) disarm();
            return false;
        }
        return true;
    }

    public static long remainingMs() {
        return isArmed() ? Math.max(0L, armedUntil - System.currentTimeMillis()) : 0L;
    }

    /** Injected at the head of Minecraft.rightClickMouse(); true means local no-op. */
    public static boolean shouldBlockRightClick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!AntiAxeAaContext.isInAA(mc)) {
            if (armedUntil > 0L) disarm();
            return false;
        }
        return isArmed() && isLookingAtClaimZone(mc);
    }

    public static boolean isLookingAtClaimZone(Minecraft mc) {
        if (mc == null || mc.thePlayer == null || mc.theWorld == null) return false;
        AxisAlignedBB box = claimZone(mc);
        if (box == null) return false;
        Vec3 eye = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 look = mc.thePlayer.getLook(1.0f);
        double reach = mc.playerController == null
            ? 5.0
            : Math.max(5.0, mc.playerController.getBlockReachDistance());
        Vec3 end = eye.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        return AntiAxeRules.intersects(eye.xCoord, eye.yCoord, eye.zCoord,
            end.xCoord, end.yCoord, end.zCoord,
            box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    private static AxisAlignedBB claimZone(Minecraft mc) {
        if (claimWorld == mc.theWorld && claimBox != null) return claimBox;

        List<EntityArmorStand> markers = new ArrayList<EntityArmorStand>();
        try {
            for (Entity entity : new ArrayList<Entity>(mc.theWorld.loadedEntityList)) {
                if (!(entity instanceof EntityArmorStand) || entity.isDead) continue;
                String name = EnumChatFormatting.getTextWithoutFormattingCodes(entity.getCustomNameTag());
                if (name == null) continue;
                String upper = name.toUpperCase(java.util.Locale.ROOT);
                if (upper.contains("LUCKY CHEST") || upper.contains("RIGHT-CLICK TO CLAIM")
                        || upper.contains("THE PUNCHER")) {
                    markers.add((EntityArmorStand) entity);
                }
            }
        } catch (Throwable ignored) { }

        TileEntityChest bestChest = null;
        double bestScore = Double.POSITIVE_INFINITY;
        try {
            for (TileEntity tile : new ArrayList<TileEntity>(mc.theWorld.loadedTileEntityList)) {
                if (!(tile instanceof TileEntityChest)) continue;
                TileEntityChest chest = (TileEntityChest) tile;
                double px = chest.getPos().getX() + 0.5;
                double py = chest.getPos().getY() + 0.5;
                double pz = chest.getPos().getZ() + 0.5;
                double playerScore = squared(px - mc.thePlayer.posX)
                    + squared(py - mc.thePlayer.posY)
                    + squared(pz - mc.thePlayer.posZ);
                if (playerScore > 100.0) continue;
                double markerScore = markers.isEmpty() ? playerScore : Double.POSITIVE_INFINITY;
                for (EntityArmorStand marker : markers) {
                    markerScore = Math.min(markerScore,
                        squared(px - marker.posX) + squared(py - marker.posY)
                            + squared(pz - marker.posZ));
                }
                if (markerScore < bestScore && markerScore <= 49.0) {
                    bestScore = markerScore;
                    bestChest = chest;
                }
            }
        } catch (Throwable ignored) { }

        AxisAlignedBB box = null;
        if (bestChest != null) {
            double x = bestChest.getPos().getX();
            double y = bestChest.getPos().getY();
            double z = bestChest.getPos().getZ();
            box = new AxisAlignedBB(x - 1.35, y - 0.75, z - 1.35,
                                    x + 2.35, y + 4.75, z + 2.35);
        } else if (!markers.isEmpty()) {
            EntityArmorStand nearest = markers.get(0);
            double nearestSq = mc.thePlayer.getDistanceSqToEntity(nearest);
            for (EntityArmorStand marker : markers) {
                double distanceSq = mc.thePlayer.getDistanceSqToEntity(marker);
                if (distanceSq < nearestSq) {
                    nearest = marker;
                    nearestSq = distanceSq;
                }
            }
            box = new AxisAlignedBB(nearest.posX - 1.85, nearest.posY - 4.0, nearest.posZ - 1.85,
                                    nearest.posX + 1.85, nearest.posY + 2.0, nearest.posZ + 1.85);
        }
        claimWorld = mc.theWorld;
        claimBox = box;
        return box;
    }

    private static double squared(double value) {
        return value * value;
    }
}
