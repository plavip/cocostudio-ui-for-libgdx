/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freyja.libgdx.cocostudio.ui.parser.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.ObjectData;
import org.freyja.libgdx.cocostudio.ui.parser.WidgetParser;
import org.freyja.libgdx.cocostudio.ui.widget.TTFLabel;
import org.freyja.libgdx.cocostudio.ui.widget.TTFLabelStyle;

public class CCLabel extends WidgetParser {

    @Override
    public String getClassName() {
        return "TextObjectData";
    }

    @Override
    public Actor parse(CocoStudioUIEditor editor, ObjectData widget) {

        final TTFLabelStyle labelStyle = editor.createLabelStyle(widget,
            widget.getLabelText(), editor.getColor(widget.getCColor(), 0));

        TTFLabel label = new TTFLabel(widget.getLabelText(), labelStyle);
        // 水平
        int h = 0;
        if ("HT_Center".equals(widget.getHorizontalAlignmentType())) {
            h = Align.center;
        } else if ("HT_Left".equals(widget.getHorizontalAlignmentType())) {
            h = Align.left;
        } else if ("HT_Right".equals(widget.getHorizontalAlignmentType())) {
            h = Align.right;
        } else {//默认左对齐
            h = Align.left;
        }

        // 垂直
        int v = 0;
        if ("HT_Center".equals(widget.getHorizontalAlignmentType())) {
            v = Align.center;
        } else if ("HT_Top".equals(widget.getHorizontalAlignmentType())) {
            v = Align.top;
        } else if ("HT_Bottom".equals(widget.getHorizontalAlignmentType())) {
            v = Align.bottom;
        } else {//默认居上
            v = Align.top;
        }

        label.setAlignment(h, v);

        return label;
    }
}
