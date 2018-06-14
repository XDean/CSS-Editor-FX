package xdean.css.editor.context.option;

import javax.inject.Inject;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import xdean.css.editor.context.option.model.Option;

@Component
public class OptionBindProcessor implements BeanPostProcessor {

  @Inject
  Config config;

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof Option) {
      ((Option<?>) bean).bind(config);
    }
    return bean;
  }
}
