package org.lab406.com.impl;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

import java.util.Arrays;

public class PacketUtils {
    private static final int MAC_ADDRESS_SIZE = 6;

    /**
     * start position of destination MAC address in array.
     */
    private static final int DST_MAC_START_POSITION = 0;

    /**
     * end position of destination MAC address in array.
     */
    private static final int DST_MAC_END_POSITION = 6;

    /**
     * start position of source MAC address in array.
     */
    private static final int SRC_MAC_START_POSITION = 6;

    /**
     * end position of source MAC address in array.
     */
    private static final int SRC_MAC_END_POSITION = 12;

    /**
     * start position of ethernet type in array.
     */
    private static final int ETHER_TYPE_START_POSITION = 12;

    /**
     * end position of ethernet type in array.
     */
    private static final int ETHER_TYPE_END_POSITION = 14;

    private PacketUtils() {
        //prohibite to instantiate this class
    }
    public static byte[] extractDstMac(final byte[] packet){
        return Arrays.copyOfRange(packet, DST_MAC_START_POSITION, DST_MAC_END_POSITION);
    }
    public static byte[] extractSrcMac(final byte[] packet){
        return Arrays.copyOfRange(packet, SRC_MAC_START_POSITION, SRC_MAC_END_POSITION);

    }
    public static byte[] extractMacType(final byte[] packet){
        return Arrays.copyOfRange(packet, ETHER_TYPE_START_POSITION, ETHER_TYPE_END_POSITION);
    }
    public static MacAddress rawMacToMac(final byte[] rawMac){
        MacAddress macAdd=null;
        if (rawMac != null && rawMac.length == MAC_ADDRESS_SIZE) {
            StringBuilder sb = new StringBuilder();
            for (byte octet : rawMac) {
                sb.append(String.format(":%02X", octet));
            }
            macAdd = new MacAddress(sb.substring(1));
        }
        return macAdd;
    }
    public static String rawMacToString(final byte[] rawMac){
        String  macAdd=null;
        if (rawMac != null && rawMac.length == MAC_ADDRESS_SIZE) {
            StringBuilder sb = new StringBuilder();
            for (byte octet : rawMac) {
                sb.append(String.format(":%02X", octet));
            }
            macAdd = sb.substring(1);
        }
        return macAdd;
    }
}
