package dev.micx.antiaxe;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertFalse;

public class PacketBoundaryTest {

    @Test
    public void runtimeClassesContainNoPacketOrSendQueueReferences() throws Exception {
        Class<?>[] classes = {AntiAxeGuard.class, AntiAxeMod.class};
        for (Class<?> type : classes) {
            String resource = "/" + type.getName().replace('.', '/') + ".class";
            InputStream input = type.getResourceAsStream(resource);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            String constants = new String(output.toByteArray(), StandardCharsets.ISO_8859_1);
            assertFalse(constants.contains("net/minecraft/network"));
            assertFalse(constants.contains("addToSendQueue"));
            assertFalse(constants.contains("sendQueue"));
            assertFalse(constants.contains("C02PacketUseEntity"));
            assertFalse(constants.contains("C08PacketPlayerBlockPlacement"));
        }
    }
}
