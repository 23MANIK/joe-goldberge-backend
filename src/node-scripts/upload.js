const fs = require('fs');
const https = require('https');
const path = require('path');
const TeraboxUploader = require('terabox-upload-tool');
require('dotenv').config();

const credentials = {
  ndus: process.env.TERABOX_NDUS,
  appId: process.env.TERABOX_APP_ID,
  uploadId: process.env.TERABOX_UPLOAD_ID,
  jsToken: process.env.TERABOX_JS_TOKEN,
  browserId: process.env.TERABOX_BROWSER_ID
};

const uploader = new TeraboxUploader(credentials);

// Accept url and localFileName as command line arguments
const inputUrl = process.argv[2];
const inputLocalFileName = process.argv[3];

if (!inputUrl || !inputLocalFileName) {
  console.error('Usage: node upload.js <url> <localFileName>');
  process.exit(1);
}

const url = inputUrl;
const localFileName = inputLocalFileName;
const teraBoxDestFolder = '/blackbox/video';

function downloadFile(url, dest, cb) {
  const file = fs.createWriteStream(dest);
  https.get(url, response => {
    response.pipe(file);
    file.on('finish', () => {
      file.close(cb);
    });
  }).on('error', err => {
    fs.unlink(dest, () => {});
    console.error('Download error:', err.message);
  });
}

downloadFile(url, localFileName, async () => {
  try {
    console.log('Download completed. Starting upload...');
    const progressCallback = (loaded, total) => {
      const percent = ((loaded / total) * 100).toFixed(2);
      process.stdout.write(`\rUploading: ${percent}%`);
    };
    const result = await uploader.uploadFile(localFileName, progressCallback, teraBoxDestFolder);
    if (result.success) {
      console.log('\n✅ File uploaded successfully!');
      console.log('Details:', result.fileDetails);
    } else {
      console.log('\n❌ Upload failed:', result.message);
    }
  } catch (error) {
    console.error('\nAn error occurred:', error.message);
  } finally {
    // Delete the file after upload attempt
    fs.unlink(localFileName, (err) => {
      if (err) {
        console.error(`Error deleting file ${localFileName}:`, err.message);
      } else {
        console.log(`\nDeleted local file: ${localFileName}`);
      }
    });
  }
});
