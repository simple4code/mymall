package com.hzc.mymall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.hzc.mymall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000122607149";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCnlVypcxPbRyJ/9IrEPXF/geTmTyZQPGc6sHLQrfN0Gu2IVUK0RFUr5wV3ale3M5A1+C47kmCuKT5Abi3kj7N5d4TufnHqakhV6PWktn0FrG7JhaUDbdzjGpegh491/NXvW59Lxzus95pxSAbtinAauRr2oOBUjxBKrqu4sSUDn+YsDz06NJrSxDl/W42n+qufJPzEKJs8RDYwhJ79w7oBDa7XsxhpZUxwXYyIRRAbmWApimYuLX9wSBkRyOOnwM6AN0yQU4pBufudQdZnmnL6fqp+hjN2KULJWAclAlUQCtjXH8ZqGTYc2xDvxzT/ABNuyQgDmuauAVRMlmnDOENBAgMBAAECggEBAJjpMYawKUMg0jr80I2lHuXgiRMYz3XQ18R2ch01S/n6BBks6tN/slN/1b4Te3v/gautC8pxkuP3YAT8Q8XU1UeVJedT2u+xENXK3jDwDn3Zy7D30Ayj9zQF9KqWZvGaxT5FVMGZRjtaJkMNHL0sX4QSuKanPmoJrCzo2mmIwaRehhhC1FyqXg3pxs4GM8DBJ0zzZmmuIHJbeMizhVHTpDv8uukRdn7PKXBTMk43+47vabv7dW8AWVlYPgGNXdY+BPJyFHi6SpEFrq70ZtltTz6OpHoUarB5zGL5zObzAnH46efuClpdZrtfeFxFTF5JzKl/Rc7lAATgfPk1iL8p/dECgYEA/DbtHondKOmtyXdA5FGI7rdv4+qH6Zcn+bYNAwIy3X8RIhGqiWcGoxIv//jT97a6ZddZnO8mzaqte4aUbKENkevcrbi/FjcEqAdniQxiFONCWTsmeTT2JZro/pL0a6tvv1xK5jGA3ojg1G0D9BRoPSdKKin2fTr18SJxp3SghzUCgYEAqhlC0vkJ3aDGCYcueq3c5UUxbK9Ld5IuFmKe5nqy+fijm602/yCweHjvj8g6ZdZ7A28CbfkfXC7Zmsq9+CFVOZJ+9Ci9gcPxMpoMOmaYGdFciloDKiKQC/SgYHyDP4bmRBf4b95aPLLUcMQRERUIyHesn/9YjGz+RV/XplGuMV0CgYBGtA8c9IDvKzLwu2GRn9nHMd1IA0M+lIU8V3CmW0NvHGPtLQ6lniAaLssN8u9ZDfyK+CfC+rfiB+rCKiQJn8xyD7C5coT+8UV45Tp9DN+iA2NiKFbMu7AVMqwGUaP7Wv/koTbN+SqrN6vvYPAuyFnAavc7gq/6w0CW7JT34JXv0QKBgAKTH0/SNTQFmvnJ8gopV46g3X6nqJzGuavdEkqq0Mq32MIifRKfjAxGyFmEzl18QaBrrb28Z50dmG2fZAC9gGy+qu1HMJcmQCBj9Ittwh+h66SseB/LJiMIMOVE/TeY8yNUuY3376W7jzulvBxmFRvzs8k4ND7r521lZf4+fkSBAoGBAKh3Zc4XvNr9qfZMJg/w61O6QrXnE6cH00eQknNFK5djHj3LKkWUo7KTHQrtn3msVfCgU/+wGVcWUG9p1qtjedYtsUTf3yXZWb0M3qn6tVW8q5kVBodscQ/ow2K8r+L71Rbcmc6TKT+XQ8V+im4Q8v49mYSMgdW34shZSygK7Qj1";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlZwcvhGotkRU8RgWUxqAtCeg1wVb3ThlZiLo/p1OW95SzN/gP3Jwaj/cUmwz1HdLfyxFA19twPex3ngSVcJYCpYklm+FKZFVqu3FjJ2x9XaoOgwktYerF/DzOLzDlMsxb7omRDq+YizMDxTEvApAO85pBHocSglhRKEuTCmILxjXCHdJcAv75xdptGjlW0P02NinIY17TSfKrsvuMBYRtlEf1EVSH0IThxHAsouynK7l4lKtuwkhkV/MtiDUT1RufaKWNwatGGHvjwW6pFSvMU39wllkgoI+Q+Ro6BlHF6Wq1cJumY8eV5soqkc24BvEED49VHR/hGAxlMhP3zqpQwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
