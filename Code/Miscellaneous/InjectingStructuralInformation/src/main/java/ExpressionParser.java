import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

public final class ExpressionParser {

  private static StringJoiner astRepresentation = new StringJoiner("");

  private ExpressionParser() {
  }

  public static StringJoiner parse(Expression expr, StringJoiner astRepr) {
    astRepresentation = astRepr;
    expr.ifAnnotationExpr(annotationExprConsumer);
    expr.ifArrayAccessExpr(arrayAccessExprConsumer);
    expr.ifArrayCreationExpr(arrayCreationExprConsumer);
    expr.ifArrayInitializerExpr(arrayInitializerExprConsumer);
    expr.ifAssignExpr(assignExprConsumer);
    expr.ifBinaryExpr(binaryExprConsumer);
    expr.ifCastExpr(castExprConsumer);
    expr.ifClassExpr(classExprConsumer);
    expr.ifConditionalExpr(conditionalExprConsumer);
    expr.ifEnclosedExpr(enclosedExprConsumer);
    expr.ifFieldAccessExpr(fieldAccessExprConsumer);
    expr.ifInstanceOfExpr(instanceOfExprConsumer);
    expr.ifLambdaExpr(lambdaExprConsumer);
    expr.ifLiteralExpr(literalExprConsumer);
    expr.ifMethodCallExpr(methodCallExprConsumer);
    expr.ifMethodReferenceExpr(methodReferenceExprConsumer);
    expr.ifNameExpr(nameExprConsumer);
    expr.ifObjectCreationExpr(objectCreationExprConsumer);
    expr.ifPatternExpr(patternExprConsumer);
    expr.ifSuperExpr(superExprConsumer);
    expr.ifSwitchExpr(switchExprConsumer);
    expr.ifThisExpr(thisExprConsumer);
    expr.ifTypeExpr(typeExprConsumer);
    expr.ifUnaryExpr(unaryExprConsumer);
    expr.ifVariableDeclarationExpr(variableDeclarationExprConsumer);
    return astRepresentation;
  }

  private static Consumer<NormalAnnotationExpr> normalAnnotationExprConsumer = (NormalAnnotationExpr expr) -> {
    for (MemberValuePair pair : expr.getPairs()) {
      astRepresentation.add(pair.getNameAsString());
      astRepresentation = parse(pair.getValue(), astRepresentation);
    }
  };

  private static Consumer<SingleMemberAnnotationExpr> singleMemberAnnotationExprConsumer = (
      SingleMemberAnnotationExpr expr) -> {
    astRepresentation = parse(expr.getMemberValue(), astRepresentation);
  };

  private static Consumer<AnnotationExpr> annotationExprConsumer = (AnnotationExpr expr) -> {
    addTypeName(expr);
    astRepresentation.add("@" + expr.getNameAsString());
    expr.ifSingleMemberAnnotationExpr(singleMemberAnnotationExprConsumer);
    expr.ifNormalAnnotationExpr(normalAnnotationExprConsumer);
  };

  private static Consumer<ArrayAccessExpr> arrayAccessExprConsumer = (ArrayAccessExpr expr) -> {
    addTypeName(expr);
    astRepresentation = parse(expr.getName(), astRepresentation);
    astRepresentation = parse(expr.getIndex(), astRepresentation);
  };

  private static Consumer<ArrayCreationExpr> arrayCreationExprConsumer = (ArrayCreationExpr expr) -> {
    addTypeName(expr);
    astRepresentation.add("new");
    astRepresentation.add(expr.getElementType().asString());
    for (ArrayCreationLevel level : expr.getLevels()) {
      ParserUtility.addAnnotations(level.getAnnotations(), astRepresentation);
      astRepresentation.add(ParserUtility.createTag(level.getMetaModel().getTypeName()));
      astRepresentation.add("[]");
      Optional<Expression> dimension = level.getDimension();
      if (dimension.isPresent()) {
        astRepresentation = parse(dimension.get(), astRepresentation);
      }
    }
    Optional<ArrayInitializerExpr> init = expr.getInitializer();
    if (init.isPresent())
      astRepresentation = parse(init.get(), astRepresentation);
  };

  private static Consumer<ArrayInitializerExpr> arrayInitializerExprConsumer = (ArrayInitializerExpr expr) -> {
    addTypeName(expr);
    astRepresentation.add("{");
    NodeList<Expression> values = expr.getValues();
    if(values.isNonEmpty()){
      astRepresentation = parse(values.get(0), astRepresentation);
      for(int i = 1; i < values.size(); i++){
        astRepresentation.add(",");
        astRepresentation = parse(values.get(i), astRepresentation);
      }
    }
    astRepresentation.add("}");
  };

  private static Consumer<AssignExpr> assignExprConsumer = (AssignExpr expr) -> {
    addTypeName(expr);
    astRepresentation = parse(expr.getTarget(), astRepresentation);
    astRepresentation.add(expr.getOperator().name());
    astRepresentation = parse(expr.getValue(), astRepresentation);
  };

