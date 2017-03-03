package org.ali.alipay;

import android.text.TextUtils;
import com.alipay.sdk.app.PayTask;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.util.Log;
import android.os.Message;
import android.os.Handler;
import android.widget.Toast;
import org.apache.cordova.PluginResult;

public class AliPayPlugin extends CordovaPlugin {
    private static String TAG = "AliPayPlugin";

    //商户PID
    private String partner = "";
    //商户收款账号
    private String seller = "";
    //商户私钥，pkcs8格式
    private String APPID = "2016083001824756";

    private String privateKey = "";

    protected CallbackContext currentCallbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        partner = webView.getPreferences().getString("partner", "");
        seller = webView.getPreferences().getString("seller", "");
        privateKey = webView.getPreferences().getString("privatekey", "");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        try {
            JSONObject arguments = args.getJSONObject(0);
            String tradeNo = arguments.getString("tradeNo");
            String subject = arguments.getString("subject");
            String body = arguments.getString("body");
            String price = arguments.getString("price");
            String payinfo = arguments.getString("payinfo");
            String notifyUrl = "http://sys.oonline.sciencereading.cn/api/alipay/notify_url.php";
            //callbackContext.error(0);
            this.pay(tradeNo, subject, body, payinfo, notifyUrl, callbackContext);
        } catch (JSONException e) {
            callbackContext.error(new JSONObject());
            e.printStackTrace();
            return false;
        }
        return true;
    }





    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    Map<String, Object> obj = (Map<String, Object>) msg.obj;

                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) obj.get("result"));
                    /**
                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    CallbackContext callbackContext = (CallbackContext)obj.get("callback");
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。


                        callbackContext.success(payResult.toJson());
                        Toast.makeText(cordova.getActivity(),"支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        //callbackContext.error(payResult.toJson());
                        PluginResult result = new PluginResult(PluginResult.Status.ERROR);
                        result.setKeepCallback(true);
                        callbackContext.sendPluginResult(result);
                        Toast.makeText(cordova.getActivity(), "支付失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                /*case SDK_AUTH_FLAG: {
                    @SuppressWarnings("unchecked")
                    AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
                    String resultStatus = authResult.getResultStatus();

                    // 判断resultStatus 为“9000”且result_code
                    // 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
                    if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
                        // 获取alipay_open_id，调支付时作为参数extern_token 的value
                        // 传入，则支付账户为该授权账户
                        Toast.makeText(PayDemoActivity.this,
                                "授权成功\n" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        // 其他状态值则为授权失败
                        Toast.makeText(PayDemoActivity.this,
                                "授权失败" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT).show();

                    }
                    break;
                }*/
                default:
                    break;
            }
        };
    };

    public void pay(String tradeNo, String subject, String body, String in_payinfo, String notifyUrl, final CallbackContext callbackContext) {
        //currentCallbackContext = callbackContext;

        Map<String, String> params = buildOrderParamMap("2016083001824756", tradeNo , partner, seller, subject, body, "0.01");
        String orderParam = buildOrderParam(params);
        String sign = getSign(params, privateKey);

        //final String payInfo = orderParam + "&" + sign;

        //final String payInfo = "app_id=2016083001824756&biz_content=%7B%22timeout_express%22%3A%2230m%22%2C%22seller_id%22%3A%22%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%2C%22total_amount%22%3A%220.01%22%2C%22subject%22%3A%22%5Cu63cf%5Cu8ff0%22%2C%22body%22%3A%22%5Cu51fa%5Cu7248%5Cu793e%22%2C%22out_trade_no%22%3A%22sale11320161202163414609%22%7D&charset=utf-8&format=json&method=alipay.trade.app.pay¬ify_url=http%3A%2F%2Fsys.sp.kf.gli.cn%2Fapi%2Falipay%2Fnotify_url.php×tamp=2016-12-02+16%3A34%3A14&version=1.0&sign=peBM%2BEhbFoetwQpDqefp6u9peFN%2BxL0xje2GLqhex7bc76%2FpexA8r2eiq2m2WnjoAFQCJutbzae5PdU4FYJVfbVbyYUtcAFk9OYSf3GCI67wLfW2t%2BtImmzJEBKFsMB0zFmPn1kMZbRwOVaB1rvI%2BZ7NdRdOopY601zFDegI55E%3D&sign_type=RSA";

        final String payInfo = in_payinfo;

        // 订单
        /*String orderInfo = createRequestParameters(subject, body, "0.01", tradeNo, notifyUrl);

        // 对订单做RSA 签名
        String sign = sign(orderInfo);
        try {
            // 仅需对sign 做URL编码
            sign = URLEncoder.encode(sign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
                + getSignType();*/


        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                PayTask alipay = new PayTask(cordova.getActivity());
                Map<String, String> result = alipay.payV2(payInfo, true);
                Map<String , Object> obj = new HashMap<String, Object>();
                obj.put("result" ,result);
                obj.put("callback" ,callbackContext);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = 1;
                msg.obj = obj;
                mHandler.sendMessage(msg);

                // 构造PayTask 对象
                //PayTask alipay = new PayTask(cordova.getActivity());
                // 调用支付接口，获取支付结果
                //Map<String, String> result = alipay.payV2(payInfo, true);


                //Log.i("msp", result.toString());

                //Message msg = new Message();
                //msg.what = SDK_PAY_FLAG;
                //msg.obj = result;
               // mHandler.sendMessage(msg);
                //String result = alipay.payV2(payInfo,true);

                //PayResult payResult = new PayResult(result);
                /*if (TextUtils.equals(payResult.getResultStatus(), "9000")) {
                    callbackContext.success(payResult.toJson());
                } else {
                    // 判断resultStatus 为非“9000”则代表可能支付失败
                    // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                    if (TextUtils.equals(payResult.getResultStatus(), "8000")) {
                        callbackContext.success(payResult.toJson());
                    } else {
                        callbackContext.error(payResult.toJson());
                    }
                }*/
            }
        });
        this.cordova.setActivityResultCallback(this);
        /*
         Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask(cordova.getActivity());
                Map<String, String> result = alipay.payV2(payInfo, true);
                Map<String , Object> obj = new HashMap<String, Object>();
                obj.put("result" ,result);
                obj.put("callback" ,callbackContext);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = 1;
                msg.obj = obj;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
        */
    }

    /**
     * create the order info. 创建订单信息
     */
    public String createRequestParameters(String subject, String body, String price, String tradeNo, String notifyUrl) {

        // APP 支付
        /*
        String orderInfo = "app_id=" + "\"" + "2016083001824756" + "\"";

        orderInfo += "&biz_content=" + "\"" + "{\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\"0.01\",\"subject\":\"测试\",\"body\":\"我是测试数据\",\"out_trade_no\":\"" + tradeNo +  "\"}" + "\"";

        orderInfo += "&charset=\"utf-8\"";

        orderInfo += "&method=\"alipay.trade.app.pay\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + "http://sys.online.sciencereading.cn/api/alipay/notify_url.php" + "\"";

        orderInfo += "&sign_type=\"RSA\"";

        orderInfo += "&timestamp=\"2016-07-29 16:55:53\"";

        orderInfo += "&version=\"1.0\"";*/

    // 移动支付

        //String biz_content = {"timeout_express"+":"+"30m","seller_id:"+ "\"" + seller + "\""};

        // 商品金额
        String orderInfo = "&total_fee=" + "\"" + price + "\"";

        // 签约合作者身份ID
        orderInfo += "&partner=" + "\"" + partner + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + seller + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + tradeNo + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";


        orderInfo += "&notify_url=" + "\"" + "http://sys.online.sciencereading.cn/api/alipay/notify_url.php" + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";


        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        //orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        //orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空


        //orderInfo += "&return_url=\"http://sys.online.sciencereading.cn/api/alipay/return_url.php\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        //orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    public static Map<String, String> buildOrderParamMap(String app_id,String tradeNo,String partner,String seller,String subject,String body,String total_fee) {
        Map<String, String> keyValues = new HashMap<String, String>();

        keyValues.put("app_id", app_id);

        keyValues.put("biz_content", "{\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\"0.01\",\"subject\":\"1\",\"body\":\"123456\",\"out_trade_no\":\"" + tradeNo +  "\"}");

        keyValues.put("charset", "utf-8");

        keyValues.put("method", "alipay.trade.app.pay");

        keyValues.put("sign_type", "RSA");

        keyValues.put("timestamp", "2016-07-29 16:55:53");

        keyValues.put("version", "1.0");

        keyValues.put("notify_url","http://sys.online.sciencereading.cn/api/alipay/notify_url.php");


        return keyValues;
    }


    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content 待签名订单信息
     */
    public String sign(String content) {
        return SignUtils.sign(content, privateKey);
    }



    /**
     * 构造支付订单参数信息
     *
     * @param map
     * 支付订单参数
     * @return
     */
    public static String buildOrderParam(Map<String, String> map) {
        List<String> keys = new ArrayList<String>(map.keySet());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            String value = map.get(key);
            sb.append(buildKeyValue(key, value, true));
            sb.append("&");
        }

        String tailKey = keys.get(keys.size() - 1);
        String tailValue = map.get(tailKey);
        sb.append(buildKeyValue(tailKey, tailValue, true));

        return sb.toString();
    }


    /**
     * 拼接键值对
     *
     * @param key
     * @param value
     * @param isEncode
     * @return
     */
    private static String buildKeyValue(String key, String value, boolean isEncode) {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append("=");
        if (isEncode) {
            try {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                sb.append(value);
            }
        } else {
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * 对支付参数信息进行签名
     *
     * @param map
     *            待签名授权信息
     *
     * @return
     */
    public static String getSign(Map<String, String> map, String rsaKey) {
        List<String> keys = new ArrayList<String>(map.keySet());
        // key排序
        Collections.sort(keys);

        StringBuilder authInfo = new StringBuilder();
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            String value = map.get(key);
            authInfo.append(buildKeyValue(key, value, false));
            authInfo.append("&");
        }

        String tailKey = keys.get(keys.size() - 1);
        String tailValue = map.get(tailKey);
        authInfo.append(buildKeyValue(tailKey, tailValue, false));

        String oriSign = SignUtils.sign(authInfo.toString(), rsaKey);
        String encodedSign = "";

        try {
            encodedSign = URLEncoder.encode(oriSign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "sign=" + encodedSign;
    }
}
