package cn.iris.cloud.metrics.register;

import cn.iris.cloud.metrics.config.MetricsConfigProperties;
import cn.iris.cloud.metrics.config.MetricsConfigTag;
import cn.iris.cloud.metrics.utils.Environments;
import cn.iris.cloud.metrics.utils.JsonUtil;
import cn.iris.cloud.metrics.utils.MetricsConstant;
import cn.iris.cloud.thread.ThreadPoolFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RegisterManager
 * refer: https://www.consul.io/api-docs/agent/service#register-service
 *
 * @author wuhao
 */

public class ServerRegisterTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRegisterTask.class);
    private AtomicBoolean start = new AtomicBoolean(false);
    private ScheduledExecutorService scheduledExecutorService = ThreadPoolFactory.getSingleScheduledThreadPoolExecutor("RegisterManager");

    public void start(MetricsConfigProperties properties) {
        if (start.compareAndSet(false, true)) {
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                regMetricsServer(properties, null);
            }, properties.getRegIntervalTime(), properties.getRegIntervalTime(), TimeUnit.SECONDS);
        }
    }

    public void regMetricsServer(MetricsConfigProperties properties, ServerRegistration registration) {
        String url = null;
        try {
            url = properties.getConsulUrl();
            if (properties.isAutoReg() && StringUtils.isNotBlank(url)) {
                if (registration == null){
                    registration = getServerRegistration(properties);
                }
                String[] urls = url.split(",");
                for (String item : urls) {
                    checkAndReg(item, registration);
                }
            }
        } catch (Exception e) {
            LOGGER.info("regMetricsServer fail:{}", url);
        }
    }

    public void checkAndReg(String url, ServerRegistration registration) {
        try {
            if (check(url, registration)) {
                return;
            }
            reg(url, JsonUtil.toJson(registration).toString());
        } catch (Exception e) {
            LOGGER.info("checkAndReg fail:{} {}", url);
        }
    }

    public boolean check(String url, ServerRegistration registration) {
        String checkUrl = "";
        String checkBody = "";
        try {
            checkUrl = url+"/" + registration.getId();
            checkBody = httpRequest(checkUrl, "", "GET" );
            if (StringUtils.isEmpty(checkBody)){
                return false;
            }
            JsonNode jsonNode = JsonUtil.parse(checkBody);
            if (jsonNode.has(MetricsConstant.ID)){
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.warn("checkUrl:{} body:{} fail {}", checkUrl, checkBody, e.getMessage());
        }
        return false;
    }

    public void reg(String url, String content) {
        httpRequest(url + "/register", content, "PUT");
    }

    public String httpRequest(String url, String content, String method) {
        String body = "";
        try {
            URL regUrl = new URL(url);
            URLConnection urlConnection = regUrl.openConnection();
            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setDoOutput(true);
            if (StringUtils.isNotEmpty(content)){
                OutputStream os = connection.getOutputStream();
                os.write(content.getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();
            }
            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            inputStream.close();
            outputStream.close();
            body = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.warn("httpRequest url:{} req{}: rep{} fail", url, content, body);
        }
        return body;
    }

    /**
     * 获取注册信息
     *
     * @return
     */
    public ServerRegistration getServerRegistration(MetricsConfigProperties properties) {
        ServerRegistration registration = new ServerRegistration();

        //服务标签
        List<String> tags = new ArrayList<>();
        tags.add(properties.getAppName());

        //服务检测
        List<ServerCheck> checks = new ArrayList<>();
        ServerCheck serverCheck = new ServerCheck();
        serverCheck.setTimeout(properties.getDataTimeOut());
        serverCheck.setInterval(properties.getKeepAliveTime());
        serverCheck.setDeregisterCriticalServiceAfter(properties.getKeepAliveExpireTime());
        serverCheck.setHttp(String.format(MetricsConstant.HEALTH_URL,
			properties.getIp(), properties.getPort()));
        checks.add(serverCheck);

        ServerMeta serverMeta = new ServerMeta();
        serverMeta.setVersion("1.0");
        serverMeta.setApp(Environments.getServerApp());

        registration.setId(properties.getNodeName())
                .setName(Environments.getServerApp())
                .setAddress(properties.getIp())
                .setPort(properties.getPort())
                .setTags(tags)
                .setMeta(serverMeta)
                .setChecks(checks);
        return registration;
    }
}

/**
 * curl -X PUT -d '{"id": "test1","name": "test1","address": "192.168.56.12","port": 9100,"tags": ["service"],"checks": [{"http": "http://192.168.56.12:9100/","interval": "5s"}]}'
 * http://127.0.0.1:8500/v1/agent/service/register
 */