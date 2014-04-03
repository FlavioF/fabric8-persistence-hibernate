fabric:create --clean --wait-for-provisioning


profile-create --parents feature-dosgi be-fabric-spatial-test

profile-edit --repositories mvn:com.bikeemotion.feature/feature-persistence/0.1-SNAPSHOT/xml/features be-fabric-spatial-test
profile-edit --repositories mvn:com.bikeemotion.feature/feature-rest/0.1-SNAPSHOT/xml/features be-fabric-spatial-test

profile-edit --features persistence-managed-jpa-hibernate be-fabric-spatial-test
profile-edit --features rest be-fabric-spatial-test

profile-edit --bundles mvn:com.bikeemotion.ds/ds-postgresql/0.1-SNAPSHOT be-fabric-spatial-test
profile-edit --bundles mvn:com.bikeemotion/common-framework/0.1-SNAPSHOT be-fabric-spatial-test
profile-edit --bundles mvn:com.bikeemotion/json-framework/0.1-SNAPSHOT be-fabric-spatial-test
profile-edit --bundles mvn:com.github.pires.example/dal/0.1-SNAPSHOT be-fabric-spatial-test
profile-edit --bundles mvn:com.github.pires.example/dal-impl/0.1-SNAPSHOT be-fabric-spatial-test
profile-edit --bundles mvn:com.github.pires.example/rest/0.1-SNAPSHOT be-fabric-spatial-test

container-create-child --profile be-fabric-spatial-test root be-fabric-spatial-test-1