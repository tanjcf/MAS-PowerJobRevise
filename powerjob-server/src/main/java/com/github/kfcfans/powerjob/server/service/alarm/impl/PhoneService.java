package com.github.kfcfans.powerjob.server.service.alarm.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.kfcfans.powerjob.server.common.PowerJobServerConfigKey;
import com.github.kfcfans.powerjob.server.persistence.core.model.JobInfoDO;
import com.github.kfcfans.powerjob.server.persistence.core.model.UserInfoDO;
import com.github.kfcfans.powerjob.server.persistence.core.model.WorkflowInfoDO;
import com.github.kfcfans.powerjob.server.service.alarm.Alarm;
import com.github.kfcfans.powerjob.server.service.alarm.Alarmable;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/***
 *电话通知
 * @author tanjc
 * @since 20200826
 */
@Slf4j
@Service
public class PhoneService implements Alarmable {
    private static final String TYPE = "phone";
    private static final String TITLE = "alarm_phone";
    private static String CONTENT = "执行异常";
    @Override
    public void onFailed(Alarm alarm, List<UserInfoDO> targetUserList,String wfName,String jobName) {
        SimpleDateFormat df = new SimpleDateFormat("HH");
        int hours=Integer.parseInt(df.format(new Date()));
        if (CollectionUtils.isEmpty(targetUserList) ||hours<9) {
            return;
        }
       for (int i=0;i<targetUserList.size();i++){
           log.error("[PhoneService] send Phone failed, reason is {}",targetUserList.get(i).getToken() );
           userPhone(targetUserList.get(i),wfName,jobName);

       }
    }


    public void userPhone(UserInfoDO userInfoDO,String wfName,String jobName) {

        JSONObject jsonParam = new JSONObject();
       // targetUserList.get(1).getPhone();
        jsonParam.put(PowerJobServerConfigKey.LINKED_APP_RECEIVER, userInfoDO.getPhone());
        jsonParam.put(PowerJobServerConfigKey.LINKED_APP_TYPE, TYPE);
        jsonParam.put(PowerJobServerConfigKey.LINKED_APP_TITLE, TITLE);
        if (jobName.equals("")){
            jsonParam.put(PowerJobServerConfigKey.LINKED_APP_CONTENT,wfName+":" + CONTENT);
        }else {
            jsonParam.put(PowerJobServerConfigKey.LINKED_APP_CONTENT,wfName+"@"+jobName+":" + CONTENT);
        }

        String param = jsonParam.toJSONString();


        String sendPost = sendPost2(PowerJobServerConfigKey.LINKED_APP_URL, param,userInfoDO.getToken());
        System.out.println(sendPost);
    }

    public static String sendPost2(String url, String data,String servicetokens) {
        String response = null;
        try {
            CloseableHttpClient httpclient = null;
            CloseableHttpResponse httpresponse = null;
            try {
                httpclient = HttpClients.createDefault();
                HttpPost httppost = new HttpPost(url);

                StringEntity stringentity = new StringEntity(data,
                        ContentType.create("text/json", "UTF-8"));
                httppost.setEntity(stringentity);
                httppost.setHeader(PowerJobServerConfigKey.LINKED_APP_SERVICETOKEN,servicetokens);
                httpresponse = httpclient.execute(httppost);
                response = EntityUtils
                        .toString(httpresponse.getEntity());

            } finally {
                if (httpclient != null) {
                    httpclient.close();
                }
                if (httpresponse != null) {
                    httpresponse.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
