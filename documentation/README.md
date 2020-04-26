# Documentation on using the DSS REST-api
The purpose of this documentation is to explain as clearly as possible how to do single document signing with the native DSS REST-api. On this documentation the focus is on creating XAdES signatures that are included in the final XML file (ENVELOPING).

The original DSS project can be found at:
https://github.com/esig/dss-demonstrations and 
https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/eSignature

Special thanks to Pierrick Vandenbroucke ([@pvandenbroucke](https://github.com/pvandenbroucke)) for helping to create this documentation.

## Basics on creating signatures with DSS
A signature can be created using DSS in three atomic stateless steps:

 1. **Get Data To Sign**: compute the data to be signed. DSS requires the original document (or its digest) and different parameters such as the signing time, certificate used for the signing, the certificate chain - and so on. The different parameters can be found in the Java XAdESSSignatureParameter class (or any other signing method) and also in the OpenAPI/Postman documentation. The result of this method is **not** a digest!
 
 2. **Sign**: compute the signature value, which is a digest with encryption.
 
 3. **Sign Document**: Incorporate the signature value from step (2) in the final signed document. DSS requires the exact same parameters that were used in steps (1) and (2). Most of the [DSS cookbook examples](https://dss.nowina.lu/doc/dss-documentation.html) opreate the 3 steps on the same side (local machine).

Because the steps are atomic, the web-service allows you to move different operation(s) on different servers. The purpose of the two webservices that DSS provides are different:

 - **DSS-SIGNATURE-REST**: Create or extend signed documents with external resources such as CRL, OCSP, Trusted lists etc. This service handles the operations (1) and (3).
 
 - **DSS-SERVER-SIGNING-REST**:  Retriving the server side certificate(s) and computing the signature value from a HSM (*Hardware security module*) or a KeyStore. This service handles the operation (2).

## Suggested topologies
???? TODO

## Step by step on creating XAdES signatures
This guide is best followed using the [Postman examples](./DSS-REST-simplified.json) provided. We will follow the general 3 steps on creating a signed document, but there will be specifications along the way.

**Step 0**
Before step (1) we need to get the **encodedCertificate** from the server. The certificate can be obtained using GET at

    /services/rest/server-signing/key/certificate
The response contains the encodedCertificate and also the complete certificate chain if the certificate has one.

This step can be replaced if you want to use a certificate from a local machine or from another source than the server itself.

**Step 1**
We now use the POST method at

    /services/rest/signature/one-document/getDataToSign
  The request body contains two main parts: **parameters** and **toSignDocument**.

At the parameters we give the signing parameters for step (2). The parameters will be embedded to the response bytes so that step (2) will create the signature using those parameters.

We give most of the signing parameters at

    "signatureLevel" : "XAdES_BASELINE_B",
    "signaturePackaging" : "ENVELOPED",
    "signatureAlgorithm" : "RSA_SHA256",
    "encryptionAlgorithm" : "RSA",
    "digestAlgorithm" : "SHA256",
the signaturePackaging changes whether the signature is embed to the original file or returned in a separate detached file.

We then give the certificate from step (0) at 

    "signingCertificate" : {
      "encodedCertificate" : "MIIC6jCCAdKgAwIBAgIGLtYU17tXMA0GCSqGSIb3DQEBCwUAMDAxGzAZBgNVBAMMElJvb3RTZWxmU2lnbmVkRmFrZTERMA8GA1UECgwIRFNTLXRlc3QwHhcNMTcwNjA4MTEyNjAxWhcNNDcwNzA0MDc1NzI0WjAoMRMwEQYDVQQDDApTaWduZXJGYWtlMREwDwYDVQQKDAhEU1MtdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMI3kZhtnipn+iiZHZ9ax8FlfE5Ow/cFwBTfAEb3R1ZQUp6/BQnBt7Oo0JWBtc9qkv7JUDdcBJXPV5QWS5AyMPHpqQ75Hitjsq/Fzu8eHtkKpFizcxGa9BZdkQjh4rSrtO1Kjs0Rd5DQtWSgkeVCCN09kN0ZsZ0ENY+Ip8QxSmyztsStkYXdULqpwz4JEXW9vz64eTbde4vQJ6pjHGarJf1gQNEc2XzhmI/prXLysWNqC7lZg7PUZUTrdegABTUzYCRJ1kWBRPm4qo0LN405c94QQd45a5kTgowHzEgLnAQI28x0M3A59TKC+ieNc6VF1PsTLpUw7PNI2VstX5jAuasCAwEAAaMSMBAwDgYDVR0PAQH/BAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IBAQCK6LGA01TR+rmU8p6yhAi4OkDN2b1dbIL8l8iCMYopLCxx8xqq3ubZCOxqh1X2j6pgWzarb0b/MUix00IoUvNbFOxAW7PBZIKDLnm6LsckRxs1U32sC9d1LOHe3WKBNB6GZALT1ewjh7hSbWjftlmcovq+6eVGA5cvf2u/2+TkKkyHV/NR394nXrdsdpvygwypEtXjetzD7UT93Nuw3xcV8VIftIvHf9LjU7h+UjGmKXG9c15eYr3SzUmv6kyOI0Bvw14PWtsWGl0QdOSRvIBBrP4adCnGTgjgjk9LTcO8B8FKrr+8lHGuc0bp4lIUToiUkGILXsiEeEg9WAqm+XqO"
    },


On the toSignDocument we give the document encoded in base64 to the **bytes** and the filename to **name**. If we are signging a digest rather than full document we give the **digestAlgorithm**.

    "toSignDocument" : {
	    "bytes" : "PG5vdGU+Cjx0bz5Ub3ZlPC90bz4KPGZyb20+SmFuaTwvZnJvbT4KPGhlYWRpbmc+UmVtaW5kZXI8L2hlYWRpbmc+Cjxib2R5PkRvbid0IGZvcmdldCBtZSB0aGlzIHdlZWtlbmQhPC9ib2R5Pgo8L25vdGU+",
	    "digestAlgorithm" : null,
	    "name" : "RemoteDocument.xml"
	}
It is very importat not to miss the **signingDate** at blevelParams. This must be the exactly same value at the step (3) as well.

    "signingDate" : 1542794107033,
   
 
All of the fields on the [Postman examples](./DSS-REST-simplified.json) are not mandatory. Minimal needed parameters here are signatureLevel, signaturePackaging, digestAlgorithm, signingCertificate and signingTime. All of these parameters must also be equal between step(3) signDocument call (especially aforementioned signingDate). If the time is not provided the DSS server will instantiate a new date and produce a broken signature. The certificate chain is also recommended in the parameters.

**Step 2**
Here we use POST at

    /services/rest/server-signing/sign/certificate/SHA256
    
It requires only one field **bytes** where we give the returned bytes from the step (1). The DSS webservice will read the signing parameters from the data and create the signature.

**Step 3**
We use POST at

    /services/rest/signature/one-document/signDocument

The request body contains three main parts: parameters, signature and documents.

At the parameters we give the exact same parameter configuration as in step (1). There are some new blevelParams for example regarding the signerLocation. Most of these can be left null. Again, it is important that the **signingDate** here is exactly the same as in step (1).

On the signature part we give the **algorithm** used to create the signature and the **value** of the signature which was returned at step (2).

    "signatureValue" : {
	    "algorithm" : "RSA_SHA256",
	    "value" : "gss6N8n+lYGY9iI8hCfmaQnfJp8tr12RQcoaM9Zre6MFUyhc6EuzymGDoApN4vNseWeKOCzV5bJG+c1f3esLOANJYG2oA2Hr0pGKjv6LTuyNrxHCCGCOGktt2CfdE1p1bfGsFOTYp9i09UK48v9EBHMQt5mfYsVFhXlFTXNmWgNoHy8cujAvv62OydeUs0IJDwu0dRQ1aIfZVbAE3BMQAtEXH78jRQIWnU2b7AX5LnoxG842foGvQ7tNv6z4IDXuG3xwOIfcVB0MKQBNxyidEKkiYzygPfhMrY4Wvd+bMGHuuOVufRF3LIKKyxeuab9LkIf6AqNanTLopm6/RYuY2w=="
    },


On the documents part we give the **bytes** of the original document encoded in base64 just like in step (1). If we have a digest rather than an original document we also give the digestAlgorithm.

    "toSignDocument" : {
	    "bytes" : "PG5vdGU+Cjx0bz5Ub3ZlPC90bz4KPGZyb20+SmFuaTwvZnJvbT4KPGhlYWRpbmc+UmVtaW5kZXI8L2hlYWRpbmc+Cjxib2R5PkRvbid0IGZvcmdldCBtZSB0aGlzIHdlZWtlbmQhPC9ib2R5Pgo8L25vdGU+",
	    "digestAlgorithm" : null,
	    "name" : "RemoteDocument.xml"
    }

The return of this step contains a **bytes** field which is a base64 encoded result of the whole process. On the [Postman examples](./DSS-REST-simplified.json) XAdES (ENVELOPING) is used so the return is a base64 encoded XML file where the signature is embedded inside the XML.

## Validating a signed document
This continues right where the step by step guide left off.
Validating uses the POST at

    /services/rest/validation/validateSignature

 We give the **bytes** as they are returned from step (3) and the **name** of the document. If the document is a digest we also give the digestAlgorithm. Policy and signatureId can be left null.

    {
	    "signedDocument" : {
		    "bytes" : "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9Im5vIj8+PG5vdGU+Cjx0bz5Ub3ZlPC90bz4KPGZyb20+SmFuaTwvZnJvbT4KPGhlYWRpbmc+UmVtaW5kZXI8L2hlYWRpbmc+Cjxib2R5PkRvbid0IGZvcmdldCBtZSB0aGlzIHdlZWtlbmQhPC9ib2R5Pgo8ZHM6U2lnbmF0dXJlIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIiBJZD0iaWQtZGQ2N2MxNzA5OTFkZTEwYWEwNWY0YmY4Mjc5ZjYyNTgiPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz48ZHM6UmVmZXJlbmNlIElkPSJyLWlkLWRkNjdjMTcwOTkxZGUxMGFhMDVmNGJmODI3OWY2MjU4LTEiIFVSST0iIj48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvVFIvMTk5OS9SRUMteHBhdGgtMTk5OTExMTYiPjxkczpYUGF0aD5ub3QoYW5jZXN0b3Itb3Itc2VsZjo6ZHM6U2lnbmF0dXJlKTwvZHM6WFBhdGg+PC9kczpUcmFuc2Zvcm0+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+PGRzOkRpZ2VzdFZhbHVlPlZXL21RM3RvbGYrWHZEcFVxWnphWWh3V1N4ZHU3dlFOWHdkMDdBWS8wOUU9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48ZHM6UmVmZXJlbmNlIFR5cGU9Imh0dHA6Ly91cmkuZXRzaS5vcmcvMDE5MDMjU2lnbmVkUHJvcGVydGllcyIgVVJJPSIjeGFkZXMtaWQtZGQ2N2MxNzA5OTFkZTEwYWEwNWY0YmY4Mjc5ZjYyNTgiPjxkczpUcmFuc2Zvcm1zPjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz48L2RzOlRyYW5zZm9ybXM+PGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTI1NiIvPjxkczpEaWdlc3RWYWx1ZT5HcFA0QVpsb1NmWkZLZ2diNkJvR3p0OERLMy8zV0pkaFdTVkhKelVQdUwwPTwvZHM6RGlnZXN0VmFsdWU+PC9kczpSZWZlcmVuY2U+PC9kczpTaWduZWRJbmZvPjxkczpTaWduYXR1cmVWYWx1ZSBJZD0idmFsdWUtaWQtZGQ2N2MxNzA5OTFkZTEwYWEwNWY0YmY4Mjc5ZjYyNTgiPmdzczZOOG4rbFlHWTlpSThoQ2ZtYVFuZkpwOHRyMTJSUWNvYU05WnJlNk1GVXloYzZFdXp5bUdEb0FwTjR2TnNlV2VLT0N6VjViSkcrYzFmM2VzTE9BTkpZRzJvQTJIcjBwR0tqdjZMVHV5TnJ4SENDR0NPR2t0dDJDZmRFMXAxYmZHc0ZPVFlwOWkwOVVLNDh2OUVCSE1RdDVtZllzVkZoWGxGVFhObVdnTm9IeThjdWpBdnY2Mk95ZGVVczBJSkR3dTBkUlExYUlmWlZiQUUzQk1RQXRFWEg3OGpSUUlXblUyYjdBWDVMbm94Rzg0MmZvR3ZRN3ROdjZ6NElEWHVHM3h3T0lmY1ZCME1LUUJOeHlpZEVLa2lZenlnUGZoTXJZNFd2ZCtiTUdIdXVPVnVmUkYzTElLS3l4ZXVhYjlMa0lmNkFxTmFuVExvcG02L1JZdVkydz09PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlDNmpDQ0FkS2dBd0lCQWdJR0x0WVUxN3RYTUEwR0NTcUdTSWIzRFFFQkN3VUFNREF4R3pBWkJnTlZCQU1NRWxKdmIzUlRaV3htVTJsbmJtVmtSbUZyWlRFUk1BOEdBMVVFQ2d3SVJGTlRMWFJsYzNRd0hoY05NVGN3TmpBNE1URXlOakF4V2hjTk5EY3dOekEwTURjMU56STBXakFvTVJNd0VRWURWUVFEREFwVGFXZHVaWEpHWVd0bE1SRXdEd1lEVlFRS0RBaEVVMU10ZEdWemREQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQU1JM2taaHRuaXBuK2lpWkhaOWF4OEZsZkU1T3cvY0Z3QlRmQUViM1IxWlFVcDYvQlFuQnQ3T28wSldCdGM5cWt2N0pVRGRjQkpYUFY1UVdTNUF5TVBIcHFRNzVIaXRqc3EvRnp1OGVIdGtLcEZpemN4R2E5Qlpka1FqaDRyU3J0TzFLanMwUmQ1RFF0V1Nna2VWQ0NOMDlrTjBac1owRU5ZK0lwOFF4U215enRzU3RrWVhkVUxxcHd6NEpFWFc5dno2NGVUYmRlNHZRSjZwakhHYXJKZjFnUU5FYzJYemhtSS9wclhMeXNXTnFDN2xaZzdQVVpVVHJkZWdBQlRVellDUkoxa1dCUlBtNHFvMExONDA1Yzk0UVFkNDVhNWtUZ293SHpFZ0xuQVFJMjh4ME0zQTU5VEtDK2llTmM2VkYxUHNUTHBVdzdQTkkyVnN0WDVqQXVhc0NBd0VBQWFNU01CQXdEZ1lEVlIwUEFRSC9CQVFEQWdFR01BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQ0s2TEdBMDFUUitybVU4cDZ5aEFpNE9rRE4yYjFkYklMOGw4aUNNWW9wTEN4eDh4cXEzdWJaQ094cWgxWDJqNnBnV3phcmIwYi9NVWl4MDBJb1V2TmJGT3hBVzdQQlpJS0RMbm02THNja1J4czFVMzJzQzlkMUxPSGUzV0tCTkI2R1pBTFQxZXdqaDdoU2JXamZ0bG1jb3ZxKzZlVkdBNWN2ZjJ1LzIrVGtLa3lIVi9OUjM5NG5YcmRzZHB2eWd3eXBFdFhqZXR6RDdVVDkzTnV3M3hjVjhWSWZ0SXZIZjlMalU3aCtVakdtS1hHOWMxNWVZcjNTelVtdjZreU9JMEJ2dzE0UFd0c1dHbDBRZE9TUnZJQkJyUDRhZENuR1RnamdqazlMVGNPOEI4Rktycis4bEhHdWMwYnA0bElVVG9pVWtHSUxYc2lFZUVnOVdBcW0rWHFPPC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PGRzOk9iamVjdD48eGFkZXM6UXVhbGlmeWluZ1Byb3BlcnRpZXMgeG1sbnM6eGFkZXM9Imh0dHA6Ly91cmkuZXRzaS5vcmcvMDE5MDMvdjEuMy4yIyIgVGFyZ2V0PSIjaWQtZGQ2N2MxNzA5OTFkZTEwYWEwNWY0YmY4Mjc5ZjYyNTgiPjx4YWRlczpTaWduZWRQcm9wZXJ0aWVzIElkPSJ4YWRlcy1pZC1kZDY3YzE3MDk5MWRlMTBhYTA1ZjRiZjgyNzlmNjI1OCI+PHhhZGVzOlNpZ25lZFNpZ25hdHVyZVByb3BlcnRpZXM+PHhhZGVzOlNpZ25pbmdUaW1lPjIwMTgtMTEtMjFUMDk6NTU6MDdaPC94YWRlczpTaWduaW5nVGltZT48eGFkZXM6U2lnbmluZ0NlcnRpZmljYXRlVjI+PHhhZGVzOkNlcnQ+PHhhZGVzOkNlcnREaWdlc3Q+PGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTUxMiIvPjxkczpEaWdlc3RWYWx1ZT4xNHdNakRGemZzcWtkWlVzblBIMC9oK1pvOHJ6OERFd2lNcTJZTzF3TlRmcGxMM3drUTdFMGwyeVpQWWRlcUdLOVN4Q1RsenAxMVJORVVlTEtNc0NlUT09PC9kczpEaWdlc3RWYWx1ZT48L3hhZGVzOkNlcnREaWdlc3Q+PHhhZGVzOklzc3VlclNlcmlhbFYyPk1ENHdOS1F5TURBeEd6QVpCZ05WQkFNTUVsSnZiM1JUWld4bVUybG5ibVZrUm1GclpURVJNQThHQTFVRUNnd0lSRk5UTFhSbGMzUUNCaTdXRk5lN1Z3PT08L3hhZGVzOklzc3VlclNlcmlhbFYyPjwveGFkZXM6Q2VydD48L3hhZGVzOlNpZ25pbmdDZXJ0aWZpY2F0ZVYyPjx4YWRlczpTaWduYXR1cmVQb2xpY3lJZGVudGlmaWVyPjx4YWRlczpTaWduYXR1cmVQb2xpY3lJbXBsaWVkLz48L3hhZGVzOlNpZ25hdHVyZVBvbGljeUlkZW50aWZpZXI+PC94YWRlczpTaWduZWRTaWduYXR1cmVQcm9wZXJ0aWVzPjx4YWRlczpTaWduZWREYXRhT2JqZWN0UHJvcGVydGllcz48eGFkZXM6RGF0YU9iamVjdEZvcm1hdCBPYmplY3RSZWZlcmVuY2U9IiNyLWlkLWRkNjdjMTcwOTkxZGUxMGFhMDVmNGJmODI3OWY2MjU4LTEiPjx4YWRlczpNaW1lVHlwZT50ZXh0L3htbDwveGFkZXM6TWltZVR5cGU+PC94YWRlczpEYXRhT2JqZWN0Rm9ybWF0PjwveGFkZXM6U2lnbmVkRGF0YU9iamVjdFByb3BlcnRpZXM+PC94YWRlczpTaWduZWRQcm9wZXJ0aWVzPjwveGFkZXM6UXVhbGlmeWluZ1Byb3BlcnRpZXM+PC9kczpPYmplY3Q+PC9kczpTaWduYXR1cmU+PC9ub3RlPg==",
		    "digestAlgorithm" : null,
		    "name" : "RemoteDocument-signed-xades-baseline-b.xml"
	    },
	    "policy" : null,
	    "signatureId" : null
    }

## CORS Headers with front-end applications
Browser same-origin policy helps protect sites that use authenticated sessions. This means that for example on the DSS validation endpoint it does not matter wether we have set the CORS headers or not as the endpoint does not care about the user. If the DSS webservice's signing endpoints are used with for example (stateful) API-keys, configuring the CORS headers is more important. It is also good practice to set the CORS headers manually on every endpoint that will be used by any front-end applications.

As this is a demonstration, the CORS headers are set on the Tomcat configuration so that it adds globally on every request the header ```Access-Control-Allow-Origin: *```. **This needs to be modified if it is important that the endpoints can be used only on selected sites.**

## References to the Java source
Integration tests can be found at:
 [https://github.com/AaltoSmartCom/dss-demonstrations/tree/master/dss-demo-webapp/src/test/java/eu/europa/esig/dss/web/ws](https://github.com/AaltoSmartCom/dss-demonstrations/tree/master/dss-demo-webapp/src/test/java/eu/europa/esig/dss/web/ws)

This class:
[https://github.com/AaltoSmartCom/dss-demonstrations/blob/master/dss-demo-webapp/src/test/java/eu/europa/esig/dss/web/ws/SignatureRestServiceIT.java](https://github.com/AaltoSmartCom/dss-demonstrations/blob/master/dss-demo-webapp/src/test/java/eu/europa/esig/dss/web/ws/SignatureRestServiceIT.java)
handles the steps (1) and (3) on the server side and the step (2) on the local machine.

This class:
[https://github.com/AaltoSmartCom/dss-demonstrations/blob/master/dss-demo-webapp/src/test/java/eu/europa/esig/dss/web/ws/SoapServerSigningIT.java](https://github.com/AaltoSmartCom/dss-demonstrations/blob/master/dss-demo-webapp/src/test/java/eu/europa/esig/dss/web/ws/SoapServerSigningIT.java)
handles the step (2) on the server side.

