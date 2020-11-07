/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static com.github.tonivade.purefun.data.ImmutableList.toImmutableList;
import static com.github.tonivade.purefun.data.ImmutableMap.toImmutableMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.github.tonivade.purefun.Tuple;
import com.github.tonivade.purefun.Tuple2;
import com.github.tonivade.purefun.data.ImmutableList;
import com.github.tonivade.purefun.data.ImmutableMap;
import com.github.tonivade.purefun.data.Sequence;
import com.github.tonivade.purefun.type.Option;
import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

@SuppressWarnings("preview")
@SupportedAnnotationTypes("com.github.tonivade.purejson.Json")
public class JsonAnnotationProcessor extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (TypeElement annotation : annotations) {
      for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
        if (element.getKind() == ElementKind.RECORD) {
          processingEnv.getMessager().printMessage(Kind.NOTE, element.getSimpleName() + " record found");
          saveFile(modelForRecord((TypeElement) element));
        } else if (element.getKind() == ElementKind.CLASS) {
          processingEnv.getMessager().printMessage(Kind.NOTE, element.getSimpleName() + " pojo found");
          saveFile(modelForPojo((TypeElement) element));
        } else {
          processingEnv.getMessager().printMessage(
              Kind.ERROR, element.getSimpleName() + " is not supported: " + element.getKind());
        }
      }
    }
    return true;
  }

  record Model(String packageName, String name, TypeMirror type, Sequence<Field> fields) {

    String getAdapterName() {
      return name + "Adapter";
    }

    public JavaFile build() {
      TypeSpec typeSpec = TypeSpec.enumBuilder(getAdapterName())
          .addModifiers(Modifier.PUBLIC)
          .addSuperinterface(ParameterizedTypeName.get(ClassName.get(JsonAdapter.class), TypeName.get(type)))
          .addEnumConstant("INSTANCE")
          .addFields(buildAdapters())
          .addMethod(MethodSpec.methodBuilder("encode")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(TypeName.get(type), "value")
              .returns(JsonNode.class)
              .addCode(encodeMethod())
              .build())
          .addMethod(MethodSpec.methodBuilder("decode")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(JsonNode.class, "node")
              .returns(TypeName.get(type))
              .addCode(decodeMethod())
              .build())
          .build();
      return JavaFile.builder(packageName, typeSpec).build();
    }

    private List<FieldSpec> buildAdapters() {
      var list = new ArrayList<FieldSpec>();
      for (var field : fields) {
        list.add(
            FieldSpec.builder(
                ParameterizedTypeName.get(ClassName.get(JsonAdapter.class), TypeName.get(field.type).box()),
                    field.getAdapterName(), Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(CodeBlock.builder().add("$T.adapter($L)", JsonAdapter.class, field.getFieldType()).build())
                .build());
      }
      return list;
    }

    private CodeBlock encodeMethod() {
      var builder = CodeBlock.builder();
      for (var field : fields) {
        builder.addStatement("var $N = $T.entry($S, $L.encode($N.$N()))",
            field.name, JsonDSL.class, field.name, field.getAdapterName(), "value", field.accessor.getSimpleName());
      }
      String params = fields.map(Field::name).join(", ");
      return builder.addStatement("return $T.object($L)", JsonDSL.class, params).build();
    }

    private CodeBlock decodeMethod() {
      var builder = CodeBlock.builder();
      builder.addStatement("var $N = $N.asObject()", "object", "node");
      for (var field : fields) {
        builder.addStatement("var $N = $L.decode($N.get($S))",
            field.name, field.getAdapterName(), "object", field.name);
      }
      String params = fields.map(Field::name).join(", ");
      return builder.addStatement("return new $N($L)", name, params).build();
    }
  }

  record Field(String name, TypeMirror type, ExecutableElement accessor) {

    String getAdapterName() {
      return name.toUpperCase() + "_ADAPTER";
    }

    CodeBlock getFieldType() {
      var typeName = TypeName.get(type);
      if (typeName.isPrimitive()) {
        return CodeBlock.builder().add("$T.class", typeName).build();
      }
      if (typeName instanceof ClassName c) {
        return CodeBlock.builder().add("$T.class", typeName).build();
      }
      if (typeName instanceof ParameterizedTypeName p) {
        return CodeBlock.builder().add("new $T<$T>(){}.getType()", TypeToken.class, p).build();
      }
      throw new UnsupportedOperationException(typeName.toString());
    }
  }

  private void saveFile(Model model) {
    try {
      JavaFileObject test = createFile(model.packageName(), model.getAdapterName());

      try (Writer openWriter = test.openWriter()) {
        model.build().writeTo(openWriter);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private JavaFileObject createFile(String packageName, String className) throws IOException {
    String qualifiedName = packageName != null ? packageName + "." + className : className;
    return processingEnv.getFiler().createSourceFile(qualifiedName);
  }

  private Model modelForPojo(TypeElement element) {
    ImmutableList<VariableElement> fields = element.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.FIELD)
        .map(e -> (VariableElement) e)
        .collect(toImmutableList());

    element.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
        .map(e -> (ExecutableElement) e)
        .filter(c -> c.getParameters().size() == fields.size())
        .findFirst().map(Option::some).orElseGet(Option::none)
        .ifEmpty(() -> processingEnv.getMessager()
            .printMessage(Kind.ERROR, "no proper constructor found: " + element.getSimpleName()
                + fields.map(VariableElement::asType).join(",", "(", ")")));

    ImmutableMap<String, ExecutableElement> methods = element.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.METHOD)
        .filter(e -> e.getSimpleName().toString().startsWith("get"))
        .map(e -> (ExecutableElement) e)
        .map(e -> Tuple.of(e.getSimpleName().toString(), e))
        .collect(toImmutableMap(Tuple2::get1, Tuple2::get2));

    String qualifiedName = element.getQualifiedName().toString();
    String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
    String simpleName = element.getSimpleName().toString();

    return new Model(packageName, simpleName, element.asType(), fields.stream().map(
        f -> {
          var name = f.getSimpleName().toString();
          var key = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
          var accessor = methods.get(key).getOrElseThrow();
          return new Field(
              f.getSimpleName().toString(),
              accessor.getReturnType(),
              accessor);
        })
        .collect(toImmutableList()));
  }

  private Model modelForRecord(TypeElement element) {
    ImmutableList<RecordComponentElement> fields = element.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.RECORD_COMPONENT)
        .map(e -> (RecordComponentElement) e)
        .collect(toImmutableList());

    element.getEnclosedElements().stream()
        .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
        .map(e -> (ExecutableElement) e)
        .findFirst().map(Option::some).orElseGet(Option::none)
        .ifEmpty(() -> processingEnv.getMessager()
            .printMessage(Kind.ERROR, "no proper constructor found: " + element.getSimpleName()
                + fields.map(RecordComponentElement::asType).join(",", "(", ")")));

    String qualifiedName = element.getQualifiedName().toString();
    String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
    String simpleName = element.getSimpleName().toString();

    return new Model(packageName, simpleName, element.asType(), fields.stream().map(
        f -> new Field(
            f.getSimpleName().toString(),
            f.getAccessor().getReturnType(),
            f.getAccessor()))
        .collect(toImmutableList()));
  }
}
