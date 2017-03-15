Cordova 支付宝支付插件
======


## 安装命令

	cordova plugin add https://github.com/EiyouZk/cordova-plugin-alipay.git



## 使用方法
```
window.alipay.pay({
	tradeNo: tradeNo,
	subject: "测试标题",
	body: "我是测试内容",
	price: 0.02,
	notifyUrl: "http://your.server.notify.url"
}, function(successResults){alert(results)}, function(errorResults){alert(results)});
```
### 参数说明

* tradeNo 这个是支付宝需要的，应该是一个唯一的ID号
* subject 这个字段会显示在支付宝付款的页面
* body 订单详情，没找到会显示哪里
* price 价格，支持两位小数
* payinfo 订单休息
* function(successResults){} 是成功之后的回调函数
* function(errorResults){} 是失败之后的回调函数

注：改变之前加密、签名均在客户端的方式，有服务端生成订单信息以及加密、签名之后，通过payinfo传入，所以实际参数只有payinfo以及成功或者失败的回调函数。

`successResults`和`errorResults`分别是成功和失败之后支付宝SDK返回的结果，类似如下内容

```
// 成功
{
	resultStatus: "9000",
	memo: "",
	result: "partner=\"XXXX\"&seller_id=\"XXXX\"&out_trade_no=\"XXXXX\"..."	
}
```
```
// 用户取消
{
	memo: "用户中途取消", 
	resultStatus: "6001", 
	result: ""
}
```

* resultStatus的含义请参照这个官方文档：[客户端返回码](https://doc.open.alipay.com/doc2/detail?treeId=59&articleId=103671&docType=1)
* memo：一般是一些纯中文的解释，出错的时候会有内容。
* result: 是所有支付请求参数的拼接起来的字符串。

### 关于私钥
这里用的私钥一定是**PKCS**格式的，详细生成步骤请参照官方文档：[RSA私钥及公钥生成](https://doc.open.alipay.com/doc2/detail.htm?spm=0.0.0.0.WSkmo8&treeId=58&articleId=103242&docType=1)  

文档中描述的这一步：`OpenSSL> pkcs8 -topk8 -inform PEM -in rsa_private_key.pem -outform PEM -nocrypt`会将生成的私钥**打印到屏幕上**，记得复制下来。


## 手动安装
1. 使用git命令将插件下载到本地，并标记为$CORDOVA_PLUGIN_DIR

		git clone https://github.com/CUGCQH/cordova-plugin-alipay.git && cd cordova-plugin-alipay && export CORDOVA_PLUGIN_DIR=$(pwd)

2. 安装

		cordova plugin add $CORDOVA_PLUGIN_DIR --variable PARTNER_ID=[你的商户PID可以在账户中查询] --variable SELLER_ACCOUNT=[你的商户支付宝帐号]

