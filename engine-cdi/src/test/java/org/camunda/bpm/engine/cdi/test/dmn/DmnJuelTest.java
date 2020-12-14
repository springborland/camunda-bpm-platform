/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.cdi.test.dmn;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.cdi.test.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;

import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;

public class DmnJuelTest extends CdiProcessEngineTestCase {

  @Named
  public static class BeanFoo {

    protected String bar = "baz";

    public String getBar() {
      return bar;
    }

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/cdi/test/dmn/JuelTest.dmn"})
  public void shouldResolveBean() {
    // given

    // when
    DmnDecisionTableResult result = decisionService.evaluateDecisionTableByKey("Decision_00khy2n")
        .evaluate();

    // then
    assertThat((String)result.getSingleEntry()).isEqualTo("bar");
  }

}
