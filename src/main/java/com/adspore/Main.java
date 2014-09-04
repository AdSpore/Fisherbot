package com.adspore;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static final String SERVICE = "lighthouse.seattle1.adspore.com";
    public static final String ADSPORE_NAMESPACE = "http://adspore.com/v1/lighthouse";
    public static int WANDER_STEP_COUNT = 20;


    public static void main(String[] args) {
        SmackConfiguration.DEBUG_ENABLED = true;

        if (null != args && args.length > 0) {
            WANDER_STEP_COUNT = Integer.parseInt(args[0]);
        }
        
        ConnectionHelper helper = new ConnectionHelper();

        Bot bot = Bot.createRandomBot(helper.mConnection);
        LOG.info("Created user: " + bot.getUsername());

        try {
            Thread.sleep(1000);

            bot.addLighthouseToRoster();
            LOG.info("Added Lighthouse to bot's roster...");

            Thread.sleep(5000);

            bot.sendAccount();
            LOG.info("Sent account info to lighthouse");

            Thread.sleep(3000);

        } catch (InterruptedException e) {
            LOG.error("Shouldn't be interrupted here...");
        }


        LOG.info("Setup complete, beginning location update sequence...");
        CountDownLatch finished = bot.startWalking();
        try {
            finished.await();
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOG.warn("Should not be getting interrupted");
        }



        try {
            bot.removeAccount();
            Thread.sleep(5000);
            helper.mConnection.disconnect();
        } catch (SmackException.NotConnectedException e) {
            LOG.error("NOT CONNECTED EXCEPTION", e);
        } catch (InterruptedException e) {}
    }


}
