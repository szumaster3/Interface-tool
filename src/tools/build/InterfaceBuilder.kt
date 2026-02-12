package tools.build

import tools.build.Copy.Companion.newInterface
import tools.print.GenericPrinter.printInterface

object InterfaceBuilder {
    @JvmStatic
    fun build() {
        val c = newInterface()
        Copy
            .to(c.targetInterface)
            .from(672)
            .startAt(0)
            .copyRange(0, 24)
            .save()

        printInterface(c.targetInterface)

        /*
               Copy.to(4000)
                       .startAt(0)
                       .addComponents(
                               c -> {
                                   c.type = 4;
                                   c.text = "Hello";
                                   c.fontId = 494;
                                   c.color = 0xFFFFFF;
                                   c.basePositionX = 20;
                                   c.basePositionY = 20;
                               }
                       )
                       .save();
               Copy.to(4100)
                       .startAt(5)
                       .addComponents(
                               c -> {
                                   c.type = 5;
                                   c.spriteId = 1234;
                                   c.baseWidth = 32;
                                   c.baseHeight = 32;
                                   c.aString4784 = "Click";
                                   c.rightclickOptions = new String[]{"Use", "Examine"};
                                   c.optionMask = 1;
                               }
                       )
                       .save();


               Copy.to(4200)
                       .from(548)
                       .startAt(1)
                       .copy(10).parent(0).x(5).y(5)
                       .save();

               Copy.to(4300)
                       .startAt(0)
                       .addComponents(
                               c -> {
                                   c.type = 2;
                                   c.baseWidth = 4;
                                   c.baseHeight = 7;
                                   c.inventoryTriggers = new int[]{0};
                                   c.optionMask = 1024;
                                   c.rightclickOptions = new String[]{
                                           "Use", "Drop", "Examine"
                                   };
                               }
                       )
                       .save();

               Copy.to(4400)
                       .startAt(0)
                       .addComponents(
                               c -> {
                                   c.type = 5;
                                   c.spriteId = 2000;
                                   c.anInt4747 = 2001;
                                   c.baseWidth = 64;
                                   c.baseHeight = 32;
                                   c.onMouseHoverScript = new Object[]{1};
                                   c.onMouseLeaveScript = new Object[]{0};
                               }
                       )
                       .save();

               Copy.to(4500)
                       .from(548)
                       .startAt(0)
                       .modify(c -> c.hidden = false)
                       .copy(20)
                       .x(100)
                       .y(200)
                       .text("Custom text")
                       .color(0xFF0000)
                       .save();


               Copy.to(4600)
                       .startAt(0)
                       .addComponents(
                               c -> {
                                   c.type = 3;
                                   c.color = IColors.COLOR_PALETTE[23];
                                   c.transparency = 120;
                                   c.baseWidth = 200;
                                   c.baseHeight = 300;
                               }
                       )
                       .addComponents(
                               c -> {
                                   c.type = 4;
                                   c.text = "Menu";
                                   c.fontId = 494;
                                   c.basePositionX = 10;
                                   c.basePositionY = 10;
                               }
                       )
                       .save();

           }

         */
    }
}
