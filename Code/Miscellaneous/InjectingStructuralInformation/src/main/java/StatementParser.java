import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;

public final class StatementParser {

  private static StringJoiner astRepresentation = new StringJoiner(" ");

  private StatementParser() {
  }

  public static StringJoiner parse(Statement stmt, StringJoiner astRepr) {
    astRepresentation = astRepr;
    astRepresentation.add(ParserUtility.createTag(stmt.getMetaModel().getTypeName()));
    stmt.ifAssertStmt(assertStmtConsumer);
    stmt.ifBlockStmt(blockStmtConsumer);
    stmt.ifBreakStmt(breakStmtConsumer);
    stmt.ifContinueStmt(continueStmtConsumer);
    stmt.ifDoStmt(doStmtConsumer);
    // stmt.ifEmptyStmt(emptyStmtConsumer);
    stmt.ifExplicitConstructorInvocationStmt(explicitConstructorInvocationStmtConsumer);
    stmt.ifExpressionStmt(expressionStmtConsumer);
    stmt.ifForEachStmt(forEachStmtConsumer);
    stmt.ifForStmt(forStmtConsumer);
    stmt.ifIfStmt(ifStmtConsumer);
    stmt.ifLabeledStmt(labeledStmtConsumer);
    stmt.ifLocalClassDeclarationStmt(localClassDeclarationStmtConsumer);
    stmt.ifLocalRecordDeclarationStmt(localRecordDeclarationStmtConsumer);
    stmt.ifReturnStmt(returnStmtConsumer);
    stmt.ifSwitchStmt(switchStmtConsumer);
    stmt.ifSynchronizedStmt(synchronizedStmtConsumer);
    stmt.ifThrowStmt(throwStmtConsumer);
    stmt.ifTryStmt(tryStmtConsumer);
    stmt.ifUnparsableStmt(unparsableStmtConsumer);
    stmt.ifWhileStmt(whileStmtConsumer);
    stmt.ifYieldStmt(yieldStmtConsumer);
    return astRepresentation;
  }

  private static Consumer<AssertStmt> assertStmtConsumer = (AssertStmt stmt) -> {
    astRepresentation.add("assert");
    astRepresentation = ExpressionParser.parse(stmt.getCheck(), astRepresentation);
    if (stmt.getMessage().isPresent()) {
      astRepresentation = ExpressionParser.parse(stmt.getMessage().get(), astRepresentation);
    }
  };

  private static Consumer<BlockStmt> blockStmtConsumer = (BlockStmt stmt) -> {
    astRepresentation.add("{");
    for (Statement statement : stmt.asBlockStmt().getStatements()){
      astRepresentation = parse(statement, astRepresentation);
      astRepresentation.add(";");
    }
    astRepresentation.add("}");
  };

  private static Consumer<BreakStmt> breakStmtConsumer = (BreakStmt stmt) -> {
    astRepresentation.add("break");
    Optional<SimpleName> label = stmt.getLabel();
    if (label.isPresent())
      astRepresentation.add(label.get().getIdentifier());
  };

  private static Consumer<ContinueStmt> continueStmtConsumer = (ContinueStmt stmt) -> {
    astRepresentation.add("continue");
    Optional<SimpleName> label = stmt.getLabel();
    if (label.isPresent())
      astRepresentation.add(label.get().getIdentifier());
  };

  private static Consumer<DoStmt> doStmtConsumer = (DoStmt stmt) -> {
    astRepresentation.add("do");
    astRepresentation = parse(stmt.getBody(), astRepresentation);
    astRepresentation.add("while");
    astRepresentation.add("(");
    astRepresentation = ExpressionParser.parse(stmt.getCondition(), astRepresentation);
    astRepresentation.add(")");
  };

  private static Consumer<EmptyStmt> emptyStmtConsumer = (EmptyStmt stmt) -> {
    astRepresentation.add(";");
  };

  private static Consumer<ExplicitConstructorInvocationStmt> explicitConstructorInvocationStmtConsumer = (
      ExplicitConstructorInvocationStmt stmt) -> {
    String keyword = stmt.isThis() ? "this" : "super";
    astRepresentation.add(keyword);

    Optional<Expression> expression = stmt.getExpression();
    if (expression.isPresent()) {
      astRepresentation = ExpressionParser.parse(expression.get(), astRepresentation);
    }
    Optional<NodeList<Type>> typeArguments = stmt.getTypeArguments();
    if (typeArguments.isPresent()) {
      for (Type typeArg : typeArguments.get()) {
        astRepresentation = TypeParser.parse(typeArg, astRepresentation);
      }
    }
    ParserUtility.addArguments(stmt.getArguments(), astRepresentation);
  };

  private static Consumer<ExpressionStmt> expressionStmtConsumer = (ExpressionStmt stmt) -> {
    astRepresentation = ExpressionParser.parse(stmt.asExpressionStmt().getExpression(), astRepresentation);
  };

  private static Consumer<ForEachStmt> forEachStmtConsumer = (ForEachStmt stmt) -> {
    astRepresentation.add("for");
    astRepresentation = ExpressionParser.parse(stmt.getVariable(), astRepresentation);
    astRepresentation.add(":");
    astRepresentation = ExpressionParser.parse(stmt.getIterable(), astRepresentation);
    astRepresentation = parse(stmt.getBody(), astRepresentation);
  };

