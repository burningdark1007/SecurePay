package com.sunmange.unionpay.acp.demo.token;

import com.sunmange.unionpay.acp.demo.DemoBase;
import com.sunmange.unionpay.acp.sdk.AcpService;
import com.sunmange.unionpay.acp.sdk.LogUtil;
import com.sunmange.unionpay.acp.sdk.SDKConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Enrollment transaction
 */
public class Form_5_1_2_Enrollment extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String merId = req.getParameter("merId");
        String orderId = req.getParameter("orderId");
        String txnTime = req.getParameter("txnTime");
        String phoneNo = req.getParameter("phoneNo");
        String accNo = req.getParameter("accNo");

        Map<String, String> contentData = new HashMap<String, String>();

        /***For an all-channel UnionPay system, all parameters can be left unchanged except the one encoding, which you need to set as required.***/
        contentData.put("version", DemoBase.version);                    //M
        contentData.put("encoding", DemoBase.encoding);                //M
        contentData.put("signMethod", SDKConfig.getConfig().getSignMethod()); //M
        // Value: 79
        contentData.put("txnType", "79");// M
        //04: Easy enrollment
        //07: SecurePlus enrollment
        contentData.put("txnSubType", "04");// M
        // 000301: Merchant-hosted
        // 000902: Token payment
        contentData.put("bizType", "000201");// M
        // 0: Merchant direct access
        // 1: Acquirer Access
        contentData.put("accessType", "0");// M
        // Channel type
        contentData.put("channelType", "07");// M

        contentData.put("merId", merId);                               //M client number (this client number is used only for transaction test and debugging and this client has configured to encrypt sensitive information). In the test, please modify this client number to the one you have applied [the test client number starting with 777 you have registered does not support product collecting]

        contentData.put("orderId", orderId);                           //M client order number, consisting of 8-40 alphanumeric characters, no “-” or “_” is allowed, but custom rules are allowed
        contentData.put("txnTime", txnTime);                           //M order delivery time: It must be in format of YYYYMMDDhhmmss. Be sure to use the current time. Otherwise, an error of invalid txnTime will be reported.
        contentData.put("accType", "01");                              //M account number type

        Map<String, String> customerInfoMap = new HashMap<String, String>();
        customerInfoMap.put("phoneNo", phoneNo);                       //Phone number
        customerInfoMap.put("cvn2", "123");                            //The three digits corresponding to cvn2 at the back of the card
        customerInfoMap.put("expired", "1711");                        //Validity: The year should come before the month.
        customerInfoMap.put("smsCode", "111111");                        //SMS authentication code: Always fill in 111111 in test environment.
        ////////////If the client has enabled the right [encrypt sensitive information by the client], you need to encrypt accNo and phoneNo for use.
        String accNo1 = AcpService.encryptData(accNo, "UTF-8");  //A test card number is used here because it is in test environment. In normal environment, please use a real card number instead.
        contentData.put("accNo", accNo1);  //M
        contentData.put("encryptCertId", AcpService.getEncryptCertId());       //certId of the encryption certificate, which is configured under the acpsdk.encryptCert.path property of the acp_sdk.properties file.
        String customerInfoStr = AcpService.getCustomerInfoWithEncrypt(customerInfoMap, null, DemoBase.encoding);

        contentData.put("customerInfo", customerInfoStr);    //M

        /**Sign the requested parameters, and send http.post requests and receive synchronous response messages.**/
        Map<String, String> reqData = AcpService.sign(contentData, DemoBase.encoding);             //In a message, the values of certId and signature are obtained from the signData method and are assigned with values automatically. Therefore, you just need to ensure that the certificate is correctly configured.
        String requestBackUrl = SDKConfig.getConfig().getBackRequestUrl();                                 //At the url of the transaction request, you can read the acpsdk.backTransUrl in the corresponding property file acp_sdk.properties from the configuration file.
        Map<String, String> rspData = AcpService.post(reqData, requestBackUrl, DemoBase.encoding); //Send request messages and receive synchronous responses (the default connection timeout is 30s and the timeout for reading the returned result is 30s); Here, after calling signData, do not make any modification to the value of any key in submitFromData before calling submitUrl. Any such modification may cause failure to signature authentication.

        /**To proceed the response codes, you need to compile a program based on your business logics. The logics for response code processing below are for reference only.------------->**/
        //For response code specifications, refer to Part 5 “Appendix” in Specifications for Platform Access Interfaces- by selecting Help Center > Download > Product Interface Specifications at open.unionpay.com.
        if (!rspData.isEmpty()) {
            if (AcpService.validate(rspData, DemoBase.encoding)) {
                LogUtil.writeLog("Signature authentication succeeds");
                String respCode = rspData.get("respCode");
                if (("00").equals(respCode)) {
                    //Succeed
                    //TODO
                } else {
                    //Other response codes are failure. Please find the cause or treat it as a failure.
                    //TODO
                }
            } else {
                LogUtil.writeErrorLog("Signature authentication fails");
                //TODO Find the reason why the signature authentication fails
            }
        } else {
            //The returned http state code is incorrect.
            LogUtil.writeErrorLog("No returned message is obtained or the returned http state code is not 200.");
        }
        String reqMessage = DemoBase.genHtmlResult(reqData);
        String rspMessage = DemoBase.genHtmlResult(rspData);
        resp.getWriter().write("Request message:<br/>" + reqMessage + "<br/>" + "Response message:</br>" + rspMessage + "");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

}