  private static Consumer<BinaryExpr> binaryExprConsumer = (BinaryExpr expr) -> {
    addTypeName(expr);
    astRepresentation = parse(expr.getLeft(), astRepresentation);
    astRepresentation.add(expr.getOperator().name());
    astRepresentation = parse(expr.getRight(), astRepresentation);
  };

  private static Consumer<CastExpr> castExprConsumer = (CastExpr expr) -> {
    addTypeName(expr);
    astRepresentation.add(expr.getTypeAsString());
    astRepresentation = parse(expr.getExpression(), astRepresentation);
  };

  private static Consumer<ClassExpr> classExprConsumer = (ClassExpr expr) -> {
    addTypeName(expr);
    astRepresentation = TypeParser.parse(expr.getType(), astRepresentation);
    astRepresentation.add(expr.toString());
  };

  private static Consumer<ConditionalExpr> conditionalExprConsumer = (ConditionalExpr expr) -> {
    addTypeName(expr);
    astRepresentation = parse(expr.getCondition(), astRepresentation);
    astRepresentation.add("?");
    astRepresentation = parse(expr.getThenExpr(), astRepresentation);
    astRepresentation.add(":");
    astRepresentation = parse(expr.getElseExpr(), astRepresentation);
  };

  private static Consumer<EnclosedExpr> enclosedExprConsumer = (EnclosedExpr expr) -> {
    addTypeName(expr);
    astRepresentation.add("(");
    astRepresentation = parse(expr.getInner(), astRepresentation);
    astRepresentation.add(")");
  };

  private static Consumer<FieldAccessExpr> fieldAccessExprConsumer = (FieldAccessExpr expr) -> {
    astRepresentation = parse(expr.getScope(), astRepresentation);
    addTypeName(expr);
    astRepresentation.add(expr.getNameAsString());
  };

  private static Consumer<InstanceOfExpr> instanceOfExprConsumer = (InstanceOfExpr expr) -> {
    addTypeName(expr);
    astRepresentation = parse(expr.getExpression(), astRepresentation);
    astRepresentation.add("instanceof");
    astRepresentation = TypeParser.parse(expr.getType(), astRepresentation);
    if (expr.getPattern().isPresent()) {
      System.err.println("Need to impement parsing of PatternExpr in instanceOfExprConsumer");
      // TODO:
    }
  };

  private static Consumer<LambdaExpr> lambdaExprConsumer = (LambdaExpr expr) -> {
    addTypeName(expr);
    ParserUtility.addParameters(expr.getParameters(), astRepresentation);
    astRepresentation.add("->");
    astRepresentation = StatementParser.parse(expr.getBody(), astRepresentation);
  };

  private static Consumer<NameExpr> nameExprConsumer = (NameExpr expr) -> {
    addTypeName(expr);
    // astRepresentation.add(expr.getMetaModel().getTypeNameGenerified()); //
    // SimpleName
    astRepresentation.add(expr.getNameAsString());
  };

  private static Consumer<MethodCallExpr> methodCallExprConsumer = (MethodCallExpr expr) -> {
    Optional<Expression> scope = expr.getScope();
    if (scope.isPresent())
      astRepresentation = parse(scope.get(), astRepresentation);
    Optional<NodeList<Type>> typeArgs = expr.getTypeArguments();
    if (typeArgs.isPresent()) {
      ParserUtility.addTypeArguments(typeArgs.get(), astRepresentation);
    }
    addTypeName(expr);
    astRepresentation.add(expr.getNameAsString());
    ParserUtility.addArguments(expr.getArguments(), astRepresentation);
  };

  private static Consumer<MethodReferenceExpr> methodReferenceExprConsumer = (MethodReferenceExpr expr) -> {
    astRepresentation = parse(expr.getScope(), astRepresentation);
    addTypeName(expr);
    Optional<NodeList<Type>> typeArgs = expr.getTypeArguments();
    if (typeArgs.isPresent()) {
      ParserUtility.addTypeArguments(typeArgs.get(), astRepresentation);
    astRepresentation.add(expr.getIdentifier());
    }
  };

  private static Consumer<BooleanLiteralExpr> booleanLiteralExprConsumer = (BooleanLiteralExpr expr) -> {
    astRepresentation.add(expr.toString());
  };

  private static Consumer<CharLiteralExpr> charLiteralExprConsumer = (CharLiteralExpr expr) -> {
    astRepresentation.add("'" + expr.getValue() + "'");
  };

  private static Consumer<DoubleLiteralExpr> doubleLiteralExprConsumer = (DoubleLiteralExpr expr) -> {
    astRepresentation.add(expr.getValue());
  };

  private static Consumer<IntegerLiteralExpr> integerLiteralExprConsumer = (IntegerLiteralExpr expr) -> {
    astRepresentation.add(expr.getValue());
  };

  private static Consumer<LongLiteralExpr> longLiteralExprConsumer = (LongLiteralExpr expr) -> {
    astRepresentation.add(expr.getValue());
  };

