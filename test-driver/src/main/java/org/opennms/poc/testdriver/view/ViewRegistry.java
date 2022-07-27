package org.opennms.poc.testdriver.view;

import static j2html.TagCreator.*;

import j2html.tags.DomContent;
import java.util.Map;
import java.util.TreeMap;

public class ViewRegistry {

  private Map<String, String> links = new TreeMap<>();

  public void register(String link, String label) {
    if (links.containsKey(link)) {
      throw new IllegalArgumentException("Link " + link + " is already registered");
    }
    links.put(link, label);
  }

  public DomContent menu() {
    return pre(
      ul(
        each(links.entrySet(), entry -> li(
          a(" | " + entry.getValue()).withHref(entry.getKey())
        ))
      )
    );
  }

}
