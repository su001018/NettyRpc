package org.example.client.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

public class TestBeanFactoryProcessor implements BeanFactoryPostProcessor, InstantiationAwareBeanPostProcessor {
    public TestBeanFactoryProcessor(){
        System.out.println("TestBeanFactoryProcessor Constructor");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        System.out.println("TestBeanFactoryProcessor postProcessBeanFactory()");
    }

}
