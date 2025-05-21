package ca.gc.aafc.objectstore.api.async;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AsyncConsumer<T> implements Consumer<T> {

  private final List<T> accepted = new ArrayList<>();

  @Override
  public void accept(T o) {
    accepted.add(o);
  }

  public void clear() {
    accepted.clear();
  }

  public List<T> getAccepted() {
    return accepted;
  }
}