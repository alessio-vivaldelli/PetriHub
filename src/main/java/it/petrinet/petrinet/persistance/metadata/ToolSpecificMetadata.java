package it.petrinet.petrinet.persistance.metadata;

import java.util.HashMap;
import java.util.Map;

public class ToolSpecificMetadata {
  private final String toolName;
  private final String version;
  private final Map<String, String> data = new HashMap<>();

  public ToolSpecificMetadata(String toolName, String version) {
    this.toolName = toolName;
    this.version = version;
  }

  public void put(String key, String value) {
    data.put(key, value);
  }

  public String get(String key) {
    return data.get(key);
  }
}
