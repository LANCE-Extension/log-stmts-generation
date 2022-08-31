import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.TypeParameter;

public final class BodyDeclarationParser {

  private static StringJoiner astRepresentation = new StringJoiner("");

  private BodyDeclarationParser() {
  }

  public static StringJoiner parse(BodyDeclaration<?> decl, StringJoiner astRepr) {
    astRepresentation = astRepr;
    decl.ifAnnotationMemberDeclaration(annotationMemberDeclarationConsumer);
    decl.ifCallableDeclaration(callableDeclarationConsumer);
    decl.ifCompactConstructorDeclaration(compactConstructorDeclarationConsumer);
    decl.ifEnumConstantDeclaration(enumConstantDeclarationConsumer);
    decl.ifFieldDeclaration(fieldDeclarationConsumer);
    decl.ifInitializerDeclaration(initializerDeclarationConsumer);
    decl.ifTypeDeclaration(typeDeclarationConsumer);
    return astRepresentation;
  }

  private static Consumer<AnnotationDeclaration> annotationDeclarationConsumer = (AnnotationDeclaration decl) -> {
    System.err.println("AnnotationDeclaration need to be implemented");
    // TODO:
  };

  private static Consumer<AnnotationMemberDeclaration> annotationMemberDeclarationConsumer = (
      AnnotationMemberDeclaration decl) -> {
    System.err.println("AnnotationMemberDeclaration needs to be implemented");
    // TODO:
  };

  private static Consumer<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationConsumer = (
      ClassOrInterfaceDeclaration decl) -> {
    astRepresentation.add(ParserUtility.createTag(decl.getMetaModel().getTypeName()));
    ParserUtility.addAnnotations(decl.getAnnotations(), astRepresentation);
    ParserUtility.addModifiers(decl.getModifiers(), astRepresentation);
    if (decl.isInterface()) {
      astRepresentation.add("interface");
    } else {
      astRepresentation.add("class");
    }
    astRepresentation.add(decl.getNameAsString());
    for (TypeParameter typeParam : decl.getTypeParameters()) {
      astRepresentation = TypeParser.parse(typeParam, astRepresentation);
    }
    if (decl.getExtendedTypes().isNonEmpty()) {
      astRepresentation.add("extends");
      for (ClassOrInterfaceType extendedType : decl.getExtendedTypes()) {
        astRepresentation = TypeParser.parse(extendedType, astRepresentation);
      }
    }
    if (decl.getImplementedTypes().isNonEmpty()) {
      astRepresentation.add("implements");
      for (ClassOrInterfaceType implementedType : decl.getImplementedTypes()) {
        astRepresentation = TypeParser.parse(implementedType, astRepresentation);
      }
    }
    for (BodyDeclaration<?> member : decl.getMembers()) {
      astRepresentation = parse(member, astRepresentation);
    }
  };

  private static Consumer<CompactConstructorDeclaration> compactConstructorDeclarationConsumer = (
      CompactConstructorDeclaration decl) -> {
    System.err.println("CompactConstructorDeclaration needs to be implemented");
    // TODO:
  };

  private static Consumer<ConstructorDeclaration> constructorDeclarationConsumer = (ConstructorDeclaration decl) -> {
    astRepresentation.add(ParserUtility.createTag(decl.getMetaModel().getTypeName()));
    ParserUtility.addAnnotations(decl.getAnnotations(), astRepresentation);
    ParserUtility.addModifiers(decl.getModifiers(), astRepresentation);
    for (TypeParameter typeParam : decl.getTypeParameters()) {
      astRepresentation = TypeParser.parse(typeParam, astRepresentation);
    }
    astRepresentation.add(decl.getNameAsString());
    ParserUtility.addParameters(decl.getParameters(), astRepresentation);
    if (decl.getThrownExceptions().isNonEmpty()) {
      astRepresentation.add("throws");
      for (ReferenceType exception : decl.getThrownExceptions()) {
        astRepresentation = TypeParser.parse(exception, astRepresentation);
      }
    }
    astRepresentation = StatementParser.parse(decl.getBody(), astRepresentation);
  };

  private static Consumer<EnumConstantDeclaration> enumConstantDeclarationConsumer = (EnumConstantDeclaration decl) -> {
    System.err.println("EnumConstantDeclaration needs to be implemented");
    // TODO:
  };

  private static Consumer<EnumDeclaration> enumDeclarationConsumer = (EnumDeclaration decl) -> {
    System.err.println("EnumDeclaration needs to be implemented");
    // TODO:
  };

  private static Consumer<FieldDeclaration> fieldDeclarationConsumer = (FieldDeclaration decl) -> {
    astRepresentation.add(ParserUtility.createTag(decl.getMetaModel().getTypeName()));
    ParserUtility.addAnnotations(decl.getAnnotations(), astRepresentation);
    ParserUtility.addModifiers(decl.getModifiers(), astRepresentation);
    for (VariableDeclarator variable : decl.getVariables()) {
      astRepresentation = TypeParser.parse(variable.getType(), astRepresentation);
      astRepresentation.add(variable.getNameAsString());
      Optional<Expression> init = variable.getInitializer();
      if (init.isPresent()) {
        astRepresentation.add("ASSIGN");
        astRepresentation = ExpressionParser.parse(init.get(), astRepresentation);
      }
    }
  };

  private static Consumer<InitializerDeclaration> initializerDeclarationConsumer = (InitializerDeclaration decl) -> {
    astRepresentation = StatementParser.parse(decl.getBody(), astRepresentation);
  };

  private static Consumer<MethodDeclaration> methodDeclarationConsumer = (MethodDeclaration decl) -> {
    astRepresentation = MethodParser.parse(decl, astRepresentation);
  };

  private static Consumer<RecordDeclaration> recordDeclarationConsumer = (RecordDeclaration decl) -> {
    System.err.println("RecordDeclaration needs to be implemented");
    // TODO:
  };

  private static Consumer<CallableDeclaration> callableDeclarationConsumer = (CallableDeclaration decl) -> {
    decl.ifConstructorDeclaration(constructorDeclarationConsumer);
    decl.ifMethodDeclaration(methodDeclarationConsumer);
  };

  private static Consumer<TypeDeclaration> typeDeclarationConsumer = (TypeDeclaration decl) -> {
    decl.ifAnnotationDeclaration(annotationDeclarationConsumer);
    decl.ifClassOrInterfaceDeclaration(classOrInterfaceDeclarationConsumer);
    decl.ifEnumDeclaration(enumDeclarationConsumer);
    decl.ifRecordDeclaration(recordDeclarationConsumer);
  };

}