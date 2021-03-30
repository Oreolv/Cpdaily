# -*- coding: utf-8 -*-
import sys
import json
import uuid
import oss2
import yaml
import base64
import requests
from pyDes import des, CBC, PAD_PKCS5
from datetime import datetime, timedelta, timezone
from urllib.parse import urlparse
from urllib3.exceptions import InsecureRequestWarning

# debug模式
debug = False
if debug:
    requests.packages.urllib3.disable_warnings(InsecureRequestWarning)


# 读取yml配置
def getYmlConfig(yaml_file='config.yml'):
    file = open(yaml_file, 'r', encoding="utf-8")
    file_data = file.read()
    file.close()
    config = yaml.load(file_data, Loader=yaml.FullLoader)
    return dict(config)


# 全局配置
config = getYmlConfig(yaml_file='config.yml')
apis = {'login-url': 'http://authserver.cumt.edu.cn/authserver/login?service=http%3A%2F%2Fauthserver.cumt.edu.cn%2Fauthserver%2Fmobile%2Fcallback%3FappId%3D744946645598208000', 'host': 'cumt.campusphere.net'}


# 获取当前utc时间，并格式化为北京时间
def getTimeStr():
    utc_dt = datetime.utcnow().replace(tzinfo=timezone.utc)
    bj_dt = utc_dt.astimezone(timezone(timedelta(hours=8)))
    return bj_dt.strftime("%Y-%m-%d %H:%M:%S")


# 输出调试信息，并及时刷新缓冲区
def log(content):
    print(getTimeStr() + ' ' + str(content))
    sys.stdout.flush()

# 登陆并获取session
def getSession(user, apis):
    api='http://47.98.49.243:8080/wisedu-unified-login-api-v1.0/api/login'
    user = user['user']
    params = {
        'login_url': apis['login-url'],
        'needcaptcha_url': '',
        'captcha_url': '',
        'username': user['username'],
        'password': user['password']
    }

    cookies = {}
    # 借助上一个项目开放出来的登陆API，模拟登陆
    res = requests.post(url=api, data=params, verify=not debug)
    # cookieStr可以使用手动抓包获取到的cookie，有效期暂时未知，请自己测试
    cookieStr = str(res.json()['cookies'])
# log(cookieStr)
    if cookieStr == 'None':
        log(res.json())
        
    # 解析cookie
    for line in cookieStr.split(';'):
        name, value = line.strip().split('=', 1)
        cookies[name] = value
    session = requests.session()
    session.cookies = requests.utils.cookiejar_from_dict(cookies)
    session.get(url='https://cumt.campusphere.net/portal/login')
    return session




def getUnSignedTasks(session, apis):
    headers = {
        'Accept': 'application/json, text/plain, */*',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36',
        'content-type': 'application/json',
        'Accept-Encoding': 'gzip,deflate',
        'Accept-Language': 'zh-CN,en-US;q=0.8',
        'Content-Type': 'application/json;charset=UTF-8'
    }
    # 第一次请求每日签到任务接口，主要是为了获取MOD_AUTH_CAS
    res = session.post(
        url='https://{host}/wec-counselor-sign-apps/stu/sign/queryDailySginTasks'.format(host=apis['host']),
        headers=headers, data=json.dumps({}), verify=not debug)
    # 第二次请求每日签到任务接口，拿到具体的签到任务
    res = session.post(
        url='https://{host}/wec-counselor-sign-apps/stu/sign/queryDailySginTasks'.format(host=apis['host']),
        headers=headers, data=json.dumps({}), verify=not debug)
    if len(res.json()['datas']['unSignedTasks']) < 1:
        log('当前没有未签到任务')
        exit(-1)
    # log(res.json())
    latestTask = res.json()['datas']['unSignedTasks'][0]
    return {
        'signInstanceWid': latestTask['signInstanceWid'],
        'signWid': latestTask['signWid']
    }


# 获取签到任务详情
def getDetailTask(session, params, apis):
    headers = {
        'Accept': 'application/json, text/plain, */*',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36',
        'content-type': 'application/json',
        'Accept-Encoding': 'gzip,deflate',
        'Accept-Language': 'zh-CN,en-US;q=0.8',
        'Content-Type': 'application/json;charset=UTF-8'
    }
    res = session.post(
        url='https://{host}/wec-counselor-sign-apps/stu/sign/detailSignTaskInst'.format(host=apis['host']),
        headers=headers, data=json.dumps(params), verify=not debug)
    data = res.json()['datas']
    # log(data)
    return data


# 填充表单
def fillForm(task, session, user, apis):
    user = user['user']
    form = {}
    form['signInstanceWid'] = task['signInstanceWid']
    form['longitude'] = user['lon']
    form['latitude'] = user['lat']
    return form



# DES加密
def DESEncrypt(s, key='ST83=@XV'):
    key = key
    iv = b"\x01\x02\x03\x04\x05\x06\x07\x08"
    k = des(key, CBC, iv, pad=None, padmode=PAD_PKCS5)
    encrypt_str = k.encrypt(s)
    return base64.b64encode(encrypt_str).decode()


# 提交签到任务
def submitForm(session, user, form, apis):
    user = user['user']
    # Cpdaily-Extension
    extension = {
        "lon": user['lon'],
        "model": "OPPO R11 Plus",
        "appVersion": "8.1.14",
        "systemVersion": "4.4.4",
        "userId": user['username'],
        "systemName": "android",
        "lat": user['lat'],
        "deviceId": str(uuid.uuid1())
    }

    headers = {
        # 'tenantId': '1019318364515869',
        'User-Agent': 'Mozilla/5.0 (Linux; Android 4.4.4; OPPO R11 Plus Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/33.0.0.0 Safari/537.36 okhttp/3.12.4',
        'CpdailyStandAlone': '0',
        'extension': '1',
        'Cpdaily-Extension': DESEncrypt(json.dumps(extension)),
        'Content-Type': 'application/json; charset=utf-8',
        'Accept-Encoding': 'gzip',
        # 'Host': 'swu.cpdaily.com',
        'Connection': 'Keep-Alive'
    }
    res = session.post(url='https://{host}/wec-counselor-sign-apps/stu/sign/completeSignIn'.format(host=apis['host']),
                       headers=headers, data=json.dumps(form), verify=not debug)
    message = res.json()['message']
    if message == 'SUCCESS':
        log('自动签到成功')
    else:
        log('自动签到失败，原因是：' + message)
        



# 主函数
def main():
    for user in config['users']:
        log("\"分分钟\" 今日校园晚签到脚本 V2.1")
        log("软件后续更新发布在交流群326731424")
        log('当前用户：' + str(user['user']['username']))
        log('脚本开始执行...')
        log('开始模拟登陆...')
        session = getSession(user, apis)
        if session != None:
            log('模拟登陆成功...')
        else:
            log('模拟登陆失败...')
            log('原因可能是学号或密码错误，请检查配置后，重启脚本...')
        params = getUnSignedTasks(session, apis)
        task = getDetailTask(session, params, apis)
        form = fillForm(task, session, user, apis)
        # form = getDetailTask(session, user, params, apis)
        submitForm(session, user, form, apis)
    
    log("任务已完成，请手动关闭软件")
    input()

if __name__ == '__main__':
    main()
