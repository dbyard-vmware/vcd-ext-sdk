/* *
 * api-extension-template-vcloud-director
 * Copyright (c) 2017-2018 VMware, Inc. All Rights Reserved.
 * SPDX-License-Identifier: BSD-2-Clause
 * */
package com.vmware.vcloud.object.extensibility.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vcloud.extensibility.platform.ExtensionErrorMessage;
import com.vmware.vcloud.extensibility.platform.PlacementProposalType;
import com.vmware.vcloud.extensibility.platform.Requirements;
import com.vmware.vcloud.object.extensibility.amqp.AmqpManager;
import com.vmware.vcloud.object.extensibility.amqp.impl.AmqpManagerImpl;

/**
 * Hello world object extension for the Requirements gathering phase. Receives Requirements
 * extension message and responds back with the same unmodified requirements. The RMQ credentials
 * are passed as arguments to the application
 */
public class HelloWorldCalculateSolutionObjectExtension {

    private final static Logger logger = LoggerFactory.getLogger(HelloWorldCalculateSolutionObjectExtension.class);


    public static void main(String[] args) {
        if (args.length != 5) {
            logger.error("Invalid arguments. Following 5 arguments must be supplied: "
                    + "rmqHost rmqUser rmqPassword contentType concurrentConsumers");
            return;
        }
        new HelloWorldCalculateSolutionObjectExtension().registerListener(args[0], args[1], args[2], 
                AmqpManager.ContentType.valueOf(args[3]), Integer.parseInt(args[4]));
    }

    /**
     * Registers a listener using the supplied RMQ credentials. Exchange, queue and routing info are
     * hard coded. The exchange value should match what is used during Object Extension registration
     */
    void registerListener(String rmqHost, String rmqUser, String rmqPassword, AmqpManager.ContentType contentType, int concurrentConsumers) {
        logger.info("Application started for RMQ: {}@{}. concurrent consumer count: {}", 
                rmqUser, rmqHost, concurrentConsumers);

        AmqpManager amqpManager = new AmqpManagerImpl();
        amqpManager.configure(rmqHost, rmqUser, rmqPassword);
        amqpManager.registerObjectExtensionListener(
                "reference-service-exchange",
                "reference-service-blocking-solution-queue",
                "urn:extensionPoint:vm:calculateSolution",
                this,
                "handleSolutionsMessage",
                contentType,
                concurrentConsumers);
    }

    /**
     * Callback method that gets invoked when an AMQP message is received.
     * 
     * @param vcdRequirementsMessage
     *            set of requirements sent from VCD during a create VM workflow
     */
    public PlacementProposalType handleSolutionsMessage(PlacementProposalType placementProposalType) {
        logger.info("Received Object Extension calculate solution message from VCD. Proposed hub: {}", 
                placementProposalType.getProposedSolution().getSubjectHubAssignment().get(0).getHubUri());
        
        logger.info("Replying with same unmodified solution message");
        
        return placementProposalType;
    }
    
    /**
     * Handles error messages sent by vCD to this extension 
     * 
     * @param vcdErrorMessage
     */
    public void handleSolutionsMessage(ExtensionErrorMessage vcdErrorMessage) {
        logger.error("Received extension error message from VCD: {}", vcdErrorMessage.getMessage());
    }
}
