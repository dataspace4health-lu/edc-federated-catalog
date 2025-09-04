# Introduction 
This is the code base of Federated Catalog extensions which uses the Federated Catalog(an EDC component) as dependency.

# Getting Started

# Build and Test
To build the image run the below command on top level of the repo
    docker build -t localhost:5432/edc-federated-catalog .
    docker push localhost:5432/edc-federated-catalog

# Contribute
Author :  Aastha Rai
To contribute in the project by writing extensions, use the folder extensions/src/main/java/org/eclipse/edc/extensions
The same extension needs to be registered as a service in the extensions/src/main/resources/META-INF/services - check the existing service for reference.