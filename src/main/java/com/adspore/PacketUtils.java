package com.adspore;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.bytestreams.ibb.packet.Data;
import org.jivesoftware.smackx.bytestreams.ibb.packet.DataPacketExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by ddevine on 8/18/14.
 */
public final class PacketUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PacketUtils.class);



    public static void sendPresence(XMPPConnection connection, Bot bot) {
        Presence subRequest = new Presence(Presence.Type.subscribed);
        subRequest.setTo(Main.SERVICE);
        subRequest.setFrom(connection.getUser());
        LOG.info("Sending Presence:" + subRequest.toString());
        try {
            connection.sendPacket(subRequest);
        } catch (SmackException.NotConnectedException e) {
            LOG.error("Send Presence--Subscription request:"+ bot.getUsername(), e);
            e.printStackTrace();
        }
    }






    private static class LocalListener implements PacketListener {
        @Override
        public void processPacket(Packet packet) throws SmackException.NotConnectedException {
            LOG.debug("Got Response");
        }
    }



    public static void sendRegistration(XMPPConnection connection) {
        Registration registration = new Registration();
        registration.setTo("lighthouse.seattle1.adspore.com");
        registration.setFrom(connection.getUser());
        registration.setType(IQ.Type.set);
        HashMap<String,String> values = new HashMap<String,String>();
        values.put("name", "dilbert");
        values.put("email", "dilbert@foo.bar");
        registration.setAttributes(values);

        LOG.info("Sending Registration:" + registration.toString());

        try {
            connection.sendPacket(registration);
        } catch (SmackException.NotConnectedException e) {
            LOG.error("not connected", e);
        }
    }

}
