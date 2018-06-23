package xdean.css.editor.service;

import java.text.MessageFormat;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import xdean.css.editor.context.setting.model.option.Option;

@Service
public class MessageService {

  @Inject
  private MessageSource messageSource;

  @Inject
  private Option<Locale> localeOption;

  public String getMessage(String code, Object... args) throws NoSuchMessageException {
    return messageSource.getMessage(code, args, localeOption.getValue());
  }

  public String getMessageDefault(String defaultMessage, String code, Object... args) {
    return messageSource.getMessage(code, args, defaultMessage, localeOption.getValue());
  }

  public MessageFormat getMessageFormat(String code) {
    return new MessageFormat(getMessage(code), localeOption.getValue());
  }
}
