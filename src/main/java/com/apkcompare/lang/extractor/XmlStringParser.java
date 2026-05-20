package com.apkcompare.lang.extractor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XmlStringParser {

    private XmlStringParser() {}

    public static ParseResult parse(InputStream input) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(input);

        Map<String, String> strings = new LinkedHashMap<>();
        List<String> duplicateKeys = new ArrayList<>();

        NodeList nodes = doc.getElementsByTagName("string");
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (!(node instanceof Element element)) {
                continue;
            }
            String name = element.getAttribute("name");
            if (name == null || name.isBlank()) {
                continue;
            }
            String value = element.getTextContent();
            if (value != null) {
                value = value.trim();
            } else {
                value = "";
            }
            if (strings.containsKey(name)) {
                duplicateKeys.add(name);
            }
            strings.put(name, value);
        }

        return new ParseResult(strings, duplicateKeys);
    }

    public record ParseResult(Map<String, String> strings, List<String> duplicateKeys) {}
}
