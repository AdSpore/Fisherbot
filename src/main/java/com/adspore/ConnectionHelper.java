package com.adspore;

import org.apache.commons.lang3.RandomStringUtils;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ddevine on 8/18/14.
 */
public class ConnectionHelper {
    Logger LOG = LoggerFactory.getLogger(ConnectionHelper.class);
    XMPPTCPConnection mConnection;

    public ConnectionHelper() {
        ConnectionConfiguration config = new ConnectionConfiguration("seattle1.adspore.com", 5222);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        mConnection = new XMPPTCPConnection(config);
        try {
            mConnection.connect();
        } catch (XMPPException e) {
            LOG.error("XMPPException");
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("Ooops, looks like you can't connect");
            e.printStackTrace();
        } catch (SmackException e) {
            LOG.error("Ooops, smack exception");
            e.printStackTrace();
        }
    }





    /**
     * Attempts to delete the account of the provided Bot
     * @param bot
     */
    public void deleteBot(Bot bot) {
        AccountManager manager = AccountManager.getInstance(mConnection);
        try {
            manager.deleteAccount();
        } catch (XMPPException e) {
            LOG.error("Delete Bot:" + bot.getUsername(), e);
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            LOG.error("Delete Bot:" + bot.getUsername(), e);
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            LOG.error("Delete Bot:" + bot.getUsername(), e);
            e.printStackTrace();
        }
    }



}
