/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.util.DefaultTextOutput;
import com.google.gwt.resources.css.CssGenerationVisitor;
import com.google.gwt.resources.css.GenerateCssAst;
import com.google.gwt.resources.css.ast.CssStylesheet;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author edewit@redhat.com
 */
public class SelectorMinifyVisitorTest {

  @Test
  public void shouldCreateListOfSelectorsAndTheirMinifiedCounterParts() throws UnableToCompleteException, URISyntaxException {
    // given
    SelectorMinifyVisitor visitor = new SelectorMinifyVisitor();
    final CssStylesheet stylesheet = GenerateCssAst.exec(TreeLogger.NULL, getClass().getResource("/simple.css"));

    // when
    visitor.accept(stylesheet);
    Map<String,String> convertedSelectors = visitor.getConvertedSelectors();

    // then
    assertNotNull(convertedSelectors);
    assertEquals(new HashSet<String>(
            asList("title", "store", "test", "item", "name", "merge", "active", "dummy", "something", "comment")),
            convertedSelectors.keySet());
  }

  @Test
  public void shouldReuseAlreadyMnifiedSelectors() throws UnableToCompleteException {
    // given
    SelectorMinifyVisitor visitor = new SelectorMinifyVisitor();
    final CssStylesheet stylesheet = GenerateCssAst.exec(TreeLogger.NULL, getClass().getResource("/reuse.css"));

    // when
    visitor.accept(stylesheet);

    //then

    final DefaultTextOutput defaultTextOutput = new DefaultTextOutput(false);
    new CssGenerationVisitor(defaultTextOutput).accept(stylesheet);

    final String minified = "E1ukd9qA";
    final int matches = StringUtils.countMatches(defaultTextOutput.toString(), minified);
    assertEquals(2, matches);

    final Map<String, String> selectors = visitor.getConvertedSelectors();
    assertEquals(minified, selectors.get("store"));
  }
}
