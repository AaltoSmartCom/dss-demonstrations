#!/bin/bash
# 
# Jaan Taponen(jaan.taponen@aalto.fi) Aalto Smartcom
# Usage: setup-keys.sh [filename] [password]
# Alternatively you can run the script without parameters and let the script generate them for you.
#
if [[ $# -ne 2 ]] ; then
    echo 'No parameters detected.'
    while true; do
        read -p "Create .p12 certificate?[Yy/Nn] " yn </dev/tty
        case $yn in
        [Yy]*)
            openssl req -newkey rsa:2048 -nodes -keyout key.pem -x509 -days 365 -out certificate.pem
            openssl x509 -text -noout -in certificate.pem
            echo -n 'Give export password: '
            read -s password
            filename='user_a.p12'
            openssl pkcs12 -inkey key.pem -in certificate.pem -export -out $filename -passout pass:$password -name certificate
            rm certificate.pem && rm key.pem && clear
            break
            ;;
        [Nn]*) 
            echo 'Please give 2 commandline parameters: [filename.p12, password]'
            exit 1
            break 
            ;;
        *) echo "Please answer yes or no." ;;
        esac
    done
    else
        filename=$1
        password=$2
fi
# Checks for the certificate existion.
if [ -f "$filename" ]; then
    echo "$filename OK!"
else 
    echo "Filename: $filename does not exist."
    exit 1
fi

if openssl pkcs12 -in ${filename} -nodes -passin pass:"${password}" | openssl x509 -noout -subject >/dev/null; then
    echo "p.12 keystore OK"
else
    echo "Wrong password?"
fi
#
# Configuration generation
#
clear
echo '-----------------------------------' && echo ''
echo 'DSS init script (c) Aalto Smartcom' && echo ''
echo '-----------------------------------'
echo 'Generating dss.properties file...'
cp template.properties dss.properties
perl -pi -e "s/user\_a\_rsa\.p12/${filename}/g" ./dss.properties
perl -pi -e "s/password\_user/${password}/g" ./dss.properties
yes | cp -rf ./dss.properties ./dss-demo-webapp/src/main/resources/dss.properties
yes | cp -rf $filename ./dss-demo-webapp/src/main/resources/$filename
echo 'Script completed succesfully.'