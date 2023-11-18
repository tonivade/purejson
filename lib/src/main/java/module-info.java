import com.github.tonivade.purejson.JsonAnnotationProcessor;

module com.github.tonivade.purejson {
  exports com.github.tonivade.purejson;
  
  provides javax.annotation.processing.Processor with JsonAnnotationProcessor;

  requires com.github.tonivade.purefun.annotation;
  requires com.github.tonivade.purefun.core;
  requires com.squareup.javapoet;
  requires java.compiler;
  requires com.eclipsesource.json;
}