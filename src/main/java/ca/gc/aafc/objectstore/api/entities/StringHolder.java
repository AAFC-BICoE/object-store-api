package ca.gc.aafc.objectstore.api.entities;

/**
 * Used to represent undefined value (not provided) as well as null (explicitly set) value.
 */
public sealed interface StringHolder {
  record Defined(String value) implements StringHolder {
  }

  record Null() implements StringHolder {
  }

  record Undefined() implements StringHolder {
  }

  static StringHolder of(String value) {
    return new Defined(value);
  }

  static StringHolder ofNull() {
    return new Null();
  }

  static StringHolder undefined() {
    return new Undefined();
  }
}