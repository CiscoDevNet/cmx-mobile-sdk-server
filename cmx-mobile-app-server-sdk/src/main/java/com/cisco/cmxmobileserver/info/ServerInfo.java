package com.cisco.cmxmobileserver.info;

public class ServerInfo {
    
    public String getStartupInfo() {
        StringBuffer serverInfo = new StringBuffer();
        serverInfo.append(Version.getInstance().getVersionNumber());
        return serverInfo.toString();
    }

}
