package org.lab406.com.impl;

public class OFPPort {
    public static final String OFPP_NORMAL = "0xfffffffa";/* Forward using non-OpenFlow pipeline. */
    public static final String OFPP_FLOOD = "0xfffffffb";/* Flood using non-OpenFlow pipeline. */
    public static final String OFPP_ALL = "0xfffffffc";/* All standard ports except input port. */
    public static final String OFPP_CONTROLLER = "0xfffffffd";/* Send to controller. */
    public static final String OFPP_LOCAL = "0xfffffffe";/* Local openflow "port". */
    public static final String OFPP_ANY = "0xffffffff";/* Special value used in some requests when
no port is specified (i.e. wildcarded). */
    public static final String OFPP_MAX = "0xffffff00";/* Reserved OpenFlow Port (fake output "ports"). */
    public static final String OFPP_UNSET = "0xfffffff7"; /* Output port not set in action-set.
used only in OXM_OF_ACTSET_OUTPUT. */
    public static final String OFPP_IN_PORT = "0xfffffff8"; /* Send the packet out the input port. This
reserved port must be explicitly used
in order to send back out of the input
port. */
    public static final String OFPP_TABLE = "0xfffffff9"; /* Submit the packet to the first flow table
NB: This destination port can only be
used in packet-out messages. */

}
