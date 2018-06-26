package xdean.css.editor.model;

import java.nio.file.Path;
import java.util.Optional;

import xdean.jex.extra.collection.Either;

public class FileWrapper {

  public static FileWrapper existFile(Path path) {
    return new FileWrapper(Either.left(path));
  }

  public static FileWrapper newFile(int i) {
    return new FileWrapper(Either.right(i));
  }

  public final Either<Path, Integer> fileOrNew;

  public FileWrapper(Either<Path, Integer> fileOrNewOrder) {
    this.fileOrNew = fileOrNewOrder;
  }

  public boolean isExistFile() {
    return fileOrNew.isLeft();
  }

  public boolean isNewFile() {
    return fileOrNew.isRight();
  }

  public Optional<Path> getExistFile() {
    return fileOrNew.asLeft();
  }

  public Optional<Integer> getNewOrder() {
    return fileOrNew.asRight();
  }

  public String getFileName() {
    return fileOrNew.unify(p -> p.getFileName().toString(), i -> newFileName(i));
  }

  public static String newFileName(int i) {
    return "new " + i;
  }

  @Override
  public String toString() {
    return getFileName();
  }
}
