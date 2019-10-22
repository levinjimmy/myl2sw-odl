package org.lab406.com.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.infrautils.utils.concurrent.JdkFutures;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FlowUtils {
    private static final Logger LOG = LoggerFactory.getLogger(FlowUtils.class);
    private static final AtomicLong flowIdInc = new AtomicLong();
    public FlowUtils() {
    }

    public static void packetOut(PacketProcessingService packetProcessingService, NodeRef egressNodeRef, NodeConnectorRef ingressNodeConnectorRef,NodeConnectorRef egressNodeConnectorRef, byte[] payload) {
        Preconditions.checkNotNull(packetProcessingService);
        LOG.info("Forwarding packet of size {} out of port {}", payload.length, egressNodeConnectorRef);

        //Construct input for RPC call to packet processing service
        TransmitPacketInput input = new TransmitPacketInputBuilder()
                .setPayload(payload)
                .setNode(egressNodeRef)
                .setEgress(egressNodeConnectorRef)
                .setIngress(ingressNodeConnectorRef)
                .build();
        JdkFutures.addErrorLogging(packetProcessingService.transmitPacket(input), LOG, "transmitPacket");
    }

    public static FlowBuilder createOutputFlowBuilder(NodeConnectorId egressNodeConnectorId, int priority){
        //       用于存储独立的Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        List<Action> actionList = Lists.newArrayList();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setOutputNodeConnector(egressNodeConnectorId);
        output.setMaxLength(65535); //Send full packet and No buffer
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        // 设置指令列表，在Instructions中只设置了一个指令
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());

//        创建流表项
        FlowBuilder flowBuilder = new FlowBuilder();
        String flowIdStr = "L2_Rule_" + String.valueOf(flowIdInc.getAndIncrement());
        FlowId flowId = new FlowId(flowIdStr);
        flowBuilder.setId(flowId);
        FlowKey flowKey = new FlowKey(flowId);
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short) 0);
        flowBuilder.setKey(flowKey);
        flowBuilder.setPriority(priority);
        flowBuilder.setFlowName(flowIdStr);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setFlags(new FlowModFlags(false, false, false, false, false));
        flowBuilder.setBufferId(OFConstants.OFP_NO_BUFFER);
        flowBuilder.setInstructions(isb.setInstruction(instructions).build());
        return flowBuilder;

    }
    public static void programFlow(DataBroker dataBroker, NodeId nodeId, Match match,  NodeConnectorId egressNodeConnectorId, int priority) {


//        添加匹配项
        FlowBuilder flowBuilder = createOutputFlowBuilder(egressNodeConnectorId,priority);
        flowBuilder.setMatch(match);
//        设置DataStore的路径
        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId()))
                .child(Flow.class, flowBuilder.getKey())
                .build();
        DataStoreUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), true);
    }

    public static void programL2Flow(DataBroker dataBroker, NodeId nodeId, String macDst,  NodeConnectorId egressNodeConnectorId, int priority) {
        LOG.info("正在下发流表");
        MatchBuilder matchBuilder = new MatchBuilder();
        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetDestinationBuilder ethernetDestinationBuilder = new EthernetDestinationBuilder();
        ethernetDestinationBuilder.setAddress(new MacAddress(macDst));
        ethernetMatch.setEthernetDestination(ethernetDestinationBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());
        programFlow(dataBroker, nodeId, matchBuilder.build(), egressNodeConnectorId, priority);
    }

    public static void createFwdAllToControllerFlow(DataBroker dataBroker, NodeId nodeId) {
        LOG.info("为{}下发table-miss流表项", nodeId.getValue());
        // Create output action -> send to controller
//       用于存储独立的Instructions
        NodeConnectorId controllerId = new NodeConnectorId(new Uri(OutputPortValues.CONTROLLER.toString()));
        programFlow(dataBroker,nodeId,
                new MatchBuilder().build(),
                controllerId,
                0);
    }

}