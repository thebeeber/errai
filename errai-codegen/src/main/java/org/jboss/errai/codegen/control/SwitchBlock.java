/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.control;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.codegen.AbstractStatement;
import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.exception.InvalidTypeException;
import org.jboss.errai.codegen.literal.LiteralValue;
import org.jboss.errai.codegen.meta.MetaClassFactory;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class SwitchBlock extends AbstractStatement {
  private List<Class<?>> supportedTypes = new ArrayList<Class<?>>() {
    {
      add(Integer.class);
      add(Character.class);
      add(Short.class);
      add(Byte.class);
      add(Enum.class);
    }
  };

  private Statement switchExprStmt;
  private String switchExpr;
  private Map<LiteralValue<?>, BlockStatement> caseBlocks = new LinkedHashMap<LiteralValue<?>, BlockStatement>();
  private BlockStatement defaultBlock;

  public SwitchBlock() {}

  public SwitchBlock(Statement switchExprStmt) {
    this.switchExprStmt = switchExprStmt;
  }

  public void addCase(LiteralValue<?> value) {
    caseBlocks.put(value, new BlockStatement());
  }

  public BlockStatement getCaseBlock(LiteralValue<?> value) {
    return caseBlocks.get(value);
  }

  public BlockStatement getDefaultBlock() {
    if (defaultBlock == null)
      defaultBlock = new BlockStatement();

    return defaultBlock;
  }

  public void setSwitchExpr(Statement switchExprStmt) {
    this.switchExprStmt = switchExprStmt;
  }

  public void setSwitchExpr(String expr) {
    this.switchExpr = expr;
  }

  @Override
  public String generate(Context context) {
    StringBuilder buf = new StringBuilder("switch (");
    if (switchExpr == null) {
      buf.append(switchExprStmt.generate(context)).append(") {\n ");
    }
    else {
      buf.append(switchExpr).append(") {\n ");
    }

    checkSwitchExprType();

    if (!caseBlocks.isEmpty()) {
      for (LiteralValue<?> value : caseBlocks.keySet()) {
        if (!switchExprStmt.getType().getErased().asBoxed().isAssignableFrom(value.getType().getErased())) {
          throw new InvalidTypeException(
              value.generate(context) + " is not a valid value for " + switchExprStmt.getType().getFullyQualifiedName());
        }
        // case labels must be unqualified
        String val = value.generate(context);
        int idx = val.lastIndexOf('.');
        if (idx != -1) {
          val = val.substring(idx + 1);
        }
        buf.append("case ").append(val).append(": ").append(getCaseBlock(value).generate(Context.create(context)))
            .append("\n");
      }
    }

    if (defaultBlock != null) {
      buf.append("default: ").append(defaultBlock.generate(Context.create(context))).append("\n");
    }

    return buf.append("}").toString();
  }

  private void checkSwitchExprType() {
    boolean validType = false;
    for (Class<?> clazz : supportedTypes) {
      if (MetaClassFactory.get(clazz).isAssignableFrom(switchExprStmt.getType().asBoxed())) {
        validType = true;
        break;
      }
    }
    if (!validType)
      throw new InvalidTypeException("Type not permitted in switch statements:" + 
          switchExprStmt.getType().getFullyQualifiedName());
  }
}
