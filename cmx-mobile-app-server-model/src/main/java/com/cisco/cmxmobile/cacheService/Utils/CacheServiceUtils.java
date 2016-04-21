package com.cisco.cmxmobile.cacheService.Utils;

public final class CacheServiceUtils 
{
    private CacheServiceUtils()
    {
    }
    
    public static String formatHexTo12Digits(String hexString)
    {
        String delimiter = ":";
        
        String[] octets = hexString.split(delimiter);
        if (octets.length != 6) {
            throw new IllegalArgumentException("Not a HEX String : " + hexString);
        }
        
        String formatedHex = ""; 
                
        for (int i = 0; i < octets.length; i++) {
            String octet = octets[i];
            
            int digitsInOctet = octet.length();

            if (digitsInOctet == 1) {
                formatedHex += "0" + octet;
            } else {
                formatedHex += octet;
            }
            
            if (i < octets.length - 1) {
                formatedHex += delimiter;
            }
        }
        
        return formatedHex;
    }
}
