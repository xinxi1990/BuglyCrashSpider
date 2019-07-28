const puppeteer = require('puppeteer');
const log = (s) => console.log(s);
const app_id = 'xxxx';
const p_id = '1';

const qq = 'xxx';
const qq_pass = 'xxx';

const options = {
    headless: true,
    devtools: false,
    ignoreHTTPSErrors: true
};

async function login_success(x_token, bugly_session) {
    log("bugly_session: " + bugly_session)
    log("x-token: " + x_token)
}

async function login(login_frame) {
    log("模拟登录")
    await login_frame.click('#switcher_plogin')
    await login_frame.type('#u', qq)
    await login_frame.type('#p', qq_pass)
    await login_frame.click('#loginform > div.submit > a')
}

async function get_token_session(browser, page) {
    await page.setRequestInterception(true)
    let bugly_session = undefined
    let x_token = undefined
    const interceptor = async (request) => {
        const headers = request.headers()
        if (x_token == undefined && bugly_session == undefined) {
            log(headers)
            log("获取 token 和 cookie")
            x_token = headers['x-token']
            const cookies = await page.cookies()
            bugly_session = cookies.filter(c => c.name == 'bugly_session')[0].value
            await login_success(x_token, bugly_session)
            await browser.close();
        }
        request.continue()
    }

    page.on('request', interceptor)
}

(async () => {
    const browser = await puppeteer.launch(options)
    const page = await browser.newPage()
    await page.goto('https://bugly.qq.com/v2/crash-reporting/crashes/' + app_id + '?pid=' + p_id)
    await page.waitForNavigation()
    let url = await page.url()
    log(url)
    if (url.indexOf("auth") > 0) {
        log("登录")
        const login_frame = await page.frames().filter(iframe => iframe._name == 'ptlogin_iframe')[0]
        await login(login_frame)
        await page.waitForNavigation()
        url = await page.url()
        log(url)
        if (url.indexOf("bugly") > 0) {
            log("登录成功")
            await get_token_session(browser, page)
        } else {
            log("模拟登录失败")
        }
    } else {
        log("无需登录")
    }
})()