package edu.berkeley.ground.api.models.neo4j;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.berkeley.ground.api.Neo4jTest;
import edu.berkeley.ground.api.models.NodeVersion;
import edu.berkeley.ground.api.models.Tag;
import edu.berkeley.ground.api.versions.GroundType;
import edu.berkeley.ground.exceptions.GroundException;

import static org.junit.Assert.*;

public class Neo4jNodeVersionFactoryTest extends Neo4jTest {

  public Neo4jNodeVersionFactoryTest() throws GroundException {
    super();
  }

  @Test
  public void testNodeVersionCreation() throws GroundException {
    String nodeName = "testNode";
    long nodeId = super.factories.getNodeFactory().create(nodeName, new HashMap<>()).getId();

    String structureName = "testStructure";
    long structureId = super.factories.getStructureFactory().create(structureName, new HashMap<>()).getId();

    Map<String, GroundType> structureVersionAttributes = new HashMap<>();
    structureVersionAttributes.put("intfield", GroundType.INTEGER);
    structureVersionAttributes.put("boolfield", GroundType.BOOLEAN);
    structureVersionAttributes.put("strfield", GroundType.STRING);

    long structureVersionId = super.factories.getStructureVersionFactory().create(
        structureId, structureVersionAttributes, new ArrayList<>()).getId();

    Map<String, Tag> tags = new HashMap<>();
    tags.put("intfield", new Tag(-1, "intfield", 1, GroundType.INTEGER));
    tags.put("strfield", new Tag(-1, "strfield", "1", GroundType.STRING));
    tags.put("boolfield", new Tag(-1, "boolfield", true, GroundType.BOOLEAN));

    String testReference = "http://www.google.com";
    Map<String, String> parameters = new HashMap<>();
    parameters.put("http", "GET");

    long nodeVersionId = super.factories.getNodeVersionFactory().create(tags,
        structureVersionId, testReference, parameters, nodeId, new ArrayList<>()).getId();

    NodeVersion retrieved = super.factories.getNodeVersionFactory().retrieveFromDatabase(nodeVersionId);

    assertEquals(nodeId, retrieved.getNodeId());
    assertEquals(structureVersionId, retrieved.getStructureVersionId());
    assertEquals(testReference, retrieved.getReference());

    assertEquals(parameters.size(), retrieved.getParameters().size());
    assertEquals(tags.size(), retrieved.getTags().size());

    Map<String, String> retrievedParameters = retrieved.getParameters();
    Map<String, Tag> retrievedTags = retrieved.getTags();

    for (String key : parameters.keySet()) {
      assert (retrievedParameters).containsKey(key);
      assertEquals(parameters.get(key), retrievedParameters.get(key));
    }

    for (String key : tags.keySet()) {
      assert (retrievedTags).containsKey(key);
      assertEquals(tags.get(key), retrievedTags.get(key));
    }

    List<Long> leaves = super.factories.getNodeFactory().getLeaves(nodeName);

    assertTrue(leaves.contains(nodeVersionId));
    assertTrue(1 == leaves.size());
  }


  @Test
  public void testTransitiveClosure() throws GroundException {
    String nodeName = "testNode1";
    long nodeId = super.factories.getNodeFactory()
        .create(nodeName, new HashMap<>()).getId();

    long nodeVersionId = super.factories.getNodeVersionFactory().create(new HashMap<>(),
        -1, null, new HashMap<>(), nodeId, new ArrayList<>()).getId();

    nodeName = "testNode2";
    long secondNodeId = super.factories.getNodeFactory().create(nodeName, new HashMap<>()).getId();

    long secondNVId = super.factories.getNodeVersionFactory().create(new HashMap<>(), -1,
        null, new HashMap<>(), secondNodeId, new ArrayList<>()).getId();

    String edgeName = "testEdge1";
    long edgeId = super.factories.getEdgeFactory()
        .create(edgeName, nodeId, secondNodeId, new HashMap<>()).getId();

    super.factories.getEdgeVersionFactory().create(new HashMap<>(), -1, null, new
        HashMap<>(), edgeId, secondNVId, nodeVersionId, -1, -1, new ArrayList<>()).getId();

    nodeName = "testNode3";
    nodeId = super.factories.getNodeFactory().create(nodeName, new HashMap<>()).getId();

    long thirdNVId = super.factories.getNodeVersionFactory().create(new HashMap<>(), -1,
        null, new HashMap<>(), nodeId, new ArrayList<>()).getId();


    edgeName = "testEdge3";
    edgeId = super.factories.getEdgeFactory().create(edgeName, secondNodeId, nodeId,
        new HashMap<>()).getId();

    super.factories.getEdgeVersionFactory().create(new HashMap<>(), -1, null, new
        HashMap<>(), edgeId, thirdNVId, secondNVId, -1, -1, new ArrayList<>()).getId();


    List<Long> reachable = super.factories.getNodeVersionFactory()
        .getTransitiveClosure(thirdNVId);

    assertTrue(reachable.contains(nodeVersionId));
    assertTrue(reachable.contains(secondNVId));
  }
}
