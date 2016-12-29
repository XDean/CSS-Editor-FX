package xdean.css.editor.config.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import xdean.jex.extra.Either;
import xdean.jex.util.cache.CacheUtil;

public class OptionGroup {

  List<Either<Option<?>, OptionGroup>> list;
  String describe;

  public OptionGroup(String describe) {
    this.describe = describe;
    list = new ArrayList<>();
  }

  public <T extends Option<?>> T add(T o) {
    list.add(Either.left(o));
    return o;
  }

  public OptionGroup add(OptionGroup og) {
    list.add(Either.right(og));
    return og;
  }

  public List<Either<Option<?>, OptionGroup>> getChildren() {
    return CacheUtil.cache(this, () -> Collections.unmodifiableList(list));
  }

  @SuppressWarnings("unchecked")
  public <T> List<Option<T>> getChildren(Class<T> clz) {
    return list.stream()
        .filter(e -> e.isLeft())
        .map(e -> e.getLeft())
        .filter(o -> clz.isInstance(o.get()))
        .map(o -> (Option<T>) o)
        .collect(Collectors.toList());
  }

  public String getDescribe() {
    return describe;
  }
}
