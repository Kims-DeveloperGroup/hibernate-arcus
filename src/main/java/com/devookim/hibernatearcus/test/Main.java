package com.devookim.hibernatearcus.test;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException {


        TargetInterface proxyInstance = (TargetInterface) Proxy.newProxyInstance(
                TargetInterface.class.getClassLoader(),
                new Class[] { TargetInterface.class },
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] methodArgs) throws Throwable {
                        return "string";
                    }
                });

        System.out.println(proxyInstance.printHello("rica.rdo"));


        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(TargetInterfaceImpl.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args1, proxy) -> {
            System.out.println("before");
//                Object ret = proxy.invoke(obj, args);
            Object ret = proxy.invokeSuper(obj, args1);
            System.out.println("after");
            return ret;
        });
        TargetInterfaceImpl o = (TargetInterfaceImpl)enhancer.create();
        System.out.println(o.printHello("rica.rdo"));

    }
}
