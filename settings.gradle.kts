rootProject.name = "edc-federated-catalog"
include("extensions")
include("extensions:catalog-node-resolver")
include("extensions:store:sql:participant-store-sql")
findProject(":extensions:catalog-node-resolver")?.name = "catalog-node-resolver"
findProject(":extensions:store:sql:participant-store-sql")?.name = "participant-store-sql"