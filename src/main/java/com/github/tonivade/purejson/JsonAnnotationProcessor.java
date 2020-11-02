/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.purejson;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;

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

          List<RecordComponentElement> fields = element.getEnclosedElements().stream()
              .filter(e -> e.getKind() == ElementKind.RECORD_COMPONENT)
              .map(e -> (RecordComponentElement) e)
              .collect(toList());

          List<ExecutableElement> constructors = element.getEnclosedElements().stream()
              .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
              .map(e -> (ExecutableElement) e)
              .filter(c -> c.getParameters().size() == fields.size())
              .collect(toList());

          System.out.println("Record: " + element.getSimpleName());
          System.out.println("fields:" + fields);
          System.out.println("accessors:" + fields.stream().map(RecordComponentElement::getAccessor).collect(toList()));
          System.out.println("constructors:" + constructors);
        } else if (element.getKind() == ElementKind.CLASS) {
          processingEnv.getMessager().printMessage(Kind.NOTE, element.getSimpleName() + " pojo found");

          List<VariableElement> fields = element.getEnclosedElements().stream()
              .filter(e -> e.getKind() == ElementKind.FIELD)
              .map(e -> (VariableElement) e)
              .collect(toList());

          List<ExecutableElement> constructors = element.getEnclosedElements().stream()
              .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
              .map(e -> (ExecutableElement) e)
              .filter(c -> c.getParameters().size() == fields.size())
              .collect(toList());

          List<ExecutableElement> methods = element.getEnclosedElements().stream()
              .filter(e -> e.getKind() == ElementKind.METHOD)
              .filter(e -> e.getSimpleName().toString().startsWith("get"))
              .map(e -> (ExecutableElement) e)
              .collect(toList());
          
          System.out.println("Pojo: " + element.getSimpleName());
          System.out.println("fields:" + fields);
          System.out.println("accessors:" + methods);
          System.out.println("constructors:" + constructors);
        } else {
          processingEnv.getMessager().printMessage(Kind.ERROR, element.getSimpleName() + " is not supported: " + element.getKind());
        }
      }
    }
    return true;
  }
}
