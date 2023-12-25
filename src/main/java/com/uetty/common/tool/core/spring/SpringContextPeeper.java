package com.uetty.common.tool.core.spring;

import com.uetty.common.tool.core.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * static方式获取spring配置的窥视器
 * @author vince
 */
@Slf4j
public class SpringContextPeeper implements EnvironmentAware, ApplicationContextAware, BeanPostProcessor {

    // BeanPostProcessor用于在其他Bean初始化前后增加自定义处理，利用 BeanPostProcessor 接口的实现，可优先初始化加载该类

    private static volatile Environment environment;

    private static volatile ApplicationContext applicationContext;

    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("set environment");
        SpringContextPeeper.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextPeeper.applicationContext = applicationContext;
    }

    public static String getProperties(String key, String defValue) {
        if (environment == null) {
            throw new RuntimeException("environment has not been initialized");
        }

        return environment.getProperty(key, defValue);
    }

    public static String getProperties(String key) {
        if (environment == null) {
            throw new RuntimeException("environment has not been initialized");
        }

        return environment.getProperty(key);
    }

    public static int getIntProperties(String key, int defValue) {
        String value = getProperties(key);

        if (value == null) {
            return defValue;
        }

        return Integer.parseInt(value);
    }

    public static int getIntProperties(String key) {
        return getIntProperties(key, 0);
    }

    public static boolean getBooleanProperties(String key) {
        return getBooleanProperties(key, false);
    }

    public static boolean getBooleanProperties(String key, boolean defValue) {
        String value = getProperties(key);

        if (value == null) {
            return defValue;
        }
        return Boolean.parseBoolean(value);
    }

    public static <T> T getBean(Class<T> requiredType) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(requiredType);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return null;
        }
    }

    public static <T> T getBeanQuiet(Class<T> requiredType) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(requiredType);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> List<T> getBeansQuiet(Class<T> requiredType) {
        if (applicationContext == null) {
            return null;
        }
        try {
            Map<String, T> beansOfType = applicationContext.getBeansOfType(requiredType);
            return new ArrayList<>(beansOfType.values());
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return null;
        }
    }

    public static <T> T getBean(Class<T> requiredType, String name) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(name, requiredType);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return null;
        }
    }

    public static <T> T getBeanQuiet(Class<T> requiredType, String name) {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(name, requiredType);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isDomain(String host) {
        boolean isDomain = false;
        if (StringUtil.isNotBlank(host)) {
            char c = host.toLowerCase(Locale.ROOT).charAt(host.length() - 1);
            if (c >= 'a' && c <= 'z' && host.contains(".")) {
                isDomain = true;
            }
        }
        return isDomain;
    }

}
