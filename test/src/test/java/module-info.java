module com.github.tonivade.purejson.test {
  exports com.github.tonivade.purejson.test;

  requires transitive com.github.tonivade.purefun;
  requires transitive com.github.tonivade.purefun.core;
  requires transitive com.github.tonivade.purefun.monad;
  requires transitive com.github.tonivade.purefun.instances;
  requires transitive com.github.tonivade.purefun.typeclasses;
  requires transitive com.github.tonivade.purejson;
  requires transitive com.github.tonivade.purecheck;
  requires transitive java.compiler;
  requires transitive org.junit.jupiter.api;
  requires transitive com.google.gson;
}
