package com.adspore;

import com.vividsolutions.jts.geom.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.geotools.referencing.GeodeticCalculator;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ddevine on 8/18/14.
 */
public class Bot implements PacketListener {
    private static final Logger LOG = LoggerFactory.getLogger(Bot.class);

    public GeometryFactory mGeometryFactory = new GeometryFactory(new PrecisionModel(), 4326);


    /*
            INITIAL BOUNDRY AREA FOR THIS BOT...
     */
    public static final Coordinate[] BOUNDRY_POLYGON = {
            new Coordinate(-122.03595095748895, 47.52907124210668),
            new Coordinate(-122.03243189926141, 47.53170809451744),
            new Coordinate(-122.02502900238031, 47.5310416498341),
            new Coordinate(-122.02372008438104, 47.52850618505619),
            new Coordinate(-122.02706748123163, 47.52749196482462),
            new Coordinate(-122.02959948654168, 47.52718769493089),
            new Coordinate(-122.0320885765075, 47.527173205844264),
            new Coordinate(-122.03290396804803, 47.528955333464424),
            new Coordinate(-122.03595095748895, 47.52907124210668),
    };

    /**
     * JTS Polygon, created during bot construction from the above coordinates.
     */
    private Polygon mBoundry;

    /**
        Spawning coordinate of the bot, after this, it's on it's own...
     */
    private Coordinate spawnPosn = new Coordinate(-122.02947074050897, 47.52936101259178);


    /**
     * Bot's 'username' really just a mRandom string...
     */
    private final String mUsername;

    /**
     * Bot's login password, really just a mRandom string...
     */
    private final String mPassword;


    /**
     * Object ID returned from server, used for location updates and other commands.
     */
    private Long mObjectId;

    /**
     * Connection that this bot is associated with.  For testing, each bot should have it's
     * own...
     */
    private final XMPPTCPConnection mConnection;


    /**
     * Provides a mechanism for the bot to update it's position on the map
     */
    private LocationUpdateRunnable mRunnable;

    /**
     * Construct the calculator during bot creation
     */
    private GeodeticCalculator calculator = new GeodeticCalculator();


    private Bot(){
        mUsername = null;
        mPassword = null;
        mConnection = null;
        mBoundry = null;
    }

    private Bot(XMPPTCPConnection connection, String username, String password) {
        mUsername = username;
        mPassword = password;
        mConnection = connection;
        mBoundry = mGeometryFactory.createPolygon(BOUNDRY_POLYGON);
    }



    /**
     * Creates a Bot object on the built in connection with a mRandom name and password.
     * @return
     */
    public static Bot createRandomBot(XMPPTCPConnection connection) {
        StringBuilder sb = new StringBuilder();
        sb.append("bot_");

        String userSuffix = RandomStringUtils.randomAlphabetic(6);
        sb.append(userSuffix);

        Bot bot = new Bot(connection, sb.toString(), RandomStringUtils.randomAlphabetic(6) );
        AccountManager manager = AccountManager.getInstance(connection);

        try {
            manager.createAccount(bot.getUsername(), bot.getPassword());
            connection.login(bot.getUsername(), bot.getPassword());

        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }
        return bot;
    }


    public void addProviders() {
        ProviderManager.addIQProvider("account", "http://adspore.com/v1/lighthouse", new AccountIQ.AccountIQProvider());
    }

    public void addLighthouseToRoster() {
        Roster roster = mConnection.getRoster();
        try {
            roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
            roster.createEntry("lighthouse.seattle1.adspore.com", "lighthouse", null);
        } catch (SmackException.NotLoggedInException e) {
            LOG.error("Not logged in", e);
        } catch (SmackException.NoResponseException e) {
            LOG.error("No Response", e);
        } catch (XMPPException.XMPPErrorException e) {
            LOG.error("XMPP Error", e);
        } catch (SmackException.NotConnectedException e) {
            LOG.error("Not Connected", e);
        }
    }

    public void sendAccount() {
        addProviders();
        AccountIQ accountIQ = new AccountIQ(getUsername(), "foo@foo.bar", true, 0L);
        accountIQ.setTo(Main.SERVICE);
        accountIQ.setFrom(mConnection.getUser());
        accountIQ.setType(IQ.Type.set);
        LOG.info("Sending AccountInfo:" + accountIQ.toString());
        try {

            mConnection.addPacketListener(this, new AcceptEverythingPacketFilter());
            mConnection.sendPacket(accountIQ);
        } catch (SmackException.NotConnectedException e) {
            LOG.error("Caught exception", e);
        }
    }


    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;
    }

    public Long getObjectId() {
        return mObjectId;
    }


    public CountDownLatch startWalking() {
        mRunnable = new LocationUpdateRunnable(200);
        return mRunnable.getLatch();
    }


    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
        if (packet instanceof IQ) {
            LOG.info("Received IQ:" + packet.toString());
        }
    }

    private class AcceptEverythingPacketFilter implements PacketFilter {
        @Override
        public boolean accept(Packet packet) {
            LOG.info("Checking packet:" + packet.toString());
            return true;
        }
    }

    private class LocationUpdateRunnable implements Runnable {
        private CountDownLatch mSteps;
        private Point mCurrent;
        public double mAzimuth = 0.0;
        public Random mRandom = new Random();
        private Thread mThread;


        public LocationUpdateRunnable(int steps) {
            mSteps = new CountDownLatch(steps);
            mCurrent = mGeometryFactory.createPoint(spawnPosn);
            mThread = new Thread(this);
            mThread.start();
        }

        public CountDownLatch getLatch() {
            return mSteps;
        }

        @Override
        public void run() {

            while (mSteps.getCount() > 0) {
                mSteps.countDown();
                Point next = calculateNextPosition();
                LOG.info("Calculated next posn:" + next.toText());
                //  Send next point as message...
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    LOG.error("Shouldn't be geting interrupted...");
                }
            }
        }

        protected Point calculateNextPosition() {
            boolean increase = mRandom.nextBoolean();
            if (increase) {
                mAzimuth += 0.6;
            } else {
                mAzimuth -= 0.4;
            }


            calculator.setStartingGeographicPoint(mCurrent.getX(), mCurrent.getY());
            calculator.setDirection((mAzimuth %360), 10.0);
            Point2D pResult = calculator.getDestinationGeographicPoint();
            Point possibleDestination = mGeometryFactory.createPoint(new Coordinate(pResult.getX(), pResult.getY()));

            if (!mBoundry.contains(possibleDestination)) {
                Point center = mBoundry.getCentroid();
                calculator.setStartingGeographicPoint(possibleDestination.getX(), possibleDestination.getY());
                calculator.setDestinationGeographicPoint(center.getX(), center.getY());
                mAzimuth = calculator.getAzimuth();

                calculator.setStartingGeographicPoint(mCurrent.getX(), mCurrent.getY());
                calculator.setDirection(mAzimuth, 10.0);
                pResult = calculator.getDestinationGeographicPoint();

                possibleDestination = mGeometryFactory.createPoint(new Coordinate(pResult.getX(), pResult.getY()));

                LOG.debug("\t ...Ooops, wandered out of bounds, changing heading!");
            }
            return possibleDestination;
        }
    }

}
