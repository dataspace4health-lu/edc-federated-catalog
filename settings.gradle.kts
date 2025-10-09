rootProject.name = "edc-federated-catalog"
include("extensions")
include("extensions:catalog-node-resolver")
findProject(":extensions:catalog-node-resolver")?.name = "catalog-node-resolver"

