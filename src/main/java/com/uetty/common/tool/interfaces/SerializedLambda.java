package com.uetty.common.tool.interfaces;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.invoke.MethodHandleInfo;
import java.util.Objects;

/**
 * JDK SerializedLambda类替换类
 * <p>偷梁换柱，运行时替换掉jdk类</p>
 */
public class SerializedLambda implements Serializable {

    private static final long serialVersionUID = 8025925345765570181L;
    private final Class<?> capturingClass;
    private final String functionalInterfaceClass;
    private final String functionalInterfaceMethodName;
    private final String functionalInterfaceMethodSignature;
    private final String implClass;
    private final String implMethodName;
    private final String implMethodSignature;
    private final int implMethodKind;
    private final String instantiatedMethodType;
    private final Object[] capturedArgs;

    public SerializedLambda(Class<?> capturingClass, String functionalInterfaceClass, String functionalInterfaceMethodName, String functionalInterfaceMethodSignature, int implMethodKind, String implClass, String implMethodName, String implMethodSignature, String instantiatedMethodType, Object[] capturedArgs) {
        this.capturingClass = capturingClass;
        this.functionalInterfaceClass = functionalInterfaceClass;
        this.functionalInterfaceMethodName = functionalInterfaceMethodName;
        this.functionalInterfaceMethodSignature = functionalInterfaceMethodSignature;
        this.implMethodKind = implMethodKind;
        this.implClass = implClass;
        this.implMethodName = implMethodName;
        this.implMethodSignature = implMethodSignature;
        this.instantiatedMethodType = instantiatedMethodType;
        this.capturedArgs = (Object[])((Object[]) Objects.requireNonNull(capturedArgs)).clone();
    }

    public String getCapturingClass() {
        return this.capturingClass.getName().replace('.', '/');
    }

    public String getFunctionalInterfaceClass() {
        return this.functionalInterfaceClass;
    }

    public String getFunctionalInterfaceMethodName() {
        return this.functionalInterfaceMethodName;
    }

    public String getFunctionalInterfaceMethodSignature() {
        return this.functionalInterfaceMethodSignature;
    }

    public String getImplClass() {
        return this.implClass;
    }

    public String getImplMethodName() {
        return this.implMethodName;
    }

    public String getImplMethodSignature() {
        return this.implMethodSignature;
    }

    public int getImplMethodKind() {
        return this.implMethodKind;
    }

    public final String getInstantiatedMethodType() {
        return this.instantiatedMethodType;
    }

    public int getCapturedArgCount() {
        return this.capturedArgs.length;
    }

    public Object getCapturedArg(int i) {
        return this.capturedArgs[i];
    }

    /**
     * 序列化函数接口类实例
     */
    private static byte[] serializeObject(SerializableFunction<?, ?> lambda) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(lambda);
            oos.flush();

            return baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed to serialize object of type: " + lambda.getClass(), ex);
        }
    }

    private static SerializedLambda fakeDeserializeObject(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream objIn = new ObjectInputStream(bais) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                    Class<?> clazz;
                    try {
                        clazz = Class.forName(objectStreamClass.getName());
                    } catch (Exception ex) {
                        clazz = super.resolveClass(objectStreamClass);
                    }
                    // 将java.lang.invoke.SerializedLambda偷梁换柱为当前类
                    return clazz == java.lang.invoke.SerializedLambda.class ? SerializedLambda.class : clazz;
                }
            }
        ) {
            return (SerializedLambda) objIn.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("This is impossible to happen", e);
        }
    }

    /**
     * 通过反序列化转换 lambda 表达式，该方法只能序列化 lambda 表达式，不能序列化接口实现或者正常非 lambda 写法的对象
     *
     * @param lambda lambda对象
     * @return 返回解析后的 SerializedLambda
     */
    public static SerializedLambda resolve(SerializableFunction<?, ?> lambda) {
        if (!lambda.getClass().isSynthetic()) {
            throw new RuntimeException("该方法仅能传入 lambda 表达式产生的合成类");
        }

        // JDK Object 序列化
        final byte[] bytes = serializeObject(lambda);

        // JDK Object 反序列化时偷梁换柱
        return fakeDeserializeObject(bytes);
    }

    @Override
    public String toString() {
        String implKind = MethodHandleInfo.referenceKindToString(this.implMethodKind);
        return String.format("SerializedLambda[%s=%s, %s=%s.%s:%s, %s=%s %s.%s:%s, %s=%s, %s=%d]", "capturingClass", this.capturingClass, "functionalInterfaceMethod", this.functionalInterfaceClass, this.functionalInterfaceMethodName, this.functionalInterfaceMethodSignature, "implementation", implKind, this.implClass, this.implMethodName, this.implMethodSignature, "instantiatedMethodType", this.instantiatedMethodType, "numCaptured", this.capturedArgs.length);
    }

}
