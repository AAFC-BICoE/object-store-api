package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.gc.aafc.objectstore.api.entities.Agent;
import ca.gc.aafc.objectstore.api.testsupport.factories.AgentFactory;
import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;

public class AgentCRUDIT extends BaseEntityCRUDIT {

  private static String RANDOM_NAME = TestableEntityFactory.generateRandomNameLettersOnly(10);
  
  private Agent agentUnderTest = AgentFactory.newAgent()
      .displayName(RANDOM_NAME)
      .build();

  @Override
  public void testSave() {
    assertNull(agentUnderTest.getId());
    save(agentUnderTest);
    assertNotNull(agentUnderTest.getId());
  }

  @Override
  public void testFind() {
    Agent fetchedAgent = find(Agent.class,
        agentUnderTest.getId());
    assertEquals(agentUnderTest.getId(), fetchedAgent.getId());
    assertEquals(RANDOM_NAME, fetchedAgent.getDisplayName());
  }

  @Override
  public void testRemove() {
    Integer id = agentUnderTest.getId();
    deleteById(Agent.class, id);
    assertNull(find(Agent.class, id));
  }
}