  private static Consumer<NullLiteralExpr> nullLiteralExprConsumer = (NullLiteralExpr expr) -> {
    astRepresentation.add(expr.toString());
  };

  private static Consumer<StringLiteralExpr> stringLiteralExprConsumer = (StringLiteralExpr expr) -> {
    astRepresentation.add("\"" + expr.getValue() + "\"");
  };

  private static Consumer<TextBlockLiteralExpr> textBlockLiteralExprConsumer = (TextBlockLiteralExpr expr) -> {
    System.err.println("TextBlockLiteralExpr needs to be implemented");
    // TODO:
  };

  private static Consumer<LiteralExpr> literalExprConsumer = (LiteralExpr expr) -> {
    addTypeName(expr);
    expr.ifBooleanLiteralExpr(booleanLiteralExprConsumer);
    expr.ifCharLiteralExpr(charLiteralExprConsumer);
    expr.ifDoubleLiteralExpr(doubleLiteralExprConsumer);
    expr.ifIntegerLiteralExpr(integerLiteralExprConsumer);
    expr.ifLongLiteralExpr(longLiteralExprConsumer);
    expr.ifNullLiteralExpr(nullLiteralExprConsumer);
    expr.ifStringLiteralExpr(stringLiteralExprConsumer);
    expr.ifTextBlockLiteralExpr(textBlockLiteralExprConsumer);
  };

  private static Consumer<ObjectCreationExpr> objectCreationExprConsumer = (ObjectCreationExpr expr) -> {
    Optional<Expression> scope = expr.getScope();
    if (scope.isPresent()) {
      astRepresentation = parse(scope.get(), astRepresentation);
    }
    addTypeName(expr);
    astRepresentation.add("new");
    astRepresentation = TypeParser.parse(expr.getType(), astRepresentation);
    Optional<NodeList<Type>> typeArgs = expr.getTypeArguments();
    if(typeArgs.isPresent()){
      ParserUtility.addTypeArguments(typeArgs.get(), astRepresentation);
    }
    ParserUtility.addArguments(expr.getArguments(), astRepresentation);
    Optional<NodeList<BodyDeclaration<?>>> bodyDeclList = expr.getAnonymousClassBody();
    if (bodyDeclList.isPresent()) {
      bodyDeclList.get().forEach(bodyDecl -> BodyDeclarationParser.parse(bodyDecl, astRepresentation));
    }
  };

  private static Consumer<PatternExpr> patternExprConsumer = (PatternExpr expr) -> {
    addTypeName(expr);
    System.err.println("PatternExpr needs to be implemented");
    // TODO:
  };

  private static Consumer<SuperExpr> superExprConsumer = (SuperExpr expr) -> {
    addTypeName(expr);
    astRepresentation.add(expr.toString());
  };

  private static Consumer<SwitchExpr> switchExprConsumer = (SwitchExpr expr) -> {
    addTypeName(expr);
    System.err.println("SwitchExpr needs to be implemented");
    // TODO:
  };

  private static Consumer<ThisExpr> thisExprConsumer = (ThisExpr expr) -> {
    Optional<Name> typeName = expr.getTypeName();
    if (typeName.isPresent()) {
      astRepresentation.add(ParserUtility.createTag(typeName.get().getMetaModel().getTypeName()));
      astRepresentation.add(typeName.get().getIdentifier());
    }
    addTypeName(expr);
    astRepresentation.add("this");
  };

  private static Consumer<TypeExpr> typeExprConsumer = (TypeExpr expr) -> {
    addTypeName(expr);
    astRepresentation = TypeParser.parse(expr.getType(), astRepresentation);
  };

  private static Consumer<UnaryExpr> unaryExprConsumer = (UnaryExpr expr) -> {
    addTypeName(expr);
    if (expr.isPostfix()) {
      astRepresentation = parse(expr.getExpression(), astRepresentation);
      astRepresentation.add(expr.getOperator().name());
    } else {
      astRepresentation.add(expr.getOperator().name());
      astRepresentation = parse(expr.getExpression(), astRepresentation);
    }
  };

  private static Consumer<VariableDeclarationExpr> variableDeclarationExprConsumer = (VariableDeclarationExpr expr) -> {
    addTypeName(expr);
    ParserUtility.addAnnotations(expr.getAnnotations(), astRepresentation);
    ParserUtility.addModifiers(expr.getModifiers(), astRepresentation);
    for (VariableDeclarator declarator : expr.getVariables()) {
      astRepresentation.add(ParserUtility.createTag(declarator.getMetaModel().getTypeName()));
      astRepresentation = TypeParser.parse(declarator.getType(), astRepresentation);
      astRepresentation.add(declarator.getNameAsString());
      Optional<Expression> init = declarator.getInitializer();
      if (init.isPresent()) {
        astRepresentation.add("ASSIGN");
        astRepresentation = parse(init.get(), astRepresentation);
      }
    }
  };

  private static void addTypeName(Expression expr) {
    astRepresentation.add(ParserUtility.createTag(expr.getMetaModel().getTypeName()));
  }

}
