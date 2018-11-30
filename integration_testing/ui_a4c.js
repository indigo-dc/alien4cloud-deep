const puppeteer = require("puppeteer");
var program = require('commander');
var fs = require("fs");

program
  .version('0.1.0')
  .option('-h, --host <host>', 'Host that is running A4C')
  .option('-u, --adminuser <adminuser>', 'The user name of the admin user')
  .option('-p, --adminpassw <adminpassw>', 'The password of the admin user')
  .option('-t, --apptopo <apptopo>', 'The file containing the YAML automated testing application topology')
  .parse(process.argv);

var pageH = 1080;
var pageW = 1100;
var orchestratorInstanceName = "AutomatedOrchestratorName";
var orchestratorLocationInstanceName = "AutomatedOrchestratorLocationName";
var infrastructureType = "Deep Orchestrator";
var appName = "AutomatedApp";
var appTopo = fs.readFileSync(program.apptopo, 'utf8');

async function getProperty(element, property) {
    return await (await element.getProperty(property)).jsonValue();
}

async function doLogin(page, adminuser, adminpassw) {
    await page.waitForSelector("input[name='userName']");
    await page.waitForSelector("input[name='userPassword']");
    await page.type("input[name='userName']", adminuser);
    await page.type("input[name='userPassword']", adminpassw);
    await page.click("button[name='btn-login']");
}

async function doCreateOrchestratorInstance(page, orchestratorInstanceName) {
    await page.waitForSelector("a[id='menu.admin']");
    await page.click("a[id='menu.admin']");
    await page.waitForSelector("a[id='homepage.am.admin.orchestrators']");
    await page.click("a[id='homepage.am.admin.orchestrators']");
    await page.waitForSelector("button[id='new-orchestrator']");
    await page.click("button[id='new-orchestrator']");
    await page.waitForSelector("input[id='orchestrator_name_id']");
    await page.type("input[id='orchestrator_name_id']", orchestratorInstanceName);
    const option = (await page.$x('//select[@name = "plugin"]/option[contains(text(), "IndigoDC")]'))[0];
  const value = await (await option.getProperty('value')).jsonValue();
  await page.select('select[name="plugin"]', value);
  await page.click("button[id='modal-create-button']");
}

async function doEnableOrchestratorInstance(page, orchestratorInstanceName, orchestratorLocationInstanceName, infrastructureType) {
    await page.waitForSelector("table.table.table-hover");
    let automatedOrchestratorRow = "//td[contains(text(), '" + orchestratorInstanceName + "')]";

    await page.waitForFunction("document.querySelector('table.table.table-hover').innerText.includes('" + orchestratorInstanceName + "')");
    let td = (await page.$x(automatedOrchestratorRow))[0];
    let tr   = (await td.$x( '..' ))[0];
    await tr.click();

    await page.waitForSelector("div[id=\"orchestrator-state\"]");
    await page.click("div[id='toast-container']").catch(e => e);

    let connectedEl = (await page.$x('//div[@id="orchestrator-state"]'))[0];
    let innerText = await getProperty(connectedEl, 'innerText');
    if (!innerText.includes('Connected')) {
      await page.waitForSelector("a[id='orchestrator-enable-button']");
      await page.click("a[id='orchestrator-enable-button']");
      await page.waitForFunction("document.querySelector('div[id=\"orchestrator-state\"]').innerText.includes('Connected')");
    }

    await page.click("a[id='menu.orchestrators.locations']");
    await page.waitForFunction("document.querySelector('a[id=\"menu.orchestrators.locations\"]').parentElement.classList.contains('active')");
    let nameEl = (await page.$x('//span[@editable-text="uiModel.locationDTO.location.name"]'))[0];
    let btnNewLoc = (await page.$x('//button[@id="new-location-button"]'))[0];
    //innerText = await getProperty(nameEl, 'innerText');
    if (nameEl === undefined && btnNewLoc !== undefined) {
      await page.waitForSelector("button[id='new-location-button']");
      await page.click("button[id='new-location-button']");
      await page.waitForSelector("input[id='location_name_id']");
      await page.type("input[id='location_name_id']", orchestratorLocationInstanceName);

      const option = (await page.$x('//select[@name = "infrastructureType"]/option[contains(text(), "' + infrastructureType + '")]'))[0];
      const value = await (await option.getProperty('value')).jsonValue();
      await page.select('select[name="infrastructureType"]', value);
      await page.click("button[id='new-location-create-button']");
    }
}

