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
package org.freyja.libgdx.cocostudio.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import org.freyja.libgdx.cocostudio.ui.model.ObjectData;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * 控件 转换器
 *
 * @author i see
 */
public abstract class BaseWidgetParser {

    /**
     * get widget type name
     */
    public abstract String getClassName();

    /**
     * convert cocostudio widget to libgdx actor
     */
    public abstract Actor parse(CocoStudioUIEditor editor, ObjectData widget);

    CocoStudioUIEditor editor;

    /**
     * common attribute parser<br>
     * <p/>
     * according cocstudio ui setting properties of the configuration file
     *
     * @param editor
     * @param widget
     * @param option
     * @param parent
     * @param actor
     * @return
     */
    public Actor commonParse(CocoStudioUIEditor editor, ObjectData widget,
                             Group parent, Actor actor) {
        this.editor = editor;
        actor.setName(widget.getName());
        actor.setSize(widget.getSize().getX(), widget.getSize().getY());

        // set origin
        if (widget.getAnchorPoint() != null)
            actor.setOrigin(widget.getAnchorPoint().getScaleX() * actor.getWidth(),
                widget.getAnchorPoint().getScaleY() * actor.getHeight());


        // cocos anchor bug

//        if (widget.getPosition() != null) {
//            if (parent == null) {
//                actor.setPosition(widget.getPosition().getX() - actor.getOriginX(),
//                    widget.getPosition().getY() - actor.getOriginY());
//            } else {
//
//                // 锚点要算上父控件的锚点,也就是原点
//                actor.setX(parent.getOriginX()
//                    - (actor.getOriginX() - widget.getPosition().getX()));
//
//                actor.setY(parent.getOriginY()
//                    - (actor.getOriginY() - widget.getPosition().getY()));
//            }
//        }

        //判空，因为新版本的单独节点没有Postion属性
        if (widget.getPosition() != null) {
            actor.setPosition(widget.getPosition().getX() - actor.getOriginX(),
                widget.getPosition().getY() - actor.getOriginY());
        }

        // CocoStudio的编辑器ScaleX,ScaleY 会有负数情况
        //判空，因为新版本的单独节点没有Scale属性
        if (widget.getScale() != null) {
            actor.setScale(widget.getScale().getScaleX(), widget.getScale()
                .getScaleY());
        }

        if (widget.getRotation() != 0) {// CocoStudio 是顺时针方向旋转,转换下.
            actor.setRotation(360 - widget.getRotation() % 360);
        }

        // 设置可见
        actor.setVisible(widget.isVisibleForFrame());

        Color color = editor.getColor(widget.getCColor(), widget.getAlpha());

        actor.setColor(color);

        actor.setTouchable(widget.isTouchEnable() ? Touchable.enabled
            : Touchable.disabled);

        // callback

        addCallback(actor, widget);
        // callback

        addActor(editor, actor, widget);

        if (widget.getChildren() == null || widget.getChildren().size() == 0) {
            //添加Action
            if (editor.getActionTagActionMap().containsKey(widget.getActionTag())) {
                actor.addAction(editor.getActionTagActionMap().get(widget.getActionTag()));
            }

            return actor;
        }

        return null;
    }

    public void addCallback(final Actor actor, final ObjectData widget) {
        if (widget.getCallBackType() == null
            || widget.getCallBackType().isEmpty()) {
            return;
        }
        if ("Click".equals(widget.getCallBackType())) {
            actor.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    invoke(actor, widget.getCallBackName());
                    super.clicked(event, x, y);
                }
            });
        } else if ("Touch".equals(widget.getCallBackType())) {

            actor.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y,
                                         int pointer, int button) {
                    invoke(actor, widget.getCallBackName());
                    return super.touchDown(event, x, y, pointer, button);
                }
            });
        }

    }

    public void invoke(Actor actor, String methodName) {
        Stage stage = actor.getStage();
        if (stage == null) {
            return;
        }

        if (methodName == null || methodName.isEmpty()) {
            // default callback method
            methodName = actor.getName();
        }

        if (methodName == null || methodName.isEmpty()) {
            editor.error("CallBackName isEmpty");
            return;
        }

        Class clazz = stage.getClass();

        Method method = null;
        try {
            method = clazz.getMethod(methodName);
        } catch (Exception e) {
            editor.debug(clazz.getName() + "没有这个回调方法:" + methodName);
        }

        if (method == null) {
            return;
        }
        try {
            method.invoke(stage);
        } catch (Exception e) {
            e.printStackTrace();
            editor.error(clazz.getName() + "回调出错:" + methodName);
        }

    }

    protected void addActor(CocoStudioUIEditor editor, Actor actor,
                            ObjectData option) {

        Array<Actor> arrayActors = editor.getActors().get(actor.getName());
        if (arrayActors == null) {
            arrayActors = new Array<Actor>();
        }
        arrayActors.add(actor);
        editor.getActors().put(actor.getName(), arrayActors);

        editor.getActionActors().put(option.getActionTag(), actor);
    }

    /**
     * 子控件根据zOrder属性排序
     */
    protected void sort(final ObjectData widget, Group group) {
        group.getChildren().sort(new Comparator<Actor>() {
            @Override
            public int compare(Actor arg0, Actor arg1) {
                return getZOrder(widget, arg0.getName())
                    - getZOrder(widget, arg1.getName());
            }
        });

    }

    /**
     * 由于libgdx的zindex并不表示渲染层级,所以这里采用这种方式来获取子控件的当前层级
     */
    public static int getZOrder(ObjectData widget, String name) {
        if (name == null) {
            return 0;
        }
        for (ObjectData child : widget.getChildren()) {
            if (name.equals(child.getName())) {
                return child.getZOrder();
            }
        }
        return 0;
    }
}
