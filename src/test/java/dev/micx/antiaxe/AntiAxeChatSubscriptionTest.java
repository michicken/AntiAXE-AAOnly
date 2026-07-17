package dev.micx.antiaxe;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;

public class AntiAxeChatSubscriptionTest {

    @Test
    public void receivesPuncherMessageEvenWhenAnotherModHidesIt() throws Exception {
        Method method = AntiAxeMod.class.getMethod("onChat", ClientChatReceivedEvent.class);
        SubscribeEvent subscription = method.getAnnotation(SubscribeEvent.class);
        assertTrue(subscription.receiveCanceled());
    }
}
