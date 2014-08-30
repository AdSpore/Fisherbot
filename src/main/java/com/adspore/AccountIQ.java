package com.adspore;

/**
 * Created by ddevine on 8/28/14.
 */

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.IQProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;

/**
 *      ACCOUNT IQ EXTENSION
 */
public class AccountIQ extends IQ implements PacketExtension {
    private static final Logger LOG = LoggerFactory.getLogger(AccountIQ.class);

    protected String username;
    protected String email;
    protected boolean visibility;
    protected Long objectid;


    /**
     * Default AccountIQ, variables to be set when parsed from incoming stream...
     * This is required, as when reading incoming AccountIQ packets, we don't know what the values of these are going
     * to be until we read them.
     */
    public AccountIQ() {
        username = "";
        email = "";
        visibility = false;
        objectid = null;
    }

    /**
     * Fully Configured AccountIQ,
     * @param username
     * @param email
     * @param visibility
     * @param objectId
     */
    public AccountIQ(String username, String email, boolean visibility, Long objectId) {
        this.username = username;
        this.email = email;
        this.visibility = visibility;
        objectid = objectId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public Long getObjectid() {
        return objectid;
    }

    public void setObjectid(Long objectid) {
        this.objectid = objectid;
    }


    @Override
    public CharSequence getChildElementXML() {
        StringBuilder sb = new StringBuilder();

        sb.append("<account xmlns=\"http://adspore.com/v1/lighthouse\">");
        if (null != username && !"".equals(username)) {
            sb.append("<username>");
            sb.append(username);
            sb.append("</username>");
        } else {
            sb.append("<username/>");
        }

        if (null != email && !"".equals(email)) {
            sb.append("<email>");
            sb.append(email);
            sb.append("</email>");
        } else {
            sb.append("</email>");
        }

        if (visibility) {
            sb.append("<visibility>true</visibility>");
        } else {
            sb.append("<visibility>false</visibility>");
        }

        if (null != objectid) {
            sb.append("<objectid>");
            sb.append(objectid.toString());
            sb.append("</objectid>");
        } else {
            sb.append("<objectid/>");
        }
        sb.append("</account>");
        return sb.toString();
    }

    @Override
    public String getNamespace() {
        return "http://adspore.com/v1/lighthouse";
    }

    @Override
    public String getElementName() {
        return "account";
    }

    public static class AccountIQProvider implements IQProvider {
        @Override
        public IQ parseIQ(XmlPullParser parser) throws Exception {
            LOG.info("Received call to ParseIQ:");

            String tagname = parser.getName();
            int event = parser.getEventType();

            if ("account".equals(tagname) && XmlPullParser.START_TAG == event) {
                AccountIQ result = new AccountIQ();

                parser.next();
                event = parser.getEventType();
                String elementName = parser.getName();

                while (!"account".equals(elementName) && XmlPullParser.END_TAG != event) {
                    switch (event ) {
                        case XmlPullParser.START_TAG:
                                if ("username".equals(elementName)) {
                                    result.setUsername(parser.nextText());
                                } else if ("email".equals(elementName)) {
                                    result.setEmail(parser.nextText());
                                } else if ("visibility".equals(elementName)) {
                                    result.setVisibility(Boolean.parseBoolean(parser.nextText()));
                                } else if ("objectid".equals(elementName)) {
                                    result.setObjectid(Long.parseLong(parser.nextText()));
                                }
                            break;
                    }
                    parser.next();
                    event = parser.getEventType();
                    elementName = parser.getName();
                }

                return result;
            }
            return null;
        }
    }
}
