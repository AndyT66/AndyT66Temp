truststore is the global content-chh-fedora truststore. It uses this to hold Fedora server certificates
for trust purposes when using HTTPS. The content-chh-fedora SSL layer will probe for the Fedora server
certificate and automatically put it in this truststore. The Fedora web services clients then use this
truststore to communicate with the secure Fedora. All mounted Fedoras will have their server certificates
stored in this file.