package ca.gc.aafc.objectstore.api.config;

import java.util.UUID;

public class ObjectStoreVocabularyConfiguration {

  // Constant, by Liquibase migration
  public static final UUID MANAGED_ATTRIBUTE_VOCAB_UUID = UUID.fromString("b8527bdf-a1d2-465d-a8bb-2a66d552de23");

  private ObjectStoreVocabularyConfiguration() {
    // no-op
  }

  public enum DinaComponent {
    METADATA,
    DERIVATIVE;

    public static DinaComponent fromString(String s) {
      for (DinaComponent source : DinaComponent.values()) {
        if (source.name().equalsIgnoreCase(s)) {
          return source;
        }
      }
      return null;
    }
  }
}
