const TeraboxUploader = require("terabox-upload-tool");

const credentials = {
  ndus: "YOUR_NDUS",          // Required: from cookies
  appId: "YOUR_APPID",        // Required: from network requests
  uploadId: "YOUR_UPLOADID",  // Required: from network requests
  jsToken: "YOUR_JSTOKEN",    // Required
  browserId: "YOUR_BROWSERID" // Required
};

const uploader = new TeraboxUploader(credentials);
