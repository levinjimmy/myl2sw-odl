package org.lab406.com.impl;




import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

public class SwitchEnterListener implements DataTreeChangeListener<FlowCapableNode> {
    private static final Logger LOG = LoggerFactory.getLogger(SwitchEnterListener.class);
    private DataBroker dataBroker;
    public SwitchEnterListener() {
    }

    public SwitchEnterListener(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<FlowCapableNode>> changes) {
        for(DataTreeModification<FlowCapableNode> change:changes){
            if (change.getRootNode().getModificationType()== DataObjectModification.ModificationType.WRITE){
//                从事件中析出目标
//                LOG.info("Node connected:  {}",
//                        change.getRootPath().getRootIdentifier().firstIdentifierOf(Node.class));
                Node nodeData= (Node) DataStoreUtils.readData(dataBroker, LogicalDatastoreType.OPERATIONAL, change.getRootPath().getRootIdentifier().firstIdentifierOf(Node.class));
                LOG.info("Node connected:{}", nodeData.getId().getValue());
//                为Openflow下发流表为Output:Normal
                FlowUtils.createFwdAllToControllerFlow(dataBroker,nodeData.getId());
            }
        }
    }
}
