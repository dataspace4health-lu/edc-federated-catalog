/*
 *  Copyright (c) 2024 Eclipse Dataspace Components
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 */

package com.nttdata.dataspace.fc;


import org.eclipse.edc.iam.verifiablecredentials.spi.validation.TrustedIssuerRegistry;
import org.eclipse.edc.policy.context.request.spi.RequestCatalogPolicyContext;
import org.eclipse.edc.policy.context.request.spi.RequestContractNegotiationPolicyContext;
import org.eclipse.edc.policy.context.request.spi.RequestTransferProcessPolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.util.Set;

public class ScopeExtension implements ServiceExtension {

    @Inject
    private PolicyEngine policyEngine;

    @Inject
    private TrustedIssuerRegistry trustedIssuerRegistry;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        monitor.debug("Scope Extension initializing..");
        
        String catalogScopeString = context.getConfig().getString(ScopeConstants.CATALOG_REQUEST_SCOPE_PROPERTY, ScopeConstants.DEFAULT_CATALOG_REQUEST_SCOPE);
        var catalogScopeMappingFunction = new ScopeMappingFunction(Set.of(catalogScopeString.split(",")));
        // Register scope mapping for catalog requests
        policyEngine.registerPostValidator(RequestCatalogPolicyContext.class, catalogScopeMappingFunction::apply);

        monitor.info("Scope Extension initialized - registered scope mapping for catalog requests");

    }

}
