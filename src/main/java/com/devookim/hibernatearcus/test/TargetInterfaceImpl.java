package com.devookim.hibernatearcus.test;

public class TargetInterfaceImpl implements TargetInterface {
    @Override
    public String printHello(String name) {
        return "Hello " + name;
    }
}
