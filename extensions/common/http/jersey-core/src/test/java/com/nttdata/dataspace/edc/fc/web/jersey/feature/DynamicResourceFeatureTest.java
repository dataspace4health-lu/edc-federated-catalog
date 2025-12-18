package com.nttdata.dataspace.edc.fc.web.jersey.feature;

import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class DynamicResourceFeatureTest {

    @Test
    void configure_shouldRegisterDynamicResources() {
        class TestResource {}

        Object dynamicResource1 = new Object();
        Object dynamicResource2 = new Object();

        Map<Class<?>, List<Object>> dynamicResources = Map.of(
                TestResource.class, List.of(dynamicResource1, dynamicResource2)
        );

        DynamicResourceFeature feature = new DynamicResourceFeature(dynamicResources);

        @SuppressWarnings("rawtypes")
        ResourceInfo resourceInfo = mock(ResourceInfo.class);
        when(resourceInfo.getResourceClass()).thenReturn((Class) TestResource.class);

        FeatureContext context = mock(FeatureContext.class);

        feature.configure(resourceInfo, context);

        verify(context).register(dynamicResource1);
        verify(context).register(dynamicResource2);
    }

    @Test
    void configure_shouldNotRegisterIfNoDynamicResources() {
        class TestResource {}

        Map<Class<?>, List<Object>> dynamicResources = Map.of();

        DynamicResourceFeature feature = new DynamicResourceFeature(dynamicResources);

        @SuppressWarnings("rawtypes")
        ResourceInfo resourceInfo = mock(ResourceInfo.class);
        when(resourceInfo.getResourceClass()).thenReturn((Class) TestResource.class);

        FeatureContext context = mock(FeatureContext.class);

        feature.configure(resourceInfo, context);

        verify(context, never()).register(any());
    }
}
