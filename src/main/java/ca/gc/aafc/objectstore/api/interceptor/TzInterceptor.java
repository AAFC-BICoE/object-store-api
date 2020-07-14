package ca.gc.aafc.objectstore.api.interceptor;

import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

public class TzInterceptor extends HandlerInterceptorAdapter {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    TimeZone tz = RequestContextUtils.getTimeZone(request);
    if (tz != null) {
      TimeZoneAwareLocaleContext context = new TimeZoneAwareLocaleContext() {
        @Override
        public Locale getLocale() {
          return null;
        }

        @Override
        public TimeZone getTimeZone() {
          return tz;
        }
      };
      LocaleContextHolder.setLocaleContext(context);
    }
    return true;
  }
}
