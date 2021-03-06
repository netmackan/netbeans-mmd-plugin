package com.igormaznitsa.ideamindmap.lang.highlighter;

import com.igormaznitsa.ideamindmap.lang.MMLexer;
import com.igormaznitsa.ideamindmap.lang.tokens.MMTokens;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import gnu.trove.THashMap;

import javax.annotation.Nonnull;
import java.util.Map;

public class MMHighlighter extends SyntaxHighlighterBase {
  private static final Map<IElementType, TextAttributesKey> keys1;
  private static final Map<IElementType, TextAttributesKey> keys2;

  @Nonnull
  public Lexer getHighlightingLexer() {
    return new MMLexer();
  }

  public static final TextAttributesKey MMD_HEADER_LINE = TextAttributesKey.createTextAttributesKey(
    "MMD.HEADE_LINE",
    DefaultLanguageHighlighterColors.LINE_COMMENT
  );

  public static final TextAttributesKey MMD_HEADER_DELIMITER = TextAttributesKey.createTextAttributesKey(
    "MMD.HEADER_DELIMITER",
    DefaultLanguageHighlighterColors.KEYWORD
  );

  public static final TextAttributesKey MMD_ATTRIBUTES = TextAttributesKey.createTextAttributesKey(
    "MMD.ATTRIBUTES",
    DefaultLanguageHighlighterColors.MARKUP_ATTRIBUTE
  );

  public static final TextAttributesKey MMD_TOPIC = TextAttributesKey.createTextAttributesKey(
    "MMD.TOPIC",
    DefaultLanguageHighlighterColors.KEYWORD
  );

  public static final TextAttributesKey MMD_EXTRA_NAME = TextAttributesKey.createTextAttributesKey(
    "MMD.EXTRANAME",
    DefaultLanguageHighlighterColors.LABEL
  );

  public static final TextAttributesKey MMD_EXTRA_TEXT = TextAttributesKey.createTextAttributesKey(
    "MMD.EXTRATEXT",
    DefaultLanguageHighlighterColors.STRING
  );

  public static final TextAttributesKey MMD_UNKNOWN= TextAttributesKey.createTextAttributesKey(
    "MMD.UNKNOWN",
    DefaultLanguageHighlighterColors.METADATA
  );

  public static final TextAttributesKey MMD_FILE= TextAttributesKey.createTextAttributesKey(
    "MMD.FILE",
    DefaultLanguageHighlighterColors.GLOBAL_VARIABLE
  );

  static {
    keys1 = new THashMap<IElementType, TextAttributesKey>();
    keys2 = new THashMap<IElementType, TextAttributesKey>();

    keys1.put(MMTokens.HEADER_LINE, MMD_HEADER_LINE);
    keys1.put(MMTokens.HEADER_DELIMITER, MMD_HEADER_DELIMITER);
    keys1.put(MMTokens.ATTRIBUTES, MMD_ATTRIBUTES);
    keys1.put(MMTokens.EXTRA_BODY, MMD_EXTRA_TEXT);
    keys1.put(MMTokens.EXTRA_TYPE, MMD_EXTRA_NAME);
    keys1.put(MMTokens.UNKNOWN, MMD_UNKNOWN);
    keys1.put(MMTokens.TOPIC, MMD_TOPIC);
    keys1.put(MMTokens.FILE, MMD_FILE);
  }

  @Nonnull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    return SyntaxHighlighterBase.pack(keys1.get(tokenType), keys2.get(tokenType));
  }

  public static final Map<TextAttributesKey, Pair<String, HighlightSeverity>> DISPLAY_NAMES = new THashMap<TextAttributesKey, Pair<String, HighlightSeverity>>(6);
  static {
    DISPLAY_NAMES.put(MMD_HEADER_LINE, Pair.create("Header line",HighlightSeverity.INFORMATION));
    DISPLAY_NAMES.put(MMD_HEADER_DELIMITER, Pair.create("Header delimiter", HighlightSeverity.INFORMATION));
    DISPLAY_NAMES.put(MMD_ATTRIBUTES, Pair.create("Attributes", HighlightSeverity.INFORMATION));
    DISPLAY_NAMES.put(MMD_TOPIC, Pair.create("Topic", HighlightSeverity.INFORMATION));
    DISPLAY_NAMES.put(MMD_FILE, Pair.create("File", HighlightSeverity.INFORMATION));
    DISPLAY_NAMES.put(MMD_EXTRA_NAME, Pair.create("Extra name", HighlightSeverity.INFORMATION));
    DISPLAY_NAMES.put(MMD_EXTRA_TEXT, Pair.create("Extra name", HighlightSeverity.INFORMATION));
    DISPLAY_NAMES.put(MMD_UNKNOWN, Pair.create("Unknown", HighlightSeverity.WARNING));
  }
}
