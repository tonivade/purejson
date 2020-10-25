/*
 * Copyright (c) 2020, Antonio Gabriel Muñoz Conejo <antoniogmc at gmail dot com>
 * Distributed under the terms of the MIT License
 */
package com.github.tonivade.json;

import static com.github.tonivade.json.JsonElement.EMPTY_ARRAY;
import static com.github.tonivade.json.JsonElement.EMPTY_OBJECT;
import static com.github.tonivade.json.JsonElement.array;
import static com.github.tonivade.json.JsonElement.object;
import static com.github.tonivade.json.JsonPrimitive.number;
import static com.github.tonivade.json.JsonPrimitive.string;

import java.util.LinkedHashMap;
import java.util.List;

import org.petitparser.grammar.json.JsonGrammarDefinition;
import org.petitparser.tools.GrammarParser;
import org.petitparser.utils.Functions;

public class JsonParser extends GrammarParser {

  public JsonParser() {
    super(new JsonParserDefinition());
  }

  private static final class JsonParserDefinition extends JsonGrammarDefinition {

    public JsonParserDefinition() {
      action("elements", Functions.withoutSeparators());
      action("members", Functions.withoutSeparators());
      action("array", (List<List<JsonElement>> input) ->
        input.get(1) != null ? array(input.get(1)) : EMPTY_ARRAY);
      action("object", (List<List<List<Object>>> input) -> {
        if (input.get(1) != null) {
          var result = new LinkedHashMap<String, JsonElement>();
          for (List<Object> list : input.get(1)) {
            result.put(
                ((JsonPrimitive.JsonString) list.get(0)).value(),
                (JsonElement) list.get(2));
          }
          return object(result.entrySet());
        }
        return EMPTY_OBJECT;
      });

      action("trueToken", Functions.constant(JsonPrimitive.TRUE));
      action("falseToken", Functions.constant(JsonPrimitive.FALSE));
      action("nullToken", Functions.constant(JsonElement.NULL));
      redef("stringToken", ref("stringPrimitive").trim());
      action("numberToken", (String input) -> {
        double floating = Double.parseDouble(input);
        long integral = (long) floating;
        if (floating == integral && input.indexOf('.') == -1) {
          return number(integral);
        } else {
          return number(floating);
        }
      });

      action("stringPrimitive",
          (List<List<Character>> input) -> string(listToString(input.get(1))));
      action("characterEscape", Functions.lastOfList());
      action("characterEscape", ESCAPE_TABLE_FUNCTION);
      action("characterOctal", (List<String> input) -> {
        // cannot be larger than 0xFFFF, so we should be safe with 16-bit
        return Character.toChars(Integer.parseInt(input.get(1), 16))[0];
      });
    }
  }
}
