import com.github.tonivade.purejson.JsonAnnotationProcessor;

module com.github.tonivade.purejson {
  exports com.github.tonivade.purejson;

  provides javax.annotation.processing.Processor with JsonAnnotationProcessor;

  requires transitive com.github.tonivade.purefun;
  requires transitive com.github.tonivade.purefun.core;
  requires com.squareup.javapoet;
  requires transitive java.compiler;
  requires com.eclipsesource.json;
}