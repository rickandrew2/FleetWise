package com.fleetwise.vehicle;

import com.fleetwise.vehicle.dto.EpaVehicleOption;
import com.fleetwise.vehicle.dto.FuelEconomyVehicleData;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class FuelEconomyGovApiClient implements FuelEconomyApiClient {

    private static final String BASE_URL = "https://www.fueleconomy.gov/ws/rest";
    private final RestTemplate restTemplate;

    public FuelEconomyGovApiClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
    }

    @Override
    public List<EpaVehicleOption> lookupVehicleOptions(int year, String make, String model) {
        String url = BASE_URL + "/vehicle/menu/options?year=" + year + "&make=" + make + "&model=" + model;
        try {
            String xml = restTemplate.getForObject(url, String.class);
            if (xml == null || xml.isBlank()) {
                return List.of();
            }
            Document document = parseXml(xml);
            NodeList items = document.getElementsByTagName("menuItem");
            List<EpaVehicleOption> options = new ArrayList<>();
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                String idText = readChildText(item, "value");
                String label = readChildText(item, "text");
                if (idText != null && !idText.isBlank()) {
                    try {
                        int epaVehicleId = Integer.parseInt(idText.trim());
                        Optional<FuelEconomyVehicleData> details = fetchVehicleData(epaVehicleId);
                        options.add(new EpaVehicleOption(
                                epaVehicleId,
                                label,
                                details.map(FuelEconomyVehicleData::combinedMpg).orElse(null),
                                details.map(FuelEconomyVehicleData::fuelType).orElse(null)));
                    } catch (NumberFormatException ignored) {
                        // Ignore malformed entries and keep parsing safe.
                    }
                }
            }
            return options;
        } catch (RestClientException ex) {
            return List.of();
        }
    }

    @Override
    public Optional<FuelEconomyVehicleData> fetchVehicleData(int epaVehicleId) {
        String url = BASE_URL + "/vehicle/" + epaVehicleId;
        try {
            String xml = restTemplate.getForObject(url, String.class);
            if (xml == null || xml.isBlank()) {
                return Optional.empty();
            }
            Document document = parseXml(xml);
            Double combinedMpg = readDoubleTag(document, "comb08");
            Double cityMpg = readDoubleTag(document, "city08");
            Double highwayMpg = readDoubleTag(document, "highway08");
            String fuelType = readFirstTagText(document, "fuelType1");

            return Optional.of(new FuelEconomyVehicleData(combinedMpg, cityMpg, highwayMpg, fuelType));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Document parseXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse FuelEconomy API response", ex);
        }
    }

    private String readFirstTagText(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        Node node = nodes.item(0);
        return node == null ? null : node.getTextContent();
    }

    private Double readDoubleTag(Document document, String tagName) {
        String value = readFirstTagText(document, tagName);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String readChildText(Node parentNode, String childTagName) {
        if (parentNode == null || parentNode.getChildNodes() == null) {
            return null;
        }
        NodeList childNodes = parentNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (childTagName.equals(child.getNodeName())) {
                return child.getTextContent();
            }
        }
        return null;
    }
}