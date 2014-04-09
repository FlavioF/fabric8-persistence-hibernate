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
package com.github.pires.example.dal.impl.entities;


import javax.persistence.*;

import com.github.pires.example.dal.entities.JSON;
import com.vividsolutions.jts.geom.Point;
import org.hibernate.annotations.Type;

@Entity
@org.hibernate.annotations.TypeDefs({@org.hibernate.annotations.TypeDef(name = "JSON", defaultForType =  com.github.pires.example.dal.entities.JSON.class, typeClass = com.github.pires.example.dal.impl.json.JSONUserType.class)})
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  
  @Version
  private Long version;
  
  private String name;
  
  private JSON properties;
  
  @Column(nullable = false, unique = true)
  @Type(type = "org.hibernate.spatial.GeometryType")
  private Point location;

  //public API
  public UserEntity() {
    
    this.properties = new JSON();
  }

  public Long getId() {
    return id;
  }

  public Long getVersion() {
    return version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public JSON getProperties() {
    return properties;
  }

  public void setProperties(JSON properties) {
    this.properties = properties;
  }
  
  public Point getLocation() {
    return this.location;
  }

  public void setLocation(Point location) {
    this.location = location;
  }
  
  @Override
  public String toString() {
    return "Person{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", properties=" + properties.getValue().toString()
            + '}';
  }
}