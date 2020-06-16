package com.uetty.common.tool.core.validate;

import com.uetty.common.tool.core.string.StringUtil;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ValidationException;
import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 注解验参封装
 * 依赖javax.validation:validation-api中的注解
 */
@SuppressWarnings("unused")
@Slf4j
public class ValidationUtil {

    /**
     * 根据注解自动验参（无分组）
     * @param obj 验参类
     */
    public static void validate(Object obj) {
        validate(obj, null);
    }

    /**
     * 根据注解自动验参
     * @param obj 验参类
     * @param group 分组组对象
     */
    public static void validate(Object obj, Class<?> group) {
        if (obj == null) {
            return;
        }
        Class<?> clz = obj.getClass();
        Map<Field, Set<Annotation>> annotations = getAnnotations(clz);
        for (Map.Entry<Field, Set<Annotation>> fieldSetEntry : annotations.entrySet()) {
            Field field = fieldSetEntry.getKey();
            String fieldName = field.getName();
            Object val;
            try {
                val = field.get(obj);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                continue;
            }
            Set<Annotation> annos = fieldSetEntry.getValue();
            for (Annotation anno : annos) {
                ValidatorEnums validatorEnums = ValidatorEnums.valueOf(anno.getClass());
                if (validatorEnums != null
                        && validatorEnums.validator.accept(group, anno)) {
                    validatorEnums.validator.validateAnnotation(val, fieldName, anno);
                }
            }
        }
    }

