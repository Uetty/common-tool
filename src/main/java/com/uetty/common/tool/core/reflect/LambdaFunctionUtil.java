package com.uetty.common.tool.core.reflect;

import com.uetty.common.tool.interfaces.SerializableFunction;
import com.uetty.common.tool.interfaces.SerializedLambda;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author vince
 * <p>两种方式从获取函数方法的方法名</p>
 */
public class LambdaFunctionUtil {

    /**
     * JDK反序列化Lambda方法时，会先反序列化一个名称为SerializedLambda的工厂类（工厂类反序列化后，便注入了实际动态连接到的目标方法，叫做方法动态连接操作类可能更好），
     * 工厂类的readResolve方法会从实际动态连接方法类上找到名称为"$deserializeLambda$"方法来完成lambda方法自身的反序列化，最后返回反序列化的方法
     * <p>这里利用上面的过程伪造一个SerializedLambda偷梁换柱，而当伪造的SerializedLambda方法不存在readResolve方法时，不再执行动态连接方法的反序列化，直接返回该工厂类方法。
     * 从通过工厂类中的"getImplMethodName"方法便能获取Lambda方法动态连接后的方法名称</p>
     * @param serializableFunction 需要获取动态连接方法名的方法
     * @return 返回运行时动态连接的方法名
     */
    public static <T, R> String getFunctionImplName_1(SerializableFunction<T, R> serializableFunction) {

        SerializedLambda fakeSerializedLambda = SerializedLambda.resolve(serializableFunction);

        return fakeSerializedLambda.getImplMethodName();
    }

    /**
     * Lambda方法类实例上，存在一个名称为"writeReplace"的私有方法，通过该方法可获取注入动态连接信息后的SerializedLambda工厂类实例
     * <p>这里便是利用反射方式获取"writeReplace"方法，达到获取Lambda方法动态连接后的方法名称</p>
     * @param serializableFunction 需要获取动态连接方法名的方法
     * @return 返回运行时动态连接的方法名
     */
    public static <T, R> String getFunctionImplName_2(SerializableFunction<T, R> serializableFunction) {
        try {
            Class<? extends SerializableFunction> serializableFunctionClazz = serializableFunction.getClass();
            Method writeReplaceMethod = serializableFunctionClazz.getDeclaredMethod("writeReplace");
            writeReplaceMethod.setAccessible(Boolean.TRUE);
            // 通过方法类的writeReplace方法获取Lambda方法的 动态连接辅助类 SerializedLambda
            java.lang.invoke.SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) writeReplaceMethod.invoke(serializableFunction);

            return serializedLambda.getImplMethodName();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("获取动态连接方法名失败", e);
        }
    }

    @Data
    public static class TestEntity {

        String type;

        String name;

        String value;
    }

    public static void main(String[] args) {

        System.out.println(getFunctionImplName_1(TestEntity::getType));
        System.out.println(getFunctionImplName_2(TestEntity::getType));
    }
}