  private static Consumer<ForStmt> forStmtConsumer = (ForStmt stmt) -> {
    astRepresentation.add("for");
    for (Expression init : stmt.getInitialization()) {
      astRepresentation = ExpressionParser.parse(init, astRepresentation);
    }
    Optional<Expression> compare = stmt.getCompare();
    if (compare.isPresent()) {
      astRepresentation = ExpressionParser.parse(compare.get(), astRepresentation);
    }
    for (Expression update : stmt.getUpdate()) {
      astRepresentation = ExpressionParser.parse(update, astRepresentation);
    }
    astRepresentation = parse(stmt.getBody(), astRepresentation);
  };

  private static Consumer<IfStmt> ifStmtConsumer = (IfStmt stmt) -> {
    astRepresentation.add("if");
    astRepresentation.add("(");
    astRepresentation = ExpressionParser.parse(stmt.getCondition(), astRepresentation);
    astRepresentation.add(")");
    astRepresentation = parse(stmt.getThenStmt(), astRepresentation);
    Optional<Statement> elseStmt = stmt.getElseStmt();
    if (elseStmt.isPresent()) {
      astRepresentation.add("else");
      astRepresentation = parse(elseStmt.get(), astRepresentation);
    }
  };

  private static Consumer<LabeledStmt> labeledStmtConsumer = (LabeledStmt stmt) -> {
    astRepresentation.add(stmt.getLabel().getIdentifier());
    astRepresentation.add(":");
    astRepresentation = parse(stmt.getStatement(), astRepresentation);
  };

  private static Consumer<LocalClassDeclarationStmt> localClassDeclarationStmtConsumer = (
      LocalClassDeclarationStmt stmt) -> {
    astRepresentation = BodyDeclarationParser.parse(stmt.getClassDeclaration(), astRepresentation);
  };

  private static Consumer<LocalRecordDeclarationStmt> localRecordDeclarationStmtConsumer = (
      LocalRecordDeclarationStmt stmt) -> {
    System.err.println("LocalRecordDeclarationStmt needs to be implemented");
    // TODO:
  };

  private static Consumer<ReturnStmt> returnStmtConsumer = (ReturnStmt stmt) -> {
    astRepresentation.add("return");
    Optional<Expression> expr = stmt.getExpression();
    if (expr.isPresent()) {
      astRepresentation = ExpressionParser.parse(expr.get(), astRepresentation);
    }
  };

  private static Consumer<SwitchStmt> switchStmtConsumer = (SwitchStmt stmt) -> {
    astRepresentation.add("switch");
    astRepresentation = ExpressionParser.parse(stmt.getSelector(), astRepresentation);
    for (SwitchEntry entry : stmt.getEntries()) {
      astRepresentation.add(ParserUtility.createTag(entry.getMetaModel().getTypeName()));
      for (Expression label : entry.getLabels()) {
        astRepresentation = ExpressionParser.parse(label, astRepresentation);
      }
      for (Statement switchEntryStmt : entry.getStatements()) {
        astRepresentation = parse(switchEntryStmt, astRepresentation);
      }
    }
  };

  private static Consumer<SynchronizedStmt> synchronizedStmtConsumer = (SynchronizedStmt stmt) -> {
    astRepresentation = ExpressionParser.parse(stmt.getExpression(), astRepresentation);
    astRepresentation = parse(stmt.getBody(), astRepresentation);
  };

  private static Consumer<ThrowStmt> throwStmtConsumer = (ThrowStmt stmt) -> {
    astRepresentation.add("throw");
    ExpressionParser.parse(stmt.getExpression(), astRepresentation);
  };

  private static Consumer<UnparsableStmt> unparsableStmtConsumer = (UnparsableStmt stmt) -> {
    System.err.println("Unparseable stmt: " + stmt.toString());
  };

  private static Consumer<WhileStmt> whileStmtConsumer = (WhileStmt stmt) -> {
    astRepresentation.add("while");
    astRepresentation.add("(");
    astRepresentation = ExpressionParser.parse(stmt.getCondition(), astRepresentation);
    astRepresentation.add(")");
    astRepresentation = parse(stmt.getBody(), astRepresentation);
  };

  private static Consumer<YieldStmt> yieldStmtConsumer = (YieldStmt stmt) -> {
    astRepresentation.add("yield");
    astRepresentation = ExpressionParser.parse(stmt.getExpression(), astRepresentation);
  };

  private static Consumer<TryStmt> tryStmtConsumer = (TryStmt stmt) -> {
    astRepresentation.add("try");
    for (Expression resource : stmt.getResources()) {
      astRepresentation = ExpressionParser.parse(resource, astRepresentation);
    }
    astRepresentation.add(ParserUtility.createTag(stmt.getTryBlock().getMetaModel().getTypeName()));
    for (Statement statement : stmt.getTryBlock().getStatements()) {
      astRepresentation = parse(statement, astRepresentation);
    }
    for (CatchClause clause : stmt.getCatchClauses()) {
      astRepresentation.add(ParserUtility.createTag(clause.getMetaModel().getTypeName()));
      astRepresentation.add("catch");
      astRepresentation.add(ParserUtility.createTag(clause.getParameter().getMetaModel().getTypeName()));
      astRepresentation = TypeParser.parse(clause.getParameter().getType(), astRepresentation);
      astRepresentation.add(clause.getParameter().getNameAsString());
      astRepresentation = parse(clause.getBody(), astRepresentation);
    }
    Optional<BlockStmt> finallyStmt = stmt.getFinallyBlock();
    if (finallyStmt.isPresent()) {
      astRepresentation.add("finally");
      astRepresentation = parse(finallyStmt.get(), astRepresentation);
    }
  };
}
