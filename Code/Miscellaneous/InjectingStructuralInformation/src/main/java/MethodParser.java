import java.util.StringJoiner;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.TypeParameter;

public final class MethodParser {

  private static StringJoiner astRepresentation = new StringJoiner(" ");

  private MethodParser() {
  }

  public static StringJoiner parse(MethodDeclaration mtd, StringJoiner astRepr) {
    astRepresentation = astRepr;
    astRepresentation.add(ParserUtility.createTag(mtd.getMetaModel().getTypeName()));
    astRepresentation = parseSignature(mtd, astRepresentation);
    astRepresentation = parseBody(mtd, astRepresentation);
    return astRepresentation;
  }

  private static StringJoiner parseSignature(MethodDeclaration mtd, StringJoiner astRepresentation) {
    ParserUtility.addAnnotations(mtd.getAnnotations(), astRepresentation);
    ParserUtility.addModifiers(mtd.getModifiers(), astRepresentation);
    for (TypeParameter typeParam : mtd.getTypeParameters()) {
      astRepresentation = TypeParser.parse(typeParam, astRepresentation);
    }
    astRepresentation = TypeParser.parse(mtd.getType(), astRepresentation);
    astRepresentation.add(mtd.getNameAsString());
    ParserUtility.addParameters(mtd.getParameters(), astRepresentation);
    if (mtd.getThrownExceptions().isNonEmpty()) {
      astRepresentation.add("throws");
      for (ReferenceType exception : mtd.getThrownExceptions()) {
        astRepresentation = TypeParser.parse(exception, astRepresentation);
      }
    }
    return astRepresentation;
  }

  private static StringJoiner parseBody(MethodDeclaration mtd, StringJoiner astRepresentation) {
    if (mtd.getBody().isPresent())
      return StatementParser.parse(mtd.getBody().get(), astRepresentation);
    return astRepresentation;
  }

}
