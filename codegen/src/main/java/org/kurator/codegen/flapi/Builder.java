package org.kurator.codegen.flapi;

import unquietcode.tools.flapi.Descriptor;
import unquietcode.tools.flapi.Flapi;

/**
 * Created by lowery on 7/30/16.
 */
public class Builder {
    public static void main(String[] args) {
        Descriptor builder = Flapi.builder()
                .setPackage("org.kurator.codegen.akka")
                .setDescriptorName("actor")
                    .addMethod("of(Class actorType)").atLeast(1)
                    .addMethod("named(String name)").atMost(1)
                    .addMethod("receives(Class messageType)").atLeast(1)
                    .addMethod("mapsTo(Class c, Method m)").exactly(1)
                    .addMethod("in()").exactly(1)
                    .addMethod("arg(Class type, String name)").atLeast(1)
                    .addMethod("out()").exactly(1)
                    .addMethod("returnVal(Class type, String name)").atMost(1)
                    .addMethod("send()").exactly(1)
                    .addMethod("to(String actorRef)").exactly(1)
                    .addMethod("from(String actorRef)").last()

                    //.addMethod("in()").atMost(1)
                  //  .addMethod("arg(Class type, String name").atLeast(1)
                 //   .addMethod("toObject(String name)").
                .build();

        builder.writeToFolder("/home/lowery/IdeaProjects/kurator-fp/codegen/src/main/java");
    }
}
