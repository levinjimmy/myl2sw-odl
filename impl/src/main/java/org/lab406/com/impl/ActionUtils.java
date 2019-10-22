package org.lab406.com.impl;


import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;

public final class ActionUtils {
    public static Action dropAction(){
        return new DropActionCaseBuilder()
                .setDropAction(new DropActionBuilder().build()).build();

    }
}
