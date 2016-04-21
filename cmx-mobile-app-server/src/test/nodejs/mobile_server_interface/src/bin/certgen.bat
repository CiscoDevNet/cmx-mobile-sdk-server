openssl genrsa -out ../server-key.pem 1024
openssl req -new -config openssl.cnf -key ../server-key.pem -out ../server-csr.pem
openssl x509 -req -in ../server-csr.pem -signkey ../server-key.pem -out ../server-cert.pem