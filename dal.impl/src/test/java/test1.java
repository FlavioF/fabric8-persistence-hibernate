/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.Random;
import java.util.UUID;

import javax.persistence.*;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.bikeemotion.spatial.GeoPoint;
import com.github.pires.example.dal.entities.JSON;
import com.github.pires.example.dal.entities.User;
import com.github.pires.example.dal.impl.UserServiceImpl;
import com.github.pires.example.dal.impl.daos.UserEntityDao;

public class test1 {

  private EntityManagerFactory entityManagerFactory;
  private EntityManager entityManager;
  private EntityTransaction tx;
  private UserServiceImpl service;
  private UserEntityDao dao;
  private int expectedCount = 0;

  @BeforeClass
  public void setUpClass() throws Exception {
    // provision persistence manager
    entityManagerFactory = Persistence.createEntityManagerFactory("test");
    entityManager = entityManagerFactory.createEntityManager();
    entityManager.setFlushMode(FlushModeType.COMMIT);
    entityManager.clear();
    tx = entityManager.getTransaction();

    // provision daos
    dao = new UserEntityDao();
    dao.setEntityManager(entityManager);

    // provision services
    service = new UserServiceImpl();
    service.setUserDao(dao);
  }

  @AfterClass
  public void tearDownClass() throws Exception {
    tx = null;
    entityManager.clear();
    entityManager.close();
    entityManagerFactory.close();
  }

  private GeoPoint randomGeo() {
    return new GeoPoint(new Random().nextDouble(), new Random().nextDouble());
  }

  private User getFullObject() {
    User value = new User();

    value.setName("Alberto");
    value.setLocation(randomGeo());
    value.setProperties(new JSON("{\n" + "         \"string1\":{\n" + "            \"mandatory\":false,\n" + "            \"value\":\"teste\",\n" + "            \"type\":\"string\"\n" + "         },\n" + "         \"num1\":{\n"
        + "            \"mandatory\":false,\n" + "            \"value\":123,\n" + "            \"type\":\"number\"\n" + "         }\n" + "      }"));

    return value;
  }

  @Test
  public void test_stationBeacon_create() {
    User value = getFullObject();
    UUID id;

    try {
      tx.begin();
      service.create(value);
      tx.commit();
    } catch (Exception ex) {
      if (tx.isActive())
        tx.rollback();
      throw ex;
    }

    int count = service.findAll().size() | 0;

  }
}
