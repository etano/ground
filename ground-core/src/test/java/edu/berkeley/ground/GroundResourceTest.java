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

package edu.berkeley.ground;

import edu.berkeley.ground.db.CassandraClient;
import edu.berkeley.ground.db.Neo4jClient;
import edu.berkeley.ground.db.PostgresClient;
import edu.berkeley.ground.db.GremlinClient;
import edu.berkeley.ground.exceptions.GroundDBException;
import edu.berkeley.ground.resources.*;
import edu.berkeley.ground.util.CassandraFactories;
import edu.berkeley.ground.util.Neo4jFactories;
import edu.berkeley.ground.util.PostgresFactories;
import edu.berkeley.ground.util.GremlinFactories;
import org.junit.Before;

import java.io.File;


public class GroundResourceTest {
    private static final String BACKING_STORE_TYPE = "neo4j";
    private static final String TEST_DB_NAME = "test";

    protected NodesResource nodesResource;
    protected EdgesResource edgesResource;
    protected GraphsResource graphsResource;
    protected LineageEdgesResource lineageEdgesResource;
    protected StructuresResource structuresResource;

    @Before
    public void setUp() {
        try {
            switch (BACKING_STORE_TYPE) {
                case "postgres": {
                    setBackingStore();

                    Process p = Runtime.getRuntime().exec("python2.7 postgres_setup.py test " + TEST_DB_NAME, null, new File("scripts/postgres/"));
                    p.waitFor();

                    break;
                }

                case "cassandra": {
                    setBackingStore();

                    Process p = Runtime.getRuntime().exec("python2.7 cassandra_setup.py " + TEST_DB_NAME, null, new File("scripts/cassandra/"));
                    p.waitFor();

                    break;
                }

                case "gremlin": {
                    Process p = Runtime.getRuntime().exec("/Users/vikram/code/titan/bin/gremlin.sh -e delete_gremlin.groovy", null, new File("scripts/gremlin/"));
                    Thread.sleep(5000);
                    p.destroy();

                    setBackingStore();

                    break;
                }

                case "neo4j": {
                    Process p = Runtime.getRuntime().exec("neo4j-client -file delete_data.cypher", null, new File("scripts/neo4j/"));
                    p.waitFor();

                    setBackingStore();

                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("FATAL: Unexpected Exception. " + e.getMessage());
        }
    }

    private void setBackingStore() throws GroundDBException {
        switch (BACKING_STORE_TYPE) {
            case "postgres": {
                PostgresClient dbClient = new PostgresClient("localhost", 5432, "test", "test", "");
                PostgresFactories factoryGenerator = new PostgresFactories(dbClient);

                nodesResource = new NodesResource(factoryGenerator.getNodeFactory(), factoryGenerator.getNodeVersionFactory());
                edgesResource = new EdgesResource(factoryGenerator.getEdgeFactory(), factoryGenerator.getEdgeVersionFactory());
                graphsResource = new GraphsResource(factoryGenerator.getGraphFactory(), factoryGenerator.getGraphVersionFactory());
                lineageEdgesResource = new LineageEdgesResource(factoryGenerator.getLineageEdgeFactory(), factoryGenerator.getLineageEdgeVersionFactory());
                structuresResource = new StructuresResource(factoryGenerator.getStructureFactory(), factoryGenerator.getStructureVersionFactory());
                break;
            }

            case "cassandra": {
                CassandraClient dbClient = new CassandraClient("localhost", 9160, "test", "test", "");
                CassandraFactories factoryGenerator = new CassandraFactories(dbClient);

                nodesResource = new NodesResource(factoryGenerator.getNodeFactory(), factoryGenerator.getNodeVersionFactory());
                edgesResource = new EdgesResource(factoryGenerator.getEdgeFactory(), factoryGenerator.getEdgeVersionFactory());
                graphsResource = new GraphsResource(factoryGenerator.getGraphFactory(), factoryGenerator.getGraphVersionFactory());
                lineageEdgesResource = new LineageEdgesResource(factoryGenerator.getLineageEdgeFactory(), factoryGenerator.getLineageEdgeVersionFactory());
                structuresResource = new StructuresResource(factoryGenerator.getStructureFactory(), factoryGenerator.getStructureVersionFactory());
                break;
            }

            case "gremlin": {
                GremlinClient dbClient = new GremlinClient();
                GremlinFactories factoryGenerator = new GremlinFactories(dbClient);

                nodesResource = new NodesResource(factoryGenerator.getNodeFactory(), factoryGenerator.getNodeVersionFactory());
                edgesResource = new EdgesResource(factoryGenerator.getEdgeFactory(), factoryGenerator.getEdgeVersionFactory());
                graphsResource = new GraphsResource(factoryGenerator.getGraphFactory(), factoryGenerator.getGraphVersionFactory());
                lineageEdgesResource = new LineageEdgesResource(factoryGenerator.getLineageEdgeFactory(), factoryGenerator.getLineageEdgeVersionFactory());
                structuresResource = new StructuresResource(factoryGenerator.getStructureFactory(), factoryGenerator.getStructureVersionFactory());

                break;
            }

            case "neo4j": {
                Neo4jClient neo4jClient = new Neo4jClient("localhost:7687", "", "");
                Neo4jFactories factoryGenerator = new Neo4jFactories(neo4jClient);

                nodesResource = new NodesResource(factoryGenerator.getNodeFactory(), factoryGenerator.getNodeVersionFactory());
                edgesResource = new EdgesResource(factoryGenerator.getEdgeFactory(), factoryGenerator.getEdgeVersionFactory());
                graphsResource = new GraphsResource(factoryGenerator.getGraphFactory(), factoryGenerator.getGraphVersionFactory());
                lineageEdgesResource = new LineageEdgesResource(factoryGenerator.getLineageEdgeFactory(), factoryGenerator.getLineageEdgeVersionFactory());
                structuresResource = new StructuresResource(factoryGenerator.getStructureFactory(), factoryGenerator.getStructureVersionFactory());

                break;
            }

        }
    }
}
