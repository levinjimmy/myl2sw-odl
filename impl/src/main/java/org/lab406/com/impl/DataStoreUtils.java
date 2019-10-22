package org.lab406.com.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class DataStoreUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DataStoreUtils.class);
    public DataStoreUtils() {
    }
    public static Node getNode(DataBroker dataBroker,LogicalDatastoreType dataStoreType, InstanceIdentifier<Node> nodePath){
        Node result;
        result = readData(dataBroker, dataStoreType, nodePath);
        return result;
    }
    public static <T extends DataObject> T readData(DataBroker dataBroker, LogicalDatastoreType dataStoreType, InstanceIdentifier<T> iid) {
        Preconditions.checkNotNull(dataBroker);
        ReadOnlyTransaction readTransaction = dataBroker.newReadOnlyTransaction();
        try {
            Optional<T> optionalData = readTransaction.read(dataStoreType, iid).get();
            if (optionalData.isPresent()) {
                return (T)optionalData.get();
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Read transaction for identifier {} failed with error {}", iid, e.getMessage());
            readTransaction.close();
        }
        return null;
    }
    public static <T extends DataObject> boolean writeData(DataBroker dataBroker, LogicalDatastoreType logicalDatastoreType,
                                                           InstanceIdentifier<T> iid, T dataObject, boolean isAdd) {
        Preconditions.checkNotNull(dataBroker);
        WriteTransaction modification = dataBroker.newWriteOnlyTransaction();
        if (isAdd) {
            if (dataObject == null) {
                LOG.warn("Invalid attempt to add a non-existent object to path {}", iid);
                return false;
            }
            modification.merge(logicalDatastoreType, iid, dataObject, true /*createMissingParents*/);
        }
        else {
            modification.delete(LogicalDatastoreType.CONFIGURATION, iid);
        }
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        try {
            commitFuture.checkedGet();
            LOG.info("Transaction success for {} of object {}", (isAdd) ? "add" : "delete", dataObject);
            return true;
        } catch (Exception e) {
            LOG.error("Transaction failed with error {} for {} of object {}", e.getMessage(), (isAdd) ? "add" : "delete", dataObject);
            modification.cancel();
            return false;
        }
    }
    public static <T extends DataObject> ListenableFuture<?> writeFlowToConfig(DataBroker dataBroker, LogicalDatastoreType logicalDatastoreType,
                                                                               InstanceIdentifier<T> iid, T dataObject){
        ReadWriteTransaction addFlowTransaction = dataBroker.newReadWriteTransaction();
        addFlowTransaction.put(logicalDatastoreType, iid, dataObject, true);
        return addFlowTransaction.submit();
    }
}
