import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;

public final class ParserUtility {
  private static Set<String> tags = new HashSet<>();

  private ParserUtility() {
    throw new UnsupportedOperationException("Utility class and cannot be instantiated");
  }

  public static void addAnnotations(NodeList<AnnotationExpr> annotations, StringJoiner astRepresentation) {
    for (AnnotationExpr annotation : annotations) {
      astRepresentation = ExpressionParser.parse(annotation, astRepresentation);
    }
  }

  public static void addModifiers(NodeList<Modifier> modifiers, StringJoiner astRepresentation) {
    for (Modifier modifier : modifiers) {
      astRepresentation.add(createTag(modifier.getMetaModel().toString()));
      astRepresentation.add(modifier.toString());
    }
  }

  public static void addParameters(NodeList<Parameter> parameters, StringJoiner astRepresentation) {
    if(parameters.isNonEmpty()){
      astRepresentation.add("(");
      int iterationCounter = 0;
      for (Parameter parameter : parameters) {
        iterationCounter += 1;
        astRepresentation.add(createTag(parameter.getMetaModel().toString()));
        addAnnotations(parameter.getAnnotations(), astRepresentation);
        addModifiers(parameter.getModifiers(), astRepresentation);
        astRepresentation = TypeParser.parse(parameter.getType(), astRepresentation);
        astRepresentation.add(parameter.getNameAsString());
        if (iterationCounter < parameters.size())
          astRepresentation.add(",");
      }
      astRepresentation.add(")");
    }
  }

  public static void addArguments(NodeList<Expression> args, StringJoiner astRepresentation){
    if (args.isNonEmpty()){
      astRepresentation.add("(");
      for(int i = 0; i < args.size(); i++){
        astRepresentation = ExpressionParser.parse(args.get(i), astRepresentation);
        if(i != args.size()-1)
          astRepresentation.add(",");
      }
      astRepresentation.add(")");
    }else{
      astRepresentation.add("( )");
    }
  }

  public static void addTypeArguments(NodeList<Type> typeArgs, StringJoiner astRepresentation){
    if(typeArgs.isNonEmpty()){
      astRepresentation.add("<");
      astRepresentation = TypeParser.parse(typeArgs.get(0), astRepresentation);
      for(int i = 1; i< typeArgs.size(); i++){
        astRepresentation.add(",");
        astRepresentation = TypeParser.parse(typeArgs.get(i), astRepresentation);
      }
      astRepresentation.add(">");
    }else{
      astRepresentation.add("< >");
    }
  }

  public static String createTag(String nodeTypeName) {
    String tag = String.format("<ast_node_%s>", nodeTypeName);
    return tag;
  }

}