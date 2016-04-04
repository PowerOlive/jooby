package org.jooby;

import static org.junit.Assert.assertEquals;

import org.jooby.Route.Definition;
import org.junit.Test;

public class RouteCollectionTest {

  @Test
  public void renderer() {
    Definition def = new Route.Definition("*", "*", (req, rsp, chain) -> {
    });
    new Route.Collection(def)
        .renderer("json");

    assertEquals("json", def.attr("renderer").get());
  }

  @Test
  public void attr() {
    Definition def = new Route.Definition("*", "*", (req, rsp, chain) -> {
    });
    new Route.Collection(def)
        .attr("foo", "bar");

    assertEquals("bar", def.attr("foo").get());
  }
}
