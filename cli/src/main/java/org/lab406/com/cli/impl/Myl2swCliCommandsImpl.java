/*
 * Copyright © 2017 lab406.Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.lab406.com.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.lab406.com.cli.api.Myl2swCliCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Myl2swCliCommandsImpl implements Myl2swCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(Myl2swCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public Myl2swCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("Myl2swCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}
