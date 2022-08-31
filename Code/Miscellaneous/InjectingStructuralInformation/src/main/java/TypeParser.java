import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.*;

public final class TypeParser {
  private static StringJoiner astRepresentation = new StringJoiner("");

  private TypeParser() {
  }

  public static StringJoiner parse(Type type, StringJoiner astRepr) {
    astRepresentation = astRepr;
    astRepresentation.add(ParserUtility.createTag(type.getMetaModel().getTypeName()));

    ParserUtility.addAnnotations(type.getAnnotations(), astRepresentation);

    type.ifIntersectionType(intersectionTypeConsumer);
    type.ifPrimitiveType(x -> astRepresentation.add(x.toString()));
    type.ifReferenceType(referenceTypeConsumer);
    type.ifUnionType(unionTypeConsumer);
    type.ifUnknownType(x -> astRepresentation.add(x.toString()));
    type.ifVoidType(x -> astRepresentation.add(x.toString()));
    type.ifWildcardType(wildcardTypeConsumer);
    return astRepresentation;
  }

  private static Consumer<IntersectionType> intersectionTypeConsumer = (IntersectionType type) -> {
    System.err.println("IntersectionType needs to be implemented");
    // TODO:
  };

  private static Consumer<UnionType> unionTypeConsumer = (UnionType type) -> {
    for (ReferenceType element : type.getElements()) {
      astRepresentation = parse(element, astRepresentation);
    }
  };

  private static Consumer<ClassOrInterfaceType> classOrInterfaceTypeConsumer = (ClassOrInterfaceType type) -> {
    Optional<ClassOrInterfaceType> scope = type.getScope();
    if (scope.isPresent()) {
      astRepresentation = parse(scope.get(), astRepresentation);
    }
    astRepresentation.add(type.getNameAsString());
    Optional<NodeList<Type>> typeArgs = type.getTypeArguments();
    if (typeArgs.isPresent()) {
      ParserUtility.addTypeArguments(typeArgs.get(), astRepresentation);
    }
  };

  private static Consumer<ArrayType> arrayTypeConsumer = (ArrayType type) -> {
    astRepresentation = parse(type.getElementType(), astRepresentation);
    for (int i = 0; i < type.getArrayLevel(); i++) {
      astRepresentation.add("[]");
    }
  };

  private static Consumer<TypeParameter> typeParameterConsumer = (TypeParameter type) -> {
    astRepresentation.add(type.getNameAsString());
    if (type.getTypeBound().isNonEmpty()) {
      astRepresentation.add("extends");
      for (ClassOrInterfaceType typeBound : type.getTypeBound()) {
        astRepresentation = parse(typeBound, astRepresentation);
      }
    }
  };

  private static Consumer<ReferenceType> referenceTypeConsumer = (ReferenceType type) -> {
    type.ifArrayType(arrayTypeConsumer);
    type.ifClassOrInterfaceType(classOrInterfaceTypeConsumer);
    type.ifTypeParameter(typeParameterConsumer);
  };

  private static Consumer<WildcardType> wildcardTypeConsumer = (WildcardType type) -> {
    astRepresentation.add("?");
    Optional<ReferenceType> extendedType = type.getExtendedType();
    if (extendedType.isPresent()) {
      astRepresentation.add("extends");
      astRepresentation = parse(extendedType.get(), astRepresentation);
    }
    Optional<ReferenceType> superType = type.getSuperType();
    if (superType.isPresent()) {
      astRepresentation.add("super");
      astRepresentation = parse(superType.get(), astRepresentation);
    }
  };
}