async function doCreateApp(page, orchestratorInstanceName, appName, pageW, pageH, appTopo) {
    //await page.sleep(5000);
    const menuApp = await page.waitForSelector("a[id='menu.applications']");
    await menuApp.click();
    const btnNewApp = await page.waitForSelector("button[id='app-new-btn']");

    let nameEl = (await page.$x('//div[contains(string(), "' + appName + '")]'))[0];
    if (nameEl === undefined) {
      await btnNewApp.click();
      const inpName = await page.waitForSelector("input[id='nameid']");
      await inpName.type(appName);
      await page.click("button[id='btn-create']");
      await page.waitForSelector("span[editable-text=\"application.name\"]");
    } else
      await nameEl.click();

    await page.waitFor(1000);
    //await page.waitForFunction("document.querySelector('div.text-muted > table.table-condensed.grp-margin').textContent.includes('Environment')");
    let envSelXpath = "//td[contains(text(), 'Environment')]";
    let td = (await page.$x(envSelXpath))[0];
    let tr = (await td.$x( '..' ))[0];
    await tr.click();
    let btnEditTopo = await page.waitForSelector("button[id=\"edit-topo-btn\"]");
    await btnEditTopo.click();
    let searchNodeEl = await page.waitForSelector("input[id=\"search-query\"]");
    await searchNodeEl.type("tosca.nodes.indigo.Compute");
    await searchNodeEl.type(String.fromCharCode(13));// ENTER

    const computeNodeEl = await page.waitForSelector("span[uib-tooltip=\"tosca.nodes.indigo.Compute\"]");
    const rect = await page.evaluate((computeNodeEl) => {
        const {top, left, bottom, right} = computeNodeEl.getBoundingClientRect();
        return {top, left, bottom, right};
    }, computeNodeEl);

    // Mouse actions don't work well
    // Check https://github.com/GoogleChrome/puppeteer/issues/2085
    // await page.mouse.move(rect.left, rect.top);
    // await page.mouse.down();
    // await page.mouse.move(500, 500);
    // await page.waitFor(400);
    // await page.mouse.up();
    // await page.keyboard.type(String.fromCharCode(27));// ESC

    await page.click("a[id=\"am.editor_app_env.editor.files\"]");
    await page.waitFor(1000);
    let topoSelectorEl =  (await page.$x('//div[contains(@class, "tree-label") and contains(string(), "topology.yml")]'))[0];
    await topoSelectorEl.click();
    // let textareaEl =  (await page.$x('//textarea[contains(@class, "ace_text-input")]'))[0];
    // await page.evaluate(async () => {
    //   document.querySelector("textarea.ace_text-input").value = "";
    //
    // });
    //await page.waitForFunction("document.querySelector(\"textarea.ace_text-input\").value.length > 0");

    //await page.waitForSelector("textarea.ace_text-input");
    await page.focus('textarea.ace_text-input');
    var lines = appTopo.split('\n');
    let previousIdx = 0;
    let lineSpaces = [];
    for (let line of lines) {
      let idx = 0;
      while (idx < line.length && line[idx] === ' ')
        ++idx;

      lineSpaces.push(idx);
      //await page.keyboard.type(new Array(idx).join(' '));
      await page.keyboard.type(line.trim());
      await page.keyboard.type("\n");//String.fromCharCode(13));
      // while(previousIdx > 0) {
      //    await page.keyboard.press("Backspace")// (String.fromCharCode(8));
      //    --previousIdx;
      //  }
      //  previousIdx = idx;
    }

    for (let idx=lines.length-1; idx>=0; --idx) {
      await page.keyboard.press("ArrowUp");
      await page.keyboard.press("Home");
      await page.keyboard.type(new Array(lineSpaces[idx]).join(' '));
    }

    // await page.keyboard.down('ControlLeft');
    // await page.keyboard.down('A');
    //
    // await page.keyboard.up('A');
    // await page.keyboard.up('ControlLeft');
    // await page.keyboard.down("Delete");// ENTER
    // await page.keyboard.up("Delete");// ENTER

    // await page.evaluate(appTopo => {
    //   console.log(document.querySelector("textarea.ace_text-input").value);
    //   document.querySelector("textarea.ace_text-input").value = "";
    //   document.querySelector("textarea.ace_text-input").value = appTopo;
    //
    // }, appTopo);
    //await page.waitForSelector("textarea.ace_text-input");
    //await page.keyboard.type(appTopo);
    //await page.waitForFunction("document.querySelector('textarea.ace_text-input').value.length > 0");
    //await textareaEl.type(appTopo)
    //await page.waitForFunction("document.querySelector('textarea.ace_text-input').value = \"\"");
    //await page.type('textarea[class=\"ace_text-input\"]', "tst23423423434");
    await page.waitForSelector("button[id=\"save-file\"]");
    await page.click("button[id=\"save-file\"]");

    // await page.waitForSelector("button.btn.btn-success.btn-xs.ng-binding[ng-click=\"save()\"]");
    // let saveBtnEl =  (await page.$x('//button[contains(@class, "btn-success") and contains(string(), "Save")]'))[0];
    // await saveBtnEl.click();
    let envLinkEl = (await page.$x('//a[contains(text(), "Environment")]'))[0];
    await envLinkEl.click();
    await page.waitForSelector("button.btn-primary[ng-click=\"save()\"]");
    let saveBtnDlgEl = (await page.$x('//button[contains(@class, "btn-primary") and contains(text(), "Save")]'))[0];
    saveBtnDlgEl.click();
    let linkTopoloGyEl = await page.waitForSelector("a[id=\"applications.detail.environment.deploynext.topology\"]");
    linkTopoloGyEl.click();
    btnEditTopo = await page.waitForSelector("button[id=\"edit-topo-btn\"]");
    await btnEditTopo.click();
    envLinkEl = (await page.$x('//a[contains(text(), "Environment")]'))[0];
    await envLinkEl.click();
    console.log(orchestratorInstanceName);
    let orchestratorBtnEl = await page.waitForSelector("span.location-match.clickable-media");//(await page.waitForXPath('//span/b[contains(@class, "ng-binding") and contains(text(), "' + orchestratorInstanceName + '")]'))[0];
    await orchestratorBtnEl.click();

    // Two get errors, get rid of them
    await page.click("div[id='toast-container']").catch(e => e);
    await page.click("div[id='toast-container']").catch(e => e);

    let linkReviewDeployEl = await page.waitForSelector("a[id=\"applications.detail.environment.deploynext.deploy\"]");
    linkReviewDeployEl.click();
    let btnDeployEl = await page.waitForSelector("button[id=\"btn-deploy\"]");
    btnDeployEl.click();
    //await page.waitForXPath("a[contains(@class, \"btn-danger\") and contains(string(), \"Undeploy\")]");
    let linkUndeployEl = await page.waitForSelector("a.btn.btn-danger.ng-binding.ng-scope.disabled");
    await page.reload();
}

puppeteer.launch({headless: true, dumpio: true}).then(async browser => {
  const page = await browser.newPage();
  page.on('console', consoleObj => console.log(consoleObj.text()));
  await page.setViewport({ width: pageW, height: pageH })
  console.log(program.host);
  await page.goto(program.host);

  await doLogin(page, program.adminuser, program.adminpassw);
  await doCreateOrchestratorInstance(page, orchestratorInstanceName);
  await doEnableOrchestratorInstance(page, orchestratorInstanceName, orchestratorLocationInstanceName, infrastructureType);
  await doCreateApp(page, orchestratorInstanceName, appName, pageW, pageH, appTopo);

	await browser.close();
});
