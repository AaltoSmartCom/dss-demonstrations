# Demonstrations for DSS : Digital Signature Service

This is the demonstration repository for project DSS : https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eSignature. 
The MOCCA -adapter has been deleted since it's unecessary for the current use case and had broken depedencies that have been deleted by the Austrian authority.

- Find more documentation in the "[documentation](./documentation)" directory.

## Generating you own .p12 keystore

If you want to change the key provided in the repository you should do it *before* building the docker image.
You can run the [setup-keys.sh](./setup.keys) with 2 parameters: your own keystore file and its password.
```
bash setup.keys example.p12 password
```
Or you can just run it and let the script generate them for you. 
```
bash setup.keys
```

## Building the enviroment

REST-endpoint can be compiled with the given Dockerfile which will first create a build image and then the actual
production image.
```
docker build -t signer . && docker run -d -p 8080:8080 signer
```

## Example queries

For the actual usement of the REST please take a look at the [documentation](./documentation) of the project.
You can find some example Postman collection in the [DSS-REST-collection.json](./DSS-REST-collection.json) file.
Alternatively the original sample file can be found [here](https://github.com/esig/dss/blob/master/dss-cookbook/src/main/postman/DSS%20REST%20services.postman_collection.json)

## Developing Without docker
If you for some reason want to generate the original tomcat zip files you can do it by running:
```
mvn install || true && mvn package
```

Just make sure you have Maven and your preferred version of jdk11 installed.
