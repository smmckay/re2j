package us.abbies.b.re2jit;

import com.google.gwt.core.client.EntryPoint;
import us.abbies.b.re2jit.Pattern;

class FakeGWTEntryPoint implements EntryPoint {
  @Override
  public void onModuleLoad() {
    Pattern p = Pattern.compile("foo");
    p.matcher("bar");
  }
}
