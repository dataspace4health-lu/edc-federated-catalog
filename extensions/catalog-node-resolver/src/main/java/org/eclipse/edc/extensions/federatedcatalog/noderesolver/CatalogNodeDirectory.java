package org.eclipse.edc.extensions.federatedcatalog.noderesolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.eclipse.edc.crawler.spi.TargetNode;
import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.monitor.Monitor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public class CatalogNodeDirectory implements TargetNodeDirectory {
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper mapper;
    private final File participantListFile;
    private final Monitor monitor;


    @Override
    public List<TargetNode> getAll() {
        try {
            var entries = mapper.readValue(participantListFile, MAP_TYPE);

            return entries.entrySet().stream()
                    .map(e -> createNode(e.getKey(), e.getValue()))
                    .toList();
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    @Override
    public void insert(TargetNode targetNode) {

    }

    @Override
    public TargetNode remove(String s) {
        return null;
    }

    private TargetNode createNode(String name, String url) {
        return new TargetNode(
                "https://w3id.org/edc/v0.0.1/ns/",
                name, url, List.of("dataspace-protocol-http"));
    }
}
