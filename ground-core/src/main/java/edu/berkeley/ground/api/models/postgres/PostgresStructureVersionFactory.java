package edu.berkeley.ground.api.models.postgres;

import edu.berkeley.ground.api.models.StructureVersion;
import edu.berkeley.ground.api.models.StructureVersionFactory;
import edu.berkeley.ground.api.versions.GroundType;
import edu.berkeley.ground.api.versions.postgres.PostgresVersionFactory;
import edu.berkeley.ground.db.DBClient;
import edu.berkeley.ground.db.DbDataContainer;
import edu.berkeley.ground.db.PostgresClient;
import edu.berkeley.ground.db.PostgresClient.PostgresConnection;
import edu.berkeley.ground.db.QueryResults;
import edu.berkeley.ground.exceptions.GroundException;
import edu.berkeley.ground.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PostgresStructureVersionFactory extends StructureVersionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresStructureVersionFactory.class);
    private PostgresClient dbClient;

    private PostgresStructureFactory structureFactory;
    private PostgresVersionFactory versionFactory;

    public PostgresStructureVersionFactory(PostgresStructureFactory structureFactory, PostgresVersionFactory versionFactory, PostgresClient dbClient) {
        this.dbClient = dbClient;
        this.structureFactory = structureFactory;
        this.versionFactory = versionFactory;
    }

    public StructureVersion create(String structureId,
                                   Map<String, GroundType> attributes,
                                   Optional<String> parentId) throws GroundException {

        PostgresConnection connection = this.dbClient.getConnection();
        String id = IdGenerator.generateId(structureId);

        this.versionFactory.insertIntoDatabase(connection, id);

        List<DbDataContainer> insertions = new ArrayList<>();
        insertions.add(new DbDataContainer("id", GroundType.STRING, id));
        insertions.add(new DbDataContainer("structure_id", GroundType.STRING, structureId));

        connection.insert("StructureVersions", insertions);

        for (String key : attributes.keySet()) {
            List<DbDataContainer> itemInsertions = new ArrayList<>();
            itemInsertions.add(new DbDataContainer("svid", GroundType.STRING, id));
            itemInsertions.add(new DbDataContainer("key", GroundType.STRING, key));
            itemInsertions.add(new DbDataContainer("type", GroundType.STRING, attributes.get(key).toString()));

            connection.insert("StructureVersionItems", itemInsertions);
        }

        this.structureFactory.update(connection, structureId, id, parentId);

        connection.commit();
        LOGGER.info("Created structure version " + id + " in structure " + structureId + ".");

        return StructureVersionFactory.construct(id, structureId, attributes);
    }

    public StructureVersion retrieveFromDatabase(String id) throws GroundException {
        PostgresConnection connection = this.dbClient.getConnection();

        List<DbDataContainer> predicates = new ArrayList<>();
        predicates.add(new DbDataContainer("id", GroundType.STRING, id));
        QueryResults resultSet = connection.equalitySelect("StructureVersions", DBClient.SELECT_STAR, predicates);

        List<DbDataContainer> attributePredicates = new ArrayList<>();
        attributePredicates.add(new DbDataContainer("svid", GroundType.STRING, id));
        QueryResults attributesSet = connection.equalitySelect("StructureVersionItems", DBClient.SELECT_STAR, attributePredicates);

        Map<String, GroundType> attributes = new HashMap<>();

        do {
            attributes.put(attributesSet.getString(2), GroundType.fromString(attributesSet.getString(3)));
        } while (attributesSet.next());

        String structureId = resultSet.getString(2);

        connection.commit();
        LOGGER.info("Retrieved structure version " + id + " in structure " + structureId + ".");

        return StructureVersionFactory.construct(id, structureId, attributes);
    }
}
