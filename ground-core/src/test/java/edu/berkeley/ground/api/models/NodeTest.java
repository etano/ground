/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package edu.berkeley.ground.api.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class NodeTest {
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializesToJSON() throws Exception {
        final Node node = new Node("Nodes.test", "test");
        final String expected = MAPPER.writeValueAsString(MAPPER.readValue(fixture("fixtures/models/node.json"), Node.class));

        assertThat(MAPPER.writeValueAsString(node)).isEqualTo(expected);
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        final Node node = new Node("Nodes.test", "test");
        assertThat(MAPPER.readValue(fixture("fixtures/models/node.json"), Node.class)).isEqualToComparingFieldByField(node);
    }
}