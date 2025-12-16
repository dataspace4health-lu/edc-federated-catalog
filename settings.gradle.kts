rootProject.name = "edc-federated-catalog"
include("extensions")
include("extensions:catalog-node-resolver")
include("extensions:http-jersey-core")
include("extensions:store:sql:participant-store-sql")
findProject(":extensions:catalog-node-resolver")?.name = "catalog-node-resolver"
findProject(":extensions:http-jersey-core")?.name = "http-jersey-core"
findProject(":extensions:store:sql:participant-store-sql")?.name = "participant-store-sql"