    /**
     * 获取该类下所有属性与其注解
     * @param clz 类class
     */
    private static Map<Field, Set<Annotation>> getAnnotations(Class<?> clz) {
        Map<Field, Set<Annotation>> map = new HashMap<>();
        Field[] fields = clz.getFields();
        for (Field field : fields) {
            field.setAccessible(true);
            putAnnotations(map, field);
        }

        Field[] declaredFields = clz.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            putAnnotations(map, field);
        }
        return map;
    }

    private static void putAnnotations(Map<Field, Set<Annotation>> map, Field field) {
        Annotation[] annotations = field.getAnnotations();

        Set<Annotation> set = new HashSet<>(Arrays.asList(annotations));
        map.compute(field, (k, v) -> {
            if (v == null) return set;
            v.addAll(set);
            return v;
        });
    }

    private static String getMessage(String message, String def) {
        if (StringUtil.startsWith(message, "{javax.validation.")) {
            return def;
        }
        return message;
    }

    private static String patternAddFlags(String regexp, Pattern.Flag[] flags) {
        Set<Pattern.Flag> flagSet = new HashSet<>(Arrays.asList(flags));
        StringBuilder regexpBuilder = new StringBuilder(regexp);
        for (Pattern.Flag flag : flagSet) {
            switch (flag) {
                case UNIX_LINES:
                    regexpBuilder.insert(0, "(?d)");
                    break;
                case CASE_INSENSITIVE:
                    regexpBuilder.insert(0, "(?i)");
                    break;
                case COMMENTS:
                    regexpBuilder.insert(0, "(?x)");
                    break;
                case MULTILINE:
                    regexpBuilder.insert(0, "(?m)");
                    break;
                case DOTALL:
                    regexpBuilder.insert(0, "(?s)");
                    break;
                case UNICODE_CASE:
                    regexpBuilder.insert(0, "(?u)");
                    break;
                case CANON_EQ:
                    // 不支持
                    break;
            }
        }
        return regexpBuilder.toString();
    }

    /**
     * 是否接受该组
     * @param group 分组组对象
     * @param groups 分组列表
     * @return 组列表为空或者group在组列表中，返回true，否则返回false
     */
    private static boolean acceptGroup(Class<?> group, Class<?>[] groups) {
        if (groups.length == 0) {
            return true;
        }
        for (Class<?> item : groups) {
            if (item == group) {
                return true;
            }
        }
        return false;
    }

    private static void throwException(String message) {
         throw new ValidationException(message);
    }

    /**
     * 检查器接口
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    private interface Validator<T extends Annotation> {

        boolean accept0(Class<?> clz, T t);

        void validateAnnotation0(Object value, String fieldName, T t);

        default boolean accept(Class<?> clz, Annotation annotation) {
            return accept0(clz, (T) annotation);
        }

        default void validateAnnotation(Object value, String fieldName, Annotation annotation) {
            validateAnnotation0(value, fieldName, (T) annotation);
        }
    }

    /**
     * 注解处理器放到枚举里管理
     */
    enum ValidatorEnums {
        // 还没有的注解后面陆续补全

        /**
         * NotBlank注解处理
         */
        NOT_BLANK(NotBlank.class, new Validator<NotBlank>() {
            @Override
            public boolean accept0(Class<?> clz, NotBlank notBlank) {
                return ValidationUtil.acceptGroup(clz, notBlank.groups());
            }

            @Override
            public void validateAnnotation0(Object value, String fieldName, NotBlank notBlank) {
                if (value == null || StringUtil.isBlank(value.toString())) {
                    ValidationUtil.throwException(
                            ValidationUtil.getMessage(notBlank.message(),
                            StringUtil.camelToUnderLineStyle(fieldName) + " cannot be blank"));
                }
            }
        }),
        /**
         * NotEmpty注解处理
         */
        NOT_EMPTY(NotEmpty.class, new Validator<NotEmpty>() {
            @Override
            public boolean accept0(Class<?> clz, NotEmpty annotation) {
                return ValidationUtil.acceptGroup(clz, annotation.groups());
            }

            @Override
            public void validateAnnotation0(Object value, String fieldName, NotEmpty notEmpty) {
                if (value == null || StringUtil.isEmpty(value.toString())) {
                    ValidationUtil.throwException(
                            ValidationUtil.getMessage(notEmpty.message(),
                            StringUtil.camelToBlankSeparate(fieldName) + " cannot be empty"));
                }
            }
        }),
        /**
         * NotNull注解处理
         */
        NOT_NULL(NotNull.class, new Validator<NotNull>() {
            @Override
            public boolean accept0(Class<?> clz, NotNull notNull) {
                return ValidationUtil.acceptGroup(clz, notNull.groups());
            }

            @Override
            public void validateAnnotation0(Object value, String fieldName, NotNull notNull) {
                if (value == null) {
                    ValidationUtil.throwException(
                            ValidationUtil.getMessage(notNull.message(),
                            StringUtil.camelToBlankSeparate(fieldName) + " required"));
                }
            }
        }),
        /**
         * Size注解处理
         */
        SIZE(Size.class, new Validator<Size>() {
            // 复用封装
            private void throwException(String fieldName, Size size) {
                String message = ValidationUtil.getMessage(size.message(),
                        StringUtil.camelToBlankSeparate(fieldName) + " length must between " + size.min() + " and " + size.max());

                ValidationUtil.throwException(message);
            }

            @Override
            public boolean accept0(Class<?> clz, Size size) {
                return ValidationUtil.acceptGroup(clz, size.groups());
            }

            @Override
            public void validateAnnotation0(Object value, String fieldName, Size size) {
                if (value == null) {
                    return;
                }
                if (value instanceof CharSequence) {
                    int len = ((CharSequence) value).length();
                    if (size.min() > len || size.max() < len) {
                        this.throwException(fieldName, size);
                    }
                } else if (value instanceof Collection) {
                    Collection<?> collection = (Collection<?>) value;
                    if (size.min() > collection.size() || size.max() < collection.size()) {
                        this.throwException(fieldName, size);
                    }
                } else if (value.getClass().isArray()) {
                    int len = Array.getLength(value);
                    if (size.min() > len || size.max() < len) {
                        this.throwException(fieldName, size);
                    }
                } else if (value instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) value;
                    if (size.min() > map.size() || size.max() < map.size()) {
                        this.throwException(fieldName, size);
                    }
                }
            }
        }),
        /**
         * Max注解处理
         */
        MAX(Max.class, new Validator<Max>() {

            @Override
            public boolean accept0(Class<?> clz, Max max) {
                return ValidationUtil.acceptGroup(clz, max.groups());
            }

            @Override
            public void validateAnnotation0(Object value, String fieldName, Max max) {
                if (!(value instanceof Number)) {
                    return;
                }
                long val = max.value();

                Number number = (Number) value;
                if (number.longValue() > val) {
                    String message = ValidationUtil.getMessage(max.message(),
                            StringUtil.camelToBlankSeparate(fieldName) + " cannot greater than " + val);
                    ValidationUtil.throwException(message);
                }
            }
        }),
        /**
         * Min注解处理
         */
        MIN(Min.class, new Validator<Min>() {

            @Override
            public boolean accept0(Class<?> clz, Min min) {
                return ValidationUtil.acceptGroup(clz, min.groups());
            }

            @Override
            public void validateAnnotation0(Object value, String fieldName, Min min) {
                if (!(value instanceof Number)) {
                    return;
                }
                long val = min.value();

                Number number = (Number) value;
                if (number.longValue() < val) {
                    String message = ValidationUtil.getMessage(min.message(),
                            StringUtil.camelToBlankSeparate(fieldName) + " cannot less than " + val);
                    ValidationUtil.throwException(message);
                }
            }
        }),
        /**
         * Pattern注解处理
         */
        PATTERN(Pattern.class, new Validator<Pattern>() {
            @Override
            public boolean accept0(Class<?> clz, Pattern pattern) {
                return ValidationUtil.acceptGroup(clz, pattern.groups());
            }

            @Override
            public void validateAnnotation0(Object value, String fieldName, Pattern pattern) {
                if (value == null) {
                    return;
                }
                String regexp = patternAddFlags(pattern.regexp(), pattern.flags());
                if (!value.toString().matches(regexp)) {
                    ValidationUtil.throwException(
                            ValidationUtil.getMessage(pattern.message(),
                            StringUtil.camelToUnderLineStyle(fieldName) + " invalid value"));
                }
            }
        }),
        /**
         * Email注解处理
         */
        EMAIL(Email.class, new Validator<Email>() {
            @Override
            public boolean accept0(Class<?> group, Email email) {
                return ValidationUtil.acceptGroup(group, email.groups());
            }
            @Override
            public void validateAnnotation0(Object value, String fieldName, Email email) {
                String val = value + "";
                if(!StringUtil.checkEmail(val)) {
                    ValidationUtil.throwException(
                            ValidationUtil.getMessage(email.message(), "email invalid"));
                }
                String regexp = ValidationUtil.patternAddFlags(email.regexp(), email.flags());
                // 额外的验证
                if (!val.matches(regexp)) {
                    ValidationUtil.throwException(
                            ValidationUtil.getMessage(email.message(), "email invalid"));
                }
            }
        }),
        ;


        ////////////////////////////////////////////////////////////////////////////////////////

        Class<?> clz;
        Validator<?> validator;

        <K extends Annotation> ValidatorEnums(Class<K> clz, Validator<K> validator) {
            this.clz = clz;
            this.validator = validator;
        }

        public static ValidatorEnums valueOf(Class<?> clz) {
            ValidatorEnums[] values = ValidatorEnums.values();
            for (ValidatorEnums value : values) {
                if (value.clz == clz) {
                    return value;
                }
            }
            return null;
        }
    }
    
}
