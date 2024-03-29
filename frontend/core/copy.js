const fs = require("fs");
const path = require("path");

const from = path.resolve("./dist/captcha.umd.js");
const tos = [
  path.resolve("../chrome-extension/main.js"),
  path.resolve("../integration-demo/src/main/resources/static/captcha.js"),
];

console.log("\n");

const fromBuffer = fs.readFileSync(from);
for (let to of tos) {
  fs.writeFileSync(to, fromBuffer);
  console.log('Copied "' + from + '" to "' + to + '".');
}

console.log("\n");
