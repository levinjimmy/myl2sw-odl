/*
 * Copyright © 2017 lab406.Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.lab406.com.impl;


import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Myl2swProvider {

    private static final Logger LOG = LoggerFactory.getLogger(Myl2swProvider.class);

    private DataBroker dataBroker;
    private PacketProcessingService packetProcessingService;
    private Registration packetInRegistration;
    private ListenerRegistration<DataTreeChangeListener> dataTreeChangeListenerRegistration;
    private NotificationProviderService notificationService;

    public Myl2swProvider(DataBroker dataBroker, PacketProcessingService packetProcessingService, NotificationProviderService notificationService) {
        this.dataBroker = dataBroker;
        this.packetProcessingService = packetProcessingService;
        this.notificationService = notificationService;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {

        LOG.info("Myl2swProvider Session Initiated");
        LOG.info("Starting myl2swProvier--->");

//        需要监听的path
        final InstanceIdentifier<FlowCapableNode> flowCapableNodeInstanceIdentifier=InstanceIdentifier.create(Nodes.class)
                .child(Node.class)
                .augmentation(FlowCapableNode.class);
//        需要监听的数据库
        final DataTreeIdentifier<FlowCapableNode> dataTreeIdentifier = new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, flowCapableNodeInstanceIdentifier);
//        事件订阅处理类
        SwitchEnterListener switchEnterListener = new SwitchEnterListener(dataBroker);
//        订阅节点更新事件
        dataTreeChangeListenerRegistration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, switchEnterListener);


        SwitchLogicHandler switchLogicHandler = new SwitchLogicHandler(packetProcessingService,dataBroker);
        packetInRegistration = notificationService.registerNotificationListener(switchLogicHandler);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("Stop --->");
        dataTreeChangeListenerRegistration.close();
        packetInRegistration.close();
        LOG.info("Myl2swProvider Closed");

    }
}