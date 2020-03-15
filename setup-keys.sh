#!/bin/bash
if [[ $# -ne 2 ]] ; then
    echo 'Please give 2 commandline parameters: [filename.p12, password]'
    exit 1
fi

filename=$1
password=$2

if [ -f "$filename" ]; then
    echo "$filename OK!"
else 
    echo "$filename does not exist"
    exit 1
fi

if openssl pkcs12 -in ${filename} -nodes -passin pass:"${password}" | openssl x509 -noout -subject >/dev/null; then
    echo "p.12 keystore OK"
else
    echo "Wrong password?"
fi

cp template.properties dss.properties
perl -pi -e "s/user\_a\_rsa\.p12/${filename}/g" ./dss.properties
perl -pi -e "s/password\_user/${password}/g" ./dss.properties
mv ./dss.properties ./dss-demo-webapp/src/main/resources/dss.properties
cp $filename ./dss-demo-webapp/src/main/resources/$filename