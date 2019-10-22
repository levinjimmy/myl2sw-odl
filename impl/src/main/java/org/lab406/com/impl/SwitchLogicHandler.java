package org.lab406.com.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class SwitchLogicHandler implements PacketProcessingListener {
    private final static Logger LOG = LoggerFactory.getLogger(SwitchLogicHandler.class);
    private Map<String, Map<String, NodeConnectorRef>> forwardingMap;
    private PacketProcessingService packetProcessingService;
    private DataBroker dataBroker;

    public SwitchLogicHandler(PacketProcessingService packetProcessingService, DataBroker dataBroker) {
        this.packetProcessingService = packetProcessingService;
        this.dataBroker = dataBroker;
        forwardingMap = Maps.newHashMap();
    }


    @Override
    public void onPacketReceived(PacketReceived notification) {
//        LOG.info("PacketIn Event Happened,received packet via match:{}", notification.getIngress());

        NodeConnectorRef ingress = notification.getIngress();
        NodeConnectorRef egress;
        NodeRef nodeRef = InventoryUtils.getNodeRef(ingress);
        Optional<NodeRef> op = Optional.fromNullable(nodeRef);
        String nodeName = nodeRef.getValue().firstKeyOf(Node.class).getId().getValue();
        if(!op.isPresent()){
            LOG.warn("Get node failed!");
        }else
        {
            LOG.info("Node is {}", nodeRef.getValue().firstKeyOf(Node.class).getId().getValue());
        }
        byte[] packet = notification.getPayload();
        String macSrc = PacketUtils.rawMacToString(PacketUtils.extractSrcMac(packet));
        String macDst = PacketUtils.rawMacToString(PacketUtils.extractDstMac(packet));
        //        忽略LLDP包
        byte[] packetTypeRaw = PacketUtils.extractMacType(packet);
        int packetType = (0x0000ffff & ByteBuffer.wrap(packetTypeRaw).getShort());
        if (packetType == 0x88cc) {
            LOG.info("LLDP packet received.");
            return;
        }
        //进行Mac学习
        LOG.info("Packet is :{} --> {}", macSrc, macDst);

        if (!forwardingMap.containsKey(nodeName)) {
            forwardingMap.put(nodeName, new HashMap<String, NodeConnectorRef>());
        }
        forwardingMap.get(nodeName).put(macSrc, ingress);

        //Todo 查表，看表中是否存在对应ingress和macDst的目的地址，如果存在则写入一条流表并PacketOut出去

        if (forwardingMap.get(nodeName).containsKey(macDst)) {
            LOG.info("{}的转发表中存在对{}的转发项", nodeName, macDst);
            egress = forwardingMap.get(nodeName).get(macDst);
            FlowUtils.programL2Flow(dataBroker, InventoryUtils.getNodeId(nodeRef),
                    macDst,  InventoryUtils.getNodeConnectorId(egress), 10);
        } else {
            // Todo 如果不存在，则先记录,泛洪出去
            LOG.info("{}的转发表中不存在对{}的转发项", nodeName, macDst);
            egress = InventoryUtils.getNodeConnectorRef(new NodeConnectorId(OFPPort.OFPP_FLOOD));
        }
        FlowUtils.packetOut(packetProcessingService, nodeRef, ingress,egress, packet);
        printForwardingRule(forwardingMap);
    }

    private void printForwardingRule(Map<String, Map<String, NodeConnectorRef>> forwardingMap) {
        LOG.info("Forwarding Table Size:{}", forwardingMap.size());
        StringBuilder info = new StringBuilder();
        for (Map.Entry<String, Map<String,NodeConnectorRef>> entry: forwardingMap.entrySet()) {
            String nodeName = entry.getKey();
            info.append("\n Node:").append(nodeName).append('\n');
            Map<String, NodeConnectorRef> rules = entry.getValue();
            for (Map.Entry<String, NodeConnectorRef> rule : rules.entrySet()) {
                String dstMac = rule.getKey();
                String egress = InventoryUtils.getNodeConnectorId(rule.getValue()).getValue();

                info.append('\t').append("Dst:").append(dstMac).append("-->").append(egress).append('\n');
            }

        }
        LOG.info(info.toString());

    }


}
