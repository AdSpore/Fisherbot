package com.adspore;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

/**
 * Created by ddevine on 8/31/14.
 */
public class LocationMessage implements PacketExtension {

    private final double latitude;
    private final double longitude;
    private final double accuracy;
    private final long objectid;

    public LocationMessage(Long objectid, double longitude, double latitude, double accuracy) {
        this.objectid = objectid;
        this.longitude = longitude;
        this.latitude = latitude;
        this.accuracy = accuracy;
    }

    @Override
    public String getNamespace() {
        return "http://adspore.com/v1/lighthouse";
    }

    @Override
    public String getElementName() {
        return "userlocation";
    }



    @Override
    public CharSequence toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<userlocation>");

        sb.append("<objectid>");
        sb.append(String.valueOf(objectid));
        sb.append("</objectid>");

        sb.append("<longitude>");
        sb.append(String.valueOf(longitude));
        sb.append("</longitude>");

        sb.append("<latitude>");
        sb.append(String.valueOf(latitude));
        sb.append("</latitude>");

        sb.append("<accuracy>");
        sb.append(String.valueOf(accuracy));
        sb.append("</accuracy>");

        sb.append("</userlocation>");
        return sb.toString();
    }
}
